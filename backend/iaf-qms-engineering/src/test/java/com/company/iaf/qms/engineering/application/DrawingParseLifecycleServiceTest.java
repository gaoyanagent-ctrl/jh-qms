package com.company.iaf.qms.engineering.application;

import com.company.iaf.platform.statemachine.application.DefaultStateMachineService;
import com.company.iaf.qms.engineering.domain.model.*;
import com.company.iaf.qms.engineering.domain.repository.*;
import com.company.iaf.shared.exception.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DrawingParseLifecycleServiceTest {
    private final DrawingRevisionRepository revisions = mock(DrawingRevisionRepository.class);
    private final DrawingParseJobRepository jobs = mock(DrawingParseJobRepository.class);
    private final DrawingParseResultRepository results = mock(DrawingParseResultRepository.class);
    private final QmsAuditTrail audit = mock(QmsAuditTrail.class);
    private final QualityCharacteristicRepository characteristics = mock(QualityCharacteristicRepository.class);
    private final DrawingParseLifecycleService service = new DrawingParseLifecycleService(
            revisions, jobs, results, audit, new DefaultStateMachineService(), characteristics);
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void startsQueuedJobAndTransitionsRevision() {
        DrawingParseJob job = job(ParseJobStatus.QUEUED, 0);
        DrawingRevision revision = revision(DrawingRevisionStatus.UPLOADED, ParseStatus.PENDING, 1);
        when(jobs.findById(1, 10, 7)).thenReturn(Optional.of(job));
        when(revisions.findById(1, 10, 5)).thenReturn(Optional.of(revision));
        when(jobs.transition(9, 1, 10, 7, "QUEUED", "RUNNING", null, null, 0)).thenReturn(true);
        when(revisions.transitionState(9, 1, 10, 5, "UPLOADED", "PARSING", "RUNNING", 1)).thenReturn(true);

        service.start(9, 1, 10, 7);

        verify(audit).record(eq(1L), eq(9L), eq("DRAWING_REVISION_STATE_TRANSITIONED"),
                eq("DrawingRevision"), eq(5L), any());
    }

    @Test
    void completesRunningJobOnlyWithEvidenceForEveryEntity() throws Exception {
        DrawingParseJob job = job(ParseJobStatus.RUNNING, 1);
        DrawingRevision revision = revision(DrawingRevisionStatus.PARSING, ParseStatus.RUNNING, 2);
        when(jobs.findById(1, 10, 7)).thenReturn(Optional.of(job));
        when(revisions.findById(1, 10, 5)).thenReturn(Optional.of(revision));
        when(jobs.transition(9, 1, 10, 7, "RUNNING", "SUCCEEDED", null, null, 1)).thenReturn(true);
        when(revisions.transitionState(9, 1, 10, 5, "PARSING", "PARSED", "SUCCESS", 2)).thenReturn(true);
        DrawingParseResult result = validResult(true);

        service.complete(9, 1, 10, 7, result);

        verify(results).save(9, 1, 10, 5, 7, 11, result);
        verify(characteristics).generateDimensionCandidates(9, 1, 10, 5);
        verify(audit).record(eq(1L), eq(9L), eq("DRAWING_PARSE_RESULT_STORED"),
                eq("DrawingRevision"), eq(5L), any());
    }

    @Test
    void rejectsEntityWithoutLocatableEvidenceBeforeWriting() throws Exception {
        when(jobs.findById(1, 10, 7)).thenReturn(Optional.of(job(ParseJobStatus.RUNNING, 1)));
        when(revisions.findById(1, 10, 5)).thenReturn(Optional.of(
                revision(DrawingRevisionStatus.PARSING, ParseStatus.RUNNING, 2)));

        assertThatThrownBy(() -> service.complete(9, 1, 10, 7, validResult(false)))
                .isInstanceOf(BusinessException.class);
        verifyNoInteractions(results);
    }

    private DrawingParseResult validResult(boolean withEvidence) throws Exception {
        var json = mapper.readTree("""
            {"schemaVersion":"1.0.0","documentId":"DRAW-3","revision":"C","sheets":[
              {"sheetNo":"1","width":100,"height":80,"titleBlock":{},"views":[],
               "entities":[{"entityId":"DIM-1","entityType":"DIMENSION","sheetNo":"1",
               "evidence":[{"evidenceKey":"EV-1"}]}],"notes":[],"characteristicCandidates":[]}]}
            """);
        DrawingEntity entity = new DrawingEntity(null, 0, 0, 0, 0, "DIM-1", null,
                DrawingEntityType.DIMENSION, "DIM", "1", BigDecimal.ONE, BigDecimal.ONE,
                BigDecimal.TEN, BigDecimal.ONE, null, "8±0.5", "8±0.5", null, 0, null, null);
        SourceEvidence evidence = new SourceEvidence(null, 0, 0, 0, 0, 0, "EV-1",
                withEvidence ? "DIM-1" : null, null, "1", 1, BigDecimal.ONE, BigDecimal.ONE,
                BigDecimal.TEN, BigDecimal.ONE, "8±0.5", "8±0.5", EvidenceExtractorType.PDF_VECTOR,
                "1.0", null, null, new BigDecimal("0.98"), 0, null, null);
        return new DrawingParseResult("1.0.0", "DRAW-3", "C", json, List.of(entity), List.of(evidence));
    }

    private DrawingParseJob job(ParseJobStatus status, int version) {
        return new DrawingParseJob(7L, 1, 10, 5, 11, 1, status, "PDF", null, null,
                version, null, null);
    }

    private DrawingRevision revision(DrawingRevisionStatus status, ParseStatus parseStatus, int version) {
        return new DrawingRevision(5L, 1, 10, 3, "C", 3, 11L, "PDF", null, null, null,
                parseStatus, ReviewStatus.PENDING, status, "sum", null, null, version, null, null);
    }
}
