package com.company.iaf.platform.auth.infrastructure.security;

import com.company.iaf.platform.auth.application.AuthApplicationService;
import com.company.iaf.platform.auth.domain.model.AuthenticatedUser;
import com.company.iaf.shared.exception.BusinessException;
import com.company.iaf.shared.security.SecurityContext;
import com.company.iaf.shared.tenant.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@ConditionalOnBean(AuthApplicationService.class)
public class BearerTokenAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthApplicationService authApplicationService;

    public BearerTokenAuthenticationFilter(AuthApplicationService authApplicationService) {
        this.authApplicationService = authApplicationService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            TenantContext.clear();
            SecurityContext.clear();
            SecurityContextHolder.clearContext();
            MDC.put("traceId", traceId(request));
            String header = request.getHeader("Authorization");
            if (header != null && header.startsWith(BEARER_PREFIX)) {
                authenticate(header.substring(BEARER_PREFIX.length()));
            }
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
            SecurityContext.clear();
            SecurityContextHolder.clearContext();
            clearMdc();
        }
    }

    private void authenticate(String token) {
        try {
            AuthenticatedUser user = authApplicationService.authenticate(token);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    user,
                    null,
                    user.permissions().stream()
                            .map(permission -> new SimpleGrantedAuthority("PERM_" + permission))
                            .toList()
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            TenantContext.setTenantId(user.tenantId());
            SecurityContext.setUserId(user.userId());
            SecurityContext.setCurrentOrgId(user.currentOrgId());
            SecurityContext.setPermissions(user.permissions());
            MDC.put("tenantId", String.valueOf(user.tenantId()));
            MDC.put("userId", String.valueOf(user.userId()));
            if (user.currentOrgId() != null) {
                MDC.put("currentOrgId", String.valueOf(user.currentOrgId()));
            }
        } catch (BusinessException ignored) {
            SecurityContextHolder.clearContext();
        }
    }

    private static String traceId(HttpServletRequest request) {
        String header = request.getHeader("X-Correlation-Id");
        return header == null || header.isBlank() ? UUID.randomUUID().toString() : header;
    }

    private static void clearMdc() {
        MDC.remove("tenantId");
        MDC.remove("userId");
        MDC.remove("currentOrgId");
        MDC.remove("traceId");
    }
}
