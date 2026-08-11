package com.company.iaf.platform.core.context;

import com.company.iaf.shared.security.SecurityContext;
import com.company.iaf.shared.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class ExecutionContextTaskDecoratorTest {

    private final ExecutionContextTaskDecorator decorator = new ExecutionContextTaskDecorator();

    @AfterEach
    void clear() {
        TenantContext.clear();
        SecurityContext.clear();
        MDC.clear();
    }

    @Test
    void propagatesCapturedContextAndRestoresPreviousThreadState() {
        TenantContext.setTenantId(1L);
        SecurityContext.setUserId(7L);
        SecurityContext.setCurrentOrgId(9L);
        SecurityContext.setPermissions(Set.of("platform:user:view"));
        MDC.put("traceId", "trace-1");
        Runnable decorated = decorator.decorate(() -> {
            assertThat(TenantContext.getTenantId()).contains(1L);
            assertThat(SecurityContext.getUserId()).contains(7L);
            assertThat(SecurityContext.getCurrentOrgId()).contains(9L);
            assertThat(SecurityContext.getPermissions()).containsExactly("platform:user:view");
            assertThat(MDC.get("tenantId")).isEqualTo("1");
            assertThat(MDC.get("userId")).isEqualTo("7");
            assertThat(MDC.get("currentOrgId")).isEqualTo("9");
            assertThat(MDC.get("traceId")).isEqualTo("trace-1");
        });

        TenantContext.setTenantId(2L);
        SecurityContext.setUserId(8L);
        SecurityContext.setCurrentOrgId(10L);
        SecurityContext.setPermissions(Set.of("platform:role:view"));
        MDC.put("traceId", "trace-2");
        decorated.run();

        assertThat(TenantContext.getTenantId()).contains(2L);
        assertThat(SecurityContext.getUserId()).contains(8L);
        assertThat(SecurityContext.getCurrentOrgId()).contains(10L);
        assertThat(SecurityContext.getPermissions()).containsExactly("platform:role:view");
        assertThat(MDC.get("traceId")).isEqualTo("trace-2");
    }

    @Test
    void emptyContextTaskDoesNotInheritPreviousRequestContext() {
        Runnable decorated = decorator.decorate(() -> {
            assertThat(TenantContext.getTenantId()).isEmpty();
            assertThat(SecurityContext.getUserId()).isEmpty();
            assertThat(SecurityContext.getPermissions()).isEmpty();
        });

        TenantContext.setTenantId(2L);
        SecurityContext.setUserId(8L);
        SecurityContext.setPermissions(Set.of("platform:role:view"));
        decorated.run();

        assertThat(TenantContext.getTenantId()).contains(2L);
        assertThat(SecurityContext.getUserId()).contains(8L);
        assertThat(SecurityContext.getPermissions()).containsExactly("platform:role:view");
    }

    @Test
    void decoratedTaskClearsContextAfterCompletionInFreshThread() {
        TenantContext.setTenantId(1L);
        SecurityContext.setUserId(7L);
        Runnable decorated = decorator.decorate(() -> {
        });
        AtomicReference<Long> tenantAfterRun = new AtomicReference<>();

        Thread thread = new Thread(() -> {
            decorated.run();
            tenantAfterRun.set(TenantContext.getTenantId().orElse(null));
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }

        assertThat(tenantAfterRun.get()).isNull();
    }
}
