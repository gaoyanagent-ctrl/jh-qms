package com.company.iaf.qms.engineering.infrastructure.persistence;

import com.company.iaf.qms.engineering.domain.model.Drawing;
import com.company.iaf.qms.engineering.domain.model.DrawingRevision;
import com.company.iaf.qms.engineering.domain.model.DrawingSourceSystem;
import com.company.iaf.qms.engineering.domain.model.DrawingStatus;
import com.company.iaf.qms.engineering.domain.model.DrawingType;
import com.company.iaf.qms.engineering.domain.model.Part;
import com.company.iaf.qms.engineering.domain.model.PartStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class QmsEngineeringPostgresIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:18")
            .withDatabaseName("iaf_qms_test")
            .withUsername("iaf")
            .withPassword("iaf");

    private static DriverManagerDataSource dataSource;

    @BeforeAll
    static void migrate() {
        dataSource = new DriverManagerDataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
        String platformMigrations = Path.of("..", "iaf-app", "src", "main", "resources", "db", "migration")
                .toAbsolutePath().normalize().toString();
        Flyway.configure()
                .dataSource(dataSource)
                .locations("filesystem:" + platformMigrations, "classpath:db/migration")
                .load()
                .migrate();
    }

    @Test
    void migrationRepositoriesSequenceAndAuditWorkTogether() {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        JdbcPartRepository parts = new JdbcPartRepository(jdbc);
        JdbcDrawingRepository drawings = new JdbcDrawingRepository(jdbc);
        JdbcDrawingRevisionRepository revisions = new JdbcDrawingRevisionRepository(jdbc);
        ObjectMapper mapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();
        JdbcQmsAuditTrail audit = new JdbcQmsAuditTrail(jdbc, mapper);

        Part draftPart = new Part(null, 1L, 10L, "P-100", null, "Bracket", null,
                null, null, null, PartStatus.ACTIVE, 0, null, null);
        long partId = parts.insert(1L, draftPart);
        Drawing draftDrawing = new Drawing(null, 1L, 10L, partId, "D-100", "Drawing",
                DrawingType.PART, DrawingSourceSystem.MANUAL, DrawingStatus.ACTIVE, 0, null, null);
        long drawingId = drawings.insert(1L, draftDrawing);

        TransactionTemplate transaction = new TransactionTemplate(new DataSourceTransactionManager(dataSource));
        long revisionId = transaction.execute(status -> {
            int seq = revisions.reserveNextSequence(1L, drawingId);
            return revisions.insert(1L, DrawingRevision.metadataDraft(1L, 10L, drawingId, "Z1", seq, null, null));
        });
        DrawingRevision revision = revisions.findById(1L, 10L, revisionId).orElseThrow();
        audit.record(1L, 1L, "DRAWING_REVISION_CREATED", "DrawingRevision", revisionId, revision);

        assertThat(parts.findById(1L, 10L, partId)).isPresent();
        assertThat(parts.findById(1L, 99L, partId)).isEmpty();
        assertThat(revision.revisionSeq()).isEqualTo(1);
        assertThat(jdbc.queryForObject("select count(*) from qms_audit_log", Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject(
                "select count(*) from sys_permission where permission_code like 'qms:%'", Integer.class)).isEqualTo(6);
        assertThat(jdbc.queryForObject(
                "select count(*) from sys_menu where menu_code in ('qms', 'qms.engineering.parts')", Integer.class))
                .isEqualTo(2);
        assertThat(jdbc.queryForObject("""
                select count(*)
                  from sys_role_menu rm
                  join sys_role r on r.id = rm.role_id and r.tenant_id = rm.tenant_id
                  join sys_menu m on m.id = rm.menu_id and m.tenant_id = rm.tenant_id
                 where r.role_code = 'platform_admin'
                   and m.menu_code = 'qms.engineering.parts'
                   and rm.deleted = false
                """, Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject(
                "select primary_org_id from sys_user where tenant_id = 1 and username = 'admin'", Long.class))
                .isNotNull();
        assertThat(jdbc.queryForObject("""
                select count(*)
                  from sys_user_org uo
                  join sys_user u on u.id = uo.user_id and u.tenant_id = uo.tenant_id
                  join sys_org o on o.id = uo.org_id and o.tenant_id = uo.tenant_id
                 where u.tenant_id = 1
                   and u.username = 'admin'
                   and o.org_code = 'ROOT'
                   and uo.is_primary = true
                   and uo.deleted = false
                """, Integer.class)).isEqualTo(1);
    }
}
