package com.company.iaf.qms.engineering.domain.model;

import java.time.OffsetDateTime;

public record Part(
        Long id,
        long tenantId,
        long orgId,
        String partNo,
        String materialNo,
        String partName,
        Long customerId,
        String vehicleModel,
        Long supplierId,
        String importanceLevel,
        PartStatus status,
        int version,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
