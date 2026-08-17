package com.company.iaf.platform.auth.interfaces.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginTenantDiscoveryRequest(
        @NotBlank String username,
        @NotBlank String password
) {
}
