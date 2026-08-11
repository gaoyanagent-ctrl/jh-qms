package com.company.iaf.qms.engineering.interfaces.controller;

import com.company.iaf.qms.engineering.application.QmsEngineeringErrorCode;
import com.company.iaf.shared.exception.BusinessException;
import com.company.iaf.shared.exception.CommonErrorCode;
import com.company.iaf.shared.security.SecurityContext;
import com.company.iaf.shared.tenant.TenantContext;

final class QmsRequestContext {

    private QmsRequestContext() {
    }

    static long tenantId() {
        return TenantContext.getTenantId()
                .orElseThrow(() -> new BusinessException(CommonErrorCode.UNAUTHORIZED, "Tenant context is missing"));
    }

    static long orgId() {
        return SecurityContext.getCurrentOrgId()
                .orElseThrow(() -> new BusinessException(QmsEngineeringErrorCode.CURRENT_ORG_REQUIRED));
    }
}
