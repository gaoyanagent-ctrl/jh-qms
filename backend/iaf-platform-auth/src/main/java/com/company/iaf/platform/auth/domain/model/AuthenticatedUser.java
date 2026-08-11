package com.company.iaf.platform.auth.domain.model;

import java.util.List;
import java.util.Set;

public record AuthenticatedUser(
        Long userId,
        Long tenantId,
        String username,
        String displayName,
        Long currentOrgId,
        List<UserOrg> organizations,
        Set<String> permissions
) {
}
