package com.company.iaf.platform.auth.interfaces.dto;

import java.util.List;

public record UserRolesResponse(long userId, List<Long> roleIds) {
}
