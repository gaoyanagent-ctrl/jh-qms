package com.company.iaf.platform.auth.interfaces.controller;

import com.company.iaf.platform.auth.application.AuthApplicationService;
import com.company.iaf.platform.auth.domain.model.AuthToken;
import com.company.iaf.platform.auth.domain.model.AuthenticatedUser;
import com.company.iaf.platform.auth.interfaces.dto.CurrentUserResponse;
import com.company.iaf.platform.auth.interfaces.dto.LoginRequest;
import com.company.iaf.platform.auth.interfaces.dto.LoginResponse;
import com.company.iaf.platform.auth.interfaces.dto.LoginTenantDiscoveryRequest;
import com.company.iaf.platform.auth.interfaces.dto.LoginTenantOptionResponse;
import com.company.iaf.shared.result.Result;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/platform/auth")
public class AuthController {

    private static final String BEARER_TOKEN_TYPE = "Bearer";

    private final AuthApplicationService authApplicationService;

    public AuthController(AuthApplicationService authApplicationService) {
        this.authApplicationService = authApplicationService;
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthToken token = authApplicationService.login(request.tenantCode(), request.username(), request.password());
        AuthenticatedUser user = token.user();
        return Result.ok(new LoginResponse(
                BEARER_TOKEN_TYPE,
                token.token(),
                token.expiresAt(),
                user.tenantId(),
                user.userId(),
                user.username(),
                user.displayName(),
                user.currentOrgId(),
                user.organizations().stream().map(com.company.iaf.platform.auth.interfaces.dto.UserOrgItemResponse::from).toList(),
                user.permissions()
        ));
    }

    @PostMapping("/login/tenants")
    public Result<List<LoginTenantOptionResponse>> discoverTenants(
            @Valid @RequestBody LoginTenantDiscoveryRequest request
    ) {
        return Result.ok(authApplicationService.discoverTenants(request.username(), request.password()).stream()
                .map(LoginTenantOptionResponse::from)
                .toList());
    }

    @GetMapping("/me")
    public Result<CurrentUserResponse> me(@AuthenticationPrincipal AuthenticatedUser user) {
        return Result.ok(new CurrentUserResponse(
                user.tenantId(),
                user.userId(),
                user.username(),
                user.displayName(),
                user.currentOrgId(),
                user.organizations().stream().map(com.company.iaf.platform.auth.interfaces.dto.UserOrgItemResponse::from).toList(),
                user.permissions()
        ));
    }
}
