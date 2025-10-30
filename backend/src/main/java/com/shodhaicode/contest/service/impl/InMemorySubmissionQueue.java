package com.shodhaicode.contest.service.impl;

import com.shodhaicode.contest.service.SubmissionQueue;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

@Component
public class InMemorySubmissionQueue implements SubmissionQueue {

    private final BlockingQueue<UUID> queue = new LinkedBlockingQueue<>();

    @Override
    public void enqueue(UUID submissionId) {
        queue.offer(submissionId);
    }

    @Override
    public UUID poll() {
        try {
            return queue.poll(Duration.ofSeconds(1).toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }
}
