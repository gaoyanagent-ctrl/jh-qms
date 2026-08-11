package com.company.iaf.platform.system.interfaces.dto;

import com.company.iaf.platform.system.domain.model.I18nResource;

import java.util.List;

public record I18nResourceResponse(
        String locale,
        List<I18nResourceItem> resources
) {
    public static I18nResourceResponse from(String locale, List<I18nResource> resources) {
        return new I18nResourceResponse(locale, resources.stream().map(I18nResourceItem::from).toList());
    }
}
