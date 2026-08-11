package com.company.iaf.platform.auth.domain.model;

import java.time.Instant;

public record AuthToken(
        String token,
        Instant expiresAt,
        AuthenticatedUser user
) {
}

