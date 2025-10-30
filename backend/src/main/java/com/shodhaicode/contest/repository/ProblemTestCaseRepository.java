package com.shodhaicode.contest.repository;

import com.shodhaicode.contest.model.ProblemTestCase;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProblemTestCaseRepository extends JpaRepository<ProblemTestCase, Long> {
    List<ProblemTestCase> findByProblemIdOrderByIdAsc(Long problemId);
}
