package com.company.iaf.qms.engineering.application;

import com.company.iaf.qms.engineering.domain.model.DrawingRevision;
import com.company.iaf.qms.engineering.domain.model.QmsFileObject;
import com.company.iaf.qms.engineering.domain.repository.DrawingRevisionRepository;
import com.company.iaf.qms.engineering.domain.repository.QmsAuditTrail;
import com.company.iaf.qms.engineering.domain.repository.QmsFileObjectRepository;
import com.company.iaf.qms.engineering.domain.repository.QmsObjectStorage;
import com.company.iaf.qms.engineering.domain.repository.DrawingParseJobRepository;
import com.company.iaf.platform.statemachine.application.DefaultStateMachineService;
import com.company.iaf.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import java.util.Optional;
import java.time.OffsetDateTime;
import java.security.MessageDigest;
import java.util.HexFormat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class DrawingFileApplicationServiceTest {
    private final DrawingRevisionRepository revisions = mock(DrawingRevisionRepository.class);
    private final QmsFileObjectRepository files = mock(QmsFileObjectRepository.class);
    private final QmsObjectStorage storage = mock(QmsObjectStorage.class);
    private final QmsAuditTrail audit = mock(QmsAuditTrail.class);
    private final DrawingParseJobRepository parseJobs = mock(DrawingParseJobRepository.class);
    private final DrawingFileApplicationService service = new DrawingFileApplicationService(
            revisions, files, storage, audit, parseJobs, new DefaultStateMachineService());

    @Test
    void rejectsUnsupportedExtensionBeforeObjectStorageWrite() {
        when(revisions.findById(1, 10, 5)).thenReturn(Optional.of(DrawingRevision.metadataDraft(1, 10, 3, "A", 1, null, null)));
        MockMultipartFile file = new MockMultipartFile("file", "drawing.exe", "application/octet-stream", new byte[]{1});
        assertThatThrownBy(() -> service.upload(1, 10, 5, file))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("PDF and DWG");
        verifyNoInteractions(storage, files, audit);
    }

    @Test
    void rejectsFileWhoseContentDoesNotMatchPdfExtension() {
        when(revisions.findById(1, 10, 5)).thenReturn(Optional.of(DrawingRevision.metadataDraft(1, 10, 3, "A", 1, null, null)));
        MockMultipartFile file = new MockMultipartFile("file", "drawing.pdf", "application/pdf", "not a pdf".getBytes());

        assertThatThrownBy(() -> service.upload(1, 10, 5, file))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("PDF and DWG");
        verifyNoInteractions(storage, files, audit);
    }

    @Test
    void storesValidPdfAndAttachesItsChecksumToRevision() throws Exception {
        byte[] content = "%PDF-1.7 smoke".getBytes();
        String checksum = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
        DrawingRevision revision = DrawingRevision.metadataDraft(1, 10, 3, "A", 1, null, null);
        when(revisions.findById(1, 10, 5)).thenReturn(Optional.of(revision));
        when(storage.bucket()).thenReturn("qms-files");
        when(files.insert(anyLong(), any())).thenReturn(9L);
        when(revisions.attachFile(anyLong(), eq(1L), eq(10L), eq(5L), eq(9L), eq("PDF"), eq(checksum), eq("UPLOADED"), eq(0)))
                .thenReturn(true);
        QmsFileObject stored = new QmsFileObject(9L, 1, 10, "drawing.pdf", "application/pdf", "pdf",
                content.length, checksum, "qms-files", "object-key", 0, OffsetDateTime.now());
        when(files.findById(1, 10, 9)).thenReturn(Optional.of(stored));

        var response = service.upload(1, 10, 5,
                new MockMultipartFile("file", "drawing.pdf", "application/pdf", content));

        assertThat(response.checksumSha256()).isEqualTo(checksum);
        verify(storage).put(any(), any(), eq((long) content.length), eq("application/pdf"));
        verify(parseJobs).enqueue(anyLong(), eq(1L), eq(10L), eq(5L), eq(9L), eq("PDF"), eq(1));
        verify(audit).record(eq(1L), anyLong(), eq("DRAWING_REVISION_FILE_UPLOADED"),
                eq("DrawingRevision"), eq(5L), any());
    }
}
