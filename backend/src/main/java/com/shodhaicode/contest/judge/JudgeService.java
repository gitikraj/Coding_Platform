package com.shodhaicode.contest.judge;

import com.shodhaicode.contest.model.Submission;

public interface JudgeService {
    JudgeResult evaluate(Submission submission);
}
