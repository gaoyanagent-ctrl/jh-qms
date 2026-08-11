package com.company.iaf.platform.permission.interfaces.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AssignRoleMenusRequest(
        @NotNull List<String> menuCodes
) {
}
