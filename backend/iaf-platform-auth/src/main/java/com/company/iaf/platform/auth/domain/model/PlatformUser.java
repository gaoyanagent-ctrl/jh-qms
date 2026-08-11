package com.company.iaf.platform.auth.domain.model;

import java.time.OffsetDateTime;

/**
 * Platform user aggregate. Excludes {@code passwordHash} so the domain
 * type can be safely shared with application services and DTO assemblers
 * without leaking credentials.
 */
public record PlatformUser(
        Long id,
        Long tenantId,
        String username,
        String displayName,
        String mobile,
        String email,
        UserStatus status,
        Long primaryOrgId,
        Integer version,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public boolean isEnabled() {
        return status == UserStatus.ENABLED;
    }
}
