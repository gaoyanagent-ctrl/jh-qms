package com.company.iaf.platform.auth.infrastructure.persistence;

import com.company.iaf.platform.auth.domain.model.PlatformUser;
import com.company.iaf.platform.auth.domain.model.UserDataScope;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class JdbcPlatformAuthRepositoryIsolationTest {

    @Test
    void loginUserLookupExecutesWithTenantPredicateAndParameter() {
        CapturingJdbcTemplate jdbcTemplate = new CapturingJdbcTemplate(Optional.empty());
        JdbcAuthUserRepository repository = new JdbcAuthUserRepository(jdbcTemplate);

        repository.findByTenantIdAndUsername(2L, "admin");

        assertThat(jdbcTemplate.sql()).contains("where u.tenant_id = ?");
        assertThat(jdbcTemplate.sql()).contains("and u.username = ?");
        assertThat(jdbcTemplate.args()).containsExactly(2L, "admin");
    }

    @Test
    void userListLookupExecutesWithOrgDataScopePredicateAndParameter() {
        CapturingJdbcTemplate jdbcTemplate = new CapturingJdbcTemplate(List.of());
        JdbcPlatformUserRepository repository = new JdbcPlatformUserRepository(jdbcTemplate);

        repository.findPage(1L, null, UserDataScope.org(10L), 1, 20);

        assertThat(jdbcTemplate.sql()).contains("from sys_user_org uo");
        assertThat(jdbcTemplate.sql()).contains("uo.tenant_id = sys_user.tenant_id");
        assertThat(jdbcTemplate.sql()).contains("uo.user_id = sys_user.id");
        assertThat(jdbcTemplate.sql()).contains("uo.org_id in (?)");
        assertThat(jdbcTemplate.args()).containsExactly(1L, null, null, null, null, null, 10L, 20, 0);
    }

    @Test
    void userCountExecutesWithOrgDataScopePredicateAndParameter() {
        CapturingJdbcTemplate jdbcTemplate = new CapturingJdbcTemplate(0L);
        JdbcPlatformUserRepository repository = new JdbcPlatformUserRepository(jdbcTemplate);

        repository.count(1L, "alice", UserDataScope.org(10L));

        assertThat(jdbcTemplate.sql()).contains("from sys_user_org uo");
        assertThat(jdbcTemplate.sql()).contains("uo.org_id in (?)");
        assertThat(jdbcTemplate.args()).containsExactly(1L, "%alice%", "%alice%", "%alice%", "%alice%", "%alice%", 10L);
    }

    private static final class CapturingJdbcTemplate extends JdbcTemplate {

        private final Object result;
        private String sql;
        private List<Object> args = List.of();

        private CapturingJdbcTemplate(Object result) {
            this.result = result;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T query(String sql, ResultSetExtractor<T> rse, Object... args) {
            capture(sql, args);
            return (T) result;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
            capture(sql, args);
            return (List<T>) result;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T queryForObject(String sql, Class<T> requiredType, Object... args) {
            capture(sql, args);
            return (T) result;
        }

        private void capture(String sql, Object[] args) {
            this.sql = sql;
            this.args = Arrays.asList(args);
        }

        private String sql() {
            return sql;
        }

        private List<Object> args() {
            return args;
        }
    }
}
