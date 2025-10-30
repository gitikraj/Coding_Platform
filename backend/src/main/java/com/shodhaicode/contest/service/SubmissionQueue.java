package com.shodhaicode.contest.service;

import java.util.UUID;

public interface SubmissionQueue {
    void enqueue(UUID submissionId);

    UUID poll();
}
