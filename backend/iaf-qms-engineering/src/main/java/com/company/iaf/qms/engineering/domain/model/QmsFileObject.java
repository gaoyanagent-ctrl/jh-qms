package com.company.iaf.qms.engineering.domain.model;

import java.time.OffsetDateTime;

public record QmsFileObject(Long id, long tenantId, long orgId, String originalName,
                            String mediaType, String fileExtension, long sizeBytes,
                            String checksumSha256, String storageBucket, String storageObjectKey,
                            int version, OffsetDateTime createdAt) {
}

