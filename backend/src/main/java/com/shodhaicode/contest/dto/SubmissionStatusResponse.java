package com.shodhaicode.contest.dto;

import java.time.Instant;
import java.util.UUID;

public class SubmissionStatusResponse {
    private UUID submissionId;
    private String status;
    private String verdict;
    private Long runtimeMillis;
    private Long memoryKb;
    private String stdout;
    private String stderr;
    private Instant submittedAt;
    private Instant updatedAt;

    public SubmissionStatusResponse(UUID submissionId, String status, String verdict, Long runtimeMillis,
                                    Long memoryKb, String stdout, String stderr, Instant submittedAt,
                                    Instant updatedAt) {
        this.submissionId = submissionId;
        this.status = status;
        this.verdict = verdict;
        this.runtimeMillis = runtimeMillis;
        this.memoryKb = memoryKb;
        this.stdout = stdout;
        this.stderr = stderr;
        this.submittedAt = submittedAt;
        this.updatedAt = updatedAt;
    }

    public UUID getSubmissionId() {
        return submissionId;
    }

    public String getStatus() {
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

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
