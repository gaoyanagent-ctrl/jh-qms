package com.company.iaf.platform.system.interfaces.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record I18nResourceRequest(
        @NotBlank String locale,
        @Valid @NotEmpty List<I18nResourceItem> resources
) {
}
