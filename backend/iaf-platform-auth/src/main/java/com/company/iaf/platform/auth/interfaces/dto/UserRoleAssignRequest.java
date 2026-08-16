package com.company.iaf.platform.auth.interfaces.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UserRoleAssignRequest(@NotNull List<Long> roleIds) {
    public List<Long> safeRoleIds() {
        return roleIds == null ? List.of() : roleIds.stream().distinct().toList();
    }
}
