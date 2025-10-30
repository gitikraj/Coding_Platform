package com.shodhaicode.contest.judge;

import com.shodhaicode.contest.model.ProblemTestCase;
import com.shodhaicode.contest.model.Submission;
import com.shodhaicode.contest.model.SubmissionStatus;
import com.shodhaicode.contest.repository.ProblemTestCaseRepository;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class DockerJudgeService implements JudgeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DockerJudgeService.class);

    private final ProblemTestCaseRepository problemTestCaseRepository;

    private final String dockerImage;
    private final boolean useDocker;
    private final long timeLimitBufferMs;
    private final Path workspaceRoot;

    public DockerJudgeService(ProblemTestCaseRepository problemTestCaseRepository,
                              @Value("${judge.image:shodhaicode/judge:latest}") String dockerImage,
                              @Value("${judge.use-docker:true}") boolean useDocker,
                              @Value("${judge.time-buffer-ms:500}") long timeLimitBufferMs,
                              @Value("${judge.workspace-root:#{systemProperties['java.io.tmpdir'] + '/judge-workspaces'}}")
                              String workspaceRoot) {
        this.problemTestCaseRepository = problemTestCaseRepository;
        this.dockerImage = dockerImage;
        this.useDocker = useDocker;
        this.timeLimitBufferMs = timeLimitBufferMs;
        try {
            this.workspaceRoot = Path.of(workspaceRoot).toAbsolutePath();
            Files.createDirectories(this.workspaceRoot);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to initialize judge workspace root", e);
        }
    }

    @Override
    public JudgeResult evaluate(Submission submission) {
        String normalizedLanguage = submission.getLanguage() == null
            ? ""
            : submission.getLanguage().trim().toLowerCase(Locale.ROOT);
        LanguageStrategy strategy = resolveLanguage(normalizedLanguage);
        if (strategy == null) {
            return new JudgeResult(
                SubmissionStatus.INTERNAL_ERROR,
                "Unsupported language: " + submission.getLanguage(),
                null,
                null,
                null,
                null
            );
        }

        List<ProblemTestCase> testCases = problemTestCaseRepository
            .findByProblemIdOrderByIdAsc(submission.getProblem().getId());

        if (testCases.isEmpty()) {
            return new JudgeResult(
                SubmissionStatus.INTERNAL_ERROR,
                "No test cases configured for problem",
                null,
                null,
                null,
                null
            );
        }

        try (TempWorkspace workspace = TempWorkspace.create(workspaceRoot)) {
            Path sourceFile = workspace.path().resolve(strategy.sourceFilename());
            Files.writeString(sourceFile, submission.getSourceCode(), StandardCharsets.UTF_8);

            CommandResult compileResult = runCompile(strategy, workspace.path());
            if (!compileResult.success()) {
                return new JudgeResult(
                    SubmissionStatus.COMPILE_ERROR,
                    "Compilation failed",
                    null,
                    null,
                    null,
                    compileResult.stderr()
                );
            }

            long maxRuntime = 0L;
            String lastStdout = null;
            for (ProblemTestCase testCase : testCases) {
                Path inputFile = workspace.path().resolve("input.txt");
                Files.writeString(inputFile, testCase.getInputData(), StandardCharsets.UTF_8);

                CommandResult runResult = runExecution(strategy, workspace.path(), submission.getProblem().getTimeLimitMs());

                if (!runResult.success()) {
                    SubmissionStatus status = runResult.timedOut()
                        ? SubmissionStatus.RUNTIME_ERROR
                        : SubmissionStatus.RUNTIME_ERROR;
                    String verdict = runResult.timedOut() ? "Time Limit Exceeded" : "Runtime Error";
                    return new JudgeResult(
                        status,
                        verdict,
                        runResult.durationMs(),
                        null,
                        runResult.stdout(),
                        runResult.stderr()
                    );
                }

                String normalizedActual = normalizeOutput(runResult.stdout());
                String normalizedExpected = normalizeOutput(testCase.getExpectedOutput());
                maxRuntime = Math.max(maxRuntime, runResult.durationMs());
                lastStdout = runResult.stdout();

                if (!normalizedExpected.equals(normalizedActual)) {
                    return new JudgeResult(
                        SubmissionStatus.WRONG_ANSWER,
                        "Wrong Answer",
                        runResult.durationMs(),
                        null,
                        runResult.stdout(),
                        runResult.stderr()
                    );
                }
            }

            return new JudgeResult(
                SubmissionStatus.ACCEPTED,
                "Accepted",
                maxRuntime,
                null,
                lastStdout,
                ""
            );
        } catch (IOException e) {
            LOGGER.error("Failed to evaluate submission {}", submission.getId(), e);
            return new JudgeResult(
                SubmissionStatus.INTERNAL_ERROR,
                "Judge internal error: " + e.getMessage(),
                null,
                null,
                null,
                null
            );
        }
    }

    private CommandResult runCompile(LanguageStrategy strategy, Path workspace) throws IOException {
        if (useDocker) {
            return runDockerCommand(workspace, strategy.dockerCompileCommand(), Duration.ofSeconds(15));
        }
        return executeCommand(strategy.localCompileCommand(), workspace, Duration.ofSeconds(15), null);
    }

    private CommandResult runExecution(LanguageStrategy strategy, Path workspace, Integer timeLimitMs)
        throws IOException {
        long timeoutMs = (long) timeLimitMs + timeLimitBufferMs;
        long timeoutSeconds = Math.max(1, timeoutMs / 1000);
        if (useDocker) {
            String script = String.format(Locale.ROOT, strategy.dockerRunCommandTemplate(), timeoutSeconds);
            return runDockerCommand(workspace, script, Duration.ofMillis(timeoutMs + 1000));
        }
        byte[] stdin = Files.readAllBytes(workspace.resolve("input.txt"));
        return executeCommand(strategy.localRunCommand(), workspace, Duration.ofMillis(timeoutMs + 1000), stdin);
    }

    private LanguageStrategy resolveLanguage(String language) {
        boolean isWindows = System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win");
        return switch (language) {
            case "java" -> new LanguageStrategy(
                "Main.java",
                "cd /workspace && javac Main.java",
                List.of("javac", "Main.java"),
                "cd /workspace && timeout %ds java Main < input.txt",
                List.of("java", "Main")
            );
            case "cpp", "c++" -> new LanguageStrategy(
                "Main.cpp",
                "cd /workspace && g++ Main.cpp -O2 -std=c++17 -o Main",
                List.of("g++", "Main.cpp", "-O2", "-std=c++17", "-o", "Main"),
                "cd /workspace && timeout %ds ./Main < input.txt",
                List.of(isWindows ? "Main.exe" : "./Main")
            );
            default -> null;
        };
    }

    private CommandResult runDockerCommand(Path workspace, String script, Duration timeout) throws IOException {
        List<String> command = new ArrayList<>();
        command.add("docker");
        command.add("run");
        command.add("--rm");
        command.add("-m");
        command.add("512m");
        command.add("--cpus");
        command.add("1.0");
        command.add("--network=none");
        command.add("-v");
        command.add(normalizeDockerPath(workspace) + ":/workspace");
        command.add("-w");
        command.add("/workspace");
        command.add(dockerImage);
        command.add("bash");
        command.add("-lc");
        command.add(script);

        return executeCommand(command, null, timeout, null);
    }

    private CommandResult executeCommand(List<String> command, Path workDir, Duration timeout, byte[] stdin)
        throws IOException {
        ProcessBuilder builder = new ProcessBuilder(command);
        if (workDir != null) {
            builder.directory(workDir.toFile());
        }
        builder.redirectErrorStream(false);
        long start = System.nanoTime();
        Process process = builder.start();
        if (stdin != null) {
            if (stdin.length > 0) {
                process.getOutputStream().write(stdin);
            }
            process.getOutputStream().close();
        } else {
            process.getOutputStream().close();
        }
        boolean finished;
        try {
            finished = process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            process.destroyForcibly();
            return new CommandResult(false, true, 0, "", "Interrupted");
        }

        boolean timedOut = false;
        if (!finished) {
            timedOut = true;
            process.destroyForcibly();
            try {
                process.waitFor(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        String stdout = readAll(process.getInputStream());
        String stderr = readAll(process.getErrorStream());
        long duration = Duration.ofNanos(System.nanoTime() - start).toMillis();

        int exitCode;
        try {
            exitCode = process.exitValue();
        } catch (IllegalThreadStateException e) {
            exitCode = -1;
        }
        boolean success = exitCode == 0 && !timedOut;
        return new CommandResult(success, timedOut, duration, stdout, stderr);
    }

    private String readAll(InputStream inputStream) throws IOException {
        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }

    private String normalizeDockerPath(Path workspace) {
        String raw = workspace.toAbsolutePath().toString();
        if (System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win")) {
            return raw.replace("\\", "/");
        }
        return raw;
    }

    private String normalizeOutput(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.replaceAll("\\s+$", "").replaceAll("\\r", "");
    }

    private record LanguageStrategy(String sourceFilename,
                                    String dockerCompileCommand,
                                    List<String> localCompileCommand,
                                    String dockerRunCommandTemplate,
                                    List<String> localRunCommand) {
    }

    private record CommandResult(boolean success, boolean timedOut, long durationMs,
                                 String stdout, String stderr) {
    }

    private record TempWorkspace(Path path) implements AutoCloseable {

        static TempWorkspace create(Path root) throws IOException {
            if (!Files.exists(root)) {
                Files.createDirectories(root);
            }
            Path path = Files.createTempDirectory(root, "judge-");
            return new TempWorkspace(path);
        }

        @Override
        public void close() throws IOException {
            Files.walkFileTree(path, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.deleteIfExists(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.deleteIfExists(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }
}
