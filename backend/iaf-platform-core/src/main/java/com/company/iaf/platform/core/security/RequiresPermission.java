package com.company.iaf.platform.core.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declarative permission check for application-service use cases. The
 * aspect ({@link RequiresPermissionAspect}) enforces the check before
 * the method runs; throw-based access denial is converted to the
 * unified business exception by {@link GlobalExceptionHandler}.
 *
 * <p>Lives in {@code platform-core} (not {@code platform-permission}) so
 * every platform module can apply permission checks without depending
 * on the permission data layer. The aspect reads only from
 * {@link com.company.iaf.shared.security.SecurityContext}, which is
 * populated by the auth filter chain at request time.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresPermission {

    String[] value();

    Mode mode() default Mode.ALL;

    enum Mode {
        ALL,
        ANY
    }
}
