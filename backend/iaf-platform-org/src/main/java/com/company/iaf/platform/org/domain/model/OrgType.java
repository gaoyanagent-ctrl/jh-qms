package com.company.iaf.platform.org.domain.model;

/**
 * High-level classification of a platform organization. Manufacturing
 * factory, workshop, warehouse, and location are explicitly out of
 * scope — those belong to the manufacturing and WMS modules.
 */
public enum OrgType {
    COMPANY,
    DEPARTMENT,
    DIVISION,
    TEAM
}
