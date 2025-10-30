package com.shodhaicode.contest.repository;

import com.shodhaicode.contest.model.Contest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContestRepository extends JpaRepository<Contest, String> {
}
