package com.usds.regulations.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.usds.regulations.dto.AnalyticsResponse;
import com.usds.regulations.entity.Regulation;
import com.usds.regulations.service.AnalyticsService;
import com.usds.regulations.service.ChangeDetectionService;
import com.usds.regulations.service.ChangeDetectionService.ChangeDetectionResult;
import com.usds.regulations.service.ChangeDetectionService.ChangeHistoryEntry;
import com.usds.regulations.service.ChangeDetectionService.DuplicateDetectionResult;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*")
public class AnalyticsController {
    
    private static final Logger logger = LoggerFactory.getLogger(AnalyticsController.class);
    
    @Autowired
    private AnalyticsService analyticsService;
    
    @Autowired
    private ChangeDetectionService changeDetectionService;
    
    /**
     * Get comprehensive analytics report
     */
    @GetMapping("/report")
    public ResponseEntity<Map<String, Object>> getAnalyticsReport() {
        logger.info("Analytics report endpoint called");
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            long startTime = System.currentTimeMillis();
            AnalyticsResponse analyticsReport = analyticsService.generateAnalyticsReport();
            long endTime = System.currentTimeMillis();
            
            response.put("success", true);
            response.put("analytics", analyticsReport);
            response.put("generationTimeMs", endTime - startTime);
            
            logger.info("Analytics report generated successfully in {}ms", endTime - startTime);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error generating analytics report: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("message", "Failed to generate analytics report: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Get analytics for a specific agency
     */
    @GetMapping("/agency/{agencyName}")
    public ResponseEntity<Map<String, Object>> getAgencyAnalytics(@PathVariable String agencyName) {
        logger.info("Agency analytics endpoint called for: {}", agencyName);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            AnalyticsResponse agencyAnalytics = analyticsService.getAgencyAnalytics(agencyName);
            
            response.put("success", true);
            response.put("agencyName", agencyName);
            response.put("analytics", agencyAnalytics);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error generating agency analytics for {}: {}", agencyName, e.getMessage(), e);
            
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("agencyName", agencyName);
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Get word count distribution across agencies
     */
    @GetMapping("/word-count-distribution")
    public ResponseEntity<Map<String, Object>> getWordCountDistribution() {
        logger.info("Word count distribution endpoint called");
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> distribution = analyticsService.getWordCountDistribution();
            
            response.put("success", true);
            response.put("distribution", distribution);
            response.put("generatedAt", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting word count distribution: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Get top regulations by word count
     */
    @GetMapping("/top-regulations")
    public ResponseEntity<Map<String, Object>> getTopRegulations(
            @RequestParam(defaultValue = "10") int limit) {
        logger.info("Top regulations endpoint called with limit: {}", limit);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Regulation> topRegulations = analyticsService.getTopRegulationsByWordCount(limit);
            
            response.put("success", true);
            response.put("limit", limit);
            response.put("count", topRegulations.size());
            response.put("regulations", topRegulations);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting top regulations: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Detect changes for a specific regulation
     */
    @PostMapping("/detect-changes")
    public ResponseEntity<Map<String, Object>> detectChanges(@RequestBody ChangeDetectionRequest request) {
        logger.info("Change detection endpoint called for CFR Title {} Part {}", 
                   request.getCfrTitle(), request.getPartNumber());
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            ChangeDetectionResult result = changeDetectionService.detectChanges(
                request.getCfrTitle(),
                request.getPartNumber(),
                request.getTitle(),
                request.getContent(),
                request.getAgencyName()
            );
            
            response.put("success", true);
            response.put("changeDetection", result);
            response.put("analyzedAt", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error detecting changes: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Get change history for a regulation
     */
    @GetMapping("/change-history")
    public ResponseEntity<Map<String, Object>> getChangeHistory(
            @RequestParam Integer cfrTitle,
            @RequestParam String partNumber,
            @RequestParam(defaultValue = "30") int days) {
        logger.info("Change history endpoint called for CFR Title {} Part {} over {} days", 
                   cfrTitle, partNumber, days);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<ChangeHistoryEntry> history = changeDetectionService.getChangeHistory(cfrTitle, partNumber, days);
            
            response.put("success", true);
            response.put("cfrTitle", cfrTitle);
            response.put("partNumber", partNumber);
            response.put("days", days);
            response.put("changeCount", history.size());
            response.put("history", history);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting change history: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Detect potential duplicate regulations
     */
    @GetMapping("/detect-duplicates")
    public ResponseEntity<Map<String, Object>> detectDuplicates(
            @RequestParam(defaultValue = "0.8") double similarityThreshold) {
        logger.info("Duplicate detection endpoint called with threshold: {}", similarityThreshold);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            long startTime = System.currentTimeMillis();
            List<DuplicateDetectionResult> duplicates = changeDetectionService.detectPotentialDuplicates(similarityThreshold);
            long endTime = System.currentTimeMillis();
            
            response.put("success", true);
            response.put("similarityThreshold", similarityThreshold);
            response.put("duplicatesFound", duplicates.size());
            response.put("duplicates", duplicates);
            response.put("analysisTimeMs", endTime - startTime);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error detecting duplicates: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Generate bulk change report
     */
    @GetMapping("/bulk-change-report")
    public ResponseEntity<Map<String, Object>> getBulkChangeReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since) {
        logger.info("Bulk change report endpoint called since: {}", since);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> report = changeDetectionService.generateBulkChangeReport(since);
            
            response.put("success", true);
            response.put("report", report);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error generating bulk change report: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Get analytics summary (lightweight version)
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getAnalyticsSummary() {
        logger.info("Analytics summary endpoint called");
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            AnalyticsResponse fullReport = analyticsService.generateAnalyticsReport();
            
            // Create a lightweight summary
            Map<String, Object> summary = new HashMap<>();
            summary.put("overallStats", fullReport.getOverallStats());
            summary.put("agencyCount", fullReport.getAgencyStats() != null ? fullReport.getAgencyStats().size() : 0);
            summary.put("titleCount", fullReport.getTitleStats() != null ? fullReport.getTitleStats().size() : 0);
            summary.put("recentChangesCount", fullReport.getRecentChanges() != null ? fullReport.getRecentChanges().size() : 0);
            summary.put("generatedAt", fullReport.getGeneratedAt());
            
            response.put("success", true);
            response.put("summary", summary);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error generating analytics summary: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    // DTO for change detection request
    public static class ChangeDetectionRequest {
        private Integer cfrTitle;
        private String partNumber;
        private String title;
        private String content;
        private String agencyName;
        
        // Getters and setters
        public Integer getCfrTitle() { return cfrTitle; }
        public void setCfrTitle(Integer cfrTitle) { this.cfrTitle = cfrTitle; }
        
        public String getPartNumber() { return partNumber; }
        public void setPartNumber(String partNumber) { this.partNumber = partNumber; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public String getAgencyName() { return agencyName; }
        public void setAgencyName(String agencyName) { this.agencyName = agencyName; }
    }
    
    /**
     * Debug endpoint to test analytics components individually
     */
    @GetMapping("/debug")
    public ResponseEntity<Map<String, Object>> debugAnalytics() {
        logger.info("Debug analytics endpoint called");
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            response.put("success", true);
            response.put("message", "Analytics service is accessible");
            
            // Test basic repository access
            long count = analyticsService.getTopRegulationsByWordCount(1).size();
            response.put("repositoryTest", "OK - found " + count + " regulations");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Debug analytics failed: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
}