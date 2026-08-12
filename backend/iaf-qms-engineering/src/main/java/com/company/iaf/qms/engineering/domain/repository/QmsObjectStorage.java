package com.company.iaf.qms.engineering.domain.repository;

import java.io.InputStream;

public interface QmsObjectStorage {
    void put(String objectKey, InputStream content, long size, String mediaType);
    InputStream get(String objectKey);
    void delete(String objectKey);
    String bucket();
}

