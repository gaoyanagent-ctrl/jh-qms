package com.company.iaf.platform.org.domain.repository;

import com.company.iaf.platform.org.domain.model.Org;

import java.util.List;
import java.util.Optional;

/**
 * Persistence boundary for platform organizations. Implementations
 * live in the infrastructure layer; this contract intentionally hides
 * the storage mechanism from the application service.
 *
 * <p>Audit fields ({@code created_by}, {@code updated_by}) are supplied
 * as a separate {@code operatorUserId} parameter rather than carried
 * inside the domain model, so the actor on the write is always the
 * currently authenticated user and never the target entity's id.
 */
public interface OrgRepository {

    /**
     * Find an organization by primary key within a tenant.
     */
    Optional<Org> findById(long tenantId, long id);

    /**
     * Check whether {@code orgCode} is already used inside the tenant.
     */
    boolean existsByOrgCode(long tenantId, String orgCode);

    /**
     * Return every organization in the tenant, unordered. Tree assembly
     * is the caller's responsibility.
     */
    List<Org> findAll(long tenantId);

    /**
     * Insert a new organization. {@code operatorUserId} is recorded as
     * the actor for the create audit fields. Returns the generated
     * primary key.
     */
    long insert(long operatorUserId, Org org);

    /**
     * Update mutable fields of an existing organization. Returns
     * {@code false} when the organization does not exist or the
     * version is stale. {@code operatorUserId} is recorded as the
     * actor for {@code updated_by}.
     */
    boolean update(long operatorUserId, Org org);
}
