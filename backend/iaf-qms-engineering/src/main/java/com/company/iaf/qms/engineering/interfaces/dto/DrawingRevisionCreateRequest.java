package com.company.iaf.qms.engineering.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "Create a metadata-only drawing revision draft")
public record DrawingRevisionCreateRequest(
        @NotBlank @Size(max = 64) String revisionCode,
        LocalDate effectiveDate,
        Long supersedesRevisionId
) {
}
