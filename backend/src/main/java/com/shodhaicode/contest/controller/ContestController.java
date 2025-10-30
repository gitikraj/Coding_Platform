package com.shodhaicode.contest.controller;

import com.shodhaicode.contest.dto.ContestDto;
import com.shodhaicode.contest.dto.LeaderboardEntryDto;
import com.shodhaicode.contest.service.ContestService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contests")
public class ContestController {

    private final ContestService contestService;

    public ContestController(ContestService contestService) {
        this.contestService = contestService;
    }

    @GetMapping("/{contestId}")
    public ResponseEntity<ContestDto> getContest(@PathVariable("contestId") String contestId) {
        return ResponseEntity.ok(contestService.getContest(contestId));
    }

    @GetMapping("/{contestId}/leaderboard")
    public ResponseEntity<List<LeaderboardEntryDto>> getLeaderboard(@PathVariable("contestId") String contestId) {
        return ResponseEntity.ok(contestService.getLeaderboard(contestId));
    }
}
