package com.company.iaf.shared.context;

import com.company.iaf.shared.security.SecurityContext;
import com.company.iaf.shared.tenant.TenantContext;
import org.slf4j.MDC;

import java.util.Set;

public record ExecutionContext(
        Long tenantId,
        Long userId,
        Long currentOrgId,
        Set<String> permissions,
        String correlationId
) {
    public ExecutionContext {
        permissions = permissions == null ? Set.of() : Set.copyOf(permissions);
    }

    public static ExecutionContext capture() {
        return new ExecutionContext(
                TenantContext.getTenantId().orElse(null),
                SecurityContext.getUserId().orElse(null),
                SecurityContext.getCurrentOrgId().orElse(null),
                SecurityContext.getPermissions(),
                MDC.get("traceId")
        );
    }

    public ContextScope openScope() {
        return ContextScope.open(this);
    }

    public boolean empty() {
        return tenantId == null && userId == null && currentOrgId == null && permissions.isEmpty() && correlationId == null;
    }
}
