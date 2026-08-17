package com.company.iaf.platform.auth.domain.repository;

import com.company.iaf.platform.auth.domain.model.LoginUser;
import com.company.iaf.platform.auth.domain.model.LoginTenantCandidate;

import java.util.List;
import java.util.Optional;

public interface AuthUserRepository {

    Optional<LoginUser> findByTenantIdAndUsername(long tenantId, String username);

    default List<LoginTenantCandidate> findTenantCandidatesByUsername(String username) {
        return List.of();
    }
}
