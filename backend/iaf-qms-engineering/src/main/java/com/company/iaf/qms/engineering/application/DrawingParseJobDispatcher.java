package com.company.iaf.qms.engineering.application;

import com.company.iaf.qms.engineering.domain.model.DrawingParseJob;
import com.company.iaf.qms.engineering.domain.model.DrawingRevision;
import com.company.iaf.qms.engineering.domain.model.QmsFileObject;
import com.company.iaf.qms.engineering.domain.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DrawingParseJobDispatcher {
    private static final long SYSTEM_ACTOR = 0L;
    private final DrawingParseJobRepository jobs;
    private final DrawingRevisionRepository revisions;
    private final QmsFileObjectRepository files;
    private final QmsObjectStorage storage;
    private final DrawingParseLifecycleService lifecycle;
    private final PdfParserPort parser;
    private final boolean enabled;

    public DrawingParseJobDispatcher(DrawingParseJobRepository jobs, DrawingRevisionRepository revisions,
            QmsFileObjectRepository files, QmsObjectStorage storage, DrawingParseLifecycleService lifecycle,
            PdfParserPort parser, @Value("${qms.parser.worker-enabled:false}") boolean enabled) {
        this.jobs = jobs; this.revisions = revisions; this.files = files; this.storage = storage;
        this.lifecycle = lifecycle; this.parser = parser; this.enabled = enabled;
    }

    @Scheduled(fixedDelayString = "${qms.parser.poll-delay-ms:3000}")
    public void poll() {
        if (!enabled) return;
        for (DrawingParseJob job : jobs.findQueued(3)) process(job);
    }

    void process(DrawingParseJob queued) {
        try {
            lifecycle.start(SYSTEM_ACTOR, queued.tenantId(), queued.orgId(), queued.id());
        } catch (RuntimeException alreadyClaimed) {
            return;
        }
        try {
            DrawingRevision revision = revisions.findById(queued.tenantId(), queued.orgId(), queued.revisionId())
                    .orElseThrow(() -> new IllegalStateException("Revision disappeared after claim"));
            QmsFileObject file = files.findById(queued.tenantId(), queued.orgId(), queued.fileId())
                    .orElseThrow(() -> new IllegalStateException("Source file disappeared after claim"));
            if (!"PDF".equals(queued.parserType())) throw new IllegalArgumentException("Unsupported parser type: " + queued.parserType());
            byte[] content;
            try (var input = storage.get(file.storageObjectKey())) { content = input.readAllBytes(); }
            var result = parser.parse(content, file.originalName(), "drawing-revision-" + revision.id(), revision.revisionCode());
            lifecycle.complete(SYSTEM_ACTOR, queued.tenantId(), queued.orgId(), queued.id(), result);
        } catch (Exception failure) {
            try {
                lifecycle.fail(SYSTEM_ACTOR, queued.tenantId(), queued.orgId(), queued.id(),
                        "PDF_PARSE_FAILED", message(failure));
            } catch (RuntimeException ignored) { }
        }
    }

    private static String message(Exception failure) {
        String value = failure.getMessage();
        return value == null || value.isBlank() ? failure.getClass().getSimpleName() : value;
    }
}
