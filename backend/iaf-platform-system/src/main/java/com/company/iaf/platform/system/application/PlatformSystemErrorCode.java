package com.company.iaf.platform.system.application;

import com.company.iaf.shared.exception.ErrorCode;

public enum PlatformSystemErrorCode implements ErrorCode {
    CONFIG_NOT_FOUND("PLATFORM_SYSTEM_CONFIG_NOT_FOUND", "Platform system configuration not found"),
    INVALID_CONFIGURATION("PLATFORM_SYSTEM_INVALID_CONFIGURATION", "Platform system configuration is invalid");

    private final String code;
    private final String message;

    PlatformSystemErrorCode(String code, String message) {
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
