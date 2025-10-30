package com.shodhaicode.contest.service.impl;

import com.shodhaicode.contest.dto.SubmissionRequest;
import com.shodhaicode.contest.dto.SubmissionResponse;
import com.shodhaicode.contest.dto.SubmissionStatusResponse;
import com.shodhaicode.contest.model.Contest;
import com.shodhaicode.contest.model.ContestUser;
import com.shodhaicode.contest.model.Problem;
import com.shodhaicode.contest.model.Submission;
import com.shodhaicode.contest.model.SubmissionStatus;
import com.shodhaicode.contest.repository.ContestRepository;
import com.shodhaicode.contest.repository.ContestUserRepository;
import com.shodhaicode.contest.repository.ProblemRepository;
import com.shodhaicode.contest.repository.SubmissionRepository;
import com.shodhaicode.contest.service.SubmissionQueue;
import com.shodhaicode.contest.service.SubmissionService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class SubmissionServiceImpl implements SubmissionService {

    private final ContestRepository contestRepository;
    private final ProblemRepository problemRepository;
    private final ContestUserRepository contestUserRepository;
    private final SubmissionRepository submissionRepository;
    private final SubmissionQueue submissionQueue;

    public SubmissionServiceImpl(ContestRepository contestRepository,
                                 ProblemRepository problemRepository,
                                 ContestUserRepository contestUserRepository,
                                 SubmissionRepository submissionRepository,
                                 SubmissionQueue submissionQueue) {
        this.contestRepository = contestRepository;
        this.problemRepository = problemRepository;
        this.contestUserRepository = contestUserRepository;
        this.submissionRepository = submissionRepository;
        this.submissionQueue = submissionQueue;
    }

    @Override
    public SubmissionResponse createSubmission(SubmissionRequest request) {
        Contest contest = contestRepository.findById(request.getContestId())
            .orElseThrow(() -> new EntityNotFoundException("Contest not found: " + request.getContestId()));

        Problem problem = problemRepository.findById(request.getProblemId())
            .orElseThrow(() -> new EntityNotFoundException("Problem not found: " + request.getProblemId()));

        if (!problem.getContest().getId().equals(contest.getId())) {
            throw new IllegalArgumentException("Problem does not belong to contest");
        }

        ContestUser user = contestUserRepository.findByUsernameIgnoreCase(request.getUsername().trim())
            .orElseGet(() -> contestUserRepository.save(new ContestUser(request.getUsername().trim())));

        Submission submission = new Submission(contest, problem, user,
            request.getLanguage().trim().toLowerCase(Locale.ROOT),
            request.getSourceCode());
        submission.setStatus(SubmissionStatus.PENDING);

        Submission saved = submissionRepository.save(submission);
        submissionQueue.enqueue(saved.getId());

        return new SubmissionResponse(saved.getId(), saved.getStatus().name());
    }

    @Override
    public SubmissionStatusResponse getSubmission(UUID submissionId) {
        Submission submission = submissionRepository.findById(submissionId)
            .orElseThrow(() -> new EntityNotFoundException("Submission not found: " + submissionId));

        return new SubmissionStatusResponse(
            submission.getId(),
            submission.getStatus().name(),
            submission.getVerdict(),
            submission.getRuntimeMillis(),
            submission.getMemoryKb(),
            submission.getStdout(),
            submission.getStderr(),
            submission.getCreatedAt(),
            submission.getUpdatedAt()
        );
    }
}
