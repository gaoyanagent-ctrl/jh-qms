package com.company.iaf.platform.system.domain.model;

public record I18nResource(
        String locale,
        String resourceKey,
        String resourceValue
) {
}
