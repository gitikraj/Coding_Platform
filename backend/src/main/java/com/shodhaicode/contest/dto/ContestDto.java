package com.shodhaicode.contest.dto;

import java.time.Instant;
import java.util.List;

public class ContestDto {

    private String id;
    private String title;
    private String description;
    private Instant startTime;
    private Instant endTime;
    private List<ProblemDto> problems;

    public ContestDto() {
    }

    public ContestDto(String id, String title, String description, Instant startTime, Instant endTime,
                      List<ProblemDto> problems) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.problems = problems;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public List<ProblemDto> getProblems() {
        return problems;
    }
}
