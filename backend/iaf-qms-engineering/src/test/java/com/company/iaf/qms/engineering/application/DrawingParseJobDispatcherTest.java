package com.company.iaf.qms.engineering.application;

import com.company.iaf.qms.engineering.domain.model.*;
import com.company.iaf.qms.engineering.domain.repository.*;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import com.company.iaf.qms.engineering.infrastructure.parser.UnavailableCadParserAdapter;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DrawingParseJobDispatcherTest {
    private final DrawingParseJobRepository jobs = mock(DrawingParseJobRepository.class);
    private final DrawingRevisionRepository revisions = mock(DrawingRevisionRepository.class);
    private final QmsFileObjectRepository files = mock(QmsFileObjectRepository.class);
    private final QmsObjectStorage storage = mock(QmsObjectStorage.class);
    private final DrawingParseLifecycleService lifecycle = mock(DrawingParseLifecycleService.class);
    private final PdfParserPort parser = mock(PdfParserPort.class);
    private final CadParserPort cadParser = mock(CadParserPort.class);
    private final DrawingParseJobDispatcher dispatcher = new DrawingParseJobDispatcher(
            jobs, revisions, files, storage, lifecycle, parser, cadParser, true);

    @Test
    void claimsParsesAndCompletesQueuedPdf() {
        DrawingParseJob job = new DrawingParseJob(7L, 1, 10, 5, 11, 1,
                ParseJobStatus.QUEUED, "PDF", null, null, 0, OffsetDateTime.now(), OffsetDateTime.now());
        DrawingRevision revision = new DrawingRevision(5L, 1, 10, 3, "D", 4, 11L, "PDF",
                null, null, null, ParseStatus.PENDING, ReviewStatus.PENDING,
                DrawingRevisionStatus.UPLOADED, "sum", null, null, 0, null, null);
        QmsFileObject file = new QmsFileObject(11L, 1, 10, "drawing.pdf", "application/pdf",
                "pdf", 4, "sum", "bucket", "key", 0, OffsetDateTime.now());
        DrawingParseResult result = new DrawingParseResult("1.0.0", "drawing-revision-5", "D",
                mock(com.fasterxml.jackson.databind.JsonNode.class), List.of(), List.of());
        when(jobs.findQueued(3)).thenReturn(List.of(job));
        when(revisions.findById(1, 10, 5)).thenReturn(Optional.of(revision));
        when(files.findById(1, 10, 11)).thenReturn(Optional.of(file));
        when(storage.get("key")).thenReturn(new ByteArrayInputStream("%PDF".getBytes()));
        when(parser.parse(any(), eq("drawing.pdf"), eq("drawing-revision-5"), eq("D"))).thenReturn(result);

        dispatcher.poll();

        verify(lifecycle).start(0, 1, 10, 7);
        verify(parser).parse(any(), eq("drawing.pdf"), eq("drawing-revision-5"), eq("D"));
        verify(lifecycle).complete(0, 1, 10, 7, result);
        verify(lifecycle, never()).fail(anyLong(), anyLong(), anyLong(), anyLong(), any(), any());
    }

    @Test
    void recordsFailureAfterAClaimedParserError() {
        DrawingParseJob job = new DrawingParseJob(7L, 1, 10, 5, 11, 1,
                ParseJobStatus.QUEUED, "PDF", null, null, 0, null, null);
        when(revisions.findById(1, 10, 5)).thenThrow(new IllegalStateException("missing revision"));

        dispatcher.process(job);

        verify(lifecycle).start(0, 1, 10, 7);
        verify(lifecycle).fail(0, 1, 10, 7, "PDF_PARSE_FAILED", "missing revision");
    }

    @Test
    void reportsUnavailableCadProviderWithoutFabricatingEvidence() {
        DrawingParseJob job = new DrawingParseJob(8L, 1, 10, 6, 12, 1,
                ParseJobStatus.QUEUED, "DWG", null, null, 0, null, null);
        DrawingRevision revision = new DrawingRevision(6L, 1, 10, 3, "E", 5, 12L, "DWG",
                null, null, null, ParseStatus.PENDING, ReviewStatus.PENDING,
                DrawingRevisionStatus.UPLOADED, "sum", null, null, 0, null, null);
        QmsFileObject file = new QmsFileObject(12L, 1, 10, "drawing.dwg", "image/vnd.dwg",
                "dwg", 6, "sum", "bucket", "dwg-key", 0, OffsetDateTime.now());
        when(revisions.findById(1, 10, 6)).thenReturn(Optional.of(revision));
        when(files.findById(1, 10, 12)).thenReturn(Optional.of(file));
        when(storage.get("dwg-key")).thenReturn(new ByteArrayInputStream("AC1027".getBytes()));
        when(cadParser.parse(any(), any(), any(), any())).thenThrow(
                new UnavailableCadParserAdapter.CadProviderUnavailableException("not configured"));

        dispatcher.process(job);

        verify(lifecycle).fail(0, 1, 10, 8, "CAD_PROVIDER_UNAVAILABLE", "not configured");
        verify(lifecycle, never()).complete(anyLong(), anyLong(), anyLong(), anyLong(), any());
    }
}
