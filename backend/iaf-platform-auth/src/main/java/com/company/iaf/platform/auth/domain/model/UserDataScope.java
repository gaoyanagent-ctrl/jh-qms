package com.company.iaf.platform.auth.domain.model;

import java.util.Set;

public record UserDataScope(Set<Long> orgIds) {

    public UserDataScope {
        orgIds = orgIds == null ? Set.of() : Set.copyOf(orgIds);
    }

    public static UserDataScope none() {
        return new UserDataScope(Set.of());
    }

    public static UserDataScope org(long orgId) {
        return new UserDataScope(Set.of(orgId));
    }

    public boolean empty() {
        return orgIds.isEmpty();
    }
}
