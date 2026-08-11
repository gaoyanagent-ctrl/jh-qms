package com.company.iaf.platform.permission.application;

import com.company.iaf.shared.exception.ErrorCode;

/**
 * Module-local error codes for the platform role and permission
 * assignment surface.
 */
public enum PlatformPermissionErrorCode implements ErrorCode {

    ROLE_CODE_ALREADY_EXISTS("PLATFORM_PERMISSION_ROLE_CODE_ALREADY_EXISTS", "Role code already exists in the tenant"),
    ROLE_NOT_FOUND("PLATFORM_PERMISSION_ROLE_NOT_FOUND", "Role not found"),
    PERMISSION_NOT_FOUND("PLATFORM_PERMISSION_NOT_FOUND", "Permission code does not exist in the tenant"),
    MENU_CODE_ALREADY_EXISTS("PLATFORM_PERMISSION_MENU_CODE_ALREADY_EXISTS", "Menu code already exists in the tenant"),
    MENU_NOT_FOUND("PLATFORM_PERMISSION_MENU_NOT_FOUND", "Menu not found");

    private final String code;
    private final String message;

    PlatformPermissionErrorCode(String code, String message) {
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
