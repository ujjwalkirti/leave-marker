package com.leavemarker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class LeaveManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(LeaveManagementApplication.class, args);
    }
}
