package com.shodhaicode.contest.repository;

import com.shodhaicode.contest.model.Problem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProblemRepository extends JpaRepository<Problem, Long> {
    List<Problem> findByContest_IdOrderByOrderIndexAsc(String contestId);
}
