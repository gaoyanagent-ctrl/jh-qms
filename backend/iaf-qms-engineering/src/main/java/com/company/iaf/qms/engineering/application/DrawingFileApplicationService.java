package com.company.iaf.qms.engineering.application;

import com.company.iaf.platform.core.security.RequiresPermission;
import com.company.iaf.platform.statemachine.application.StateMachineService;
import com.company.iaf.platform.statemachine.application.StateTransition;
import com.company.iaf.qms.engineering.domain.model.DrawingRevision;
import com.company.iaf.qms.engineering.domain.model.QmsFileObject;
import com.company.iaf.qms.engineering.domain.repository.DrawingRevisionRepository;
import com.company.iaf.qms.engineering.domain.repository.DrawingParseJobRepository;
import com.company.iaf.qms.engineering.domain.repository.QmsAuditTrail;
import com.company.iaf.qms.engineering.domain.repository.QmsFileObjectRepository;
import com.company.iaf.qms.engineering.domain.repository.QmsObjectStorage;
import com.company.iaf.qms.engineering.interfaces.dto.QmsFileResponse;
import com.company.iaf.shared.exception.BusinessException;
import com.company.iaf.shared.security.SecurityContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.DigestInputStream;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;
import java.util.List;
import com.company.iaf.qms.engineering.domain.model.DrawingRevisionStatus;

@Service
public class DrawingFileApplicationService {
    private static final long MAX_SIZE = 100L * 1024 * 1024;
    private final DrawingRevisionRepository revisions;
    private final QmsFileObjectRepository files;
    private final QmsObjectStorage storage;
    private final QmsAuditTrail audit;
    private final DrawingParseJobRepository parseJobs;
    private final StateMachineService stateMachine;
    private static final List<StateTransition<DrawingRevisionStatus>> TRANSITIONS = List.of(
            new StateTransition<>(DrawingRevisionStatus.DRAFT, "upload", DrawingRevisionStatus.UPLOADED));

    public DrawingFileApplicationService(DrawingRevisionRepository revisions, QmsFileObjectRepository files,
            QmsObjectStorage storage, QmsAuditTrail audit, DrawingParseJobRepository parseJobs,
            StateMachineService stateMachine) {
        this.revisions = revisions; this.files = files; this.storage = storage; this.audit = audit;
        this.parseJobs = parseJobs; this.stateMachine = stateMachine;
    }

    @RequiresPermission("qms:drawing-revision:upload")
    @Transactional
    public QmsFileResponse upload(long tenantId, long orgId, long revisionId, MultipartFile upload) {
        DrawingRevision revision = revision(tenantId, orgId, revisionId);
        if (upload == null || upload.isEmpty()) throw new BusinessException(QmsEngineeringErrorCode.FILE_REQUIRED);
        if (upload.getSize() > MAX_SIZE) throw new BusinessException(QmsEngineeringErrorCode.FILE_TOO_LARGE);
        String original = safeName(upload.getOriginalFilename());
        String extension = extension(original);
        if (!extension.equals("pdf") && !extension.equals("dwg"))
            throw new BusinessException(QmsEngineeringErrorCode.FILE_TYPE_UNSUPPORTED);
        Path temp = null;
        String key = null;
        try {
            temp = Files.createTempFile("jh-qms-upload-", "." + extension);
            upload.transferTo(temp);
            validateSignature(temp, extension);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (DigestInputStream input = new DigestInputStream(Files.newInputStream(temp), digest)) {
                input.transferTo(OutputStream.nullOutputStream());
            }
            String checksum = HexFormat.of().formatHex(digest.digest());
            if (revision.fileId() != null) {
                if (checksum.equals(revision.checksum())) throw new BusinessException(QmsEngineeringErrorCode.FILE_DUPLICATE);
                throw new BusinessException(QmsEngineeringErrorCode.FILE_ALREADY_ATTACHED);
            }
            key = tenantId + "/drawing-revisions/" + revisionId + "/" + UUID.randomUUID() + "." + extension;
            String mediaType = extension.equals("pdf") ? "application/pdf" : "image/vnd.dwg";
            try (InputStream input = Files.newInputStream(temp)) { storage.put(key, input, upload.getSize(), mediaType); }
            long actor = SecurityContext.getUserId().orElse(0L);
            QmsFileObject draft = new QmsFileObject(null, tenantId, orgId, original, mediaType, extension,
                    upload.getSize(), checksum, storage.bucket(), key, 0, null);
            long fileId = files.insert(actor, draft);
            DrawingRevisionStatus target;
            try { target = stateMachine.requireTransition(revision.status(), "upload", TRANSITIONS).to(); }
            catch (IllegalStateException e) { throw new BusinessException(QmsEngineeringErrorCode.REVISION_INVALID_STATE); }
            if (!revisions.attachFile(actor, tenantId, orgId, revisionId, fileId, extension.toUpperCase(Locale.ROOT), checksum, target.name(), revision.version()))
                throw new BusinessException(QmsEngineeringErrorCode.FILE_ALREADY_ATTACHED);
            parseJobs.enqueue(actor, tenantId, orgId, revisionId, fileId, extension.toUpperCase(Locale.ROOT), 1);
            QmsFileObject stored = files.findById(tenantId, orgId, fileId)
                    .orElseThrow(() -> new BusinessException(QmsEngineeringErrorCode.FILE_UPLOAD_FAILED));
            audit.record(tenantId, actor, "DRAWING_REVISION_FILE_UPLOADED", "DrawingRevision", revisionId, QmsFileResponse.from(stored));
            audit.record(tenantId, actor, "DRAWING_REVISION_STATE_TRANSITIONED", "DrawingRevision", revisionId,
                    new StateChange(revision.status().name(), "upload", target.name()));
            return QmsFileResponse.from(stored);
        } catch (BusinessException e) { if (key != null) storage.delete(key); throw e;
        } catch (Exception e) { if (key != null) storage.delete(key); throw new BusinessException(QmsEngineeringErrorCode.FILE_UPLOAD_FAILED);
        } finally { if (temp != null) try { Files.deleteIfExists(temp); } catch (Exception ignored) { } }
    }
    private record StateChange(String from, String action, String to) { }

    @RequiresPermission("qms:drawing-revision:view")
    @Transactional(readOnly = true)
    public QmsFileObject metadata(long tenantId, long orgId, long revisionId) {
        DrawingRevision revision = revision(tenantId, orgId, revisionId);
        if (revision.fileId() == null) throw new BusinessException(QmsEngineeringErrorCode.FILE_NOT_FOUND);
        return files.findById(tenantId, orgId, revision.fileId())
                .orElseThrow(() -> new BusinessException(QmsEngineeringErrorCode.FILE_NOT_FOUND));
    }

    @RequiresPermission("qms:drawing-revision:view")
    public InputStream content(long tenantId, long orgId, long revisionId) {
        return storage.get(metadata(tenantId, orgId, revisionId).storageObjectKey());
    }
    private DrawingRevision revision(long tenantId, long orgId, long id) { return revisions.findById(tenantId, orgId, id)
            .orElseThrow(() -> new BusinessException(QmsEngineeringErrorCode.REVISION_NOT_FOUND)); }
    private static void validateSignature(Path file, String extension) throws Exception {
        byte[] header = new byte[5];
        int length;
        try (InputStream input = Files.newInputStream(file)) {
            length = input.read(header);
        }
        boolean validPdf = extension.equals("pdf") && length == 5
                && header[0] == '%' && header[1] == 'P' && header[2] == 'D' && header[3] == 'F' && header[4] == '-';
        boolean validDwg = extension.equals("dwg") && length >= 4
                && header[0] == 'A' && header[1] == 'C' && header[2] == '1' && header[3] == '0';
        if (!validPdf && !validDwg) throw new BusinessException(QmsEngineeringErrorCode.FILE_TYPE_UNSUPPORTED);
    }
    private static String safeName(String name) {
        if (name == null || name.isBlank()) return "drawing";
        String normalized = name.replace('\\', '/');
        return normalized.substring(normalized.lastIndexOf('/') + 1);
    }
    private static String extension(String name) { int dot=name.lastIndexOf('.'); return dot < 0 ? "" : name.substring(dot+1).toLowerCase(Locale.ROOT); }
}
