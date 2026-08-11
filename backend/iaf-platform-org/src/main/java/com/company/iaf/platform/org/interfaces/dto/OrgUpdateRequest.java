package com.company.iaf.platform.org.interfaces.dto;

import com.company.iaf.platform.org.domain.model.OrgStatus;
import com.company.iaf.platform.org.domain.model.OrgType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request body for {@code PUT /api/platform/orgs/{id}}. {@code orgCode}
 * is intentionally editable to support tenant-driven renumbering;
 * uniqueness is enforced inside the application service.
 */
public record OrgUpdateRequest(
        Long parentId,

        @NotBlank
        @Size(max = 64)
        String orgCode,

        @NotBlank
        @Size(max = 128)
        String orgName,

        @NotNull
        OrgType orgType,

        @NotNull
        OrgStatus status,

        @Min(0)
        Integer sortNo
) {
}