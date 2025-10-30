package com.shodhaicode.contest.service;

import com.shodhaicode.contest.dto.SubmissionRequest;
import com.shodhaicode.contest.dto.SubmissionResponse;
import com.shodhaicode.contest.dto.SubmissionStatusResponse;
import java.util.UUID;

public interface SubmissionService {
    SubmissionResponse createSubmission(SubmissionRequest request);

    SubmissionStatusResponse getSubmission(UUID submissionId);
}
