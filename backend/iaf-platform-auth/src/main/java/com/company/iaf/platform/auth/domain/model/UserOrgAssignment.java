package com.company.iaf.platform.auth.domain.model;

import java.time.OffsetDateTime;

public record UserOrgAssignment(
        Long orgId,
        boolean primary,
        int scopeWeight,
        OffsetDateTime validFrom,
        OffsetDateTime validTo
) {
}
