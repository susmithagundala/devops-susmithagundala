-- Allow evidence records without an attached file.
ALTER TABLE evidence
    ALTER COLUMN file_name DROP NOT NULL,
    ALTER COLUMN content_type DROP NOT NULL,
    ALTER COLUMN file_size DROP NOT NULL;

-- Permit NULL file sizes while still rejecting zero or negative sizes.
ALTER TABLE evidence
    DROP CONSTRAINT IF EXISTS chk_evidence_file_size;

ALTER TABLE evidence
    ADD CONSTRAINT chk_evidence_file_size
    CHECK (file_size IS NULL OR file_size > 0);

-- Add METADATA_ONLY as a supported storage provider.
ALTER TABLE evidence
    DROP CONSTRAINT IF EXISTS chk_evidence_storage_provider;
ALTER TABLE evidence
    ADD CONSTRAINT chk_evidence_storage_provider
    CHECK (storage_provider IN ('S3', 'LOCAL', 'METADATA_ONLY'));
