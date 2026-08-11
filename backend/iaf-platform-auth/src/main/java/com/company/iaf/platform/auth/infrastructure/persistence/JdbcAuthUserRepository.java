package com.company.iaf.platform.auth.infrastructure.persistence;

import com.company.iaf.platform.auth.domain.model.LoginUser;
import com.company.iaf.platform.auth.domain.repository.AuthUserRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

@Repository
public class JdbcAuthUserRepository implements AuthUserRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcAuthUserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<LoginUser> findByTenantIdAndUsername(long tenantId, String username) {
        return jdbcTemplate.query("""
                        select u.id,
                               u.tenant_id,
                               u.username,
                               u.display_name,
                               u.password_hash,
                               u.status,
                               p.permission_code
                          from sys_user u
                          left join sys_user_role ur
                            on ur.user_id = u.id
                           and ur.tenant_id = u.tenant_id
                           and ur.deleted = false
                          left join sys_role_permission rp
                            on rp.role_id = ur.role_id
                           and rp.tenant_id = u.tenant_id
                           and rp.deleted = false
                          left join sys_permission p
                            on p.id = rp.permission_id
                           and p.tenant_id = u.tenant_id
                           and p.deleted = false
                         where u.tenant_id = ?
                           and u.username = ?
                           and u.deleted = false
                         order by p.permission_code
                        """,
                resultSet -> {
                    LoginUser user = null;
                    Set<String> permissions = new LinkedHashSet<>();
                    while (resultSet.next()) {
                        if (user == null) {
                            user = new LoginUser(
                                    resultSet.getLong("id"),
                                    resultSet.getLong("tenant_id"),
                                    resultSet.getString("username"),
                                    resultSet.getString("display_name"),
                                    resultSet.getString("password_hash"),
                                    resultSet.getString("status"),
                                    permissions
                            );
                        }
                        String permission = resultSet.getString("permission_code");
                        if (permission != null) {
                            permissions.add(permission);
                        }
                    }
                    return Optional.ofNullable(user);
                },
                tenantId,
                username
        );
    }
}
