package com.company.iaf.qms.engineering.infrastructure.storage;

import com.company.iaf.qms.engineering.application.QmsEngineeringErrorCode;
import com.company.iaf.qms.engineering.domain.repository.QmsObjectStorage;
import com.company.iaf.shared.exception.BusinessException;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.InputStream;

@Component
public class MinioQmsObjectStorage implements QmsObjectStorage {
    private final MinioClient client;
    private final String bucket;

    public MinioQmsObjectStorage(@Value("${qms.storage.endpoint:http://minio:9000}") String endpoint,
            @Value("${qms.storage.access-key:jh-qms}") String accessKey,
            @Value("${qms.storage.secret-key:jh-qms-development-secret}") String secretKey,
            @Value("${qms.storage.bucket:jh-qms-files}") String bucket) {
        this.client = MinioClient.builder().endpoint(endpoint).credentials(accessKey, secretKey).build();
        this.bucket = bucket;
    }

    public void put(String key, InputStream content, long size, String mediaType) {
        try {
            if (!client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
                client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }
            client.putObject(PutObjectArgs.builder().bucket(bucket).object(key)
                    .stream(content, size, -1).contentType(mediaType).build());
        } catch (Exception e) { throw new BusinessException(QmsEngineeringErrorCode.FILE_STORAGE_FAILED); }
    }

    public InputStream get(String key) {
        try { return client.getObject(GetObjectArgs.builder().bucket(bucket).object(key).build()); }
        catch (Exception e) { throw new BusinessException(QmsEngineeringErrorCode.FILE_STORAGE_FAILED); }
    }

    public void delete(String key) {
        try { client.removeObject(io.minio.RemoveObjectArgs.builder().bucket(bucket).object(key).build()); }
        catch (Exception ignored) { }
    }
    public String bucket() { return bucket; }
}

