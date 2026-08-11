package com.company.iaf.platform.auth.interfaces.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.List;

public record UserOrgAssignRequest(
        @Valid List<Item> organizations
) {

    public List<Item> safeOrganizations() {
        return organizations == null ? List.of() : organizations;
    }

    public record Item(
            @NotNull Long orgId,
            boolean primary,
            Integer scopeWeight,
            OffsetDateTime validFrom,
            OffsetDateTime validTo
    ) {
    }
}
