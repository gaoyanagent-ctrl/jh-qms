package com.company.iaf.qms.engineering.application;

import com.company.iaf.platform.statemachine.application.StateMachineService;
import com.company.iaf.platform.statemachine.application.StateTransition;
import com.company.iaf.qms.engineering.domain.model.*;
import com.company.iaf.qms.engineering.domain.repository.*;
import com.company.iaf.shared.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class DrawingParseLifecycleService {
    private static final String DIM_SCHEMA_VERSION = "1.0.0";
    private static final List<StateTransition<DrawingRevisionStatus>> TRANSITIONS = List.of(
            new StateTransition<>(DrawingRevisionStatus.UPLOADED, "start-parse", DrawingRevisionStatus.PARSING),
            new StateTransition<>(DrawingRevisionStatus.PARSING, "complete-parse", DrawingRevisionStatus.PARSED),
            new StateTransition<>(DrawingRevisionStatus.PARSING, "fail-parse", DrawingRevisionStatus.FAILED));
    private final DrawingRevisionRepository revisions;
    private final DrawingParseJobRepository jobs;
    private final DrawingParseResultRepository results;
    private final QmsAuditTrail audit;
    private final StateMachineService stateMachine;
    private final QualityCharacteristicRepository characteristics;

    public DrawingParseLifecycleService(DrawingRevisionRepository revisions, DrawingParseJobRepository jobs,
            DrawingParseResultRepository results, QmsAuditTrail audit, StateMachineService stateMachine,
            QualityCharacteristicRepository characteristics) {
        this.revisions = revisions;
        this.jobs = jobs;
        this.results = results;
        this.audit = audit;
        this.stateMachine = stateMachine;
        this.characteristics = characteristics;
    }

    @Transactional
    public void start(long actorId, long tenantId, long orgId, long parseJobId) {
        DrawingParseJob job = requireJob(tenantId, orgId, parseJobId);
        DrawingRevision revision = requireRevision(tenantId, orgId, job.revisionId());
        DrawingRevisionStatus target = transition(revision.status(), "start-parse");
        if (job.status() != ParseJobStatus.QUEUED
                || !jobs.transition(actorId, tenantId, orgId, job.id(), job.status().name(),
                ParseJobStatus.RUNNING.name(), null, null, job.version())
                || !revisions.transitionState(actorId, tenantId, orgId, revision.id(), revision.status().name(),
                target.name(), ParseStatus.RUNNING.name(), revision.version())) {
            throw new BusinessException(QmsEngineeringErrorCode.PARSE_JOB_INVALID_STATE);
        }
        auditTransition(actorId, tenantId, revision.id(), revision.status(), "start-parse", target);
    }

    @Transactional
    public void complete(long actorId, long tenantId, long orgId, long parseJobId, DrawingParseResult result) {
        DrawingParseJob job = requireJob(tenantId, orgId, parseJobId);
        DrawingRevision revision = requireRevision(tenantId, orgId, job.revisionId());
        DrawingRevisionStatus target = transition(revision.status(), "complete-parse");
        validate(result, revision);
        if (job.status() != ParseJobStatus.RUNNING) {
            throw new BusinessException(QmsEngineeringErrorCode.PARSE_JOB_INVALID_STATE);
        }
        results.save(actorId, tenantId, orgId, revision.id(), job.id(), job.fileId(), result);
        characteristics.generateDimensionCandidates(actorId, tenantId, orgId, revision.id());
        if (!jobs.transition(actorId, tenantId, orgId, job.id(), job.status().name(),
                ParseJobStatus.SUCCEEDED.name(), null, null, job.version())
                || !revisions.transitionState(actorId, tenantId, orgId, revision.id(), revision.status().name(),
                target.name(), ParseStatus.SUCCESS.name(), revision.version())) {
            throw new BusinessException(QmsEngineeringErrorCode.PARSE_JOB_INVALID_STATE);
        }
        audit.record(tenantId, actorId, "DRAWING_PARSE_RESULT_STORED", "DrawingRevision", revision.id(),
                new ResultSummary(job.id(), result.schemaVersion(), result.entities().size(), result.evidence().size()));
        auditTransition(actorId, tenantId, revision.id(), revision.status(), "complete-parse", target);
    }

    @Transactional
    public void fail(long actorId, long tenantId, long orgId, long parseJobId,
                     String errorCode, String errorMessage) {
        DrawingParseJob job = requireJob(tenantId, orgId, parseJobId);
        DrawingRevision revision = requireRevision(tenantId, orgId, job.revisionId());
        DrawingRevisionStatus target = transition(revision.status(), "fail-parse");
        if (job.status() != ParseJobStatus.RUNNING
                || !jobs.transition(actorId, tenantId, orgId, job.id(), job.status().name(),
                ParseJobStatus.FAILED.name(), bounded(errorCode, 64), bounded(errorMessage, 1000), job.version())
                || !revisions.transitionState(actorId, tenantId, orgId, revision.id(), revision.status().name(),
                target.name(), ParseStatus.FAILED.name(), revision.version())) {
            throw new BusinessException(QmsEngineeringErrorCode.PARSE_JOB_INVALID_STATE);
        }
        auditTransition(actorId, tenantId, revision.id(), revision.status(), "fail-parse", target);
    }

    private void validate(DrawingParseResult result, DrawingRevision revision) {
        if (result == null || !DIM_SCHEMA_VERSION.equals(result.schemaVersion())
                || blank(result.documentId()) || !revision.revisionCode().equals(result.revisionCode())
                || result.modelJson() == null || !result.modelJson().isObject()
                || !DIM_SCHEMA_VERSION.equals(result.modelJson().path("schemaVersion").asText())
                || blank(result.modelJson().path("documentId").asText())
                || !revision.revisionCode().equals(result.modelJson().path("revision").asText())
                || !result.modelJson().path("sheets").isArray() || result.modelJson().path("sheets").isEmpty()) {
            throw new BusinessException(QmsEngineeringErrorCode.PARSE_RESULT_INVALID);
        }
        Set<String> entityIds = new HashSet<>();
        for (DrawingEntity entity : result.entities()) {
            if (entity == null || blank(entity.entityId()) || !entityIds.add(entity.entityId())
                    || entity.entityType() == null || blank(entity.sheetNo())) invalid();
        }
        Set<String> evidenced = new HashSet<>();
        Set<String> evidenceKeys = new HashSet<>();
        for (SourceEvidence evidence : result.evidence()) {
            if (evidence == null || blank(evidence.evidenceKey()) || evidence.extractorType() == null || blank(evidence.extractorVersion())
                    || blank(evidence.sheetNo()) || evidence.bboxX() == null || evidence.bboxY() == null
                    || evidence.bboxW() == null || evidence.bboxH() == null
                    || evidence.bboxW().signum() < 0 || evidence.bboxH().signum() < 0
                    || evidence.confidence() == null || evidence.confidence().compareTo(BigDecimal.ZERO) < 0
                    || evidence.confidence().compareTo(BigDecimal.ONE) > 0
                    || !evidenceKeys.add(evidence.evidenceKey())) invalid();
            if (!blank(evidence.entityId())) evidenced.add(evidence.entityId());
        }
        if (!evidenced.containsAll(entityIds)) invalid();
        Set<String> modelEntityIds = new HashSet<>();
        for (var sheet : result.modelJson().path("sheets")) {
            if (!sheet.path("entities").isArray()) invalid();
            for (var entity : sheet.path("entities")) {
                String entityId = entity.path("entityId").asText();
                if (!entityIds.contains(entityId) || !modelEntityIds.add(entityId)
                        || !entity.path("evidence").isArray() || entity.path("evidence").isEmpty()) invalid();
                for (var reference : entity.path("evidence")) {
                    if (!evidenceKeys.contains(reference.path("evidenceKey").asText())) invalid();
                }
            }
        }
        if (!modelEntityIds.equals(entityIds)) invalid();
    }

    private DrawingRevisionStatus transition(DrawingRevisionStatus from, String action) {
        try { return stateMachine.requireTransition(from, action, TRANSITIONS).to(); }
        catch (IllegalStateException e) { throw new BusinessException(QmsEngineeringErrorCode.REVISION_INVALID_STATE); }
    }
    private DrawingParseJob requireJob(long tenantId, long orgId, long id) {
        return jobs.findById(tenantId, orgId, id)
                .orElseThrow(() -> new BusinessException(QmsEngineeringErrorCode.PARSE_JOB_NOT_FOUND));
    }
    private DrawingRevision requireRevision(long tenantId, long orgId, long id) {
        return revisions.findById(tenantId, orgId, id)
                .orElseThrow(() -> new BusinessException(QmsEngineeringErrorCode.REVISION_NOT_FOUND));
    }
    private void auditTransition(long actorId, long tenantId, long revisionId,
                                 DrawingRevisionStatus from, String action, DrawingRevisionStatus to) {
        audit.record(tenantId, actorId, "DRAWING_REVISION_STATE_TRANSITIONED", "DrawingRevision",
                revisionId, new StateChange(from.name(), action, to.name()));
    }
    private static String bounded(String value, int max) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.length() <= max ? trimmed : trimmed.substring(0, max);
    }
    private static boolean blank(String value) { return value == null || value.isBlank(); }
    private static void invalid() { throw new BusinessException(QmsEngineeringErrorCode.PARSE_RESULT_INVALID); }
    private record StateChange(String from, String action, String to) { }
    private record ResultSummary(long parseJobId, String schemaVersion, int entityCount, int evidenceCount) { }
}
