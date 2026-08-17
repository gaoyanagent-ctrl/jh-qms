package com.company.iaf.mdm.application;

import com.company.iaf.shared.exception.ErrorCode;

public enum MdmErrorCode implements ErrorCode {
    MODEL_NOT_FOUND("MDM_MODEL_NOT_FOUND", "Master data model was not found"),
    MODEL_CODE_EXISTS("MDM_MODEL_CODE_EXISTS", "Master data model code already exists"),
    DOMAIN_NOT_FOUND("MDM_DOMAIN_NOT_FOUND", "Master data domain was not found"),
    MODEL_NOT_EDITABLE("MDM_MODEL_NOT_EDITABLE", "Only a draft model can be edited"),
    RECORD_NOT_FOUND("MDM_RECORD_NOT_FOUND", "Master data record was not found"),
    VALIDATION_FAILED("MDM_VALIDATION_FAILED", "Master data validation failed"),
    BUSINESS_CODE_EXISTS("MDM_BUSINESS_CODE_EXISTS", "Business code already exists"),
    IMPORT_FILE_INVALID("MDM_IMPORT_FILE_INVALID", "The import file is invalid"),
    IMPORT_TASK_NOT_FOUND("MDM_IMPORT_TASK_NOT_FOUND", "The import task was not found"),
    IMPORT_TASK_NOT_READY("MDM_IMPORT_TASK_NOT_READY", "The import task is not ready to commit"),
    IMPORT_STORAGE_FAILED("MDM_IMPORT_STORAGE_FAILED", "The import file storage operation failed"),
    IMPORT_ARTIFACT_NOT_FOUND("MDM_IMPORT_ARTIFACT_NOT_FOUND", "The import artifact was not found"),
    OPTIMISTIC_LOCK_CONFLICT("MDM_OPTIMISTIC_LOCK_CONFLICT", "The record was changed by another user"),
    RECORD_STATE_CONFLICT("MDM_RECORD_STATE_CONFLICT", "The action is not allowed in the current record state"),
    APPROVAL_ROLE_REQUIRED("MDM_APPROVAL_ROLE_REQUIRED", "An approval role must be configured"),
    MODEL_APPROVAL_INVALID_STATE("MDM_MODEL_APPROVAL_INVALID_STATE", "The model is not in the required publication approval state"),
    MODEL_APPROVAL_FORBIDDEN("MDM_MODEL_APPROVAL_FORBIDDEN", "The user is not allowed to approve this model publication"),
    APPROVAL_FORBIDDEN("MDM_APPROVAL_FORBIDDEN", "The current user is not an approver for this model");
    private final String code; private final String message;
    MdmErrorCode(String code, String message) { this.code = code; this.message = message; }
    public String code() { return code; } public String message() { return message; }
}
