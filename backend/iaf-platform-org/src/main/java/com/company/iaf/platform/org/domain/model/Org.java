package com.company.iaf.platform.org.domain.model;

import java.time.OffsetDateTime;

/**
 * Platform organization aggregate. Represents a node in the management
 * organization tree for a tenant. Manufacturing / WMS-specific nodes
 * (factory, workshop, warehouse, location) are intentionally not
 * modelled here.
 */
public record Org(
        Long id,
        Long tenantId,
        Long parentId,
        String orgCode,
        String orgName,
        OrgType orgType,
        OrgStatus status,
        int sortNo,
        Integer version,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
