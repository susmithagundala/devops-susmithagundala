ALTER TABLE evidence
ADD COLUMN evidence_type VARCHAR(30) NOT NULL DEFAULT 'OTHER';

ALTER TABLE evidence
ADD CONSTRAINT chk_evidence_type
CHECK (
    evidence_type IN (
        'REPAIR_INVOICE',
        'ACCIDENT_PHOTO',
        'POLICE_REPORT',
        'VEHICLE_DOCUMENT',
        'DRIVING_LICENCE',
        'MEDICAL_REPORT',
        'OTHER'
    )
);
