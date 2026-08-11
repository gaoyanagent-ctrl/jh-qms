package com.company.iaf.qms.engineering.application;

import com.company.iaf.platform.core.security.RequiresPermission;
import com.company.iaf.qms.engineering.domain.model.Part;
import com.company.iaf.qms.engineering.domain.model.PartStatus;
import com.company.iaf.qms.engineering.domain.repository.PartRepository;
import com.company.iaf.qms.engineering.domain.repository.QmsAuditTrail;
import com.company.iaf.qms.engineering.interfaces.dto.PartCreateRequest;
import com.company.iaf.qms.engineering.interfaces.dto.PartResponse;
import com.company.iaf.shared.exception.BusinessException;
import com.company.iaf.shared.result.PageResult;
import com.company.iaf.shared.security.SecurityContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PartApplicationService {

    private final PartRepository partRepository;
    private final QmsAuditTrail auditTrail;

    public PartApplicationService(PartRepository partRepository, QmsAuditTrail auditTrail) {
        this.partRepository = partRepository;
        this.auditTrail = auditTrail;
    }

    @RequiresPermission("qms:part:view")
    @Transactional(readOnly = true)
    public PageResult<PartResponse> list(long tenantId, long orgId, String keyword, int pageNo, int pageSize) {
        String normalizedKeyword = trimToNull(keyword);
        long offset = (long) (pageNo - 1) * pageSize;
        List<PartResponse> records = partRepository
                .findPage(tenantId, orgId, normalizedKeyword, offset, pageSize)
                .stream().map(PartResponse::from).toList();
        return new PageResult<>(records, partRepository.count(tenantId, orgId, normalizedKeyword), pageNo, pageSize);
    }

    @RequiresPermission("qms:part:view")
    @Transactional(readOnly = true)
    public PartResponse get(long tenantId, long orgId, long id) {
        return PartResponse.from(requirePart(tenantId, orgId, id));
    }

    @RequiresPermission("qms:part:create")
    @Transactional
    public PartResponse create(long tenantId, long orgId, PartCreateRequest request) {
        String partNo = normalizeRequired(request.partNo());
        if (partRepository.existsByPartNo(tenantId, orgId, partNo)) {
            throw new BusinessException(QmsEngineeringErrorCode.PART_NO_ALREADY_EXISTS);
        }
        Part draft = new Part(
                null, tenantId, orgId, partNo, trimToNull(request.materialNo()),
                normalizeRequired(request.partName()), request.customerId(), trimToNull(request.vehicleModel()),
                request.supplierId(), trimToNull(request.importanceLevel()), PartStatus.ACTIVE,
                0, null, null
        );
        long actorId = SecurityContext.getUserId().orElse(0L);
        long id = partRepository.insert(actorId, draft);
        Part created = requirePart(tenantId, orgId, id);
        auditTrail.record(tenantId, actorId, "PART_CREATED", "Part", id, created);
        return PartResponse.from(created);
    }

    private Part requirePart(long tenantId, long orgId, long id) {
        return partRepository.findById(tenantId, orgId, id)
                .orElseThrow(() -> new BusinessException(QmsEngineeringErrorCode.PART_NOT_FOUND));
    }

    static String normalizeRequired(String value) {
        return value == null ? null : value.trim();
    }

    static String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
