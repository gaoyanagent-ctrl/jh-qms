package com.company.iaf.platform.permission.domain.repository;

import com.company.iaf.platform.permission.domain.model.Permission;

import java.util.Collection;
import java.util.List;

/**
 * Persistence boundary for platform permission lookups. The role
 * assignment flow uses {@link #findAllByCodes} to validate the codes
 * submitted by callers actually exist before persisting the bindings.
 */
public interface PermissionRepository {

    /**
     * Return all active permissions in the tenant, ordered by module and
     * permission code for stable frontend rendering.
     */
    List<Permission> findAll(long tenantId);

    /**
     * Return every permission whose {@code permission_code} is contained
     * in {@code codes} inside the tenant. Order is not guaranteed.
     */
    List<Permission> findAllByCodes(long tenantId, Collection<String> codes);

    /**
     * Return all permissions currently bound to {@code roleId}. The
     * returned list is empty when the role has no bindings or does not
     * exist.
     */
    List<Permission> findAllByRoleId(long tenantId, long roleId);
}
