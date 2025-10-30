package com.shodhaicode.contest.dto;

public class TestCaseDto {
    private String input;
    private String output;

    public TestCaseDto() {
    }

    public TestCaseDto(String input, String output) {
        this.input = input;
        this.output = output;
    }

    public String getInput() {
        return input;
    }

    public String getOutput() {
        return output;
    }
}
