package com.company.iaf.shared.context;

import com.company.iaf.shared.security.SecurityContext;
import com.company.iaf.shared.tenant.TenantContext;
import org.slf4j.MDC;

public final class ContextScope implements AutoCloseable {

    private final ExecutionContext previous;

    private ContextScope(ExecutionContext previous) {
        this.previous = previous;
    }

    public static ContextScope open(ExecutionContext context) {
        ExecutionContext previous = ExecutionContext.capture();
        apply(context);
        return new ContextScope(previous);
    }

    private static void apply(ExecutionContext context) {
        TenantContext.clear();
        SecurityContext.clear();
        clearMdc();
        if (context == null || context.empty()) {
            return;
        }
        if (context.tenantId() != null) {
            TenantContext.setTenantId(context.tenantId());
            MDC.put("tenantId", String.valueOf(context.tenantId()));
        }
        if (context.userId() != null) {
            SecurityContext.setUserId(context.userId());
            MDC.put("userId", String.valueOf(context.userId()));
        }
        if (context.currentOrgId() != null) {
            SecurityContext.setCurrentOrgId(context.currentOrgId());
            MDC.put("currentOrgId", String.valueOf(context.currentOrgId()));
        }
        SecurityContext.setPermissions(context.permissions());
        if (context.correlationId() != null) {
            MDC.put("traceId", context.correlationId());
        }
    }

    @Override
    public void close() {
        apply(previous);
    }

    private static void clearMdc() {
        MDC.remove("tenantId");
        MDC.remove("userId");
        MDC.remove("currentOrgId");
        MDC.remove("traceId");
    }
}
