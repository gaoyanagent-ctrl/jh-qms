package com.company.iaf.qms.engineering.interfaces.dto;

import com.company.iaf.qms.engineering.domain.model.DrawingSourceSystem;
import com.company.iaf.qms.engineering.domain.model.DrawingType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Create a drawing under a QMS part")
public record DrawingCreateRequest(
        @NotBlank @Size(max = 128) String drawingNo,
        @NotBlank @Size(max = 256) String drawingName,
        @NotNull DrawingType drawingType,
        DrawingSourceSystem sourceSystem
) {
}
