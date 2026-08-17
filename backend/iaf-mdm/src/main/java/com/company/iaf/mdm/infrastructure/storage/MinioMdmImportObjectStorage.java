package com.company.iaf.mdm.infrastructure.storage;

import com.company.iaf.mdm.application.MdmErrorCode;
import com.company.iaf.mdm.domain.repository.MdmImportObjectStorage;
import com.company.iaf.shared.exception.BusinessException;
import io.minio.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
@ConditionalOnProperty(name="iaf.mdm.enabled",havingValue="true",matchIfMissing=true)
public class MinioMdmImportObjectStorage implements MdmImportObjectStorage {
    private final MinioClient client; private final String bucket;
    public MinioMdmImportObjectStorage(@Value("${qms.storage.endpoint:http://minio:9000}")String endpoint,
                                      @Value("${qms.storage.access-key:jh-qms}")String accessKey,
                                      @Value("${qms.storage.secret-key:jh-qms-development-secret}")String secretKey,
                                      @Value("${qms.storage.bucket:jh-qms-files}")String bucket){this.client=MinioClient.builder().endpoint(endpoint).credentials(accessKey,secretKey).build();this.bucket=bucket;}
    public void put(String key,InputStream content,long size,String mediaType){try{if(!client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build()))client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());client.putObject(PutObjectArgs.builder().bucket(bucket).object(key).stream(content,size,-1).contentType(mediaType).build());}catch(Exception e){throw new BusinessException(MdmErrorCode.IMPORT_STORAGE_FAILED);}}
    public InputStream get(String key){try{return client.getObject(GetObjectArgs.builder().bucket(bucket).object(key).build());}catch(Exception e){throw new BusinessException(MdmErrorCode.IMPORT_STORAGE_FAILED);}}
    public void delete(String key){try{client.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(key).build());}catch(Exception ignored){}}
}
