package com.company.iaf.platform.permission.interfaces.dto;

import com.company.iaf.platform.permission.domain.model.RoleStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request body for {@code PUT /api/platform/roles/{id}}.
 */
public record RoleUpdateRequest(
        @NotBlank
        @Size(max = 64)
        String roleCode,

        @NotBlank
        @Size(max = 128)
        String roleName,

        @NotBlank
        @Size(max = 32)
        String roleType,

        @NotNull
        RoleStatus status
) {
}