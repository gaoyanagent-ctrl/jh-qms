package com.company.iaf.platform.auth.infrastructure.security;

import com.company.iaf.platform.auth.application.AuthTokenStore;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class AuthTokenStoreProductionGuard implements SmartInitializingSingleton {

    private final Environment environment;
    private final AuthTokenStore authTokenStore;

    public AuthTokenStoreProductionGuard(Environment environment, AuthTokenStore authTokenStore) {
        this.environment = environment;
        this.authTokenStore = authTokenStore;
    }

    @Override
    public void afterSingletonsInstantiated() {
        boolean prod = Arrays.asList(environment.getActiveProfiles()).contains("prod");
        if (prod && authTokenStore instanceof InMemoryAuthTokenStore) {
            throw new IllegalStateException("In-memory auth token store is not allowed in prod profile. Configure a durable token store.");
        }
    }
}
