package com.company.iaf.platform.auth.interfaces.dto;

import com.company.iaf.platform.auth.domain.model.UserOrg;

import java.time.OffsetDateTime;

public record UserOrgItemResponse(
        Long id,
        Long orgId,
        String orgCode,
        String orgName,
        String orgType,
        boolean primary,
        int scopeWeight,
        OffsetDateTime validFrom,
        OffsetDateTime validTo
) {

    public static UserOrgItemResponse from(UserOrg userOrg) {
        return new UserOrgItemResponse(
                userOrg.id(),
                userOrg.orgId(),
                userOrg.orgCode(),
                userOrg.orgName(),
                userOrg.orgType(),
                userOrg.primary(),
                userOrg.scopeWeight(),
                userOrg.validFrom(),
                userOrg.validTo()
        );
    }
}
