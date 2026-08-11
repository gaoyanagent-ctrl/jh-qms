package com.company.iaf.qms.engineering.application;

import com.company.iaf.platform.core.security.RequiresPermission;
import com.company.iaf.qms.engineering.domain.model.Drawing;
import com.company.iaf.qms.engineering.domain.model.DrawingRevision;
import com.company.iaf.qms.engineering.domain.model.DrawingSourceSystem;
import com.company.iaf.qms.engineering.domain.model.DrawingStatus;
import com.company.iaf.qms.engineering.domain.repository.DrawingRepository;
import com.company.iaf.qms.engineering.domain.repository.DrawingRevisionRepository;
import com.company.iaf.qms.engineering.domain.repository.PartRepository;
import com.company.iaf.qms.engineering.domain.repository.QmsAuditTrail;
import com.company.iaf.qms.engineering.interfaces.dto.DrawingCreateRequest;
import com.company.iaf.qms.engineering.interfaces.dto.DrawingResponse;
import com.company.iaf.qms.engineering.interfaces.dto.DrawingRevisionCreateRequest;
import com.company.iaf.qms.engineering.interfaces.dto.DrawingRevisionResponse;
import com.company.iaf.shared.exception.BusinessException;
import com.company.iaf.shared.security.SecurityContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DrawingApplicationService {

    private final PartRepository partRepository;
    private final DrawingRepository drawingRepository;
    private final DrawingRevisionRepository revisionRepository;
    private final QmsAuditTrail auditTrail;

    public DrawingApplicationService(
            PartRepository partRepository,
            DrawingRepository drawingRepository,
            DrawingRevisionRepository revisionRepository,
            QmsAuditTrail auditTrail
    ) {
        this.partRepository = partRepository;
        this.drawingRepository = drawingRepository;
        this.revisionRepository = revisionRepository;
        this.auditTrail = auditTrail;
    }

    @RequiresPermission("qms:drawing:view")
    @Transactional(readOnly = true)
    public List<DrawingResponse> listDrawings(long tenantId, long orgId, long partId) {
        requirePart(tenantId, orgId, partId);
        return drawingRepository.findByPartId(tenantId, orgId, partId).stream()
                .map(DrawingResponse::from).toList();
    }

    @RequiresPermission("qms:drawing:view")
    @Transactional(readOnly = true)
    public DrawingResponse getDrawing(long tenantId, long orgId, long drawingId) {
        return DrawingResponse.from(requireDrawing(tenantId, orgId, drawingId));
    }

    @RequiresPermission("qms:drawing:create")
    @Transactional
    public DrawingResponse createDrawing(long tenantId, long orgId, long partId, DrawingCreateRequest request) {
        requirePart(tenantId, orgId, partId);
        String drawingNo = PartApplicationService.normalizeRequired(request.drawingNo());
        if (drawingRepository.existsByDrawingNo(tenantId, partId, drawingNo)) {
            throw new BusinessException(QmsEngineeringErrorCode.DRAWING_NO_ALREADY_EXISTS);
        }
        Drawing draft = new Drawing(
                null, tenantId, orgId, partId, drawingNo,
                PartApplicationService.normalizeRequired(request.drawingName()), request.drawingType(),
                request.sourceSystem() == null ? DrawingSourceSystem.MANUAL : request.sourceSystem(),
                DrawingStatus.ACTIVE, 0, null, null
        );
        long actorId = SecurityContext.getUserId().orElse(0L);
        long id = drawingRepository.insert(actorId, draft);
        Drawing created = requireDrawing(tenantId, orgId, id);
        auditTrail.record(tenantId, actorId, "DRAWING_CREATED", "Drawing", id, created);
        return DrawingResponse.from(created);
    }

    @RequiresPermission("qms:drawing-revision:view")
    @Transactional(readOnly = true)
    public List<DrawingRevisionResponse> listRevisions(long tenantId, long orgId, long drawingId) {
        requireDrawing(tenantId, orgId, drawingId);
        return revisionRepository.findByDrawingId(tenantId, orgId, drawingId).stream()
                .map(DrawingRevisionResponse::from).toList();
    }

    @RequiresPermission("qms:drawing-revision:view")
    @Transactional(readOnly = true)
    public DrawingRevisionResponse getRevision(long tenantId, long orgId, long revisionId) {
        return DrawingRevisionResponse.from(requireRevision(tenantId, orgId, revisionId));
    }

    @RequiresPermission("qms:drawing-revision:create")
    @Transactional
    public DrawingRevisionResponse createRevision(
            long tenantId,
            long orgId,
            long drawingId,
            DrawingRevisionCreateRequest request
    ) {
        requireDrawing(tenantId, orgId, drawingId);
        String revisionCode = PartApplicationService.normalizeRequired(request.revisionCode());
        if (revisionRepository.existsByRevisionCode(tenantId, drawingId, revisionCode)) {
            throw new BusinessException(QmsEngineeringErrorCode.REVISION_CODE_ALREADY_EXISTS);
        }
        if (request.supersedesRevisionId() != null) {
            DrawingRevision superseded = requireRevision(tenantId, orgId, request.supersedesRevisionId());
            if (superseded.drawingId() != drawingId) {
                throw new BusinessException(QmsEngineeringErrorCode.SUPERSEDES_REVISION_INVALID);
            }
        }
        int revisionSeq = revisionRepository.reserveNextSequence(tenantId, drawingId);
        DrawingRevision draft = DrawingRevision.metadataDraft(
                tenantId, orgId, drawingId, revisionCode, revisionSeq,
                request.effectiveDate(), request.supersedesRevisionId()
        );
        long actorId = SecurityContext.getUserId().orElse(0L);
        long id = revisionRepository.insert(actorId, draft);
        DrawingRevision created = requireRevision(tenantId, orgId, id);
        auditTrail.record(tenantId, actorId, "DRAWING_REVISION_CREATED", "DrawingRevision", id, created);
        return DrawingRevisionResponse.from(created);
    }

    private void requirePart(long tenantId, long orgId, long partId) {
        if (partRepository.findById(tenantId, orgId, partId).isEmpty()) {
            throw new BusinessException(QmsEngineeringErrorCode.PART_NOT_FOUND);
        }
    }

    private Drawing requireDrawing(long tenantId, long orgId, long drawingId) {
        return drawingRepository.findById(tenantId, orgId, drawingId)
                .orElseThrow(() -> new BusinessException(QmsEngineeringErrorCode.DRAWING_NOT_FOUND));
    }

    private DrawingRevision requireRevision(long tenantId, long orgId, long revisionId) {
        return revisionRepository.findById(tenantId, orgId, revisionId)
                .orElseThrow(() -> new BusinessException(QmsEngineeringErrorCode.REVISION_NOT_FOUND));
    }
}
