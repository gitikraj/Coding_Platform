package com.shodhaicode.contest.service.impl;

import com.shodhaicode.contest.judge.JudgeResult;
import com.shodhaicode.contest.judge.JudgeService;
import com.shodhaicode.contest.model.Submission;
import com.shodhaicode.contest.model.SubmissionStatus;
import com.shodhaicode.contest.repository.SubmissionRepository;
import com.shodhaicode.contest.service.SubmissionQueue;
import jakarta.transaction.Transactional;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SubmissionProcessingWorker {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubmissionProcessingWorker.class);

    private final SubmissionQueue submissionQueue;
    private final SubmissionRepository submissionRepository;
    private final JudgeService judgeService;

    public SubmissionProcessingWorker(SubmissionQueue submissionQueue,
                                      SubmissionRepository submissionRepository,
                                      JudgeService judgeService) {
        this.submissionQueue = submissionQueue;
        this.submissionRepository = submissionRepository;
        this.judgeService = judgeService;
    }

    @Scheduled(fixedDelayString = "${judge.worker-interval-ms:1500}")
    @Transactional
    public void processQueue() {
        UUID submissionId = submissionQueue.poll();
        if (submissionId == null) {
            return;
        }

        Optional<Submission> optionalSubmission = submissionRepository.findById(submissionId);
        if (optionalSubmission.isEmpty()) {
            LOGGER.warn("Submission {} not found when processing queue", submissionId);
            return;
        }

        Submission submission = optionalSubmission.get();
        submission.setStatus(SubmissionStatus.RUNNING);
        submission.touchUpdatedAt();
        submissionRepository.save(submission);

        JudgeResult result = judgeService.evaluate(submission);
        submission.setStatus(result.getStatus());
        submission.setVerdict(result.getVerdict());
        submission.setRuntimeMillis(result.getRuntimeMillis());
        submission.setMemoryKb(result.getMemoryKb());
        submission.setStdout(result.getStdout());
        submission.setStderr(result.getStderr());
        submission.touchUpdatedAt();
        submissionRepository.save(submission);
    }
}
