package com.shodhaicode.contest.repository;

import com.shodhaicode.contest.model.ContestUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContestUserRepository extends JpaRepository<ContestUser, Long> {
    Optional<ContestUser> findByUsernameIgnoreCase(String username);
}
