package com.company.iaf.platform.auth.interfaces.dto;

import com.company.iaf.platform.auth.domain.model.LoginTenantCandidate;

public record LoginTenantOptionResponse(
        String tenantCode,
        String tenantName
) {
    public static LoginTenantOptionResponse from(LoginTenantCandidate candidate) {
        return new LoginTenantOptionResponse(candidate.tenantCode(), candidate.tenantName());
    }
}
