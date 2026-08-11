package com.company.iaf.platform.system.interfaces.dto;

import jakarta.validation.constraints.NotBlank;

public record BrandConfigRequest(
        @NotBlank String brandName,
        String logoUrl,
        String faviconUrl,
        @NotBlank String loginHeroTitle,
        @NotBlank String loginHeroSubtitle,
        @NotBlank String loginOpsTitle,
        @NotBlank String loginOpsDescription,
        @NotBlank String loginBackgroundType,
        String loginBackgroundImageUrl
) {
}
