package com.usds.regulations.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.usds.regulations.entity.Regulation;
import com.usds.regulations.repository.RegulationRepository;

@Service
public class ChangeDetectionService {
    
    private static final Logger logger = LoggerFactory.getLogger(ChangeDetectionService.class);
    
    @Autowired
    private RegulationRepository regulationRepository;
    
    /**
     * Detect changes between existing and new regulation content
     */
    public ChangeDetectionResult detectChanges(Integer cfrTitle, String partNumber, 
                                             String newTitle, String newContent, String newAgencyName) {
        logger.debug("Detecting changes for CFR Title {} Part {}", cfrTitle, partNumber);
        
        Optional<Regulation> existingOpt = regulationRepository.findByCfrTitleAndPartNumber(cfrTitle, partNumber);
        
        if (existingOpt.isEmpty()) {
            // This is a new regulation
            return new ChangeDetectionResult(ChangeType.NEW, null, null, 
                "New regulation - no existing record found");
        }
        
        Regulation existing = existingOpt.get();
        String newChecksum = generateChecksum(newContent);
        
        ChangeDetectionResult result = new ChangeDetectionResult();
        result.setExistingChecksum(existing.getContentChecksum());
        result.setNewChecksum(newChecksum);
        
        // Check for content changes
        if (!newChecksum.equals(existing.getContentChecksum())) {
            result.setChangeType(ChangeType.CONTENT_CHANGED);
            result.setDescription("Content checksum changed");
            
            // Analyze specific changes
            result.setDetailedChanges(analyzeDetailedChanges(existing, newTitle, newContent, newAgencyName));
        } else if (!existing.getTitle().equals(newTitle)) {
            result.setChangeType(ChangeType.METADATA_CHANGED);
            result.setDescription("Title changed but content remained the same");
        } else if (!existing.getAgencyName().equals(newAgencyName)) {
            result.setChangeType(ChangeType.METADATA_CHANGED);
            result.setDescription("Agency name changed but content remained the same");
        } else {
            result.setChangeType(ChangeType.NO_CHANGE);
            result.setDescription("No changes detected");
        }
        
        return result;
    }
    
    /**
     * Analyze detailed changes between old and new regulation
     */
    private Map<String, Object> analyzeDetailedChanges(Regulation existing, String newTitle, 
                                                      String newContent, String newAgencyName) {
        Map<String, Object> changes = new HashMap<>();
        
        // Title changes
        if (!existing.getTitle().equals(newTitle)) {
            Map<String, String> titleChange = new HashMap<>();
            titleChange.put("old", existing.getTitle());
            titleChange.put("new", newTitle);
            changes.put("title", titleChange);
        }
        
        // Agency changes
        if (!existing.getAgencyName().equals(newAgencyName)) {
            Map<String, String> agencyChange = new HashMap<>();
            agencyChange.put("old", existing.getAgencyName());
            agencyChange.put("new", newAgencyName);
            changes.put("agency", agencyChange);
        }
        
        // Content changes
        if (!existing.getContent().equals(newContent)) {
            Map<String, Object> contentChange = new HashMap<>();
            contentChange.put("oldLength", existing.getContent() != null ? existing.getContent().length() : 0);
            contentChange.put("newLength", newContent != null ? newContent.length() : 0);
            contentChange.put("oldWordCount", existing.getWordCount());
            contentChange.put("newWordCount", calculateWordCount(newContent));
            
            // Calculate similarity percentage
            double similarity = calculateSimilarity(existing.getContent(), newContent);
            contentChange.put("similarityPercentage", similarity);
            
            changes.put("content", contentChange);
        }
        
        return changes;
    }
    
    /**
     * Get change history for a specific regulation
     */
    public List<ChangeHistoryEntry> getChangeHistory(Integer cfrTitle, String partNumber, int days) {
        logger.info("Getting change history for CFR Title {} Part {} over {} days", cfrTitle, partNumber, days);
        
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<Regulation> recentUpdates = regulationRepository.findRecentlyUpdated(since);
        
        List<ChangeHistoryEntry> history = new ArrayList<>();
        
        for (Regulation regulation : recentUpdates) {
            if (regulation.getCfrTitle().equals(cfrTitle) && regulation.getPartNumber().equals(partNumber)) {
                ChangeHistoryEntry entry = new ChangeHistoryEntry();
                entry.setRegulationId(regulation.getId());
                entry.setChangeDate(regulation.getUpdatedAt());
                entry.setChecksum(regulation.getContentChecksum());
                entry.setWordCount(regulation.getWordCount());
                entry.setChangeType(determineChangeType(regulation));
                history.add(entry);
            }
        }
        
        return history;
    }
    
    /**
     * Detect potential duplicates based on content similarity
     */
    public List<DuplicateDetectionResult> detectPotentialDuplicates(double similarityThreshold) {
        logger.info("Detecting potential duplicates with similarity threshold: {}", similarityThreshold);
        
        List<Regulation> allRegulations = regulationRepository.findAll();
        List<DuplicateDetectionResult> duplicates = new ArrayList<>();
        
        for (int i = 0; i < allRegulations.size(); i++) {
            for (int j = i + 1; j < allRegulations.size(); j++) {
                Regulation reg1 = allRegulations.get(i);
                Regulation reg2 = allRegulations.get(j);
                
                double similarity = calculateSimilarity(reg1.getContent(), reg2.getContent());
                
                if (similarity >= similarityThreshold) {
                    DuplicateDetectionResult duplicate = new DuplicateDetectionResult();
                    duplicate.setRegulation1(reg1);
                    duplicate.setRegulation2(reg2);
                    duplicate.setSimilarityScore(similarity);
                    duplicate.setReason(determineDuplicateReason(reg1, reg2, similarity));
                    duplicates.add(duplicate);
                }
            }
        }
        
        return duplicates;
    }
    
    /**
     * Generate bulk change report for all regulations
     */
    public Map<String, Object> generateBulkChangeReport(LocalDateTime since) {
        logger.info("Generating bulk change report since: {}", since);
        
        List<Regulation> recentlyUpdated = regulationRepository.findRecentlyUpdated(since);
        
        Map<String, Object> report = new HashMap<>();
        report.put("reportGeneratedAt", LocalDateTime.now());
        report.put("analysisPeriodStart", since);
        report.put("totalChanges", recentlyUpdated.size());
        
        // Group changes by agency
        Map<String, Integer> changesByAgency = new HashMap<>();
        Map<String, Integer> changesByTitle = new HashMap<>();
        
        for (Regulation regulation : recentlyUpdated) {
            String agency = regulation.getAgencyName();
            changesByAgency.put(agency, changesByAgency.getOrDefault(agency, 0) + 1);
            
            String title = "CFR Title " + regulation.getCfrTitle();
            changesByTitle.put(title, changesByTitle.getOrDefault(title, 0) + 1);
        }
        
        report.put("changesByAgency", changesByAgency);
        report.put("changesByTitle", changesByTitle);
        report.put("recentChanges", recentlyUpdated);
        
        return report;
    }
    
    // Helper methods and classes
    
    private String generateChecksum(String content) {
        if (content == null) content = "";
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(content.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return String.valueOf(content.hashCode());
        }
    }
    
    private Integer calculateWordCount(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        return text.trim().split("\\s+").length;
    }
    
    private double calculateSimilarity(String text1, String text2) {
        if (text1 == null) text1 = "";
        if (text2 == null) text2 = "";
        
        if (text1.equals(text2)) return 100.0;
        if (text1.isEmpty() && text2.isEmpty()) return 100.0;
        if (text1.isEmpty() || text2.isEmpty()) return 0.0;
        
        // Simple Jaccard similarity based on word sets
        String[] words1 = text1.toLowerCase().split("\\s+");
        String[] words2 = text2.toLowerCase().split("\\s+");
        
        java.util.Set<String> set1 = new java.util.HashSet<>(java.util.Arrays.asList(words1));
        java.util.Set<String> set2 = new java.util.HashSet<>(java.util.Arrays.asList(words2));
        
        java.util.Set<String> intersection = new java.util.HashSet<>(set1);
        intersection.retainAll(set2);
        
        java.util.Set<String> union = new java.util.HashSet<>(set1);
        union.addAll(set2);
        
        return union.size() > 0 ? (double) intersection.size() / union.size() * 100.0 : 0.0;
    }
    
    private String determineChangeType(Regulation regulation) {
        if (regulation.getCreatedAt() != null && regulation.getUpdatedAt() != null) {
            long diffSeconds = java.time.Duration.between(regulation.getCreatedAt(), regulation.getUpdatedAt()).getSeconds();
            if (diffSeconds < 60) {
                return "CREATED";
            }
        }
        return "UPDATED";
    }
    
    private String determineDuplicateReason(Regulation reg1, Regulation reg2, double similarity) {
        if (similarity >= 95.0) {
            return "Near identical content";
        } else if (similarity >= 80.0) {
            return "High content similarity";
        } else {
            return "Moderate content similarity";
        }
    }
    
    // Nested classes for structured responses
    
    public static class ChangeDetectionResult {
        private ChangeType changeType;
        private String existingChecksum;
        private String newChecksum;
        private String description;
        private Map<String, Object> detailedChanges;
        
        public ChangeDetectionResult() {}
        
        public ChangeDetectionResult(ChangeType changeType, String existingChecksum, 
                                   String newChecksum, String description) {
            this.changeType = changeType;
            this.existingChecksum = existingChecksum;
            this.newChecksum = newChecksum;
            this.description = description;
        }
        
        // Getters and setters
        public ChangeType getChangeType() { return changeType; }
        public void setChangeType(ChangeType changeType) { this.changeType = changeType; }
        
        public String getExistingChecksum() { return existingChecksum; }
        public void setExistingChecksum(String existingChecksum) { this.existingChecksum = existingChecksum; }
        
        public String getNewChecksum() { return newChecksum; }
        public void setNewChecksum(String newChecksum) { this.newChecksum = newChecksum; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public Map<String, Object> getDetailedChanges() { return detailedChanges; }
        public void setDetailedChanges(Map<String, Object> detailedChanges) { this.detailedChanges = detailedChanges; }
    }
    
    public static class ChangeHistoryEntry {
        private Long regulationId;
        private LocalDateTime changeDate;
        private String checksum;
        private Integer wordCount;
        private String changeType;
        
        // Getters and setters
        public Long getRegulationId() { return regulationId; }
        public void setRegulationId(Long regulationId) { this.regulationId = regulationId; }
        
        public LocalDateTime getChangeDate() { return changeDate; }
        public void setChangeDate(LocalDateTime changeDate) { this.changeDate = changeDate; }
        
        public String getChecksum() { return checksum; }
        public void setChecksum(String checksum) { this.checksum = checksum; }
        
        public Integer getWordCount() { return wordCount; }
        public void setWordCount(Integer wordCount) { this.wordCount = wordCount; }
        
        public String getChangeType() { return changeType; }
        public void setChangeType(String changeType) { this.changeType = changeType; }
    }
    
    public static class DuplicateDetectionResult {
        private Regulation regulation1;
        private Regulation regulation2;
        private double similarityScore;
        private String reason;
        
        // Getters and setters
        public Regulation getRegulation1() { return regulation1; }
        public void setRegulation1(Regulation regulation1) { this.regulation1 = regulation1; }
        
        public Regulation getRegulation2() { return regulation2; }
        public void setRegulation2(Regulation regulation2) { this.regulation2 = regulation2; }
        
        public double getSimilarityScore() { return similarityScore; }
        public void setSimilarityScore(double similarityScore) { this.similarityScore = similarityScore; }
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
    
    public enum ChangeType {
        NEW,
        NO_CHANGE,
        CONTENT_CHANGED,
        METADATA_CHANGED
    }
}