package com.shodhaicode.contest.config;

import com.shodhaicode.contest.model.Contest;
import com.shodhaicode.contest.model.Problem;
import com.shodhaicode.contest.model.ProblemTestCase;
import com.shodhaicode.contest.repository.ContestRepository;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    @Transactional
    CommandLineRunner seedData(ContestRepository contestRepository) {
        return args -> {
            if (contestRepository.existsById("shodh-101")) {
                LOGGER.info("Sample data already present; skipping seeding");
                return;
            }

            Contest contest = new Contest("shodh-101", "Shodh Starter Contest",
                "Solve introductory problems and climb the leaderboard.");
            contest.setStartTime(Instant.now().minus(1, ChronoUnit.HOURS));
            contest.setEndTime(Instant.now().plus(3, ChronoUnit.HOURS));

            Problem sumProblem = new Problem(
                "Simple Sum",
                "simple-sum",
                """
                    Given two integers, output their sum.
                    
                    Input: Two space separated integers.
                    Output: A single integer representing the sum.
                    """
            );
            sumProblem.setOrderIndex(1);
            sumProblem.addTestCase(new ProblemTestCase("1 2", "3", true));
            sumProblem.addTestCase(new ProblemTestCase("10 20", "30", false));
            sumProblem.addTestCase(new ProblemTestCase("100 -5", "95", false));

            Problem palindrome = new Problem(
                "Palindrome Check",
                "palindrome-check",
                """
                    Determine if the supplied string is a palindrome.
                    
                    Input: A single line string (letters only).
                    Output: Print "YES" if it is a palindrome, else "NO".
                    """
            );
            palindrome.setOrderIndex(2);
            palindrome.addTestCase(new ProblemTestCase("level", "YES", true));
            palindrome.addTestCase(new ProblemTestCase("hello", "NO", false));
            palindrome.addTestCase(new ProblemTestCase("radar", "YES", false));

            Problem fizzbuzz = new Problem(
                "FizzBuzz Lite",
                "fizzbuzz-lite",
                """
                    Print numbers from 1 to N. For multiples of 3 print Fizz, for multiples of 5 print Buzz,
                    for both print FizzBuzz.
                    
                    Input: A single integer N (1 <= N <= 50).
                    Output: Sequence separated by spaces.
                    """
            );
            fizzbuzz.setOrderIndex(3);
            fizzbuzz.addTestCase(new ProblemTestCase("5", "1 2 Fizz 4 Buzz", true));
            fizzbuzz.addTestCase(new ProblemTestCase("10", "1 2 Fizz 4 Buzz Fizz 7 8 Fizz Buzz", false));

            contest.addProblem(sumProblem);
            contest.addProblem(palindrome);
            contest.addProblem(fizzbuzz);

            contestRepository.save(contest);
            LOGGER.info("Seeded sample contest {}", contest.getId());
        };
    }
}
