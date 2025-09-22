package com.usds.regulations.repository;

import com.usds.regulations.entity.RegulationRelationship;
import com.usds.regulations.entity.RelationshipType;
import com.usds.regulations.entity.ConflictSeverity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegulationRelationshipRepository extends JpaRepository<RegulationRelationship, Long> {
    
    // Find all relationships for a specific regulation
    @Query("SELECT rr FROM RegulationRelationship rr WHERE " +
           "rr.sourceRegulationId = :regulationId OR rr.targetRegulationId = :regulationId")
    List<RegulationRelationship> findByRegulationId(@Param("regulationId") Long regulationId);
    
    // Find conflicts for a specific regulation
    @Query("SELECT rr FROM RegulationRelationship rr WHERE " +
           "(rr.sourceRegulationId = :regulationId OR rr.targetRegulationId = :regulationId) " +
           "AND rr.relationshipType = 'CONFLICTING'")
    List<RegulationRelationship> findConflictsByRegulationId(@Param("regulationId") Long regulationId);
    
    // Find high severity conflicts
    @Query("SELECT rr FROM RegulationRelationship rr WHERE " +
           "rr.relationshipType = 'CONFLICTING' AND rr.conflictSeverity IN ('HIGH', 'CRITICAL')")
    List<RegulationRelationship> findHighSeverityConflicts();
    
    // Find unconfirmed relationships
    @Query("SELECT rr FROM RegulationRelationship rr WHERE rr.isConfirmed = false")
    List<RegulationRelationship> findUnconfirmedRelationships();
    
    // Find relationships by type
    List<RegulationRelationship> findByRelationshipType(RelationshipType relationshipType);
    
    // Find relationships by severity
    List<RegulationRelationship> findByConflictSeverity(ConflictSeverity conflictSeverity);
    
    // Check if relationship already exists (either direction)
    @Query("SELECT rr FROM RegulationRelationship rr WHERE " +
           "((rr.sourceRegulationId = :sourceId AND rr.targetRegulationId = :targetId) OR " +
           "(rr.sourceRegulationId = :targetId AND rr.targetRegulationId = :sourceId))")
    Optional<RegulationRelationship> findExistingRelationship(@Param("sourceId") Long sourceId, 
                                                             @Param("targetId") Long targetId);
    
    // Count conflicts by severity
    @Query("SELECT rr.conflictSeverity, COUNT(rr) FROM RegulationRelationship rr WHERE " +
           "rr.relationshipType = 'CONFLICTING' GROUP BY rr.conflictSeverity")
    List<Object[]> countConflictsBySeverity();
    
    // Find redundant regulations
    @Query("SELECT rr FROM RegulationRelationship rr WHERE " +
           "rr.relationshipType = 'REDUNDANT' AND rr.similarityScore > :threshold")
    List<RegulationRelationship> findRedundantRegulations(@Param("threshold") Double threshold);
}