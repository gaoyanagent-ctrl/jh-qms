package com.company.iaf.mdm.domain.repository;

import java.io.InputStream;

public interface MdmImportObjectStorage {
    void put(String objectKey, InputStream content, long size, String mediaType);
    InputStream get(String objectKey);
    void delete(String objectKey);
}
