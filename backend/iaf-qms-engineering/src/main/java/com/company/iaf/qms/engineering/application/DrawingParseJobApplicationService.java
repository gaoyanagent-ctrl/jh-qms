package com.company.iaf.qms.engineering.application;

import com.company.iaf.platform.core.security.RequiresPermission;
import com.company.iaf.platform.statemachine.application.StateMachineService;
import com.company.iaf.platform.statemachine.application.StateTransition;
import com.company.iaf.qms.engineering.domain.model.*;
import com.company.iaf.qms.engineering.domain.repository.*;
import com.company.iaf.qms.engineering.interfaces.dto.DrawingParseJobResponse;
import com.company.iaf.shared.exception.BusinessException;
import com.company.iaf.shared.security.SecurityContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class DrawingParseJobApplicationService {
    private static final List<StateTransition<DrawingRevisionStatus>> TRANSITIONS = List.of(
            new StateTransition<>(DrawingRevisionStatus.FAILED, "retry-parse", DrawingRevisionStatus.UPLOADED));
    private final DrawingRevisionRepository revisions;
    private final DrawingParseJobRepository jobs;
    private final QmsAuditTrail audit;
    private final StateMachineService stateMachine;

    public DrawingParseJobApplicationService(DrawingRevisionRepository revisions, DrawingParseJobRepository jobs,
            QmsAuditTrail audit, StateMachineService stateMachine) {
        this.revisions=revisions; this.jobs=jobs; this.audit=audit; this.stateMachine=stateMachine;
    }

    @RequiresPermission("qms:drawing-revision:view")
    @Transactional(readOnly=true)
    public DrawingParseJobResponse latest(long tenantId, long orgId, long revisionId) {
        requireRevision(tenantId, orgId, revisionId);
        return DrawingParseJobResponse.from(jobs.findLatest(tenantId, orgId, revisionId)
                .orElseThrow(() -> new BusinessException(QmsEngineeringErrorCode.PARSE_JOB_NOT_FOUND)));
    }

    @RequiresPermission("qms:drawing-revision:view")
    @Transactional(readOnly=true)
    public List<DrawingParseJobResponse> latestByDrawing(long tenantId, long orgId, long drawingId) {
        return jobs.findLatestByDrawingId(tenantId, orgId, drawingId).stream()
                .map(DrawingParseJobResponse::from).toList();
    }

    @RequiresPermission("qms:drawing-revision:retry-parse")
    @Transactional
    public DrawingParseJobResponse retry(long tenantId, long orgId, long revisionId) {
        DrawingRevision revision=requireRevision(tenantId, orgId, revisionId);
        DrawingParseJob previous=jobs.findLatest(tenantId, orgId, revisionId)
                .orElseThrow(() -> new BusinessException(QmsEngineeringErrorCode.PARSE_JOB_NOT_FOUND));
        if (previous.status()!=ParseJobStatus.FAILED || revision.fileId()==null)
            throw new BusinessException(QmsEngineeringErrorCode.PARSE_JOB_NOT_RETRYABLE);
        DrawingRevisionStatus target;
        try { target=stateMachine.requireTransition(revision.status(), "retry-parse", TRANSITIONS).to(); }
        catch (IllegalStateException e) { throw new BusinessException(QmsEngineeringErrorCode.REVISION_INVALID_STATE); }
        long actor=SecurityContext.getUserId().orElse(0L);
        if (!revisions.transitionState(actor, tenantId, orgId, revisionId, revision.status().name(),
                target.name(), ParseStatus.PENDING.name(), revision.version()))
            throw new BusinessException(QmsEngineeringErrorCode.REVISION_INVALID_STATE);
        jobs.enqueue(actor, tenantId, orgId, revisionId, revision.fileId(), previous.parserType(), previous.attemptNo()+1);
        DrawingParseJob result=jobs.findLatest(tenantId, orgId, revisionId)
                .orElseThrow(() -> new BusinessException(QmsEngineeringErrorCode.PARSE_JOB_NOT_FOUND));
        audit.record(tenantId, actor, "DRAWING_PARSE_RETRIED", "DrawingRevision", revisionId, result);
        audit.record(tenantId, actor, "DRAWING_REVISION_STATE_TRANSITIONED", "DrawingRevision", revisionId,
                new StateChange(revision.status().name(), "retry-parse", target.name()));
        return DrawingParseJobResponse.from(result);
    }

    private DrawingRevision requireRevision(long tenantId,long orgId,long id) {
        return revisions.findById(tenantId,orgId,id)
                .orElseThrow(() -> new BusinessException(QmsEngineeringErrorCode.REVISION_NOT_FOUND));
    }

    private record StateChange(String from, String action, String to) { }
}
