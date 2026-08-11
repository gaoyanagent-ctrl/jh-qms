package com.company.iaf.platform.core.security;

import com.company.iaf.shared.exception.BusinessException;
import com.company.iaf.shared.exception.CommonErrorCode;
import com.company.iaf.shared.security.SecurityContext;
import java.util.Arrays;
import java.util.Objects;
import org.springframework.stereotype.Component;

/**
 * Helper that enforces the {@link RequiresPermission} contract against
 * {@link SecurityContext}. Sits next to {@link RequiresPermission} and
 * {@link RequiresPermissionAspect} in {@code platform-core} so it has
 * no dependency on the permission data layer; consumers must populate
 * {@link SecurityContext#getPermissions()} at request time (the auth
 * filter does this).
 */
@Component
public class PermissionChecker {

    public void requireAll(String... permissions) {
        requireAuthenticated();
        Arrays.stream(permissions)
                .filter(Objects::nonNull)
                .filter(permission -> !permission.isBlank())
                .filter(permission -> !SecurityContext.hasPermission(permission))
                .findFirst()
                .ifPresent(permission -> {
                    throw new BusinessException(CommonErrorCode.FORBIDDEN, "Missing permission: " + permission);
                });
    }

    public void requireAny(String... permissions) {
        requireAuthenticated();
        boolean hasAnyPermission = Arrays.stream(permissions)
                .filter(Objects::nonNull)
                .filter(permission -> !permission.isBlank())
                .anyMatch(SecurityContext::hasPermission);
        if (!hasAnyPermission) {
            throw new BusinessException(CommonErrorCode.FORBIDDEN, "Missing required permission");
        }
    }

    private void requireAuthenticated() {
        if (SecurityContext.getUserId().isEmpty()) {
            throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
        }
    }
}
