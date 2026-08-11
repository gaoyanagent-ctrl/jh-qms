package com.company.iaf.platform.auth.interfaces.dto;

import jakarta.validation.constraints.NotNull;

public record UserOrgContextSwitchRequest(
        @NotNull Long orgId
) {
}
