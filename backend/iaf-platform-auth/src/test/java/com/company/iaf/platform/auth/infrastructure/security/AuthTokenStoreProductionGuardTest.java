package com.company.iaf.platform.auth.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthTokenStoreProductionGuardTest {

    @Test
    void rejectsInMemoryTokenStoreInProdProfile() {
        MockEnvironment environment = new MockEnvironment().withProperty("spring.profiles.active", "prod");
        environment.setActiveProfiles("prod");

        AuthTokenStoreProductionGuard guard = new AuthTokenStoreProductionGuard(environment, new InMemoryAuthTokenStore());

        assertThatThrownBy(guard::afterSingletonsInstantiated)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("In-memory auth token store is not allowed");
    }

    @Test
    void allowsInMemoryTokenStoreOutsideProdProfile() {
        MockEnvironment environment = new MockEnvironment();

        AuthTokenStoreProductionGuard guard = new AuthTokenStoreProductionGuard(environment, new InMemoryAuthTokenStore());

        assertThatCode(guard::afterSingletonsInstantiated).doesNotThrowAnyException();
    }
}
