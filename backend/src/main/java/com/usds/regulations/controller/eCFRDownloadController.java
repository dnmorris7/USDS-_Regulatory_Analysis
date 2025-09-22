package com.usds.regulations.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.usds.regulations.repository.RegulationRepository;
import com.usds.regulations.service.EcfrApiService;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class eCFRDownloadController {
    
    private static final Logger logger = LoggerFactory.getLogger(eCFRDownloadController.class);
    
    @Autowired
    private EcfrApiService ecfrApiService;
    
    @Autowired
    private RegulationRepository regulationRepository;
    

    /**
     * eCFR health status check
     */

     @GetMapping("/")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            // Check database connectivity
            long totalRegulations = regulationRepository.count();
            
            health.put("status", "UP");
            health.put("service", "USDS Regulations Analysis Backend");
            health.put("database", "Connected");
            health.put("totalRegulations", totalRegulations);
            health.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            health.put("status", "DOWN");
            health.put("service", "USDS Regulations Analysis Backend");
            health.put("database", "Error: " + e.getMessage());
            health.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(503).body(health);
        }
    }

    /**
     * eCFR source download endpoint for any CFR title
     */
    @GetMapping("/eCFR_source-download/{title}")
    public ResponseEntity<Map<String, Object>> eCfrSourceDownloadByTitle(@PathVariable Integer title) {
        logger.info("eCFR source download endpoint called for CFR Title {}", title);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate title range (CFR titles are typically 1-50)
            if (title < 1 || title > 50) {
                response.put("success", false);
                response.put("error", "Invalid CFR title. Must be between 1 and 50.");
                response.put("message", "CFR Title " + title + " is not valid");
                return ResponseEntity.badRequest().body(response);
            }
            
            long startTime = System.currentTimeMillis();
            String result;
            
            // For now, only Title 7 is implemented in the service
            // TODO: Extend EcfrApiService to handle any title
            if (title == 7) {
                result = ecfrApiService.downloadTitle7Regulations();
            } else {
                response.put("success", false);
                response.put("error", "CFR Title " + title + " download not yet implemented");
                response.put("message", "Currently only CFR Title 7 (Agriculture) is supported");
                response.put("supportedTitles", new int[]{7});
                return ResponseEntity.status(501).body(response);
            }
            
            long endTime = System.currentTimeMillis();
            
            // Get statistics for the requested title
            long totalRegulations = regulationRepository.count();
            long titleCount = regulationRepository.countByTitle(title);
            Long totalWordCount = regulationRepository.getTotalWordCountByTitle(title);
            Double avgWordCount = regulationRepository.getAverageWordCountByTitle(title);
            
            response.put("success", true);
            response.put("message", result);
            response.put("cfrTitle", title);
            response.put("executionTimeMs", endTime - startTime);
            response.put("statistics", Map.of(
                "totalRegulationsInDatabase", totalRegulations,
                "requestedTitleCount", titleCount,
                "requestedTitleTotalWordCount", totalWordCount != null ? totalWordCount : 0,
                "requestedTitleAverageWordCount", avgWordCount != null ? Math.round(avgWordCount) : 0
            ));
            
            logger.info("eCFR source download for Title {} completed successfully in {}ms", title, endTime - startTime);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("eCFR source download for Title {} failed: {}", title, e.getMessage(), e);
            
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("cfrTitle", title);
            response.put("message", "eCFR source download for Title " + title + " failed: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Get available CFR titles for download
     */
    @GetMapping("/eCFR_source-download/available-titles")
    public ResponseEntity<Map<String, Object>> getAvailableTitles() {
        logger.info("Available CFR titles endpoint called");
        
        Map<String, Object> response = new HashMap<>();
        
        // Currently supported titles (can be expanded as more are implemented)
        Map<Integer, String> supportedTitles = Map.of(
            7, "Agriculture"
            // TODO: Add more titles as they are implemented
            // 8, "Aliens and Nationality",
            // 9, "Animals and Animal Products",
            // 10, "Energy"
        );
        
        response.put("success", true);
        response.put("supportedTitles", supportedTitles);
        response.put("totalSupportedTitles", supportedTitles.size());
        response.put("message", "Currently supported CFR titles for download");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get detailed statistics for a specific CFR title
     */
    @GetMapping("/stats/{title}")
    public ResponseEntity<Map<String, Object>> getStatsByTitle(@PathVariable Integer title) {
        logger.info("Stats endpoint called for CFR Title {}", title);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate title range
            if (title < 1 || title > 50) {
                response.put("success", false);
                response.put("error", "Invalid CFR title. Must be between 1 and 50.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Get statistics for the requested title
            long titleCount = regulationRepository.countByTitle(title);
            Long totalWordCount = regulationRepository.getTotalWordCountByTitle(title);
            Double avgWordCount = regulationRepository.getAverageWordCountByTitle(title);
            
            // Get overall database stats for context
            long totalRegulations = regulationRepository.count();
            
            response.put("success", true);
            response.put("cfrTitle", title);
            response.put("statistics", Map.of(
                "regulationCount", titleCount,
                "totalWordCount", totalWordCount != null ? totalWordCount : 0,
                "averageWordCount", avgWordCount != null ? Math.round(avgWordCount) : 0,
                "hasData", titleCount > 0
            ));
            response.put("context", Map.of(
                "totalRegulationsInDatabase", totalRegulations,
                "percentageOfTotal", totalRegulations > 0 ? Math.round((titleCount * 100.0) / totalRegulations) : 0
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting statistics for Title {}: {}", title, e.getMessage(), e);
            response.put("success", false);
            response.put("cfrTitle", title);
            response.put("error", "Failed to retrieve statistics: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * List all regulations for a specific CFR title (basic info only)
     */
    @GetMapping("/regulations/title/{title}")
    public ResponseEntity<Map<String, Object>> getRegulationsByTitle(@PathVariable Integer title) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate title range
            if (title < 1 || title > 50) {
                response.put("success", false);
                response.put("error", "Invalid CFR title. Must be between 1 and 50.");
                return ResponseEntity.badRequest().body(response);
            }
            
            var regulations = regulationRepository.findByCfrTitleOrderByWordCountDesc(title);
            
            // Convert to simpler format for API response
            var regulationSummaries = regulations.stream()
                    .map(reg -> Map.of(
                        "id", reg.getId(),
                        "partNumber", reg.getPartNumber(),
                        "title", reg.getTitle(),
                        "agencyName", reg.getAgencyName(),
                        "wordCount", reg.getWordCount() != null ? reg.getWordCount() : 0,
                        "createdAt", reg.getCreatedAt() != null ? reg.getCreatedAt().toString() : null
                    ))
                    .toList();
            
            response.put("success", true);
            response.put("cfrTitle", title);
            response.put("count", regulations.size());
            response.put("regulations", regulationSummaries);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error retrieving CFR Title {} regulations: {}", title, e.getMessage(), e);
            response.put("success", false);
            response.put("cfrTitle", title);
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Get summary of all titles in the database
     */
    @GetMapping("/regulations/titles")
    public ResponseEntity<Map<String, Object>> getAllTitlesSummary() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            long totalRegulations = regulationRepository.count();
            
            // Get counts for each title that has data
            Map<Integer, Long> titleCounts = new HashMap<>();
            Map<Integer, Long> titleWordCounts = new HashMap<>();
            
            // Check titles 1-50 for any data
            for (int title = 1; title <= 50; title++) {
                long count = regulationRepository.countByTitle(title);
                if (count > 0) {
                    titleCounts.put(title, count);
                    Long wordCount = regulationRepository.getTotalWordCountByTitle(title);
                    titleWordCounts.put(title, wordCount != null ? wordCount : 0);
                }
            }
            
            response.put("success", true);
            response.put("totalRegulations", totalRegulations);
            response.put("titlesWithData", titleCounts.keySet().size());
            response.put("titleCounts", titleCounts);
            response.put("titleWordCounts", titleWordCounts);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error retrieving titles summary: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Download ALL CFR titles (1-50) with all available parts
     * Endpoint: GET /api/eCFR_source-download-all-titles
     */
    @GetMapping("/eCFR_source-download-all-titles")
    public ResponseEntity<Map<String, Object>> downloadAllTitles() {
        logger.info("Bulk download endpoint called for ALL CFR titles (1-50) - all available parts");
        
        Map<String, Object> response = new HashMap<>();
        long startTime = System.currentTimeMillis();
        
        try {
            // This will download all parts of all 50 titles
            Map<String, Object> result = ecfrApiService.downloadAllTitles();
            
            long endTime = System.currentTimeMillis();
            long durationSeconds = (endTime - startTime) / 1000;
            
            response.put("success", true);
            response.put("message", "Bulk download of all CFR titles completed (all parts)");
            response.put("durationSeconds", durationSeconds);
            response.putAll(result);
            
            logger.info("Bulk download for all titles completed successfully in {} seconds", durationSeconds);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error in bulk download for all titles: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("durationSeconds", (System.currentTimeMillis() - startTime) / 1000);
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Download ALL CFR titles (1-50) with sample size limit per title
     * Endpoint: GET /api/eCFR_source-download-all-titles/{sampleSize}
     */
    @GetMapping("/eCFR_source-download-all-titles/{sampleSize}")
    public ResponseEntity<Map<String, Object>> downloadAllTitlesWithSample(@PathVariable Integer sampleSize) {
        logger.info("Bulk download endpoint called for ALL CFR titles (1-50) - {} parts each", sampleSize);
        
        Map<String, Object> response = new HashMap<>();
        long startTime = System.currentTimeMillis();
        
        try {
            // Validate sampleSize
            if (sampleSize < 1 || sampleSize > 100) {
                response.put("success", false);
                response.put("error", "sampleSize must be between 1 and 100");
                return ResponseEntity.badRequest().body(response);
            }
            
            // This will download limited parts of all 50 titles
            Map<String, Object> result = ecfrApiService.downloadAllTitles(sampleSize);
            
            long endTime = System.currentTimeMillis();
            long durationSeconds = (endTime - startTime) / 1000;
            
            response.put("success", true);
            response.put("message", String.format("Bulk download of all CFR titles completed (%d parts each)", sampleSize));
            response.put("sampleSize", sampleSize);
            response.put("durationSeconds", durationSeconds);
            response.putAll(result);
            
            logger.info("Bulk download for all titles (sample size {}) completed successfully in {} seconds", sampleSize, durationSeconds);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error in bulk download for all titles with sample {}: {}", sampleSize, e.getMessage(), e);
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("durationSeconds", (System.currentTimeMillis() - startTime) / 1000);
            return ResponseEntity.internalServerError().body(response);
        }
    }
}