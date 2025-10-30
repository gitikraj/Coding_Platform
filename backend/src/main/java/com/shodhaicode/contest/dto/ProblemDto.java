package com.shodhaicode.contest.dto;

import java.util.List;

public class ProblemDto {

    private Long id;
    private String title;
    private String slug;
    private String statement;
    private Integer timeLimitMs;
    private Integer memoryLimitMb;
    private Integer pointValue;
    private List<TestCaseDto> samples;

    public ProblemDto() {
    }

    public ProblemDto(Long id, String title, String slug, String statement, Integer timeLimitMs,
                      Integer memoryLimitMb, Integer pointValue, List<TestCaseDto> samples) {
        this.id = id;
        this.title = title;
        this.slug = slug;
        this.statement = statement;
        this.timeLimitMs = timeLimitMs;
        this.memoryLimitMb = memoryLimitMb;
        this.pointValue = pointValue;
        this.samples = samples;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSlug() {
        return slug;
    }

    public String getStatement() {
        return statement;
    }

    public Integer getTimeLimitMs() {
        return timeLimitMs;
    }

    public Integer getMemoryLimitMb() {
        return memoryLimitMb;
    }

    public Integer getPointValue() {
        return pointValue;
    }

    public List<TestCaseDto> getSamples() {
        return samples;
    }
}
