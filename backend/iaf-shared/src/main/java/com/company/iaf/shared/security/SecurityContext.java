package com.company.iaf.shared.security;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public final class SecurityContext {

    private static final ThreadLocal<Long> CURRENT_USER = new ThreadLocal<>();
    private static final ThreadLocal<Long> CURRENT_ORG = new ThreadLocal<>();
    private static final ThreadLocal<Set<String>> CURRENT_PERMISSIONS = new ThreadLocal<>();

    private SecurityContext() {
    }

    public static void setUserId(Long userId) {
        CURRENT_USER.set(userId);
    }

    public static Optional<Long> getUserId() {
        return Optional.ofNullable(CURRENT_USER.get());
    }

    public static void setCurrentOrgId(Long orgId) {
        CURRENT_ORG.set(orgId);
    }

    public static Optional<Long> getCurrentOrgId() {
        return Optional.ofNullable(CURRENT_ORG.get());
    }

    public static void setPermissions(Set<String> permissions) {
        CURRENT_PERMISSIONS.set(new HashSet<>(permissions));
    }

    public static Set<String> getPermissions() {
        Set<String> permissions = CURRENT_PERMISSIONS.get();
        if (permissions == null) {
            return Set.of();
        }
        return Collections.unmodifiableSet(permissions);
    }

    public static boolean hasPermission(String permission) {
        return getPermissions().contains(permission);
    }

    public static void clear() {
        CURRENT_USER.remove();
        CURRENT_ORG.remove();
        CURRENT_PERMISSIONS.remove();
    }
}
