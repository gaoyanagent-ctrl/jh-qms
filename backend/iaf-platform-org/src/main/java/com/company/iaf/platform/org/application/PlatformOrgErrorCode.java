package com.company.iaf.platform.org.application;

import com.company.iaf.shared.exception.ErrorCode;

/**
 * Module-local error codes for the platform organization surface.
 */
public enum PlatformOrgErrorCode implements ErrorCode {

    ORG_CODE_ALREADY_EXISTS("PLATFORM_ORG_CODE_ALREADY_EXISTS", "Organization code already exists in the tenant"),
    ORG_NOT_FOUND("PLATFORM_ORG_NOT_FOUND", "Organization not found"),
    ORG_HAS_CHILDREN("PLATFORM_ORG_HAS_CHILDREN", "Organization has child nodes and cannot be removed"),
    ORG_PARENT_NOT_FOUND("PLATFORM_ORG_PARENT_NOT_FOUND", "Parent organization not found");

    private final String code;
    private final String message;

    PlatformOrgErrorCode(String code, String message) {
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