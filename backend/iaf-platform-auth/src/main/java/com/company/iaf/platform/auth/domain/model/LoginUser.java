package com.company.iaf.platform.auth.domain.model;

import java.util.Set;

public record LoginUser(
        Long userId,
        Long tenantId,
        String username,
        String displayName,
        String passwordHash,
        String status,
        Set<String> permissions
) {
    public AuthenticatedUser toAuthenticatedUser(java.util.List<UserOrg> organizations) {
        Long currentOrgId = organizations.stream()
                .filter(UserOrg::primary)
                .map(UserOrg::orgId)
                .findFirst()
                .orElse(null);
        return new AuthenticatedUser(userId, tenantId, username, displayName, currentOrgId, organizations, permissions);
    }
}
