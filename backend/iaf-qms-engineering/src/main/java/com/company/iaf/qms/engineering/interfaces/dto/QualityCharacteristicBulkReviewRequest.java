package com.company.iaf.qms.engineering.interfaces.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

public record QualityCharacteristicBulkReviewRequest(
        @NotNull Decision decision,
        @NotEmpty @Size(max = 200) List<@Valid Target> targets,
        @Size(max = 1000) String comment) {
    public enum Decision { CONFIRMED, REJECTED }
    public record Target(@Positive long id, @NotNull @Positive Integer version) { }
}
