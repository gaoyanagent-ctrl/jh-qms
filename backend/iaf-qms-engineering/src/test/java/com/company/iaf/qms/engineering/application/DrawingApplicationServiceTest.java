package com.company.iaf.qms.engineering.application;

import com.company.iaf.qms.engineering.domain.model.Drawing;
import com.company.iaf.qms.engineering.domain.model.DrawingRevision;
import com.company.iaf.qms.engineering.domain.model.DrawingSourceSystem;
import com.company.iaf.qms.engineering.domain.model.DrawingStatus;
import com.company.iaf.qms.engineering.domain.model.DrawingType;
import com.company.iaf.qms.engineering.domain.model.Part;
import com.company.iaf.qms.engineering.domain.model.PartStatus;
import com.company.iaf.qms.engineering.domain.repository.DrawingRepository;
import com.company.iaf.qms.engineering.domain.repository.DrawingRevisionRepository;
import com.company.iaf.qms.engineering.domain.repository.PartRepository;
import com.company.iaf.qms.engineering.interfaces.dto.DrawingCreateRequest;
import com.company.iaf.qms.engineering.interfaces.dto.DrawingRevisionCreateRequest;
import com.company.iaf.shared.exception.BusinessException;
import com.company.iaf.shared.security.SecurityContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DrawingApplicationServiceTest {

    private final FakeStore store = new FakeStore();
    private final PartApplicationServiceTest.RecordingAuditTrail auditTrail =
            new PartApplicationServiceTest.RecordingAuditTrail();
    private DrawingApplicationService service;

    @BeforeEach
    void setUp() {
        SecurityContext.setUserId(99L);
        store.addPart(1L, 10L, 1L);
        service = new DrawingApplicationService(
                new FakePartRepository(store),
                new FakeDrawingRepository(store),
                new FakeRevisionRepository(store),
                auditTrail
        );
    }

    @AfterEach
    void clearContext() {
        SecurityContext.clear();
    }

    @Test
    void createsDrawingAndOrderedRevisionDraftsWithAudit() {
        var drawing = service.createDrawing(1L, 10L, 1L,
                new DrawingCreateRequest("D-100", "Bracket drawing", DrawingType.PART, null));
        var first = service.createRevision(1L, 10L, drawing.id(),
                new DrawingRevisionCreateRequest("Z1", null, null));
        var second = service.createRevision(1L, 10L, drawing.id(),
                new DrawingRevisionCreateRequest("Z2", null, first.id()));

        assertThat(drawing.sourceSystem()).isEqualTo(DrawingSourceSystem.MANUAL);
        assertThat(first.revisionSeq()).isEqualTo(1);
        assertThat(second.revisionSeq()).isEqualTo(2);
        assertThat(second.supersedesRevisionId()).isEqualTo(first.id());
        assertThat(auditTrail.actions).containsExactly(
                "DRAWING_CREATED:Drawing:" + drawing.id(),
                "DRAWING_REVISION_CREATED:DrawingRevision:" + first.id(),
                "DRAWING_REVISION_CREATED:DrawingRevision:" + second.id()
        );
    }

    @Test
    void rejectsMissingParentAndDuplicateBusinessKeys() {
        DrawingCreateRequest request = new DrawingCreateRequest("D-100", "Drawing", DrawingType.PART, null);
        assertThatThrownBy(() -> service.createDrawing(1L, 10L, 999L, request))
                .isInstanceOf(BusinessException.class)
                .extracting(error -> ((BusinessException) error).errorCode().code())
                .isEqualTo("QMS_PART_NOT_FOUND");

        var drawing = service.createDrawing(1L, 10L, 1L, request);
        assertThatThrownBy(() -> service.createDrawing(1L, 10L, 1L, request))
                .isInstanceOf(BusinessException.class)
                .extracting(error -> ((BusinessException) error).errorCode().code())
                .isEqualTo("QMS_DRAWING_NO_ALREADY_EXISTS");

        DrawingRevisionCreateRequest revision = new DrawingRevisionCreateRequest("Z1", null, null);
        service.createRevision(1L, 10L, drawing.id(), revision);
        assertThatThrownBy(() -> service.createRevision(1L, 10L, drawing.id(), revision))
                .isInstanceOf(BusinessException.class)
                .extracting(error -> ((BusinessException) error).errorCode().code())
                .isEqualTo("QMS_DRAWING_REVISION_CODE_ALREADY_EXISTS");
    }

    private static final class FakeStore {
        private final AtomicLong drawingIds = new AtomicLong();
        private final AtomicLong revisionIds = new AtomicLong();
        private final Map<Long, Part> parts = new LinkedHashMap<>();
        private final Map<Long, Drawing> drawings = new LinkedHashMap<>();
        private final Map<Long, DrawingRevision> revisions = new LinkedHashMap<>();

        void addPart(long tenantId, long orgId, long id) {
            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
            parts.put(id, new Part(id, tenantId, orgId, "P-" + id, null, "Part", null,
                    null, null, null, PartStatus.ACTIVE, 0, now, now));
        }
    }

    private record FakePartRepository(FakeStore store) implements PartRepository {

        @Override
        public Optional<Part> findById(long tenantId, long orgId, long id) {
            return Optional.ofNullable(store.parts.get(id))
                    .filter(value -> value.tenantId() == tenantId && value.orgId() == orgId);
        }

        @Override public boolean existsByPartNo(long tenantId, long orgId, String partNo) { return false; }
        @Override public List<Part> findPage(long tenantId, long orgId, String keyword, long offset, int pageSize) { return List.of(); }
        @Override public long count(long tenantId, long orgId, String keyword) { return 0; }
        @Override public long insert(long operatorUserId, Part part) { throw new UnsupportedOperationException(); }
    }

    private record FakeDrawingRepository(FakeStore store) implements DrawingRepository {

        @Override
        public Optional<Drawing> findById(long tenantId, long orgId, long id) {
            return Optional.ofNullable(store.drawings.get(id))
                    .filter(value -> value.tenantId() == tenantId && value.orgId() == orgId);
        }

        @Override
        public boolean existsByDrawingNo(long tenantId, long partId, String drawingNo) {
            return store.drawings.values().stream().anyMatch(value -> value.tenantId() == tenantId
                    && value.partId() == partId && value.drawingNo().equals(drawingNo));
        }

        @Override
        public List<Drawing> findByPartId(long tenantId, long orgId, long partId) {
            return store.drawings.values().stream().filter(value -> value.tenantId() == tenantId
                    && value.orgId() == orgId && value.partId() == partId).toList();
        }

        @Override
        public long insert(long operatorUserId, Drawing drawing) {
            long id = store.drawingIds.incrementAndGet();
            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
            store.drawings.put(id, new Drawing(id, drawing.tenantId(), drawing.orgId(), drawing.partId(),
                    drawing.drawingNo(), drawing.drawingName(), drawing.drawingType(), drawing.sourceSystem(),
                    DrawingStatus.ACTIVE, 0, now, now));
            return id;
        }
    }

    private record FakeRevisionRepository(FakeStore store) implements DrawingRevisionRepository {

        @Override
        public Optional<DrawingRevision> findById(long tenantId, long orgId, long id) {
            return Optional.ofNullable(store.revisions.get(id))
                    .filter(value -> value.tenantId() == tenantId && value.orgId() == orgId);
        }

        @Override
        public boolean existsByRevisionCode(long tenantId, long drawingId, String revisionCode) {
            return store.revisions.values().stream().anyMatch(value -> value.tenantId() == tenantId
                    && value.drawingId() == drawingId && value.revisionCode().equals(revisionCode));
        }

        @Override
        public int reserveNextSequence(long tenantId, long drawingId) {
            return store.revisions.values().stream().filter(value -> value.tenantId() == tenantId && value.drawingId() == drawingId)
                    .mapToInt(DrawingRevision::revisionSeq).max().orElse(0) + 1;
        }

        @Override
        public List<DrawingRevision> findByDrawingId(long tenantId, long orgId, long drawingId) {
            List<DrawingRevision> result = new ArrayList<>(store.revisions.values().stream()
                    .filter(value -> value.tenantId() == tenantId && value.orgId() == orgId && value.drawingId() == drawingId)
                    .toList());
            result.sort((left, right) -> Integer.compare(right.revisionSeq(), left.revisionSeq()));
            return result;
        }

        @Override
        public long insert(long operatorUserId, DrawingRevision revision) {
            long id = store.revisionIds.incrementAndGet();
            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
            store.revisions.put(id, new DrawingRevision(id, revision.tenantId(), revision.orgId(), revision.drawingId(),
                    revision.revisionCode(), revision.revisionSeq(), null, null, revision.effectiveDate(), null,
                    revision.supersedesRevisionId(), revision.parseStatus(), revision.reviewStatus(), revision.status(),
                    null, null, null, 0, now, now));
            return id;
        }

        @Override
        public boolean attachFile(long operatorUserId, long tenantId, long orgId, long revisionId,
                                  long fileId, String fileType, String checksum, String targetStatus, int expectedVersion) {
            return false;
        }

        @Override
        public boolean transitionState(long operatorUserId, long tenantId, long orgId, long revisionId,
                                       String fromStatus, String targetStatus, String parseStatus, int expectedVersion) {
            return false;
        }
    }
}
