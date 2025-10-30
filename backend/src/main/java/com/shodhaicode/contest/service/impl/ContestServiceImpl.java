package com.shodhaicode.contest.service.impl;

import com.shodhaicode.contest.dto.ContestDto;
import com.shodhaicode.contest.dto.LeaderboardEntryDto;
import com.shodhaicode.contest.dto.ProblemDto;
import com.shodhaicode.contest.dto.TestCaseDto;
import com.shodhaicode.contest.model.Contest;
import com.shodhaicode.contest.model.Problem;
import com.shodhaicode.contest.model.ProblemTestCase;
import com.shodhaicode.contest.model.Submission;
import com.shodhaicode.contest.model.SubmissionStatus;
import com.shodhaicode.contest.repository.ContestRepository;
import com.shodhaicode.contest.repository.SubmissionRepository;
import com.shodhaicode.contest.service.ContestService;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ContestServiceImpl implements ContestService {

    private final ContestRepository contestRepository;
    private final SubmissionRepository submissionRepository;

    public ContestServiceImpl(ContestRepository contestRepository, SubmissionRepository submissionRepository) {
        this.contestRepository = contestRepository;
        this.submissionRepository = submissionRepository;
    }

    @Override
    public ContestDto getContest(String contestId) {
        Contest contest = contestRepository.findById(contestId)
            .orElseThrow(() -> new EntityNotFoundException("Contest not found: " + contestId));
        List<Problem> sortedProblems = contest.getProblems()
            .stream()
            .sorted(Comparator.comparing(Problem::getOrderIndex))
            .toList();
        List<ProblemDto> problemDtos = sortedProblems.stream()
            .map(this::toProblemDto)
            .toList();
        return new ContestDto(
            contest.getId(),
            contest.getTitle(),
            contest.getDescription(),
            contest.getStartTime(),
            contest.getEndTime(),
            problemDtos
        );
    }

    @Override
    public List<LeaderboardEntryDto> getLeaderboard(String contestId) {
        Contest contest = contestRepository.findById(contestId)
            .orElseThrow(() -> new EntityNotFoundException("Contest not found: " + contestId));

        Map<String, List<Submission>> submissionsByUser = submissionRepository.findByContest_Id(contestId)
            .stream()
            .collect(Collectors.groupingBy(s -> s.getUser().getUsername().toLowerCase()));

        return submissionsByUser.entrySet().stream()
            .map(entry -> {
                String username = entry.getValue().isEmpty() ? entry.getKey() : entry.getValue().get(0).getUser().getUsername();
                Map<Long, Submission> bestPerProblem = entry.getValue().stream()
                    .filter(sub -> sub.getStatus() == SubmissionStatus.ACCEPTED)
                    .collect(Collectors.toMap(sub -> sub.getProblem().getId(), sub -> sub,
                        (first, second) -> first.getUpdatedAt().isBefore(second.getUpdatedAt()) ? second : first));
                int solved = bestPerProblem.size();
                int score = bestPerProblem.values().stream()
                    .map(accepted -> Objects.requireNonNullElse(accepted.getProblem().getPointValue(), 0))
                    .reduce(0, Integer::sum);
                long tieBreakerTime = entry.getValue().stream()
                    .filter(sub -> sub.getStatus() == SubmissionStatus.ACCEPTED)
                    .map(Submission::getUpdatedAt)
                    .map(Instant::toEpochMilli)
                    .min(Long::compareTo)
                    .orElseGet(() -> entry.getValue().stream()
                        .map(Submission::getUpdatedAt)
                        .map(Instant::toEpochMilli)
                        .min(Long::compareTo)
                        .orElse(Long.MAX_VALUE));
                return new LeaderboardEntryDto(username, solved, score, tieBreakerTime);
            })
            .sorted(Comparator
                .comparingInt(LeaderboardEntryDto::getTotalPoints).reversed()
                .thenComparingLong(LeaderboardEntryDto::getLastUpdated))
            .toList();
    }

    private ProblemDto toProblemDto(Problem problem) {
        List<TestCaseDto> samples = problem.getTestCases().stream()
            .filter(ProblemTestCase::isSampleCase)
            .map(tc -> new TestCaseDto(tc.getInputData(), tc.getExpectedOutput()))
            .toList();

        return new ProblemDto(
            problem.getId(),
            problem.getTitle(),
            problem.getSlug(),
            problem.getStatement(),
            problem.getTimeLimitMs(),
            problem.getMemoryLimitMb(),
            problem.getPointValue(),
            samples
        );
    }
}
