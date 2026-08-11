package com.company.iaf.platform.auth.application;

import com.company.iaf.platform.auth.domain.model.AuthToken;
import com.company.iaf.platform.auth.domain.model.AuthenticatedUser;

import java.time.Duration;
import java.util.Optional;

public interface AuthTokenStore {

    AuthToken issue(AuthenticatedUser user, Duration ttl);

    Optional<AuthToken> find(String token);
}

