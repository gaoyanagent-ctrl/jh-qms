package com.company.iaf.platform.auth.interfaces.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TenantQuotaUpdateRequest(
        @NotBlank @Size(max = 64) String quotaKey,
        @Min(0) long quotaLimit
) {
}
