package com.company.iaf.platform.core.context;

import com.company.iaf.shared.context.ContextScope;
import com.company.iaf.shared.context.ExecutionContext;
import org.springframework.core.task.TaskDecorator;

public class ExecutionContextTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        ExecutionContext captured = ExecutionContext.capture();
        return () -> {
            try (ContextScope ignored = captured.openScope()) {
                runnable.run();
            }
        };
    }
}
