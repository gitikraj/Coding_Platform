package com.shodhaicode.contest.controller;

import com.shodhaicode.contest.dto.SubmissionRequest;
import com.shodhaicode.contest.dto.SubmissionResponse;
import com.shodhaicode.contest.dto.SubmissionStatusResponse;
import com.shodhaicode.contest.service.SubmissionService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {

    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @PostMapping
    public ResponseEntity<SubmissionResponse> createSubmission(@Valid @RequestBody SubmissionRequest request) {
        return ResponseEntity.ok(submissionService.createSubmission(request));
    }

    @GetMapping("/{submissionId}")
    public ResponseEntity<SubmissionStatusResponse> getSubmission(@PathVariable("submissionId") UUID submissionId) {
        return ResponseEntity.ok(submissionService.getSubmission(submissionId));
    }
}
