package com.company.iaf.platform.auth.interfaces.dto;

import com.company.iaf.platform.auth.domain.model.UserOrg;

import java.util.List;

public record UserOrganizationsResponse(
        Long userId,
        Long primaryOrgId,
        List<UserOrgItemResponse> organizations
) {

    public static UserOrganizationsResponse from(long userId, List<UserOrg> organizations) {
        Long primaryOrgId = organizations.stream()
                .filter(UserOrg::primary)
                .map(UserOrg::orgId)
                .findFirst()
                .orElse(null);
        return new UserOrganizationsResponse(
                userId,
                primaryOrgId,
                organizations.stream().map(UserOrgItemResponse::from).toList()
        );
    }
}
