package com.shodhaicode.contest.service;

import com.shodhaicode.contest.dto.ContestDto;
import com.shodhaicode.contest.dto.LeaderboardEntryDto;
import java.util.List;

public interface ContestService {
    ContestDto getContest(String contestId);

    List<LeaderboardEntryDto> getLeaderboard(String contestId);
}
