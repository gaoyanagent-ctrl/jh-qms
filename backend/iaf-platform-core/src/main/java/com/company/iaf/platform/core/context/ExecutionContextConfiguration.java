package com.company.iaf.platform.core.context;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;

@Configuration
public class ExecutionContextConfiguration {

    @Bean
    TaskDecorator executionContextTaskDecorator() {
        return new ExecutionContextTaskDecorator();
    }
}
