package com.company.iaf.platform.system.interfaces.dto;

import com.company.iaf.platform.system.domain.model.I18nResource;
import jakarta.validation.constraints.NotBlank;

public record I18nResourceItem(
        @NotBlank String resourceKey,
        @NotBlank String resourceValue
) {
    public I18nResource toDomain(String locale) {
        return new I18nResource(locale, resourceKey, resourceValue);
    }

    public static I18nResourceItem from(I18nResource resource) {
        return new I18nResourceItem(resource.resourceKey(), resource.resourceValue());
    }
}
