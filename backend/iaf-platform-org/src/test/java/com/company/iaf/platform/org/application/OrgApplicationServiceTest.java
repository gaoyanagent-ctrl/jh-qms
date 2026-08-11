package com.company.iaf.platform.org.application;

import com.company.iaf.platform.org.domain.model.Org;
import com.company.iaf.platform.org.domain.model.OrgStatus;
import com.company.iaf.platform.org.domain.model.OrgType;
import com.company.iaf.platform.org.domain.repository.OrgRepository;
import com.company.iaf.platform.org.interfaces.dto.OrgCreateRequest;
import com.company.iaf.platform.org.interfaces.dto.OrgResponse;
import com.company.iaf.platform.org.interfaces.dto.OrgTreeNodeResponse;
import com.company.iaf.platform.org.interfaces.dto.OrgUpdateRequest;
import com.company.iaf.shared.exception.BusinessException;
import com.company.iaf.shared.security.SecurityContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrgApplicationServiceTest {

    private final InMemoryOrgRepository repository = new InMemoryOrgRepository();
    private OrgApplicationService service;

    @BeforeEach
    void setUp() {
        service = new OrgApplicationService(repository);
        SecurityContext.setUserId(99L);
    }

    @AfterEach
    void clear() {
        SecurityContext.clear();
    }

    @Test
    void createOrgPersistsAndRejectsDuplicateCode() {
        OrgCreateRequest request = new OrgCreateRequest(
                null, "ROOT", "Root", OrgType.COMPANY, OrgStatus.ENABLED, 0);

        OrgResponse created = service.createOrg(1L, request);

        assertThat(created.id()).isNotNull();
        assertThat(created.orgCode()).isEqualTo("ROOT");
        assertThat(created.orgType()).isEqualTo(OrgType.COMPANY);

        assertThatThrownBy(() -> service.createOrg(1L,
                new OrgCreateRequest(null, "ROOT", "Dup", OrgType.COMPANY, OrgStatus.ENABLED, 0)))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).errorCode().code())
                .isEqualTo("PLATFORM_ORG_CODE_ALREADY_EXISTS");
    }

    @Test
    void createOrgRequiresExistingParent() {
        assertThatThrownBy(() -> service.createOrg(1L,
                new OrgCreateRequest(999L, "CHILD", "Child", OrgType.DEPARTMENT, OrgStatus.ENABLED, 0)))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).errorCode().code())
                .isEqualTo("PLATFORM_ORG_PARENT_NOT_FOUND");
    }

    @Test
    void createOrgIsScopedToTenant() {
        service.createOrg(1L, new OrgCreateRequest(null, "T-ROOT", "Root", OrgType.COMPANY, OrgStatus.ENABLED, 0));

        OrgResponse otherTenant = service.createOrg(2L,
                new OrgCreateRequest(null, "T-ROOT", "Root", OrgType.COMPANY, OrgStatus.ENABLED, 0));

        assertThat(otherTenant.tenantId()).isEqualTo(2L);
    }

    @Test
    void updateOrgChangesFieldsAndRejectsDuplicateCode() {
        OrgResponse created = service.createOrg(1L,
                new OrgCreateRequest(null, "ROOT", "Root", OrgType.COMPANY, OrgStatus.ENABLED, 0));
        service.createOrg(1L, new OrgCreateRequest(null, "OTHER", "Other", OrgType.COMPANY, OrgStatus.ENABLED, 0));

        OrgResponse renamed = service.updateOrg(1L, created.id(),
                new OrgUpdateRequest(null, "ROOT-2", "Renamed", OrgType.DIVISION, OrgStatus.ENABLED, 1));

        assertThat(renamed.orgCode()).isEqualTo("ROOT-2");
        assertThat(renamed.orgType()).isEqualTo(OrgType.DIVISION);
        assertThat(renamed.sortNo()).isEqualTo(1);

        assertThatThrownBy(() -> service.updateOrg(1L, created.id(),
                new OrgUpdateRequest(null, "OTHER", "Conflict", OrgType.DIVISION, OrgStatus.ENABLED, 0)))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).errorCode().code())
                .isEqualTo("PLATFORM_ORG_CODE_ALREADY_EXISTS");
    }

    @Test
    void updateOrgRejectsSelfParent() {
        OrgResponse created = service.createOrg(1L,
                new OrgCreateRequest(null, "ROOT", "Root", OrgType.COMPANY, OrgStatus.ENABLED, 0));

        assertThatThrownBy(() -> service.updateOrg(1L, created.id(),
                new OrgUpdateRequest(created.id(), "ROOT", "Root", OrgType.COMPANY, OrgStatus.ENABLED, 0)))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).errorCode().code())
                .isEqualTo("PLATFORM_ORG_PARENT_NOT_FOUND");
    }

    @Test
    void updateOrgThrowsWhenMissing() {
        assertThatThrownBy(() -> service.updateOrg(1L, 9_999L,
                new OrgUpdateRequest(null, "GHOST", "Ghost", OrgType.COMPANY, OrgStatus.ENABLED, 0)))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).errorCode().code())
                .isEqualTo("PLATFORM_ORG_NOT_FOUND");
    }

    @Test
    void getTreeAssemblesParentChildStructure() {
        OrgResponse company = service.createOrg(1L,
                new OrgCreateRequest(null, "ACME", "ACME", OrgType.COMPANY, OrgStatus.ENABLED, 0));
        OrgResponse engineering = service.createOrg(1L,
                new OrgCreateRequest(company.id(), "ENG", "Engineering", OrgType.DIVISION, OrgStatus.ENABLED, 0));
        service.createOrg(1L,
                new OrgCreateRequest(engineering.id(), "BACKEND", "Backend", OrgType.TEAM, OrgStatus.ENABLED, 0));

        List<OrgTreeNodeResponse> tree = service.getTree(1L);

        assertThat(tree).hasSize(1);
        OrgTreeNodeResponse root = tree.get(0);
        assertThat(root.orgCode()).isEqualTo("ACME");
        assertThat(root.children()).hasSize(1);
        OrgTreeNodeResponse eng = root.children().get(0);
        assertThat(eng.orgCode()).isEqualTo("ENG");
        assertThat(eng.children()).hasSize(1);
        assertThat(eng.children().get(0).orgCode()).isEqualTo("BACKEND");
    }

    @Test
    void getTreeIsTenantScoped() {
        service.createOrg(1L, new OrgCreateRequest(null, "A", "A", OrgType.COMPANY, OrgStatus.ENABLED, 0));
        service.createOrg(2L, new OrgCreateRequest(null, "B", "B", OrgType.COMPANY, OrgStatus.ENABLED, 0));

        List<OrgTreeNodeResponse> tenant1 = service.getTree(1L);
        List<OrgTreeNodeResponse> tenant2 = service.getTree(2L);

        assertThat(tenant1).extracting(OrgTreeNodeResponse::orgCode).containsExactly("A");
        assertThat(tenant2).extracting(OrgTreeNodeResponse::orgCode).containsExactly("B");
    }

    @Test
    void getOrgThrowsWhenMissing() {
        assertThatThrownBy(() -> service.getOrg(1L, 9_999L))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).errorCode().code())
                .isEqualTo("PLATFORM_ORG_NOT_FOUND");
    }

    /**
     * Test double for the persistence contract. Tracks orgs by id within a
     * single map; the production repository splits them by tenant via SQL
     * where clauses so the in-memory implementation just enforces the
     * same boundary explicitly.
     */
    private static final class InMemoryOrgRepository implements OrgRepository {

        private final AtomicLong nextId = new AtomicLong(1);
        private final Map<Long, Org> orgs = new ConcurrentHashMap<>();

        @Override
        public Optional<Org> findById(long tenantId, long id) {
            Org org = orgs.get(id);
            return Optional.ofNullable(org).filter(o -> o.tenantId() == tenantId);
        }

        @Override
        public boolean existsByOrgCode(long tenantId, String orgCode) {
            return orgs.values().stream()
                    .anyMatch(org -> org.tenantId() == tenantId && org.orgCode().equals(orgCode));
        }

        @Override
        public List<Org> findAll(long tenantId) {
            List<Org> result = new ArrayList<>();
            for (Org org : orgs.values()) {
                if (org.tenantId() == tenantId) {
                    result.add(org);
                }
            }
            result.sort((a, b) -> {
                long ap = a.parentId() == null ? 0L : a.parentId();
                long bp = b.parentId() == null ? 0L : b.parentId();
                int byParent = Long.compare(ap, bp);
                if (byParent != 0) return byParent;
                int bySort = Integer.compare(a.sortNo(), b.sortNo());
                if (bySort != 0) return bySort;
                return Long.compare(a.id(), b.id());
            });
            return result;
        }

        @Override
        public long insert(long operatorUserId, Org org) {
            long id = nextId.getAndIncrement();
            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
            orgs.put(id, new Org(id, org.tenantId(), org.parentId(), org.orgCode(), org.orgName(),
                    org.orgType(), org.status(), org.sortNo(), 0, now, now));
            return id;
        }

        @Override
        public boolean update(long operatorUserId, Org org) {
            Org current = orgs.get(org.id());
            if (current == null || current.tenantId() != org.tenantId() || current.version() != org.version()) {
                return false;
            }
            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
            orgs.put(org.id(), new Org(org.id(), org.tenantId(), org.parentId(), org.orgCode(), org.orgName(),
                    org.orgType(), org.status(), org.sortNo(), org.version() + 1,
                    current.createdAt(), now));
            return true;
        }
    }
}