package com.insurance.entity;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
@Entity
@Table(name = "investigation_notes")
public class InvestigationNote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "investigation_id", nullable = false)
    private Investigation investigation;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_user_id", nullable = false)
    private AppUser author;
    @Column(nullable = false, columnDefinition = "text")
    private String note;
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
    public InvestigationNote() {
    }
    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
    }
    public Long getId() {
        return id;
    }
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
    public Investigation getInvestigation() {
        return investigation;
    }
    public void setInvestigation(Investigation v) {
        investigation = v;
    }
    public AppUser getAuthor() {
        return author;
    }
    public void setAuthor(AppUser v) {
        author = v;
    }
    public String getNote() {
        return note;
    }
    public void setNote(String v) {
        note = v;
    }
}
