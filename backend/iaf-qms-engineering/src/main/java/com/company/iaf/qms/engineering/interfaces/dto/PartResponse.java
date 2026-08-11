package com.company.iaf.qms.engineering.interfaces.dto;

import com.company.iaf.qms.engineering.domain.model.Part;
import com.company.iaf.qms.engineering.domain.model.PartStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "QMS part")
public record PartResponse(
        Long id,
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
    public static PartResponse from(Part part) {
        return new PartResponse(
                part.id(), part.orgId(), part.partNo(), part.materialNo(), part.partName(),
                part.customerId(), part.vehicleModel(), part.supplierId(), part.importanceLevel(),
                part.status(), part.version(), part.createdAt(), part.updatedAt()
        );
    }
}
