package com.company.iaf.platform.auth.infrastructure.persistence;

import com.company.iaf.platform.auth.domain.model.UserDataScope;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class JdbcPlatformUserRepositoryTest {
    @Test
    void countSeparatesSqlFragmentsWithWhitespace() {
        var jdbc=mock(JdbcTemplate.class);
        when(jdbc.queryForObject(anyString(), eq(Long.class), any(Object[].class))).thenReturn(2L);

        assertThat(new JdbcPlatformUserRepository(jdbc).count(1L, null, UserDataScope.org(1L))).isEqualTo(2L);

        var sql=ArgumentCaptor.forClass(String.class);
        verify(jdbc).queryForObject(sql.capture(), eq(Long.class), any(Object[].class));
        assertThat(sql.getValue().replaceAll("\\s+", " ")).contains("and deleted = false and exists").doesNotContain("anddeleted");
    }
}
