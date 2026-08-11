package com.company.iaf.qms.engineering.application;

import com.company.iaf.qms.engineering.domain.model.Part;
import com.company.iaf.qms.engineering.domain.repository.PartRepository;
import com.company.iaf.qms.engineering.domain.repository.QmsAuditTrail;
import com.company.iaf.qms.engineering.interfaces.dto.PartCreateRequest;
import com.company.iaf.shared.exception.BusinessException;
import com.company.iaf.shared.security.SecurityContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PartApplicationServiceTest {

    private final InMemoryPartRepository repository = new InMemoryPartRepository();
    private final RecordingAuditTrail auditTrail = new RecordingAuditTrail();
    private PartApplicationService service;

    @BeforeEach
    void setUp() {
        SecurityContext.setUserId(99L);
        service = new PartApplicationService(repository, auditTrail);
    }

    @AfterEach
    void clearContext() {
        SecurityContext.clear();
    }

    @Test
    void createsNormalizesAndAuditsPart() {
        var created = service.create(1L, 10L,
                new PartCreateRequest(" P-100 ", " M-10 ", " Bracket ", null, " D511 ", null, "A"));

        assertThat(created.partNo()).isEqualTo("P-100");
        assertThat(created.orgId()).isEqualTo(10L);
        assertThat(auditTrail.actions).containsExactly("PART_CREATED:Part:" + created.id());
    }

    @Test
    void duplicatePartNumberIsRejectedOnlyWithinSameOrg() {
        PartCreateRequest request = new PartCreateRequest("P-100", null, "Bracket", null, null, null, null);
        service.create(1L, 10L, request);

        assertThatThrownBy(() -> service.create(1L, 10L, request))
                .isInstanceOf(BusinessException.class)
                .extracting(error -> ((BusinessException) error).errorCode().code())
                .isEqualTo("QMS_PART_NO_ALREADY_EXISTS");

        assertThat(service.create(1L, 11L, request).orgId()).isEqualTo(11L);
    }

    @Test
    void listIsTenantAndOrganizationScopedAndSearchable() {
        service.create(1L, 10L, new PartCreateRequest("P-100", "MAT-A", "Bracket", null, null, null, null));
        service.create(1L, 11L, new PartCreateRequest("P-200", "MAT-B", "Hidden", null, null, null, null));
        service.create(2L, 10L, new PartCreateRequest("P-300", "MAT-C", "Other tenant", null, null, null, null));

        var page = service.list(1L, 10L, "brack", 1, 20);

        assertThat(page.total()).isEqualTo(1);
        assertThat(page.records()).extracting("partNo").containsExactly("P-100");
    }

    private static final class InMemoryPartRepository implements PartRepository {
        private final AtomicLong ids = new AtomicLong();
        private final Map<Long, Part> parts = new LinkedHashMap<>();

        @Override
        public Optional<Part> findById(long tenantId, long orgId, long id) {
            return Optional.ofNullable(parts.get(id))
                    .filter(part -> part.tenantId() == tenantId && part.orgId() == orgId);
        }

        @Override
        public boolean existsByPartNo(long tenantId, long orgId, String partNo) {
            return parts.values().stream().anyMatch(part -> part.tenantId() == tenantId
                    && part.orgId() == orgId && part.partNo().equals(partNo));
        }

        @Override
        public List<Part> findPage(long tenantId, long orgId, String keyword, long offset, int pageSize) {
            return filtered(tenantId, orgId, keyword).stream().skip(offset).limit(pageSize).toList();
        }

        @Override
        public long count(long tenantId, long orgId, String keyword) {
            return filtered(tenantId, orgId, keyword).size();
        }

        @Override
        public long insert(long operatorUserId, Part part) {
            long id = ids.incrementAndGet();
            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
            parts.put(id, new Part(id, part.tenantId(), part.orgId(), part.partNo(), part.materialNo(),
                    part.partName(), part.customerId(), part.vehicleModel(), part.supplierId(),
                    part.importanceLevel(), part.status(), 0, now, now));
            return id;
        }

        private List<Part> filtered(long tenantId, long orgId, String keyword) {
            String search = keyword == null ? null : keyword.toLowerCase();
            List<Part> result = new ArrayList<>(parts.values().stream()
                    .filter(part -> part.tenantId() == tenantId && part.orgId() == orgId)
                    .filter(part -> search == null
                            || part.partNo().toLowerCase().contains(search)
                            || part.partName().toLowerCase().contains(search)
                            || part.materialNo() != null && part.materialNo().toLowerCase().contains(search))
                    .toList());
            result.sort(Comparator.comparing(Part::id).reversed());
            return result;
        }
    }

    static final class RecordingAuditTrail implements QmsAuditTrail {
        final List<String> actions = new ArrayList<>();

        @Override
        public void record(long tenantId, long actorId, String action, String objectType, long objectId, Object afterState) {
            actions.add(action + ":" + objectType + ":" + objectId);
        }
    }
}
