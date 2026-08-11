package com.company.iaf.platform.system.interfaces.controller;

import com.company.iaf.platform.system.application.SystemConfigurationApplicationService;
import com.company.iaf.platform.system.domain.model.BrandConfig;
import com.company.iaf.platform.system.domain.model.ThemeConfig;
import com.company.iaf.platform.system.interfaces.dto.BrandConfigRequest;
import com.company.iaf.platform.system.interfaces.dto.BrandConfigResponse;
import com.company.iaf.platform.system.interfaces.dto.I18nResourceItem;
import com.company.iaf.platform.system.interfaces.dto.I18nResourceRequest;
import com.company.iaf.platform.system.interfaces.dto.I18nResourceResponse;
import com.company.iaf.platform.system.interfaces.dto.ThemeConfigRequest;
import com.company.iaf.platform.system.interfaces.dto.ThemeConfigResponse;
import com.company.iaf.platform.system.interfaces.dto.UserPreferenceRequest;
import com.company.iaf.platform.system.interfaces.dto.UserPreferenceResponse;
import com.company.iaf.shared.exception.BusinessException;
import com.company.iaf.shared.exception.CommonErrorCode;
import com.company.iaf.shared.result.Result;
import com.company.iaf.shared.security.SecurityContext;
import com.company.iaf.shared.tenant.TenantContext;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/platform")
public class SystemConfigurationController {

    private final SystemConfigurationApplicationService service;

    public SystemConfigurationController(SystemConfigurationApplicationService service) {
        this.service = service;
    }

    @GetMapping("/theme/current")
    public Result<ThemeConfigResponse> getTheme() {
        return Result.ok(ThemeConfigResponse.from(service.getTheme(currentTenantId())));
    }

    @PutMapping("/theme/current")
    public Result<ThemeConfigResponse> saveTheme(@Valid @RequestBody ThemeConfigRequest request) {
        ThemeConfig saved = service.saveTheme(
                currentTenantId(),
                new ThemeConfig(request.themeName(), request.primaryColor(), request.sidebarMode(), request.tokens())
        );
        return Result.ok(ThemeConfigResponse.from(saved));
    }

    @GetMapping("/brand/current")
    public Result<BrandConfigResponse> getBrand() {
        return Result.ok(BrandConfigResponse.from(service.getBrand(currentTenantId())));
    }

    @PutMapping("/brand/current")
    public Result<BrandConfigResponse> saveBrand(@Valid @RequestBody BrandConfigRequest request) {
        BrandConfig saved = service.saveBrand(
                currentTenantId(),
                new BrandConfig(
                        request.brandName(),
                        request.logoUrl(),
                        request.faviconUrl(),
                        request.loginHeroTitle(),
                        request.loginHeroSubtitle(),
                        request.loginOpsTitle(),
                        request.loginOpsDescription(),
                        request.loginBackgroundType(),
                        request.loginBackgroundImageUrl()
                )
        );
        return Result.ok(BrandConfigResponse.from(saved));
    }

    @GetMapping("/i18n/resources")
    public Result<I18nResourceResponse> listI18nResources(@RequestParam("locale") String locale) {
        return Result.ok(I18nResourceResponse.from(locale, service.listI18nResources(currentTenantId(), locale)));
    }

    @PutMapping("/i18n/resources")
    public Result<Void> replaceI18nResources(@Valid @RequestBody I18nResourceRequest request) {
        service.replaceI18nResources(
                currentTenantId(),
                request.locale(),
                request.resources().stream().map(item -> item.toDomain(request.locale())).toList()
        );
        return Result.ok();
    }

    @GetMapping("/preferences/me")
    public Result<UserPreferenceResponse> getMyPreference() {
        return Result.ok(UserPreferenceResponse.from(service.getMyPreference(currentTenantId(), currentUserId())));
    }

    @PutMapping("/preferences/me")
    public Result<UserPreferenceResponse> saveMyPreference(@Valid @RequestBody UserPreferenceRequest request) {
        return Result.ok(UserPreferenceResponse.from(service.saveMyPreference(currentTenantId(), currentUserId(), request.settings())));
    }

    private static long currentTenantId() {
        return TenantContext.getTenantId()
                .orElseThrow(() -> new BusinessException(CommonErrorCode.UNAUTHORIZED, "Tenant context is missing"));
    }

    private static long currentUserId() {
        return SecurityContext.getUserId()
                .orElseThrow(() -> new BusinessException(CommonErrorCode.UNAUTHORIZED, "Security context is missing"));
    }
}
