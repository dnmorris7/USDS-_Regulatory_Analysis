package com.usds.regulations.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.usds.regulations.repository.RegulationRepository;
import com.usds.regulations.service.EcfrApiService;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class TestController {
    
    private static final Logger logger = LoggerFactory.getLogger(TestController.class);
    
    @Autowired
    private EcfrApiService ecfrApiService;
    
    @Autowired
    private RegulationRepository regulationRepository;
    
    /**
     * Test endpoint to trigger eCFR API download for Title 7
     */
    @GetMapping("/test-download")
    public ResponseEntity<Map<String, Object>> testDownload() {
        logger.info("Test download endpoint called");
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            long startTime = System.currentTimeMillis();
            String result = ecfrApiService.downloadTitle7Regulations();
            long endTime = System.currentTimeMillis();
            
            // Get some statistics
            long totalRegulations = regulationRepository.count();
            long title7Count = regulationRepository.countByTitle(7);
            Long totalWordCount = regulationRepository.getTotalWordCountByTitle(7);
            
            response.put("success", true);
            response.put("message", result);
            response.put("executionTimeMs", endTime - startTime);
            response.put("statistics", Map.of(
                "totalRegulationsInDatabase", totalRegulations,
                "title7RegulationsCount", title7Count,
                "title7TotalWordCount", totalWordCount != null ? totalWordCount : 0
            ));
            
            logger.info("Download completed successfully in {}ms", endTime - startTime);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Download failed: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("message", "Download failed: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
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
     * Get statistics about stored regulations
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            long totalRegulations = regulationRepository.count();
            long title7Count = regulationRepository.countByTitle(7);
            Long title7WordCount = regulationRepository.getTotalWordCountByTitle(7);
            Double title7AvgWordCount = regulationRepository.getAverageWordCountByTitle(7);
            
            stats.put("totalRegulations", totalRegulations);
            stats.put("title7", Map.of(
                "count", title7Count,
                "totalWordCount", title7WordCount != null ? title7WordCount : 0,
                "averageWordCount", title7AvgWordCount != null ? Math.round(title7AvgWordCount) : 0
            ));
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            logger.error("Error getting statistics: {}", e.getMessage(), e);
            stats.put("error", "Failed to retrieve statistics: " + e.getMessage());
            return ResponseEntity.internalServerError().body(stats);
        }
    }
    
    /**
     * List all Title 7 regulations (basic info only)
     */
    @GetMapping("/regulations/title7")
    public ResponseEntity<Map<String, Object>> getTitle7Regulations() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            var regulations = regulationRepository.findByCfrTitleOrderByWordCountDesc(7);
            
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
            response.put("count", regulations.size());
            response.put("regulations", regulationSummaries);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error retrieving Title 7 regulations: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}