package com.company.iaf.platform.auth.interfaces.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Positive;

/**
 * Request body for {@code POST /api/platform/users}. The caller
 * supplies a plaintext password; the application service is
 * responsible for hashing it before persisting.
 */
public record UserCreateRequest(
        @NotBlank
        @Size(max = 64)
        String username,

        @NotBlank
        @Size(min = 8, max = 64)
        String password,

        @NotBlank
        @Size(max = 128)
        String displayName,

        @Size(max = 32)
        String mobile,

        @Email
        @Size(max = 128)
        String email,

        @Positive
        Long primaryOrgId
) {
    public UserCreateRequest(String username, String password, String displayName, String mobile, String email) {
        this(username, password, displayName, mobile, email, 1L);
    }
}
