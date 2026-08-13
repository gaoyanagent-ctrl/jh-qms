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
    FILE_REQUIRED("QMS_FILE_REQUIRED", "A non-empty file is required"),
    FILE_TYPE_UNSUPPORTED("QMS_FILE_TYPE_UNSUPPORTED", "Only PDF and DWG files are supported"),
    FILE_TOO_LARGE("QMS_FILE_TOO_LARGE", "File exceeds the 100 MiB limit"),
    FILE_ALREADY_ATTACHED("QMS_FILE_ALREADY_ATTACHED", "This revision already has a file"),
    FILE_DUPLICATE("QMS_FILE_DUPLICATE", "The same file is already attached to this revision"),
    FILE_NOT_FOUND("QMS_FILE_NOT_FOUND", "Drawing revision file not found"),
    FILE_STORAGE_FAILED("QMS_FILE_STORAGE_FAILED", "Object storage operation failed"),
    FILE_UPLOAD_FAILED("QMS_FILE_UPLOAD_FAILED", "File upload could not be completed"),
    REVISION_INVALID_STATE("QMS_DRAWING_REVISION_INVALID_STATE", "Drawing revision action is not allowed in its current state"),
    PARSE_JOB_NOT_FOUND("QMS_PARSE_JOB_NOT_FOUND", "Drawing parse job not found"),
    PARSE_JOB_NOT_RETRYABLE("QMS_PARSE_JOB_NOT_RETRYABLE", "Only a failed latest parse job can be retried"),
    PARSE_JOB_INVALID_STATE("QMS_PARSE_JOB_INVALID_STATE", "Parse job action is not allowed in its current state"),
    PARSE_RESULT_INVALID("QMS_PARSE_RESULT_INVALID", "Drawing parse result does not satisfy the DIM contract"),
    INTERMEDIATE_MODEL_NOT_FOUND("QMS_INTERMEDIATE_MODEL_NOT_FOUND", "Drawing intermediate model not found"),
    EVIDENCE_NOT_FOUND("QMS_SOURCE_EVIDENCE_NOT_FOUND", "Source evidence not found"),
    CHARACTERISTIC_REVIEW_INVALID("QMS_CHARACTERISTIC_REVIEW_INVALID", "Characteristic review decision is invalid"),
    CHARACTERISTIC_REVIEW_CONFLICT("QMS_CHARACTERISTIC_REVIEW_CONFLICT", "Characteristic was already reviewed or changed"),
    CHARACTERISTIC_CLASSIFICATION_INVALID("QMS_CHARACTERISTIC_CLASSIFICATION_INVALID", "Characteristic classification is invalid"),
    INSPECTION_STANDARD_NOT_FOUND("QMS_INSPECTION_STANDARD_NOT_FOUND", "Inspection standard draft not found"),
    INSPECTION_STANDARD_NO_ELIGIBLE_SOURCE("QMS_INSPECTION_STANDARD_NO_ELIGIBLE_SOURCE", "No eligible confirmed quality characteristic"),
    INSPECTION_STANDARD_CONFLICT("QMS_INSPECTION_STANDARD_CONFLICT", "Inspection standard was changed by another user"),
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
