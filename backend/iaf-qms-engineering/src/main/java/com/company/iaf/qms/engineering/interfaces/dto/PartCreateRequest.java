package com.company.iaf.qms.engineering.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Create a QMS part in the current organization")
public record PartCreateRequest(
        @NotBlank @Size(max = 128) String partNo,
        @Size(max = 128) String materialNo,
        @NotBlank @Size(max = 256) String partName,
        Long customerId,
        @Size(max = 128) String vehicleModel,
        Long supplierId,
        @Size(max = 32) String importanceLevel
) {
}
