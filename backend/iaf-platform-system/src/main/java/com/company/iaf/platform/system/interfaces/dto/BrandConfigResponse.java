package com.company.iaf.platform.system.interfaces.dto;

import com.company.iaf.platform.system.domain.model.BrandConfig;

public record BrandConfigResponse(
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
    public static BrandConfigResponse from(BrandConfig config) {
        return new BrandConfigResponse(
                config.brandName(),
                config.logoUrl(),
                config.faviconUrl(),
                config.loginHeroTitle(),
                config.loginHeroSubtitle(),
                config.loginOpsTitle(),
                config.loginOpsDescription(),
                config.loginBackgroundType(),
                config.loginBackgroundImageUrl()
        );
    }

    public BrandConfig toDomain() {
        return new BrandConfig(
                brandName,
                logoUrl,
                faviconUrl,
                loginHeroTitle,
                loginHeroSubtitle,
                loginOpsTitle,
                loginOpsDescription,
                loginBackgroundType,
                loginBackgroundImageUrl
        );
    }
}
