package com.company.iaf.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.company.iaf")
public class IafApplication {

    public static void main(String[] args) {
        SpringApplication.run(IafApplication.class, args);
    }
}
