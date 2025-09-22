package com.usds.regulations.service;

import com.usds.regulations.entity.*;
import com.usds.regulations.repository.RegulationRepository;
import com.usds.regulations.repository.RegulationRelationshipRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class RegulationRelationshipService {
    
    private static final Logger logger = LoggerFactory.getLogger(RegulationRelationshipService.class);
    
    @Autowired
    private RegulationRelationshipRepository relationshipRepository;
    
    @Autowired
    private RegulationRepository regulationRepository;
    
    /**
     * Analyze relationships for a specific regulation
     */
    public Map<String, Object> analyzeRegulationRelationships(Long regulationId) {
        logger.info("Analyzing relationships for regulation ID: {}", regulationId);
        
        Map<String, Object> analysis = new HashMap<>();
        
        Optional<Regulation> regulation = regulationRepository.findById(regulationId);
        if (regulation.isEmpty()) {
            analysis.put("error", "Regulation not found");
            return analysis;
        }
        
        List<RegulationRelationship> relationships = relationshipRepository.findByRegulationId(regulationId);
        List<RegulationRelationship> conflicts = relationshipRepository.findConflictsByRegulationId(regulationId);
        
        // Group relationships by type
        Map<RelationshipType, List<RegulationRelationship>> relationshipsByType = 
            relationships.stream().collect(Collectors.groupingBy(RegulationRelationship::getRelationshipType));
        
        // Analyze conflicts by severity
        Map<ConflictSeverity, List<RegulationRelationship>> conflictsBySeverity = 
            conflicts.stream()
                    .filter(r -> r.getConflictSeverity() != null)
                    .collect(Collectors.groupingBy(RegulationRelationship::getConflictSeverity));
        
        analysis.put("regulationId", regulationId);
        analysis.put("regulationTitle", regulation.get().getTitle());
        analysis.put("totalRelationships", relationships.size());
        analysis.put("totalConflicts", conflicts.size());
        analysis.put("relationshipsByType", relationshipsByType);
        analysis.put("conflictsBySeverity", conflictsBySeverity);
        analysis.put("hasHighSeverityConflicts", conflictsBySeverity.containsKey(ConflictSeverity.HIGH) || 
                                                conflictsBySeverity.containsKey(ConflictSeverity.CRITICAL));
        
        return analysis;
    }
    
    /**
     * Detect potential relationships between regulations (basic implementation)
     */
    public List<RegulationRelationship> detectPotentialRelationships(Integer titleNumber) {
        logger.info("Detecting potential relationships for CFR Title {}", titleNumber);
        
        List<Regulation> regulations = regulationRepository.findByCfrTitle(titleNumber);
        List<RegulationRelationship> detectedRelationships = new ArrayList<>();
        
        // Simple similarity-based detection (can be enhanced with NLP later)
        for (int i = 0; i < regulations.size(); i++) {
            for (int j = i + 1; j < regulations.size(); j++) {
                Regulation reg1 = regulations.get(i);
                Regulation reg2 = regulations.get(j);
                
                // Check if relationship already exists
                Optional<RegulationRelationship> existing = relationshipRepository
                    .findExistingRelationship(reg1.getId(), reg2.getId());
                
                if (existing.isEmpty()) {
                    RegulationRelationship relationship = analyzeRegulationPair(reg1, reg2);
                    if (relationship != null) {
                        detectedRelationships.add(relationship);
                    }
                }
            }
        }
        
        return detectedRelationships;
    }
    
    /**
     * Analyze a pair of regulations for potential relationships
     */
    private RegulationRelationship analyzeRegulationPair(Regulation reg1, Regulation reg2) {
        // Basic analysis - can be enhanced with NLP
        double similarity = calculateBasicSimilarity(reg1, reg2);
        
        if (similarity > 0.7) {
            // High similarity suggests redundancy
            RegulationRelationship relationship = new RegulationRelationship(
                reg1.getId(), reg2.getId(), RelationshipType.REDUNDANT, similarity);
            relationship.setDetectedBy("BASIC_SIMILARITY_ANALYSIS");
            relationship.setOverlapDetails(String.format(
                "High content similarity detected (%.2f). Regulations may be redundant.", similarity));
            
            return relationship;
        } else if (similarity > 0.3 && detectPotentialConflict(reg1, reg2)) {
            // Medium similarity with conflicting keywords
            RegulationRelationship relationship = new RegulationRelationship(
                reg1.getId(), reg2.getId(), RelationshipType.CONFLICTING, similarity);
            relationship.setConflictSeverity(ConflictSeverity.MEDIUM);
            relationship.setDetectedBy("KEYWORD_CONFLICT_ANALYSIS");
            relationship.setOverlapDetails("Potential conflict detected based on overlapping scope with conflicting requirements.");
            
            return relationship;
        }
        
        return null;
    }
    
    /**
     * Calculate basic similarity between two regulations
     */
    private double calculateBasicSimilarity(Regulation reg1, Regulation reg2) {
        if (reg1.getContent() == null || reg2.getContent() == null) {
            return 0.0;
        }
        
        // Simple word-based similarity (can be enhanced with TF-IDF, embeddings, etc.)
        Set<String> words1 = getWordSet(reg1.getContent());
        Set<String> words2 = getWordSet(reg2.getContent());
        
        Set<String> intersection = new HashSet<>(words1);
        intersection.retainAll(words2);
        
        Set<String> union = new HashSet<>(words1);
        union.addAll(words2);
        
        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }
    
    private Set<String> getWordSet(String content) {
        return Arrays.stream(content.toLowerCase().split("\\W+"))
                    .filter(word -> word.length() > 3) // Filter short words
                    .collect(Collectors.toSet());
    }
    
    /**
     * Detect potential conflicts using basic keyword analysis
     */
    private boolean detectPotentialConflict(Regulation reg1, Regulation reg2) {
        // Basic conflict detection - look for contradictory terms
        String content1 = reg1.getContent().toLowerCase();
        String content2 = reg2.getContent().toLowerCase();
        
        String[] conflictKeywords = {"shall not", "prohibited", "forbidden", "must not", "except", "unless"};
        String[] requirementKeywords = {"shall", "must", "required", "mandatory"};
        
        boolean hasConflictTerms1 = Arrays.stream(conflictKeywords).anyMatch(content1::contains);
        boolean hasConflictTerms2 = Arrays.stream(conflictKeywords).anyMatch(content2::contains);
        boolean hasRequirements1 = Arrays.stream(requirementKeywords).anyMatch(content1::contains);
        boolean hasRequirements2 = Arrays.stream(requirementKeywords).anyMatch(content2::contains);
        
        // Potential conflict if one has prohibitions and another has requirements in similar domain
        return (hasConflictTerms1 && hasRequirements2) || (hasConflictTerms2 && hasRequirements1);
    }
    
    /**
     * Get conflict warnings for a regulation
     */
    public List<Map<String, Object>> getConflictWarnings(Long regulationId) {
        List<RegulationRelationship> conflicts = relationshipRepository.findConflictsByRegulationId(regulationId);
        List<Map<String, Object>> warnings = new ArrayList<>();
        
        for (RegulationRelationship conflict : conflicts) {
            Map<String, Object> warning = new HashMap<>();
            
            Long otherRegulationId = conflict.getSourceRegulationId().equals(regulationId) 
                ? conflict.getTargetRegulationId() 
                : conflict.getSourceRegulationId();
            
            Optional<Regulation> otherRegulation = regulationRepository.findById(otherRegulationId);
            
            warning.put("conflictId", conflict.getId());
            warning.put("conflictingRegulationId", otherRegulationId);
            warning.put("conflictingRegulationTitle", otherRegulation.map(Regulation::getTitle).orElse("Unknown"));
            warning.put("severity", conflict.getConflictSeverity());
            warning.put("similarityScore", conflict.getSimilarityScore());
            warning.put("details", conflict.getOverlapDetails());
            warning.put("isConfirmed", conflict.getIsConfirmed());
            warning.put("detectedAt", conflict.getDetectedAt());
            
            warnings.add(warning);
        }
        
        return warnings;
    }
    
    /**
     * Save a detected relationship
     */
    public RegulationRelationship saveRelationship(RegulationRelationship relationship) {
        return relationshipRepository.save(relationship);
    }
    
    /**
     * Get system-wide conflict summary
     */
    public Map<String, Object> getConflictSummary() {
        List<Object[]> conflictCounts = relationshipRepository.countConflictsBySeverity();
        Map<String, Object> summary = new HashMap<>();
        
        for (Object[] row : conflictCounts) {
            ConflictSeverity severity = (ConflictSeverity) row[0];
            Long count = (Long) row[1];
            summary.put(severity.toString().toLowerCase() + "Conflicts", count);
        }
        
        List<RegulationRelationship> unconfirmed = relationshipRepository.findUnconfirmedRelationships();
        summary.put("unconfirmedRelationships", unconfirmed.size());
        
        return summary;
    }
}