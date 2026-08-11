package com.company.iaf.platform.auth.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for {@code POST /api/platform/users/{id}/reset-password}.
 */
public record ResetPasswordRequest(
        @NotBlank
        @Size(min = 8, max = 64)
        String newPassword
) {
}
