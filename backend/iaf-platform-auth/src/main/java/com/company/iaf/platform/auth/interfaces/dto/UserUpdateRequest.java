package com.company.iaf.platform.auth.interfaces.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for {@code PUT /api/platform/users/{id}}. Username and
 * status are intentionally not editable through this endpoint — use the
 * dedicated disable / reset-password actions for lifecycle changes.
 */
public record UserUpdateRequest(
        @NotBlank
        @Size(max = 128)
        String displayName,

        @Size(max = 32)
        String mobile,

        @Email
        @Size(max = 128)
        String email
) {
}
