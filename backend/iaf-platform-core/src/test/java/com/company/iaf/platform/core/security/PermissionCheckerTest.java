package com.company.iaf.platform.core.security;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.company.iaf.shared.exception.BusinessException;
import com.company.iaf.shared.exception.CommonErrorCode;
import com.company.iaf.shared.security.SecurityContext;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class PermissionCheckerTest {

    private final PermissionChecker permissionChecker = new PermissionChecker();

    @AfterEach
    void clearContext() {
        SecurityContext.clear();
    }

    @Test
    void requireAllPassesWhenUserHasEveryPermission() {
        SecurityContext.setUserId(1L);
        SecurityContext.setPermissions(Set.of("platform:user:view", "platform:user:create"));

        assertThatCode(() -> permissionChecker.requireAll("platform:user:view", "platform:user:create"))
                .doesNotThrowAnyException();
    }

    @Test
    void requireAllRejectsMissingPermission() {
        SecurityContext.setUserId(1L);
        SecurityContext.setPermissions(Set.of("platform:user:view"));

        assertThatThrownBy(() -> permissionChecker.requireAll("platform:user:view", "platform:user:create"))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).errorCode())
                .isEqualTo(CommonErrorCode.FORBIDDEN);
    }

    @Test
    void requireAnyPassesWhenUserHasOnePermission() {
        SecurityContext.setUserId(1L);
        SecurityContext.setPermissions(Set.of("platform:role:view"));

        assertThatCode(() -> permissionChecker.requireAny("platform:user:view", "platform:role:view"))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsUnauthenticatedUser() {
        SecurityContext.setPermissions(Set.of("platform:user:view"));

        assertThatThrownBy(() -> permissionChecker.requireAll("platform:user:view"))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).errorCode())
                .isEqualTo(CommonErrorCode.UNAUTHORIZED);
    }
}
