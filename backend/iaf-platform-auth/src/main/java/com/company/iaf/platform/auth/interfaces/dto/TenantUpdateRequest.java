package com.company.iaf.platform.auth.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TenantUpdateRequest(
        @NotBlank @Size(max = 128) String tenantName
) {
}
