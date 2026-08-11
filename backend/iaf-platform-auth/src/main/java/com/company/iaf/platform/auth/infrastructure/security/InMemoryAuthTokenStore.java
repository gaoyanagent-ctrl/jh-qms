package com.company.iaf.platform.auth.infrastructure.security;

import com.company.iaf.platform.auth.application.AuthTokenStore;
import com.company.iaf.platform.auth.domain.model.AuthToken;
import com.company.iaf.platform.auth.domain.model.AuthenticatedUser;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
@ConditionalOnProperty(name = "iaf.auth.token-store", havingValue = "memory", matchIfMissing = true)
public class InMemoryAuthTokenStore implements AuthTokenStore {

    private final ConcurrentMap<String, AuthToken> tokens = new ConcurrentHashMap<>();

    @Override
    public AuthToken issue(AuthenticatedUser user, Duration ttl) {
        AuthToken token = new AuthToken(UUID.randomUUID().toString(), Instant.now().plus(ttl), user);
        tokens.put(token.token(), token);
        return token;
    }

    @Override
    public Optional<AuthToken> find(String token) {
        AuthToken authToken = tokens.get(token);
        if (authToken == null) {
            return Optional.empty();
        }
        if (authToken.expiresAt().isBefore(Instant.now())) {
            tokens.remove(token);
            return Optional.empty();
        }
        return Optional.of(authToken);
    }
}
