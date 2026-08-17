package com.company.iaf.platform.auth.application;

import com.company.iaf.platform.auth.domain.model.AuthToken;
import com.company.iaf.platform.auth.domain.model.AuthenticatedUser;
import com.company.iaf.platform.auth.domain.model.LoginUser;
import com.company.iaf.platform.auth.domain.model.LoginTenantCandidate;
import com.company.iaf.platform.auth.domain.model.TenantInfo;
import com.company.iaf.platform.auth.domain.model.TenantStatus;
import com.company.iaf.platform.auth.domain.model.UserOrg;
import com.company.iaf.platform.auth.domain.repository.AuthUserRepository;
import com.company.iaf.platform.auth.domain.repository.TenantRepository;
import com.company.iaf.platform.auth.domain.repository.UserOrgRepository;
import com.company.iaf.shared.exception.BusinessException;
import com.company.iaf.shared.exception.CommonErrorCode;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class AuthApplicationService {

    private static final Duration TOKEN_TTL = Duration.ofHours(8);
    private static final String USER_STATUS_ENABLED = "ENABLED";
    private static final String INVALID_CREDENTIALS = "Invalid tenant, username or password";

    private final TenantRepository tenantRepository;
    private final AuthUserRepository authUserRepository;
    private final UserOrgRepository userOrgRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenStore authTokenStore;

    public AuthApplicationService(
            TenantRepository tenantRepository,
            AuthUserRepository authUserRepository,
            UserOrgRepository userOrgRepository,
            PasswordEncoder passwordEncoder,
            AuthTokenStore authTokenStore
    ) {
        this.tenantRepository = tenantRepository;
        this.authUserRepository = authUserRepository;
        this.userOrgRepository = userOrgRepository;
        this.passwordEncoder = passwordEncoder;
        this.authTokenStore = authTokenStore;
    }

    public AuthToken login(String tenantCode, String username, String password) {
        TenantInfo tenant = tenantRepository.findByTenantCode(tenantCode)
                .filter(TenantInfo::enabled)
                .orElseThrow(AuthApplicationService::invalidCredentials);
        LoginUser user = authUserRepository.findByTenantIdAndUsername(tenant.tenantId(), username)
                .orElseThrow(AuthApplicationService::invalidCredentials);

        if (!USER_STATUS_ENABLED.equals(user.status())) {
            throw invalidCredentials();
        }
        if (!passwordEncoder.matches(password, user.passwordHash())) {
            throw invalidCredentials();
        }

        return authTokenStore.issue(
                user.toAuthenticatedUser(userOrgRepository.findByUserId(user.tenantId(), user.userId())),
                TOKEN_TTL
        );
    }

    public List<LoginTenantCandidate> discoverTenants(String username, String password) {
        List<LoginTenantCandidate> tenants = authUserRepository.findTenantCandidatesByUsername(username).stream()
                .filter(LoginTenantCandidate::enabled)
                .filter(candidate -> passwordEncoder.matches(password, candidate.passwordHash()))
                .toList();
        if (tenants.isEmpty()) {
            throw invalidCredentials();
        }
        return tenants;
    }

    private static BusinessException invalidCredentials() {
        return new BusinessException(CommonErrorCode.UNAUTHORIZED, INVALID_CREDENTIALS);
    }

    public AuthenticatedUser authenticate(String token) {
        return authTokenStore.find(token)
                .map(AuthToken::user)
                .filter(this::tenantEnabled)
                .map(this::refreshOrganizationSnapshot)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.UNAUTHORIZED, "Invalid or expired token"));
    }

    private boolean tenantEnabled(AuthenticatedUser user) {
        return tenantRepository.findById(user.tenantId())
                .filter(tenant -> tenant.status() == TenantStatus.ENABLED)
                .isPresent();
    }

    private AuthenticatedUser refreshOrganizationSnapshot(AuthenticatedUser user) {
        List<UserOrg> organizations = userOrgRepository.findByUserId(user.tenantId(), user.userId());
        Long currentOrgId = organizations.stream()
                .filter(UserOrg::primary)
                .map(UserOrg::orgId)
                .findFirst()
                .orElse(null);
        return new AuthenticatedUser(
                user.userId(),
                user.tenantId(),
                user.username(),
                user.displayName(),
                currentOrgId,
                organizations,
                user.permissions()
        );
    }
}
