package com.company.iaf.platform.org.infrastructure.persistence;

import com.company.iaf.platform.org.domain.model.Org;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class JdbcOrgRepositoryTest {
    @SuppressWarnings("unchecked")
    @Test
    void findAllSeparatesSqlFragmentsWithWhitespace() {
        var jdbc=mock(JdbcTemplate.class);
        when(jdbc.query(anyString(), org.mockito.ArgumentMatchers.<RowMapper<Org>>any(), eq(1L))).thenReturn(List.of());
        new JdbcOrgRepository(jdbc).findAll(1L);
        var sql=ArgumentCaptor.forClass(String.class);
        verify(jdbc).query(sql.capture(), org.mockito.ArgumentMatchers.<RowMapper<Org>>any(), eq(1L));
        assertThat(sql.getValue().replaceAll("\\s+", " ")).contains("and deleted = false order by").doesNotContain("anddeleted");
    }
}
