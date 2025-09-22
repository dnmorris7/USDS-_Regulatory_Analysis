package com.usds.regulations.service;

import com.usds.regulations.entity.RegulationRelationship;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class AutoRelationshipService {
    
    private static final Logger logger = LoggerFactory.getLogger(AutoRelationshipService.class);
    
    @Autowired
    private RegulationRelationshipService relationshipService;
    
    /**
     * Auto-trigger relationship detection for all CFR titles with progress tracking
     */
    public Map<String, Object> autoDetectRelationshipsForAllTitles() {
        logger.info("Auto-detecting relationships for all CFR titles (1-50)");
        
        Map<String, Object> result = new HashMap<>();
        Map<Integer, Integer> titleResults = new HashMap<>();
        int totalRelationships = 0;
        int processedTitles = 0;
        int errorCount = 0;
        
        long startTime = System.currentTimeMillis();
        
        for (int title = 1; title <= 50; title++) {
            try {
                List<RegulationRelationship> relationships = relationshipService.detectPotentialRelationships(title);
                
                // Save detected relationships
                int savedCount = 0;
                for (RegulationRelationship relationship : relationships) {
                    relationshipService.saveRelationship(relationship);
                    savedCount++;
                }
                
                titleResults.put(title, savedCount);
                totalRelationships += savedCount;
                processedTitles++;
                
                if (savedCount > 0) {
                    logger.info("Detected {} relationships for Title {}", savedCount, title);
                }
                
            } catch (Exception e) {
                logger.error("Error detecting relationships for Title {}: {}", title, e.getMessage());
                titleResults.put(title, -1); // -1 indicates error
                errorCount++;
            }
        }
        
        long endTime = System.currentTimeMillis();
        long durationSeconds = (endTime - startTime) / 1000;
        
        result.put("success", true);
        result.put("totalTitlesProcessed", processedTitles);
        result.put("totalRelationshipsDetected", totalRelationships);
        result.put("errorCount", errorCount);
        result.put("durationSeconds", durationSeconds);
        result.put("titleResults", titleResults);
        result.put("autoTriggered", true);
        
        logger.info("Auto-relationship detection completed: {} relationships across {} titles in {} seconds", 
                   totalRelationships, processedTitles, durationSeconds);
        
        return result;
    }
    
    /**
     * Auto-trigger relationship detection for a specific title
     */
    public Map<String, Object> autoDetectRelationshipsForTitle(Integer title) {
        logger.info("Auto-detecting relationships for CFR Title {}", title);
        
        Map<String, Object> result = new HashMap<>();
        long startTime = System.currentTimeMillis();
        
        try {
            List<RegulationRelationship> relationships = relationshipService.detectPotentialRelationships(title);
            
            // Save detected relationships
            int savedCount = 0;
            for (RegulationRelationship relationship : relationships) {
                relationshipService.saveRelationship(relationship);
                savedCount++;
            }
            
            long endTime = System.currentTimeMillis();
            long durationMs = endTime - startTime;
            
            result.put("success", true);
            result.put("title", title);
            result.put("relationshipsDetected", savedCount);
            result.put("durationMs", durationMs);
            result.put("autoTriggered", true);
            
            logger.info("Auto-detected {} relationships for Title {} in {}ms", savedCount, title, durationMs);
            
        } catch (Exception e) {
            logger.error("Error auto-detecting relationships for Title {}: {}", title, e.getMessage());
            
            result.put("success", false);
            result.put("title", title);
            result.put("relationshipsDetected", 0);
            result.put("error", e.getMessage());
            result.put("autoTriggered", false);
        }
        
        return result;
    }
    
    /**
     * Auto-detect relationships for multiple specific titles
     */
    public Map<String, Object> autoDetectRelationshipsForTitles(List<Integer> titles) {
        logger.info("Auto-detecting relationships for {} specific titles", titles.size());
        
        Map<String, Object> result = new HashMap<>();
        Map<Integer, Integer> titleResults = new HashMap<>();
        int totalRelationships = 0;
        int processedTitles = 0;
        int errorCount = 0;
        
        long startTime = System.currentTimeMillis();
        
        for (Integer title : titles) {
            try {
                // Validate title range
                if (title < 1 || title > 50) {
                    logger.warn("Skipping invalid title: {}", title);
                    titleResults.put(title, -2); // -2 indicates invalid title
                    continue;
                }
                
                List<RegulationRelationship> relationships = relationshipService.detectPotentialRelationships(title);
                
                // Save detected relationships
                int savedCount = 0;
                for (RegulationRelationship relationship : relationships) {
                    relationshipService.saveRelationship(relationship);
                    savedCount++;
                }
                
                titleResults.put(title, savedCount);
                totalRelationships += savedCount;
                processedTitles++;
                
                if (savedCount > 0) {
                    logger.info("Detected {} relationships for Title {}", savedCount, title);
                }
                
            } catch (Exception e) {
                logger.error("Error detecting relationships for Title {}: {}", title, e.getMessage());
                titleResults.put(title, -1); // -1 indicates error
                errorCount++;
            }
        }
        
        long endTime = System.currentTimeMillis();
        long durationSeconds = (endTime - startTime) / 1000;
        
        result.put("success", true);
        result.put("requestedTitles", titles.size());
        result.put("processedTitles", processedTitles);
        result.put("totalRelationshipsDetected", totalRelationships);
        result.put("errorCount", errorCount);
        result.put("durationSeconds", durationSeconds);
        result.put("titleResults", titleResults);
        result.put("autoTriggered", true);
        
        logger.info("Auto-relationship detection completed for {} titles: {} relationships in {} seconds", 
                   processedTitles, totalRelationships, durationSeconds);
        
        return result;
    }
}