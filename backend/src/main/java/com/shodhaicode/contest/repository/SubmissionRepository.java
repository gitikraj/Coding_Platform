package com.shodhaicode.contest.repository;

import com.shodhaicode.contest.model.Contest;
import com.shodhaicode.contest.model.ContestUser;
import com.shodhaicode.contest.model.Problem;
import com.shodhaicode.contest.model.Submission;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionRepository extends JpaRepository<Submission, UUID> {
    List<Submission> findByContestAndUserOrderByCreatedAtAsc(Contest contest, ContestUser user);

    List<Submission> findByContest_Id(String contestId);

    Optional<Submission> findTopByUserAndProblemOrderByCreatedAtDesc(ContestUser user, Problem problem);
}
