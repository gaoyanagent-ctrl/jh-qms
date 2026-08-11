package com.company.iaf.platform.core.security;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

/**
 * Spring AOP aspect that intercepts any bean method annotated with
 * {@link RequiresPermission} (directly or via a class-level
 * annotation). The aspect delegates the actual allow/deny decision to
 * {@link PermissionChecker}.
 *
 * <p>Both the type-level and method-level annotations are checked so
 * shared permission sets can be declared once on a service class and
 * overridden per-method when a single operation needs different rules.
 */
@Aspect
@Component
public class RequiresPermissionAspect {

    private final PermissionChecker permissionChecker;

    public RequiresPermissionAspect(PermissionChecker permissionChecker) {
        this.permissionChecker = permissionChecker;
    }

    @Around("@within(com.company.iaf.platform.core.security.RequiresPermission)"
            + " || @annotation(com.company.iaf.platform.core.security.RequiresPermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        RequiresPermission typePermission = AnnotationUtils.findAnnotation(
                joinPoint.getTarget().getClass(),
                RequiresPermission.class
        );
        RequiresPermission methodPermission = AnnotationUtils.findAnnotation(
                signature.getMethod(),
                RequiresPermission.class
        );

        check(typePermission);
        check(methodPermission);

        return joinPoint.proceed();
    }

    private void check(RequiresPermission requiresPermission) {
        if (requiresPermission == null) {
            return;
        }
        if (requiresPermission.mode() == RequiresPermission.Mode.ANY) {
            permissionChecker.requireAny(requiresPermission.value());
            return;
        }
        permissionChecker.requireAll(requiresPermission.value());
    }
}
