package com.shodhaicode.contest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class ContestBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ContestBackendApplication.class, args);
    }
}
