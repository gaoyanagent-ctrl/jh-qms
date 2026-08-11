package com.company.iaf.platform.system.domain.model;

public record BrandConfig(
        String brandName,
        String logoUrl,
        String faviconUrl,
        String loginHeroTitle,
        String loginHeroSubtitle,
        String loginOpsTitle,
        String loginOpsDescription,
        String loginBackgroundType,
        String loginBackgroundImageUrl
) {
    public static BrandConfig defaults() {
        return new BrandConfig(
                "IAF 平台",
                null,
                null,
                "面向制造企业的工业应用基础平台",
                "统一平台管理、权限、组织、审批、集成和业务扩展能力，为 WMS、MES、SRM、QMS 等工业系统提供一致工程底座。",
                "工业运营视图",
                "主题、权限和复杂视图能力从平台层统一治理，支撑管理端、Kanban、设计器和大屏场景。",
                "token",
                null
        );
    }
}
