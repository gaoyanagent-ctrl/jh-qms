package com.company.iaf.platform.core.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.company.iaf.shared.exception.BusinessException;
import com.company.iaf.shared.exception.CommonErrorCode;
import com.company.iaf.shared.security.SecurityContext;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(classes = RequiresPermissionAspectTest.TestConfig.class)
class RequiresPermissionAspectTest {

    private final SecuredUseCase securedUseCase;

    @Autowired
    RequiresPermissionAspectTest(SecuredUseCase securedUseCase) {
        this.securedUseCase = securedUseCase;
    }

    @AfterEach
    void clearContext() {
        SecurityContext.clear();
    }

    @Test
    void allowsMethodWhenPermissionIsPresent() {
        SecurityContext.setUserId(1L);
        SecurityContext.setPermissions(Set.of("platform:user:view"));

        assertThat(securedUseCase.viewUsers()).isEqualTo("ok");
    }

    @Test
    void rejectsMethodWhenPermissionIsMissing() {
        SecurityContext.setUserId(1L);
        SecurityContext.setPermissions(Set.of("platform:role:view"));

        assertThatThrownBy(securedUseCase::viewUsers)
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).errorCode())
                .isEqualTo(CommonErrorCode.FORBIDDEN);
    }

    @Configuration
    @EnableAspectJAutoProxy
    static class TestConfig {

        @Bean
        PermissionChecker permissionChecker() {
            return new PermissionChecker();
        }

        @Bean
        RequiresPermissionAspect requiresPermissionAspect(PermissionChecker permissionChecker) {
            return new RequiresPermissionAspect(permissionChecker);
        }

        @Bean
        SecuredUseCase securedUseCase() {
            return new SecuredUseCase();
        }
    }

    public static class SecuredUseCase {

        @RequiresPermission("platform:user:view")
        public String viewUsers() {
            return "ok";
        }
    }
}
