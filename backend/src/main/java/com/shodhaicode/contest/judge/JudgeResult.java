package com.shodhaicode.contest.judge;

import com.shodhaicode.contest.model.SubmissionStatus;

public class JudgeResult {
    private final SubmissionStatus status;
    private final String verdict;
    private final Long runtimeMillis;
    private final Long memoryKb;
    private final String stdout;
    private final String stderr;

    public JudgeResult(SubmissionStatus status, String verdict, Long runtimeMillis, Long memoryKb,
                       String stdout, String stderr) {
        this.status = status;
        this.verdict = verdict;
        this.runtimeMillis = runtimeMillis;
        this.memoryKb = memoryKb;
        this.stdout = stdout;
        this.stderr = stderr;
    }

    public SubmissionStatus getStatus() {
        return status;
    }

    public String getVerdict() {
        return verdict;
    }

    public Long getRuntimeMillis() {
        return runtimeMillis;
    }

    public Long getMemoryKb() {
        return memoryKb;
    }

    public String getStdout() {
        return stdout;
    }

    public String getStderr() {
        return stderr;
    }
}
