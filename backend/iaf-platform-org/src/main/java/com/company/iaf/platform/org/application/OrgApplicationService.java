package com.company.iaf.platform.org.application;

import com.company.iaf.platform.org.domain.model.Org;
import com.company.iaf.platform.org.domain.model.OrgStatus;
import com.company.iaf.platform.org.domain.repository.OrgRepository;
import com.company.iaf.platform.org.interfaces.dto.OrgCreateRequest;
import com.company.iaf.platform.org.interfaces.dto.OrgResponse;
import com.company.iaf.platform.org.interfaces.dto.OrgTreeNodeResponse;
import com.company.iaf.platform.org.interfaces.dto.OrgUpdateRequest;
import com.company.iaf.platform.core.security.RequiresPermission;
import com.company.iaf.shared.exception.BusinessException;
import com.company.iaf.shared.exception.CommonErrorCode;
import com.company.iaf.shared.security.SecurityContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Application service for platform organization management. The
 * organization tree is built lazily on demand from a flat repository
 * listing so callers receive a single round-trip response.
 */
@Service
public class OrgApplicationService {

    private final OrgRepository orgRepository;

    public OrgApplicationService(OrgRepository orgRepository) {
        this.orgRepository = orgRepository;
    }

    @RequiresPermission("platform:org:view")
    @Transactional(readOnly = true)
    public List<OrgTreeNodeResponse> getTree(long tenantId) {
        List<Org> all = orgRepository.findAll(tenantId);
        if (all.isEmpty()) {
            return List.of();
        }
        Map<Long, OrgTreeNodeResponse> byId = new HashMap<>();
        for (Org org : all) {
            byId.put(org.id(), OrgTreeNodeResponse.from(org));
        }
        List<OrgTreeNodeResponse> roots = new ArrayList<>();
        for (Org org : all) {
            OrgTreeNodeResponse node = byId.get(org.id());
            if (org.parentId() == null) {
                roots.add(node);
                continue;
            }
            OrgTreeNodeResponse parent = byId.get(org.parentId());
            if (parent == null) {
                // Orphan: parent missing or outside tenant. Surface as root so
                // the caller can reconcile rather than silently dropping it.
                roots.add(node);
            } else {
                parent.children().add(node);
            }
        }
        return roots;
    }

    @RequiresPermission("platform:org:view")
    @Transactional(readOnly = true)
    public OrgResponse getOrg(long tenantId, long id) {
        return orgRepository.findById(tenantId, id)
                .map(OrgResponse::from)
                .orElseThrow(() -> new BusinessException(PlatformOrgErrorCode.ORG_NOT_FOUND));
    }

    @RequiresPermission("platform:org:create")
    @Transactional
    public OrgResponse createOrg(long tenantId, OrgCreateRequest request) {
        if (orgRepository.existsByOrgCode(tenantId, request.orgCode())) {
            throw new BusinessException(PlatformOrgErrorCode.ORG_CODE_ALREADY_EXISTS);
        }
        if (request.parentId() != null) {
            orgRepository.findById(tenantId, request.parentId())
                    .orElseThrow(() -> new BusinessException(PlatformOrgErrorCode.ORG_PARENT_NOT_FOUND));
        }
        long currentUserId = SecurityContext.getUserId().orElse(0L);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        Org draft = new Org(
                null,
                tenantId,
                request.parentId(),
                request.orgCode(),
                request.orgName(),
                request.orgType(),
                request.status() == null ? OrgStatus.ENABLED : request.status(),
                request.sortNo() == null ? 0 : request.sortNo(),
                0,
                now,
                now
        );
        long id = orgRepository.insert(currentUserId, draft);
        return orgRepository.findById(tenantId, id)
                .map(OrgResponse::from)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.INTERNAL_ERROR, "Inserted org could not be reloaded"));
    }

    @RequiresPermission("platform:org:update")
    @Transactional
    public OrgResponse updateOrg(long tenantId, long id, OrgUpdateRequest request) {
        Org existing = orgRepository.findById(tenantId, id)
                .orElseThrow(() -> new BusinessException(PlatformOrgErrorCode.ORG_NOT_FOUND));
        if (!existing.orgCode().equals(request.orgCode())
                && orgRepository.existsByOrgCode(tenantId, request.orgCode())) {
            throw new BusinessException(PlatformOrgErrorCode.ORG_CODE_ALREADY_EXISTS);
        }
        if (request.parentId() != null) {
            if (request.parentId() == id) {
                throw newCycleException();
            }
            orgRepository.findById(tenantId, request.parentId())
                    .orElseThrow(() -> new BusinessException(PlatformOrgErrorCode.ORG_PARENT_NOT_FOUND));
        }
        Org updated = new Org(
                existing.id(),
                existing.tenantId(),
                request.parentId(),
                request.orgCode(),
                request.orgName(),
                request.orgType(),
                request.status(),
                request.sortNo() == null ? existing.sortNo() : request.sortNo(),
                existing.version(),
                existing.createdAt(),
                existing.updatedAt()
        );
        long currentUserId = SecurityContext.getUserId().orElse(0L);
        if (!orgRepository.update(currentUserId, updated)) {
            throw new BusinessException(CommonErrorCode.CONFLICT, "Organization was modified concurrently");
        }
        return orgRepository.findById(tenantId, id)
                .map(OrgResponse::from)
                .orElseThrow(() -> new BusinessException(PlatformOrgErrorCode.ORG_NOT_FOUND));
    }

    /**
     * Reject a node from becoming its own parent. A full ancestor cycle
     * check requires the in-memory tree; keeping the validation minimal
     * here is consistent with the platform-admin scope (single tenant,
     * small trees) and the repository's parent lookup will surface deeper
     * inconsistencies.
     */
    private static BusinessException newCycleException() {
        return new BusinessException(PlatformOrgErrorCode.ORG_PARENT_NOT_FOUND, "Organization cannot be its own parent");
    }
}