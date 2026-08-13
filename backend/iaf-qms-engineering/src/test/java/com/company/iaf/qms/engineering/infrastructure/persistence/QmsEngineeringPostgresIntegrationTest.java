package com.company.iaf.qms.engineering.infrastructure.persistence;

import com.company.iaf.qms.engineering.domain.model.Drawing;
import com.company.iaf.qms.engineering.domain.model.DrawingRevision;
import com.company.iaf.qms.engineering.domain.model.DrawingSourceSystem;
import com.company.iaf.qms.engineering.domain.model.DrawingStatus;
import com.company.iaf.qms.engineering.domain.model.DrawingType;
import com.company.iaf.qms.engineering.domain.model.Part;
import com.company.iaf.qms.engineering.domain.model.PartStatus;
import com.company.iaf.qms.engineering.domain.model.*;
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
import java.math.BigDecimal;
import java.util.List;

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
    void migrationRepositoriesSequenceAndAuditWorkTogether() throws Exception {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        JdbcPartRepository parts = new JdbcPartRepository(jdbc);
        JdbcDrawingRepository drawings = new JdbcDrawingRepository(jdbc);
        JdbcDrawingRevisionRepository revisions = new JdbcDrawingRevisionRepository(jdbc);
        JdbcDrawingParseJobRepository parseJobs = new JdbcDrawingParseJobRepository(jdbc);
        ObjectMapper mapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();
        JdbcQmsAuditTrail audit = new JdbcQmsAuditTrail(jdbc, mapper);
        JdbcDrawingParseResultRepository parseResults = new JdbcDrawingParseResultRepository(jdbc, mapper);

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

        JdbcQmsFileObjectRepository files = new JdbcQmsFileObjectRepository(jdbc);
        long fileId = files.insert(1L, new QmsFileObject(null, 1, 10, "drawing.pdf",
                "application/pdf", "pdf", 100, "checksum", "bucket", "object", 0, null));
        assertThat(revisions.attachFile(1, 1, 10, revisionId, fileId, "PDF", "checksum", "UPLOADED", 0)).isTrue();
        long parseJobId = parseJobs.enqueue(1, 1, 10, revisionId, fileId, "PDF", 1);
        assertThat(parseJobs.transition(1, 1, 10, parseJobId, "QUEUED", "RUNNING", null, null, 0)).isTrue();
        assertThat(revisions.transitionState(1, 1, 10, revisionId, "UPLOADED", "PARSING", "RUNNING", 1)).isTrue();
        DrawingEntity entity = new DrawingEntity(null, 0, 0, 0, 0, "DIM-1", null,
                DrawingEntityType.DIMENSION, "DIM", "1", BigDecimal.ONE, BigDecimal.ONE,
                BigDecimal.TEN, BigDecimal.ONE, null, "8±0.5", "8±0.5", null, 0, null, null);
        SourceEvidence evidence = new SourceEvidence(null, 0, 0, 0, 0, 0, "EV-1", "DIM-1", null,
                "1", 1, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.TEN, BigDecimal.ONE,
                "technical requirement ".repeat(20) + "8±0.5", "technical requirement ".repeat(20) + "8±0.5", EvidenceExtractorType.PDF_VECTOR, "1.0", null, null,
                new BigDecimal("0.98"), 0, null, null);
        DrawingEntity cadEntity = new DrawingEntity(null, 0, 0, 0, 0, "DWG-DIM-20", "20",
                DrawingEntityType.DIMENSION, "细实线", "MODEL", BigDecimal.ZERO, BigDecimal.ZERO,
                new BigDecimal("34"), BigDecimal.ONE, mapper.readTree("{\"act_measurement\":34}"),
                "34", "34", null, 0, null, null);
        SourceEvidence cadEvidence = new SourceEvidence(null, 0, 0, 0, 0, 0, "EV-DWG-20", "DWG-DIM-20", "20",
                "MODEL", null, BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("34"), BigDecimal.ONE,
                "34", "34", EvidenceExtractorType.DWG_ENTITY, "libredwg-0.14", null, null,
                BigDecimal.ONE, 0, null, null);
        DrawingParseResult parseResult = new DrawingParseResult("1.0.0", "DRAW-1", "Z1",
                mapper.readTree("""
                    {"schemaVersion":"1.0.0","documentId":"DRAW-1","revision":"Z1","sheets":[
                    {"sheetNo":"1","width":100,"height":80,"titleBlock":{},"views":[],
                    "entities":[{"entityId":"DIM-1","entityType":"DIMENSION","sheetNo":"1",
                    "evidence":[{"evidenceKey":"EV-1"}]}],"notes":[],"characteristicCandidates":[]}]}
                    """), List.of(entity, cadEntity), List.of(evidence, cadEvidence));
        parseResults.save(1, 1, 10, revisionId, parseJobId, fileId, parseResult);

        assertThat(parts.findById(1L, 10L, partId)).isPresent();
        assertThat(parts.findById(1L, 99L, partId)).isEmpty();
        assertThat(revision.revisionSeq()).isEqualTo(1);
        assertThat(jdbc.queryForObject("select count(*) from qms_audit_log", Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject(
                "select count(*) from sys_permission where permission_code like 'qms:%'", Integer.class)).isEqualTo(10);
        assertThat(jdbc.queryForObject("select count(*) from qms_drawing_parse_job", Integer.class)).isEqualTo(1);
        assertThat(parseResults.findModel(1, 10, revisionId)).isPresent();
        assertThat(parseResults.findEntities(1, 10, revisionId)).extracting(DrawingEntity::entityId)
                .containsExactly("DIM-1", "DWG-DIM-20");
        assertThat(parseResults.findEvidence(1, 10, revisionId)).extracting(SourceEvidence::sheetNo)
                .containsExactly("1", "MODEL");
        assertThat(parseResults.findEvidence(1, 99, revisionId)).isEmpty();
        JdbcQualityCharacteristicRepository characteristics = new JdbcQualityCharacteristicRepository(jdbc);
        characteristics.generateDimensionCandidates(1, 1, 10, revisionId);
        assertThat(characteristics.findByRevision(1, 10, revisionId)).hasSize(2);
        assertThat(characteristics.findByRevision(1, 10, revisionId).get(0)).satisfies(candidate -> {
                    assertThat(candidate.nominalValue()).isEqualByComparingTo("8");
                    assertThat(candidate.upperTolerance()).isEqualByComparingTo("0.5");
                    assertThat(candidate.name()).isEqualTo("8±0.5");
                    assertThat(candidate.reviewStatus()).isEqualTo("PENDING");
                    assertThat(candidate.evidenceId()).isPositive();
                });
        assertThat(characteristics.findByRevision(1, 10, revisionId).get(1)).satisfies(candidate -> {
            assertThat(candidate.nominalValue()).isEqualByComparingTo("34");
            assertThat(candidate.upperTolerance()).isNull();
            assertThat(candidate.lowerTolerance()).isNull();
            assertThat(candidate.name()).isEqualTo("34");
        });
        var manual = characteristics.createManual(1, 1, 10, revisionId, "DIMENSION", "参考尺寸 12",
                new java.math.BigDecimal("12"), null, null, "mm", null,
                false, true, false, false, false, false, false, "manual review");
        assertThat(manual.evidenceId()).isNull();
        assertThat(manual.characteristicCode()).startsWith("MAN-");
        assertThat(manual.referenceDimension()).isTrue();
        var parsed = characteristics.findByRevision(1, 10, revisionId).stream()
                .filter(item -> item.characteristicCode().startsWith("DIM-EV-"))
                .findFirst().orElseThrow();
        jdbc.update("update qms_quality_characteristic set name='[B]6.5±0.3◆▲' where id=?", parsed.id());
        new JdbcDrawingLegendRuleRepository(jdbc).reclassifyPending(1, 1, 10);
        parsed = characteristics.findById(1, 10, revisionId, parsed.id()).orElseThrow();
        assertThat(parsed.inspectionDimension()).isTrue();
        assertThat(parsed.locationDimension()).isTrue();
        assertThat(parsed.specialCharacteristicCode()).isEqualTo("B");
        assertThat(characteristics.review(1, 1, 10, revisionId, parsed.id(), parsed.version(),
                "CONFIRMED", parsed.name(), parsed.nominalValue(), parsed.upperTolerance(),
                parsed.lowerTolerance(), parsed.unit(), parsed.characteristicType(), "B",
                false, false, true, false, false, false, false, "classified")).isTrue();
        assertThat(characteristics.findById(1, 10, revisionId, parsed.id())).get().satisfies(reviewed -> {
            assertThat(reviewed.reviewStatus()).isEqualTo("CONFIRMED");
            assertThat(reviewed.idealDimension()).isTrue();
            assertThat(reviewed.inspectionDimension()).isFalse();
            assertThat(reviewed.specialCharacteristicCode()).isEqualTo("B");
        });
        assertThat(jdbc.queryForObject("""
                select count(*)
                  from sys_role_permission rp
                  join sys_role r on r.id = rp.role_id and r.tenant_id = rp.tenant_id
                  join sys_permission p on p.id = rp.permission_id and p.tenant_id = rp.tenant_id
                 where r.role_code = 'platform_admin'
                   and p.permission_code = 'qms:drawing-revision:upload'
                   and rp.deleted = false
                """, Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject(
                "select count(*) from sys_menu where menu_code in ('qms', 'qms.engineering.parts', 'qms.engineering.legend')", Integer.class))
                .isEqualTo(3);
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
