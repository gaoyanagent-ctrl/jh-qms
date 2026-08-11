package com.company.iaf.platform.org.interfaces.dto;

import com.company.iaf.platform.org.domain.model.Org;
import com.company.iaf.platform.org.domain.model.OrgStatus;
import com.company.iaf.platform.org.domain.model.OrgType;

import java.time.OffsetDateTime;

/**
 * Flat response payload for a single organization node. Use
 * {@link OrgTreeNodeResponse} when the caller needs the hierarchical
 * representation.
 */
public record OrgResponse(
        Long id,
        Long tenantId,
        Long parentId,
        String orgCode,
        String orgName,
        OrgType orgType,
        OrgStatus status,
        Integer sortNo,
        Integer version,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static OrgResponse from(Org org) {
        return new OrgResponse(
                org.id(),
                org.tenantId(),
                org.parentId(),
                org.orgCode(),
                org.orgName(),
                org.orgType(),
                org.status(),
                org.sortNo(),
                org.version(),
                org.createdAt(),
                org.updatedAt()
        );
    }
}