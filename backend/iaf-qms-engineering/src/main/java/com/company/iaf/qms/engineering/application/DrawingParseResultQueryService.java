package com.company.iaf.qms.engineering.application;

import com.company.iaf.platform.core.security.RequiresPermission;
import com.company.iaf.qms.engineering.domain.repository.DrawingParseResultRepository;
import com.company.iaf.qms.engineering.domain.repository.DrawingRevisionRepository;
import com.company.iaf.qms.engineering.interfaces.dto.*;
import com.company.iaf.shared.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class DrawingParseResultQueryService {
    private final DrawingRevisionRepository revisions;
    private final DrawingParseResultRepository results;

    public DrawingParseResultQueryService(DrawingRevisionRepository revisions,
                                          DrawingParseResultRepository results) {
        this.revisions = revisions;
        this.results = results;
    }

    @RequiresPermission("qms:drawing-revision:view")
    @Transactional(readOnly = true)
    public DrawingIntermediateModelResponse model(long tenantId, long orgId, long revisionId) {
        requireRevision(tenantId, orgId, revisionId);
        return DrawingIntermediateModelResponse.from(results.findModel(tenantId, orgId, revisionId)
                .orElseThrow(() -> new BusinessException(QmsEngineeringErrorCode.INTERMEDIATE_MODEL_NOT_FOUND)));
    }

    @RequiresPermission("qms:drawing-revision:view")
    @Transactional(readOnly = true)
    public List<DrawingEntityResponse> entities(long tenantId, long orgId, long revisionId) {
        requireRevision(tenantId, orgId, revisionId);
        return results.findEntities(tenantId, orgId, revisionId).stream().map(DrawingEntityResponse::from).toList();
    }

    @RequiresPermission("qms:drawing-revision:view")
    @Transactional(readOnly = true)
    public List<SourceEvidenceResponse> evidence(long tenantId, long orgId, long revisionId) {
        requireRevision(tenantId, orgId, revisionId);
        return results.findEvidence(tenantId, orgId, revisionId).stream().map(SourceEvidenceResponse::from).toList();
    }

    @RequiresPermission("qms:drawing-revision:view")
    @Transactional(readOnly = true)
    public SourceEvidenceResponse evidence(long tenantId, long orgId, long revisionId, long evidenceId) {
        requireRevision(tenantId, orgId, revisionId);
        return SourceEvidenceResponse.from(results.findEvidenceById(tenantId, orgId, revisionId, evidenceId)
                .orElseThrow(() -> new BusinessException(QmsEngineeringErrorCode.EVIDENCE_NOT_FOUND)));
    }

    private void requireRevision(long tenantId, long orgId, long revisionId) {
        if (revisions.findById(tenantId, orgId, revisionId).isEmpty())
            throw new BusinessException(QmsEngineeringErrorCode.REVISION_NOT_FOUND);
    }
}
