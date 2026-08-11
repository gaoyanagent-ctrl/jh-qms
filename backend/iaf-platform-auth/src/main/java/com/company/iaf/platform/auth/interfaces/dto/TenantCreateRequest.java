package com.company.iaf.platform.auth.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TenantCreateRequest(
        @NotBlank @Size(max = 64) @Pattern(regexp = "[A-Za-z0-9_-]+") String tenantCode,
        @NotBlank @Size(max = 128) String tenantName,
        @NotBlank @Size(max = 64) @Pattern(regexp = "[A-Za-z0-9_.@-]+") String adminUsername,
        @NotBlank @Size(min = 8, max = 64) String adminPassword
) {
}
