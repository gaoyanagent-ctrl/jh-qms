package com.company.iaf.qms.engineering.interfaces.dto;

import com.company.iaf.qms.engineering.domain.model.QmsFileObject;
import java.time.OffsetDateTime;

public record QmsFileResponse(long id, String originalName, String mediaType, String fileExtension,
                              long sizeBytes, String checksumSha256, OffsetDateTime createdAt) {
    public static QmsFileResponse from(QmsFileObject f) {
        return new QmsFileResponse(f.id(), f.originalName(), f.mediaType(), f.fileExtension(),
                f.sizeBytes(), f.checksumSha256(), f.createdAt());
    }
}

