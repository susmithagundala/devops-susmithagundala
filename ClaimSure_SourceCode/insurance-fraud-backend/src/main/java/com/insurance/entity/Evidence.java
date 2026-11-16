package com.insurance.entity;
import com.insurance.enums.EvidenceType;
import com.insurance.enums.StorageProvider;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
@Entity
@Table(name = "evidence")
public class Evidence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "claim_id", nullable = false)
    private Claim claim;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uploaded_by_user_id", nullable = false)
    private AppUser uploadedBy;
    @Column(name = "file_name", nullable = false)
    private String fileName;
    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;
    @Column(name = "file_size", nullable = false)
    private Long fileSize;
    @Enumerated(EnumType.STRING)
    @Column(name = "storage_provider", nullable = false, length = 20)
    private StorageProvider storageProvider = StorageProvider.S3;
    @Column(name = "s3_bucket")
    private String s3Bucket;
    @Column(name = "s3_object_key", length = 1024)
    private String s3ObjectKey;
    @Column(name = "sha256_checksum", length = 64)
    private String sha256Checksum;
    private String description;
    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private OffsetDateTime uploadedAt;
    @Enumerated(EnumType.STRING)
    @Column(name = "evidence_type", nullable = false, length = 30)
    private EvidenceType evidenceType = EvidenceType.OTHER;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evidence_request_id")
    private EvidenceRequest evidenceRequest;
    public Evidence() {
    }
    @PrePersist
    protected void onCreate() {
        uploadedAt = OffsetDateTime.now();
    }
    public Long getId() {
        return id;
    }
    public Claim getClaim() {
        return claim;
    }
    public void setClaim(Claim v) {
        claim = v;
    }
    public AppUser getUploadedBy() {
        return uploadedBy;
    }
    public void setUploadedBy(AppUser v) {
        uploadedBy = v;
    }
    public String getFileName() {
        return fileName;
    }
    public void setFileName(String v) {
        fileName = v;
    }
    public String getContentType() {
        return contentType;
    }
    public void setContentType(String v) {
        contentType = v;
    }
    public Long getFileSize() {
        return fileSize;
    }
    public void setFileSize(Long v) {
        fileSize = v;
    }
    public StorageProvider getStorageProvider() {
        return storageProvider;
    }
    public void setStorageProvider(StorageProvider v) {
        storageProvider = v;
    }
    public String getS3Bucket() {
        return s3Bucket;
    }
    public void setS3Bucket(String v) {
        s3Bucket = v;
    }
    public String getS3ObjectKey() {
        return s3ObjectKey;
    }
    public void setS3ObjectKey(String v) {
        s3ObjectKey = v;
    }
    public String getSha256Checksum() {
        return sha256Checksum;
    }
    public void setSha256Checksum(String v) {
        sha256Checksum = v;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String v) {
        description = v;
    }
    public EvidenceType getEvidenceType() {
        return evidenceType;
    }
    public void setEvidenceType(EvidenceType v) {
        evidenceType = v;
    }
    public OffsetDateTime getUploadedAt() {
        return uploadedAt;
    }
    public EvidenceRequest getEvidenceRequest() {
        return evidenceRequest;
    }
    public void setEvidenceRequest(EvidenceRequest v) {
        evidenceRequest = v;
    }
}
