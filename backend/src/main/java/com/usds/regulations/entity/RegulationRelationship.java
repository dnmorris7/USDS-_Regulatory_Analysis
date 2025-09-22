package com.usds.regulations.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "regulation_relationships", 
    indexes = {
        @Index(name = "idx_source_regulation", columnList = "source_regulation_id"),
        @Index(name = "idx_target_regulation", columnList = "target_regulation_id"),
        @Index(name = "idx_relationship_type", columnList = "relationship_type"),
        @Index(name = "idx_similarity_score", columnList = "similarity_score"),
        @Index(name = "idx_conflict_severity", columnList = "conflict_severity"),
        @Index(name = "idx_detected_at", columnList = "detected_at")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_source_target", columnNames = {"source_regulation_id", "target_regulation_id"})
    }
)
public class RegulationRelationship {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @NotNull
    @Column(name = "source_regulation_id", nullable = false)
    private Long sourceRegulationId;
    
    @NotNull
    @Column(name = "target_regulation_id", nullable = false)
    private Long targetRegulationId;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "relationship_type", nullable = false, length = 50)
    private RelationshipType relationshipType;
    
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "1.0")
    @Column(name = "similarity_score")
    private Double similarityScore;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "conflict_severity", length = 20)
    private ConflictSeverity conflictSeverity;
    
    @Column(name = "overlap_details", columnDefinition = "TEXT")
    private String overlapDetails;
    
    @Size(max = 100)
    @Column(name = "detected_by", length = 100)
    private String detectedBy;
    
    @CreationTimestamp
    @Column(name = "detected_at", nullable = false, updatable = false)
    private LocalDateTime detectedAt;
    
    @Column(name = "is_confirmed", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isConfirmed = false;
    
    @Column(name = "review_notes", columnDefinition = "TEXT")
    private String reviewNotes;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Version
    @Column(name = "version")
    private Long version;
    
    // Constructors
    public RegulationRelationship() {}
    
    public RegulationRelationship(Long sourceRegulationId, Long targetRegulationId, 
                                RelationshipType relationshipType, Double similarityScore) {
        this.sourceRegulationId = sourceRegulationId;
        this.targetRegulationId = targetRegulationId;
        this.relationshipType = relationshipType;
        this.similarityScore = similarityScore;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getSourceRegulationId() { return sourceRegulationId; }
    public void setSourceRegulationId(Long sourceRegulationId) { this.sourceRegulationId = sourceRegulationId; }
    
    public Long getTargetRegulationId() { return targetRegulationId; }
    public void setTargetRegulationId(Long targetRegulationId) { this.targetRegulationId = targetRegulationId; }
    
    public RelationshipType getRelationshipType() { return relationshipType; }
    public void setRelationshipType(RelationshipType relationshipType) { this.relationshipType = relationshipType; }
    
    public Double getSimilarityScore() { return similarityScore; }
    public void setSimilarityScore(Double similarityScore) { this.similarityScore = similarityScore; }
    
    public ConflictSeverity getConflictSeverity() { return conflictSeverity; }
    public void setConflictSeverity(ConflictSeverity conflictSeverity) { this.conflictSeverity = conflictSeverity; }
    
    public String getOverlapDetails() { return overlapDetails; }
    public void setOverlapDetails(String overlapDetails) { this.overlapDetails = overlapDetails; }
    
    public String getDetectedBy() { return detectedBy; }
    public void setDetectedBy(String detectedBy) { this.detectedBy = detectedBy; }
    
    public LocalDateTime getDetectedAt() { return detectedAt; }
    public void setDetectedAt(LocalDateTime detectedAt) { this.detectedAt = detectedAt; }
    
    public Boolean getIsConfirmed() { return isConfirmed; }
    public void setIsConfirmed(Boolean isConfirmed) { this.isConfirmed = isConfirmed; }
    
    public String getReviewNotes() { return reviewNotes; }
    public void setReviewNotes(String reviewNotes) { this.reviewNotes = reviewNotes; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}