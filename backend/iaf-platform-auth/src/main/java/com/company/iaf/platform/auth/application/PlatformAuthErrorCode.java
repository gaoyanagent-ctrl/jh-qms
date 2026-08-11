package com.company.iaf.platform.auth.application;

import com.company.iaf.shared.exception.ErrorCode;

/**
 * Module-local error codes for the platform authentication and user
 * management surface. These are stable codes the frontend can rely on
 * without inspecting the human-readable message.
 */
public enum PlatformAuthErrorCode implements ErrorCode {

    USERNAME_ALREADY_EXISTS("PLATFORM_AUTH_USERNAME_ALREADY_EXISTS", "Username already exists in the tenant"),
    USER_NOT_FOUND("PLATFORM_AUTH_USER_NOT_FOUND", "Platform user not found"),
    CANNOT_DISABLE_SELF("PLATFORM_AUTH_CANNOT_DISABLE_SELF", "An enabled user cannot disable themselves"),
    TENANT_CODE_ALREADY_EXISTS("PLATFORM_AUTH_TENANT_CODE_ALREADY_EXISTS", "Tenant code already exists"),
    TENANT_NOT_FOUND("PLATFORM_AUTH_TENANT_NOT_FOUND", "Tenant not found"),
    TENANT_DISABLED("PLATFORM_AUTH_TENANT_DISABLED", "Tenant is disabled"),
    TENANT_QUOTA_EXCEEDED("PLATFORM_AUTH_TENANT_QUOTA_EXCEEDED", "Tenant quota exceeded");

    private final String code;
    private final String message;

    PlatformAuthErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }
}
