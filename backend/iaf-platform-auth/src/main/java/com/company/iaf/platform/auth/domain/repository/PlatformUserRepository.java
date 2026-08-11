package com.company.iaf.platform.auth.domain.repository;

import com.company.iaf.platform.auth.domain.model.PlatformUser;
import com.company.iaf.platform.auth.domain.model.UserDataScope;
import com.company.iaf.platform.auth.domain.model.UserStatus;

import java.util.List;
import java.util.Optional;

/**
 * Persistence boundary for platform user management. Implementations live
 * in the {@code infrastructure} layer; this contract intentionally hides
 * the storage mechanism from the application service.
 *
 * <p>Password handling is intentionally scoped to the authentication
 * module: the repository accepts an already-encoded password hash, and
 * the application service is responsible for running it through
 * {@link org.springframework.security.crypto.password.PasswordEncoder}
 * before calling this contract.
 *
 * <p>Audit fields ({@code created_by}, {@code updated_by}) are supplied
 * as a separate {@code operatorUserId} parameter rather than carried
 * inside the domain model, so the actor on the write is always the
 * currently authenticated user and never the target entity's id.
 */
public interface PlatformUserRepository {

    /**
     * Look up a platform user by primary key within a tenant.
     */
    Optional<PlatformUser> findById(long tenantId, long id);

    /**
     * Look up a platform user by username within a tenant.
     */
    Optional<PlatformUser> findByUsername(long tenantId, String username);

    /**
     * Check whether a username is already taken in the tenant. Used to
     * enforce uniqueness before insert.
     */
    boolean existsByUsername(long tenantId, String username);

    /**
     * Page through platform users in the tenant, optionally filtered by
     * a keyword that matches username, display name, mobile, or email.
     */
    List<PlatformUser> findPage(long tenantId, String keyword, UserDataScope dataScope, int pageNo, int pageSize);

    /**
     * Count platform users in the tenant matching the same filter as
     * {@link #findPage}.
     */
    long count(long tenantId, String keyword, UserDataScope dataScope);

    /**
     * Insert a new platform user together with its initial password hash.
     * {@code operatorUserId} is recorded as the actor for the create
     * audit fields. Returns the generated primary key.
     */
    long insert(long operatorUserId, PlatformUser user, String passwordHash);

    /**
     * Update mutable profile fields (display name, mobile, email,
     * primary org, status) of an existing user. The version field is
     * used for optimistic locking and must be returned by the caller
     * as it was read. {@code operatorUserId} is recorded as the actor
     * for {@code updated_by}.
     */
    boolean update(long operatorUserId, PlatformUser user);

    /**
     * Update only the lifecycle status of an existing user. Returns
     * {@code false} when the user does not exist or the version is
     * stale. {@code operatorUserId} is recorded as the actor for
     * {@code updated_by}.
     */
    boolean updateStatus(long operatorUserId, long tenantId, long id, UserStatus status, int expectedVersion);

    /**
     * Replace the password hash of an existing user. Returns
     * {@code false} when the user does not exist.
     * {@code operatorUserId} is recorded as the actor for
     * {@code updated_by}.
     */
    boolean updatePassword(long operatorUserId, long tenantId, long id, String passwordHash);
}
