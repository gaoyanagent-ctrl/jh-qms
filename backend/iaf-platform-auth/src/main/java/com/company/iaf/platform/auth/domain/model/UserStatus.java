package com.company.iaf.platform.auth.domain.model;

/**
 * Lifecycle status of a platform user.
 *
 * <p>Only {@link #ENABLED} users can authenticate. {@link #DISABLED} users
 * must not be able to log in even if their password still matches.
 */
public enum UserStatus {

    ENABLED,
    DISABLED
}
