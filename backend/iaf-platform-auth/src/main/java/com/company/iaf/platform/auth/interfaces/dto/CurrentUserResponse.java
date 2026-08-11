package com.company.iaf.platform.auth.interfaces.dto;

import java.util.List;
import java.util.Set;

public record CurrentUserResponse(
        Long tenantId,
        Long userId,
        String username,
        String displayName,
        Long currentOrgId,
        List<UserOrgItemResponse> organizations,
        Set<String> permissions
) {
}
