package com.lloyds.onboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class OnboardApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(OnboardApiApplication.class, args);
    }
}
