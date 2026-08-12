package com.company.iaf.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.company.iaf")
@EnableScheduling
public class IafApplication {

    public static void main(String[] args) {
        SpringApplication.run(IafApplication.class, args);
    }
}
