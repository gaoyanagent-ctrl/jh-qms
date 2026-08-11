package com.company.iaf.platform.auth.domain.repository;

import com.company.iaf.platform.auth.domain.model.LoginUser;

import java.util.Optional;

public interface AuthUserRepository {

    Optional<LoginUser> findByTenantIdAndUsername(long tenantId, String username);
}
