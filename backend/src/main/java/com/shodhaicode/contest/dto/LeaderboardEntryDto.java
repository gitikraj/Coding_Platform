package com.shodhaicode.contest.dto;

public class LeaderboardEntryDto {
    private String username;
    private int solved;
    private int totalPoints;
    private long lastUpdated;

    public LeaderboardEntryDto(String username, int solved, int totalPoints, long lastUpdated) {
        this.username = username;
        this.solved = solved;
        this.totalPoints = totalPoints;
        this.lastUpdated = lastUpdated;
    }

    public String getUsername() {
        return username;
    }

    public int getSolved() {
        return solved;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }
}
