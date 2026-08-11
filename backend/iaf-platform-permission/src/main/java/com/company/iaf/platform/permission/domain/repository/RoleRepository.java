package com.company.iaf.platform.permission.domain.repository;

import com.company.iaf.platform.permission.domain.model.Role;
import com.company.iaf.platform.permission.domain.model.RoleStatus;

import java.util.List;
import java.util.Optional;

/**
 * Persistence boundary for platform roles.
 *
 * <p>Audit fields ({@code created_by}, {@code updated_by}) are supplied
 * as a separate {@code operatorUserId} parameter rather than carried
 * inside the domain model, so the actor on the write is always the
 * currently authenticated user and never the target entity's id.
 */
public interface RoleRepository {

    Optional<Role> findById(long tenantId, long id);

    boolean existsByRoleCode(long tenantId, String roleCode);

    List<Role> findPage(long tenantId, String keyword, int pageNo, int pageSize);

    long count(long tenantId, String keyword);

    /**
     * Insert a new role. {@code operatorUserId} is recorded as the
     * actor for the create audit fields. Returns the generated primary
     * key.
     */
    long insert(long operatorUserId, Role role);

    /**
     * Update mutable fields of an existing role. {@code operatorUserId}
     * is recorded as the actor for {@code updated_by}. Returns
     * {@code false} when the role does not exist or the version is stale.
     */
    boolean update(long operatorUserId, Role role);

    /**
     * Update only the lifecycle status of an existing role. Returns
     * {@code false} when the role does not exist or the version is
     * stale. {@code operatorUserId} is recorded as the actor for
     * {@code updated_by}.
     */
    boolean updateStatus(long operatorUserId, long tenantId, long id, RoleStatus status, int expectedVersion);

    /**
     * Replace the role's permission set atomically inside a single
     * transaction. Existing rows not present in {@code permissionIds} are
     * removed; new rows are inserted. Returns {@code true} if the role
     * existed and the swap completed.
     */
    boolean replacePermissions(long operatorUserId, long tenantId, long roleId, List<Long> permissionIds);
}
