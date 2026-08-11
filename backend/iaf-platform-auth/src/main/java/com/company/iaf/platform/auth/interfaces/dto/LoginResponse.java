package com.company.iaf.platform.auth.interfaces.dto;

import java.time.Instant;
import java.util.List;
import java.util.Set;

public record LoginResponse(
        String tokenType,
        String accessToken,
        Instant expiresAt,
        Long tenantId,
        Long userId,
        String username,
        String displayName,
        Long currentOrgId,
        List<UserOrgItemResponse> organizations,
        Set<String> permissions
) {
}
