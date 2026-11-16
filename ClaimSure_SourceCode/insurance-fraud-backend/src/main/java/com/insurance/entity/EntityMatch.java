package com.insurance.entity;
import com.insurance.enums.EntityMatchReviewStatus;
import com.insurance.enums.EntityMatchType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import com.fasterxml.jackson.databind.JsonNode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
@Entity
@Table(name = "entity_matches", uniqueConstraints = @UniqueConstraint(name = "uq_entity_match", columnNames = {
        "claim_id", "matched_claim_id", "match_type" }))
public class EntityMatch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "claim_id", nullable = false)
    private Claim claim;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "matched_claim_id", nullable = false)
    private Claim matchedClaim;
    @Enumerated(EnumType.STRING)
    @Column(name = "match_type", nullable = false, length = 50)
    private EntityMatchType matchType;
    @Column(name = "confidence_score", precision = 5, scale = 4)
    private BigDecimal confidenceScore;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "match_details", columnDefinition = "jsonb")
    private JsonNode matchDetails;
    @Enumerated(EnumType.STRING)
    @Column(name = "review_status", nullable = false, length = 20)
    private EntityMatchReviewStatus reviewStatus = EntityMatchReviewStatus.PENDING;
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
    public EntityMatch() {
    }
    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
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
    public Claim getMatchedClaim() {
        return matchedClaim;
    }
    public void setMatchedClaim(Claim v) {
        matchedClaim = v;
    }
    public EntityMatchType getMatchType() {
        return matchType;
    }
    public void setMatchType(EntityMatchType v) {
        matchType = v;
    }
    public BigDecimal getConfidenceScore() {
        return confidenceScore;
    }
    public void setConfidenceScore(BigDecimal v) {
        confidenceScore = v;
    }
    public JsonNode getMatchDetails() {
    return matchDetails;
    }

    public void setMatchDetails(JsonNode matchDetails) {
    this.matchDetails = matchDetails;
    }
    public EntityMatchReviewStatus getReviewStatus() {
        return reviewStatus;
    }
    public void setReviewStatus(EntityMatchReviewStatus v) {
        reviewStatus = v;
    }
}
