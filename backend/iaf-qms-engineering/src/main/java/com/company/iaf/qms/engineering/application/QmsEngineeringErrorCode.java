package com.company.iaf.qms.engineering.application;

import com.company.iaf.shared.exception.ErrorCode;

public enum QmsEngineeringErrorCode implements ErrorCode {
    CURRENT_ORG_REQUIRED("QMS_ENGINEERING_CURRENT_ORG_REQUIRED", "Current organization context is required"),
    PART_NOT_FOUND("QMS_PART_NOT_FOUND", "Part not found"),
    PART_NO_ALREADY_EXISTS("QMS_PART_NO_ALREADY_EXISTS", "Part number already exists in the current organization"),
    DRAWING_NOT_FOUND("QMS_DRAWING_NOT_FOUND", "Drawing not found"),
    DRAWING_NO_ALREADY_EXISTS("QMS_DRAWING_NO_ALREADY_EXISTS", "Drawing number already exists for this part"),
    REVISION_NOT_FOUND("QMS_DRAWING_REVISION_NOT_FOUND", "Drawing revision not found"),
    REVISION_CODE_ALREADY_EXISTS("QMS_DRAWING_REVISION_CODE_ALREADY_EXISTS", "Revision code already exists for this drawing"),
    SUPERSEDES_REVISION_INVALID("QMS_DRAWING_REVISION_SUPERSEDES_INVALID", "Superseded revision does not belong to this drawing"),
    AUDIT_WRITE_FAILED("QMS_AUDIT_WRITE_FAILED", "QMS audit record could not be written");

    private final String code;
    private final String message;

    QmsEngineeringErrorCode(String code, String message) {
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
