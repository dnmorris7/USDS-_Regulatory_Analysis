package com.usds.regulations.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.usds.regulations.entity.Regulation;
import com.usds.regulations.entity.RegulationRelationship;
import com.usds.regulations.repository.RegulationRepository;
import com.usds.regulations.service.AutoRelationshipService;
import com.usds.regulations.service.EcfrApiService;
import com.usds.regulations.service.MockDataService;
import com.usds.regulations.service.RegulationRelationshipService;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class eCFRDownloadController {
    
    private static final Logger logger = LoggerFactory.getLogger(eCFRDownloadController.class);
    
    @Autowired
    private EcfrApiService ecfrApiService;
    
    @Autowired
    private RegulationRepository regulationRepository;
    
    @Autowired
    private MockDataService mockDataService;
    
    @Autowired
    private RegulationRelationshipService relationshipService;
    
    @Autowired
    private AutoRelationshipService autoRelationshipService;
    

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
    
    /**
     * Check for recent changes/amendments for a specific CFR title
     * Endpoint: GET /api/eCFR_source-check-recent-changes/{title}
     */
    @GetMapping("/eCFR_source-check-recent-changes/{title}")
    public ResponseEntity<Map<String, Object>> checkRecentChanges(@PathVariable Integer title) {
        logger.info("Checking recent changes for CFR Title {}", title);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate title range
            if (title < 1 || title > 50) {
                response.put("success", false);
                response.put("error", "Invalid CFR title. Must be between 1 and 50.");
                return ResponseEntity.badRequest().body(response);
            }
            
            List<Map<String, Object>> recentChanges = ecfrApiService.getRecentChangesFromECFR(title);
            
            response.put("success", true);
            response.put("title", title);
            response.put("titleName", getTitleName(title));
            response.put("recentChanges", recentChanges);
            response.put("changeCount", recentChanges.size());
            response.put("checkedAt", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error checking recent changes for Title {}: {}", title, e.getMessage());
            response.put("success", false);
            response.put("title", title);
            response.put("error", "Failed to check recent changes: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Get overall changes summary across all CFR titles
     * Endpoint: GET /api/eCFR_source-check-all-changes
     */
    @GetMapping("/eCFR_source-check-all-changes")
    public ResponseEntity<Map<String, Object>> checkAllRecentChanges() {
        logger.info("Checking recent changes for ALL CFR titles");
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> changesSummary = ecfrApiService.getOverallChangesSummary();
            
            response.put("success", true);
            response.put("message", "Overall changes summary for all CFR titles");
            response.putAll(changesSummary);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error checking changes for all titles: {}", e.getMessage());
            response.put("success", false);
            response.put("error", "Failed to check overall changes: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Helper method to get title names (private)
     */
    private String getTitleName(Integer titleNumber) {
        return switch (titleNumber) {
            case 1 -> "General Provisions";
            case 2 -> "Federal Financial Assistance";
            case 3 -> "The President";
            case 4 -> "Accounts";
            case 5 -> "Administrative Personnel";
            case 6 -> "Domestic Security";
            case 7 -> "Agriculture";
            case 8 -> "Aliens and Nationality";
            case 9 -> "Animals and Animal Products";
            case 10 -> "Energy";
            case 11 -> "Federal Elections";
            case 12 -> "Banks and Banking";
            case 13 -> "Business Credit and Assistance";
            case 14 -> "Aeronautics and Space";
            case 15 -> "Commerce and Foreign Trade";
            case 16 -> "Commercial Practices";
            case 17 -> "Commodity and Securities Exchanges";
            case 18 -> "Conservation of Power and Water Resources";
            case 19 -> "Customs Duties";
            case 20 -> "Employees' Benefits";
            case 21 -> "Food and Drugs";
            case 22 -> "Foreign Relations";
            case 23 -> "Highways";
            case 24 -> "Housing and Urban Development";
            case 25 -> "Indians";
            case 26 -> "Internal Revenue";
            case 27 -> "Alcohol, Tobacco Products and Firearms";
            case 28 -> "Judicial Administration";
            case 29 -> "Labor";
            case 30 -> "Mineral Resources";
            case 31 -> "Money and Finance: Treasury";
            case 32 -> "National Defense";
            case 33 -> "Navigation and Navigable Waters";
            case 34 -> "Education";
            case 35 -> "Reserved";
            case 36 -> "Parks, Forests, and Public Property";
            case 37 -> "Patents, Trademarks, and Copyrights";
            case 38 -> "Pensions, Bounties, and Veterans' Relief";
            case 39 -> "Postal Service";
            case 40 -> "Protection of Environment";
            case 41 -> "Public Contracts and Property Management";
            case 42 -> "Public Health";
            case 43 -> "Public Lands: Interior";
            case 44 -> "Emergency Management and Assistance";
            case 45 -> "Public Welfare";
            case 46 -> "Shipping";
            case 47 -> "Telecommunication";
            case 48 -> "Federal Acquisition Regulations System";
            case 49 -> "Transportation";
            case 50 -> "Wildlife and Fisheries";
            default -> "Unknown Title " + titleNumber;
        };
    }
    
    /**
     * Generate mock data for testing - replaces problematic eCFR API calls
     * GET /api/generate-mock-data/{title}/{count}
     */
    @GetMapping("/generate-mock-data/{title}/{count}")
    public ResponseEntity<Map<String, Object>> generateMockData(
            @PathVariable Integer title, 
            @PathVariable Integer count) {
        
        logger.info("Generating {} mock regulations for Title {}", count, title);
        
        Map<String, Object> response = new HashMap<>();
        long startTime = System.currentTimeMillis();
        
        try {
            // Validate inputs
            if (title < 1 || title > 50) {
                response.put("success", false);
                response.put("error", "Title must be between 1 and 50");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (count < 1 || count > 100) {
                response.put("success", false);
                response.put("error", "Count must be between 1 and 100");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Generate mock data
            List<Regulation> mockRegulations = mockDataService.generateMockRegulations(title, count);
            
            // Save to database
            int created = 0, updated = 0;
            List<Regulation> savedRegulations = new ArrayList<>();
            
            for (Regulation mockRegulation : mockRegulations) {
                Optional<Regulation> existing = regulationRepository
                        .findByCfrTitleAndPartNumber(mockRegulation.getCfrTitle(), mockRegulation.getPartNumber());
                
                if (existing.isPresent()) {
                    // Update existing
                    Regulation existingReg = existing.get();
                    existingReg.setTitle(mockRegulation.getTitle());
                    existingReg.setContent(mockRegulation.getContent());
                    existingReg.setWordCount(mockRegulation.getWordCount());
                    existingReg.setContentChecksum(mockRegulation.getContentChecksum());
                    existingReg.setLatestAmendedOn(mockRegulation.getLatestAmendedOn());
                    existingReg.setLatestIssueDate(mockRegulation.getLatestIssueDate());
                    existingReg.setLastUpdatedOn(mockRegulation.getLastUpdatedOn());
                    existingReg.setAmendmentCount(mockRegulation.getAmendmentCount());
                    existingReg.setEcfrLastModified(mockRegulation.getEcfrLastModified());
                    existingReg.setSourceUrl(mockRegulation.getSourceUrl());
                    savedRegulations.add(regulationRepository.save(existingReg));
                    updated++;
                } else {
                    // Create new
                    savedRegulations.add(regulationRepository.save(mockRegulation));
                    created++;
                }
            }
            
            long endTime = System.currentTimeMillis();
            long durationSeconds = (endTime - startTime) / 1000;
            
            response.put("success", true);
            response.put("message", "Generated mock data for CFR Title " + title);
            response.put("title", title);
            response.put("titleName", getTitleName(title));
            response.put("created", created);
            response.put("updated", updated);
            response.put("totalRegulations", savedRegulations.size());
            response.put("totalWordCount", savedRegulations.stream().mapToInt(r -> r.getWordCount() != null ? r.getWordCount() : 0).sum());
            response.put("durationSeconds", durationSeconds);
            response.put("sampleTitles", savedRegulations.stream().limit(3).map(Regulation::getTitle).toList());
            
            logger.info("Generated mock data: {} created, {} updated for Title {}", created, updated, title);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error generating mock data for Title {}: {}", title, e.getMessage(), e);
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("durationSeconds", (System.currentTimeMillis() - startTime) / 1000);
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Generate mock data for all CFR titles (1-50) - bulk mock data generation
     * GET /api/generate-mock-data-all-titles/{count}
     */
    @GetMapping("/generate-mock-data-all-titles/{count}")
    public ResponseEntity<Map<String, Object>> generateMockDataAllTitles(@PathVariable Integer count) {
        logger.info("Generating {} mock regulations for each of ALL CFR titles (1-50)", count);
        
        Map<String, Object> response = new HashMap<>();
        long startTime = System.currentTimeMillis();
        
        try {
            // Validate input
            if (count < 1 || count > 100) {
                response.put("success", false);
                response.put("error", "Count must be between 1 and 100");
                return ResponseEntity.badRequest().body(response);
            }
            
            int totalCreated = 0, totalUpdated = 0;
            Map<Integer, Integer> titleResults = new HashMap<>();
            List<String> sampleTitles = new ArrayList<>();
            long totalWordCount = 0;
            
            // Generate mock data for each CFR title (1-50)
            for (int title = 1; title <= 50; title++) {
                try {
                    List<Regulation> mockRegulations = mockDataService.generateMockRegulations(title, count);
                    
                    int titleCreated = 0, titleUpdated = 0;
                    
                    for (Regulation mockRegulation : mockRegulations) {
                        Optional<Regulation> existing = regulationRepository
                                .findByCfrTitleAndPartNumber(mockRegulation.getCfrTitle(), mockRegulation.getPartNumber());
                        
                        if (existing.isPresent()) {
                            // Update existing
                            Regulation existingReg = existing.get();
                            existingReg.setTitle(mockRegulation.getTitle());
                            existingReg.setContent(mockRegulation.getContent());
                            existingReg.setWordCount(mockRegulation.getWordCount());
                            existingReg.setContentChecksum(mockRegulation.getContentChecksum());
                            existingReg.setLatestAmendedOn(mockRegulation.getLatestAmendedOn());
                            existingReg.setLatestIssueDate(mockRegulation.getLatestIssueDate());
                            existingReg.setLastUpdatedOn(mockRegulation.getLastUpdatedOn());
                            existingReg.setAmendmentCount(mockRegulation.getAmendmentCount());
                            existingReg.setEcfrLastModified(mockRegulation.getEcfrLastModified());
                            existingReg.setSourceUrl(mockRegulation.getSourceUrl());
                            Regulation saved = regulationRepository.save(existingReg);
                            totalWordCount += saved.getWordCount() != null ? saved.getWordCount() : 0;
                            titleUpdated++;
                        } else {
                            // Create new
                            Regulation saved = regulationRepository.save(mockRegulation);
                            totalWordCount += saved.getWordCount() != null ? saved.getWordCount() : 0;
                            titleCreated++;
                            
                            // Collect sample titles from first few regulations
                            if (sampleTitles.size() < 5) {
                                sampleTitles.add(saved.getTitle());
                            }
                        }
                    }
                    
                    totalCreated += titleCreated;
                    totalUpdated += titleUpdated;
                    titleResults.put(title, titleCreated + titleUpdated);
                    
                } catch (Exception e) {
                    logger.warn("Failed to generate mock data for Title {}: {}", title, e.getMessage());
                    titleResults.put(title, 0);
                }
            }
            
            long endTime = System.currentTimeMillis();
            long durationSeconds = (endTime - startTime) / 1000;
            
            response.put("success", true);
            response.put("message", String.format("Generated mock data for all CFR titles (%d regulations per title)", count));
            response.put("countPerTitle", count);
            response.put("totalTitles", 50);
            response.put("totalCreated", totalCreated);
            response.put("totalUpdated", totalUpdated);
            response.put("totalRegulations", totalCreated + totalUpdated);
            response.put("totalWordCount", totalWordCount);
            response.put("durationSeconds", durationSeconds);
            response.put("titleResults", titleResults);
            response.put("sampleTitles", sampleTitles);
            
            logger.info("Generated mock data for all titles: {} created, {} updated in {} seconds", 
                       totalCreated, totalUpdated, durationSeconds);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error generating mock data for all titles: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("durationSeconds", (System.currentTimeMillis() - startTime) / 1000);
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Analyze relationships for a specific regulation
     * GET /api/regulations/{id}/relationships
     */
    @GetMapping("/regulations/{id}/relationships")
    public ResponseEntity<Map<String, Object>> analyzeRegulationRelationships(@PathVariable Long id) {
        logger.info("Analyzing relationships for regulation ID: {}", id);
        
        try {
            Map<String, Object> analysis = relationshipService.analyzeRegulationRelationships(id);
            
            if (analysis.containsKey("error")) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(analysis);
            
        } catch (Exception e) {
            logger.error("Error analyzing relationships for regulation {}: {}", id, e.getMessage());
            Map<String, Object> error = Map.of("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Get conflict warnings for a specific regulation
     * GET /api/regulations/{id}/conflicts
     */
    @GetMapping("/regulations/{id}/conflicts")
    public ResponseEntity<Map<String, Object>> getConflictWarnings(@PathVariable Long id) {
        logger.info("Getting conflict warnings for regulation ID: {}", id);
        
        try {
            List<Map<String, Object>> warnings = relationshipService.getConflictWarnings(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("regulationId", id);
            response.put("conflicts", warnings);
            response.put("conflictCount", warnings.size());
            response.put("hasConflicts", !warnings.isEmpty());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting conflicts for regulation {}: {}", id, e.getMessage());
            Map<String, Object> error = Map.of("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Detect potential relationships within a CFR title
     * POST /api/detect-relationships/{title}
     */
    @PostMapping("/detect-relationships/{title}")
    public ResponseEntity<Map<String, Object>> detectRelationships(@PathVariable Integer title) {
        logger.info("Detecting relationships for CFR Title {}", title);
        
        Map<String, Object> response = new HashMap<>();
        long startTime = System.currentTimeMillis();
        
        try {
            List<RegulationRelationship> relationships = relationshipService.detectPotentialRelationships(title);
            
            // Save detected relationships
            int saved = 0;
            for (RegulationRelationship relationship : relationships) {
                relationshipService.saveRelationship(relationship);
                saved++;
            }
            
            long endTime = System.currentTimeMillis();
            
            response.put("success", true);
            response.put("title", title);
            response.put("relationshipsDetected", relationships.size());
            response.put("relationshipsSaved", saved);
            response.put("durationMs", endTime - startTime);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error detecting relationships for title {}: {}", title, e.getMessage());
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get system-wide conflict summary
     * GET /api/conflicts/summary
     */
    @GetMapping("/conflicts/summary")
    public ResponseEntity<Map<String, Object>> getConflictSummary() {
        logger.info("Getting system-wide conflict summary");
        
        try {
            Map<String, Object> summary = relationshipService.getConflictSummary();
            return ResponseEntity.ok(summary);
            
        } catch (Exception e) {
            logger.error("Error getting conflict summary: {}", e.getMessage());
            Map<String, Object> error = Map.of("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Generate mock data for all titles with automatic relationship detection
     * POST /api/generate-mock-data-all-titles-with-relationships/{count}
     */
    @PostMapping("/generate-mock-data-all-titles-with-relationships/{count}")
    public ResponseEntity<Map<String, Object>> generateMockDataAllTitlesWithRelationships(@PathVariable int count) {
        logger.info("Starting comprehensive mock data generation for all {} CFR titles with {} regulations each + automatic relationship detection", 50, count);
        
        try {
            // Step 1: Generate mock data for all 50 CFR titles
            List<Regulation> allSavedRegulations = new ArrayList<>();
            Map<Integer, Integer> titleResults = new HashMap<>();
            Map<String, Object> response = new HashMap<>();
            LocalDateTime startTime = LocalDateTime.now();
            
            for (int titleNumber = 1; titleNumber <= 50; titleNumber++) {
                try {
                    List<Regulation> mockRegulations = mockDataService.generateEnhancedMockRegulations(titleNumber, count);
                    List<Regulation> savedRegulations = regulationRepository.saveAll(mockRegulations);
                    allSavedRegulations.addAll(savedRegulations);
                    titleResults.put(titleNumber, savedRegulations.size());
                    
                    logger.info("Generated {} mock regulations for CFR Title {}", savedRegulations.size(), titleNumber);
                    
                } catch (Exception e) {
                    logger.error("Error generating mock data for Title {}: {}", titleNumber, e.getMessage());
                    titleResults.put(titleNumber, 0);
                }
            }
            
            LocalDateTime mockDataEndTime = LocalDateTime.now();
            logger.info("Mock data generation completed. Generated {} total regulations across all titles", allSavedRegulations.size());
            
            // Step 2: Auto-trigger relationship detection for all titles
            logger.info("Starting automatic relationship detection for all CFR titles...");
            Map<String, Object> relationshipResponse = autoRelationshipService.autoDetectRelationshipsForAllTitles();
            
            LocalDateTime endTime = LocalDateTime.now();
            
            // Extract relationship results from response
            @SuppressWarnings("unchecked")
            Map<Integer, Integer> relationshipResults = (Map<Integer, Integer>) relationshipResponse.get("titleResults");
            Integer totalRelationships = (Integer) relationshipResponse.get("totalRelationships");
            if (relationshipResults == null) relationshipResults = new HashMap<>();
            if (totalRelationships == null) totalRelationships = 0;
            
            // Build comprehensive response
            response.put("success", true);
            response.put("message", "Successfully generated mock data for all 50 CFR titles and detected relationships");
            response.put("startTime", startTime);
            response.put("mockDataEndTime", mockDataEndTime);
            response.put("endTime", endTime);
            response.put("totalRegulations", allSavedRegulations.size());
            response.put("totalRelationships", totalRelationships);
            response.put("titlesProcessed", 50);
            response.put("regulationsPerTitle", count);
            
            // Mock data statistics
            Map<String, Object> mockStats = new HashMap<>();
            mockStats.put("titleResults", titleResults);
            mockStats.put("totalWordCount", allSavedRegulations.stream()
                .mapToInt(r -> r.getWordCount() != null ? r.getWordCount() : 0).sum());
            mockStats.put("averageWordCount", allSavedRegulations.isEmpty() ? 0 : 
                allSavedRegulations.stream().mapToInt(r -> r.getWordCount() != null ? r.getWordCount() : 0).sum() / allSavedRegulations.size());
            response.put("mockDataStats", mockStats);
            
            // Relationship detection statistics
            Map<String, Object> relationshipStats = new HashMap<>();
            relationshipStats.put("titleResults", relationshipResults);
            relationshipStats.put("successfulTitles", relationshipResults.entrySet().stream()
                .mapToInt(entry -> entry.getValue() > 0 ? 1 : 0).sum());
            relationshipStats.put("averageRelationshipsPerTitle", relationshipResults.isEmpty() ? 0 :
                totalRelationships / relationshipResults.size());
            response.put("relationshipStats", relationshipStats);
            
            logger.info("✅ COMPLETE WORKFLOW SUCCESS: Generated {} regulations across 50 titles and detected {} relationships", 
                allSavedRegulations.size(), totalRelationships);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ ERROR in comprehensive mock data generation with relationships: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            error.put("timestamp", LocalDateTime.now());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Auto-detect relationships for all CFR titles (standalone)
     * POST /api/auto-detect-relationships-all-titles
     */
    @PostMapping("/auto-detect-relationships-all-titles")
    public ResponseEntity<Map<String, Object>> autoDetectRelationshipsAllTitles() {
        logger.info("Starting automatic relationship detection for all 50 CFR titles");
        
        try {
            Map<String, Object> autoResponse = autoRelationshipService.autoDetectRelationshipsForAllTitles();
            
            // Extract results from the response
            @SuppressWarnings("unchecked")
            Map<Integer, Integer> results = (Map<Integer, Integer>) autoResponse.get("titleResults");
            Integer totalRelationships = (Integer) autoResponse.get("totalRelationships");
            if (results == null) results = new HashMap<>();
            if (totalRelationships == null) totalRelationships = 0;
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Automatic relationship detection completed for all CFR titles");
            response.put("totalRelationships", totalRelationships);
            response.put("titlesProcessed", results.size());
            response.put("titleResults", results);
            response.put("timestamp", LocalDateTime.now());
            
            logger.info("Auto-detection completed: {} relationships found across {} titles", totalRelationships, results.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error in automatic relationship detection: {}", e.getMessage(), e);
            Map<String, Object> error = Map.of("error", e.getMessage(), "timestamp", LocalDateTime.now());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}