package com.insurance.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;

@Service
public class S3StorageService {

    private final S3Client s3Client;
    private final String bucket;
    private final boolean enabled;

    public S3StorageService(
            S3Client s3Client,
            @Value("${app.aws.s3.bucket:}") String bucket,
            @Value("${app.storage.s3.enabled:false}") boolean enabled
    ) {
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.enabled = enabled;
    }

    private void ensureS3Enabled() {
        if (!enabled) {
            throw new IllegalStateException(
                    "S3 storage is disabled in this environment"
            );
        }

        if (bucket == null || bucket.isBlank()) {
            throw new IllegalStateException(
                    "AWS S3 bucket is not configured"
            );
        }
    }

    public void upload(
            String objectKey,
            InputStream inputStream,
            long contentLength,
            String contentType
    ) throws IOException {

        ensureS3Enabled();

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .contentType(contentType)
                .serverSideEncryption("AES256")
                .build();

        s3Client.putObject(
                request,
                RequestBody.fromInputStream(inputStream, contentLength)
        );
    }

    public ResponseInputStream<GetObjectResponse> download(
            String objectKey
    ) {
        ensureS3Enabled();

        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .build();

        return s3Client.getObject(request);
    }

    public void delete(String objectKey) {
        ensureS3Enabled();

        s3Client.deleteObject(builder ->
                builder.bucket(bucket).key(objectKey)
        );
    }

    public String getBucket() {
        return bucket;
    }
}
