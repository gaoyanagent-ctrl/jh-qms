package com.company.iaf.platform.auth.interfaces.dto;

import com.company.iaf.platform.auth.domain.model.PlatformUser;
import com.company.iaf.platform.auth.domain.model.UserOrg;
import com.company.iaf.platform.auth.domain.model.UserStatus;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Response payload for platform user APIs. {@code passwordHash} is
 * intentionally not exposed.
 */
public record UserResponse(
        Long id,
        Long tenantId,
        String username,
        String displayName,
        String mobile,
        String email,
        UserStatus status,
        Long primaryOrgId,
        List<UserOrgItemResponse> organizations,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static UserResponse from(PlatformUser user) {
        return from(user, List.of());
    }

    public static UserResponse from(PlatformUser user, List<UserOrg> organizations) {
        Long primaryOrgId = organizations.stream()
                .filter(UserOrg::primary)
                .map(UserOrg::orgId)
                .findFirst()
                .orElse(user.primaryOrgId());
        return new UserResponse(
                user.id(),
                user.tenantId(),
                user.username(),
                user.displayName(),
                user.mobile(),
                user.email(),
                user.status(),
                primaryOrgId,
                organizations.stream().map(UserOrgItemResponse::from).toList(),
                user.createdAt(),
                user.updatedAt()
        );
    }
}
