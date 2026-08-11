package com.company.iaf.shared.exception;

public enum CommonErrorCode implements ErrorCode {
    BAD_REQUEST("COMMON_BAD_REQUEST", "Bad request"),
    VALIDATION_FAILED("COMMON_VALIDATION_FAILED", "Validation failed"),
    INTERNAL_ERROR("COMMON_INTERNAL_ERROR", "Internal server error"),
    UNAUTHORIZED("COMMON_UNAUTHORIZED", "Unauthorized"),
    FORBIDDEN("COMMON_FORBIDDEN", "Forbidden"),
    NOT_FOUND("COMMON_NOT_FOUND", "Resource not found"),
    CONFLICT("COMMON_CONFLICT", "Resource conflict");

    private final String code;
    private final String message;

    CommonErrorCode(String code, String message) {
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
