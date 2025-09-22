package com.usds.regulations.service;

import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.usds.regulations.entity.Regulation;
import com.usds.regulations.repository.RegulationRepository;

@Service
public class EcfrApiService {
    
    private static final Logger logger = LoggerFactory.getLogger(EcfrApiService.class);
    private static final String ECFR_BASE_URL = "https://ecfr.federalregister.gov/api";
    
    @Autowired
    private RegulationRepository regulationRepository;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public EcfrApiService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }
    
    // ================================
    // MAIN DOWNLOAD METHODS (Database CRUD Operations)
    // ================================
    
    /**
     * Download and SAVE all regulations for a title to H2 Database (all available parts)
     * CRUD: CREATE + UPDATE operations
     */
    public Map<String, Object> downloadAndSaveTitle(Integer titleNumber) {
        logger.info("Downloading and saving ALL available parts for CFR Title {}", titleNumber);
        return downloadAndSaveTitle(titleNumber, Integer.MAX_VALUE); // Get all parts
    }
    
    /**
     * Download and SAVE all regulations for a title to H2 Database (with sample size limit)
     * CRUD: CREATE + UPDATE operations
     */
    public Map<String, Object> downloadAndSaveTitle(Integer titleNumber, Integer maxParts) {
        logger.info("Downloading and saving CFR Title {} to database (max {} parts)", titleNumber, maxParts);
        
        Map<String, Object> result = new HashMap<>();
        List<Regulation> downloadedRegulations = downloadTitleFromAPI(titleNumber, maxParts);
        
        int created = 0, updated = 0, errors = 0;
        List<Regulation> savedRegulations = new ArrayList<>();
        
        for (Regulation regulation : downloadedRegulations) {
            try {
                // CHECK if exists in database
                Optional<Regulation> existing = regulationRepository
                        .findByCfrTitleAndPartNumber(regulation.getCfrTitle(), regulation.getPartNumber());
                
                if (existing.isPresent()) {
                    // UPDATE operation
                    Regulation existingReg = existing.get();
                    if (!existingReg.getContentChecksum().equals(regulation.getContentChecksum())) {
                        existingReg.setTitle(regulation.getTitle());
                        existingReg.setContent(regulation.getContent());
                        existingReg.setAgencyName(regulation.getAgencyName());
                        existingReg.setWordCount(regulation.getWordCount());
                        existingReg.setContentChecksum(regulation.getContentChecksum());
                        
                        savedRegulations.add(regulationRepository.save(existingReg)); // DATABASE HIT
                        updated++;
                        logger.info("UPDATED Title {} Part {} in database", titleNumber, regulation.getPartNumber());
                    } else {
                        logger.debug("No changes for Title {} Part {}, skipping", titleNumber, regulation.getPartNumber());
                    }
                } else {
                    // CREATE operation
                    savedRegulations.add(regulationRepository.save(regulation)); // DATABASE HIT
                    created++;
                    logger.info("CREATED Title {} Part {} in database", titleNumber, regulation.getPartNumber());
                }
            } catch (Exception e) {
                logger.error("Error saving Title {} Part {}: {}", titleNumber, regulation.getPartNumber(), e.getMessage());
                errors++;
            }
        }
        
        result.put("downloaded", downloadedRegulations.size());
        result.put("created", created);
        result.put("updated", updated);
        result.put("errors", errors);
        result.put("totalWordCount", savedRegulations.stream().mapToInt(r -> r.getWordCount() != null ? r.getWordCount() : 0).sum());
        
        return result;
    }
    
    /**
     * Download ALL CFR titles (1-50) and save to database (all available parts)
     * CRUD: Bulk CREATE + UPDATE operations across all titles
     */
    public Map<String, Object> downloadAllTitles() {
        logger.info("Downloading ALL available parts for ALL CFR titles (1-50)");
        return downloadAllTitles(Integer.MAX_VALUE); // Get all parts for all titles
    }
    
    /**
     * Download ALL CFR titles (1-50) with sample size limit and save to database
     * CRUD: Bulk CREATE + UPDATE operations across all titles
     */
    public Map<String, Object> downloadAllTitles(Integer sampleSize) {
        logger.info("Starting download of ALL CFR titles (1-50) with max {} parts each", sampleSize);
        
        Map<String, Object> overallResult = new HashMap<>();
        List<Map<String, Object>> titleResults = new ArrayList<>();
        int totalCreated = 0, totalUpdated = 0, totalErrors = 0, totalDownloaded = 0;
        long totalWordCount = 0;
        long startTime = System.currentTimeMillis();
        
        for (int titleNumber = 1; titleNumber <= 50; titleNumber++) {
            try {
                logger.info("Processing CFR Title {} - {} ({}/50)", titleNumber, getTitleName(titleNumber), titleNumber);
                
                // Use the existing downloadAndSaveTitle method with sampleSize
                Map<String, Object> titleResult = downloadAndSaveTitle(titleNumber, sampleSize);
                titleResult.put("titleNumber", titleNumber);
                titleResult.put("titleName", getTitleName(titleNumber));
                titleResult.put("agency", getAgencyForTitle(titleNumber));
                
                titleResults.add(titleResult);
                
                // Accumulate totals
                totalCreated += (Integer) titleResult.get("created");
                totalUpdated += (Integer) titleResult.get("updated");
                totalErrors += (Integer) titleResult.get("errors");
                totalDownloaded += (Integer) titleResult.get("downloaded");
                totalWordCount += (Integer) titleResult.get("totalWordCount");
                
                logger.info("Title {} complete: Created {}, Updated {}, Errors {}", 
                        titleNumber, titleResult.get("created"), titleResult.get("updated"), titleResult.get("errors"));
                
                // Be respectful to the API - wait between titles
                Thread.sleep(2000);
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Bulk download interrupted at Title {}: {}", titleNumber, e.getMessage());
                break;
            } catch (Exception e) {
                logger.error("Error processing Title {}: {}", titleNumber, e.getMessage());
                totalErrors++;
            }
        }
        
        long endTime = System.currentTimeMillis();
        long durationMinutes = (endTime - startTime) / 60000;
        
        overallResult.put("titleResults", titleResults);
        overallResult.put("totalTitlesProcessed", titleResults.size());
        overallResult.put("totalCreated", totalCreated);
        overallResult.put("totalUpdated", totalUpdated);
        overallResult.put("totalErrors", totalErrors);
        overallResult.put("totalDownloaded", totalDownloaded);
        overallResult.put("totalWordCount", totalWordCount);
        overallResult.put("durationMinutes", durationMinutes);
        
        logger.info("Bulk download complete! Processed {} titles in {} minutes. Total: Created {}, Updated {}, Errors {}, Word Count: {}", 
                titleResults.size(), durationMinutes, totalCreated, totalUpdated, totalErrors, totalWordCount);
        
        return overallResult;
    }
    
    /**
     * Download regulations but DON'T save to database - just return data
     * NO DATABASE HITS - Pure API download
     */
    public List<Regulation> downloadTitleFromAPI(Integer titleNumber, Integer maxParts) {
        logger.info("Downloading CFR Title {} from API only (no database save)", titleNumber);
        
        try {
            String latestDate = getLatestAvailableDate();
            List<String> partNumbers = getPartNumbersForTitle(titleNumber, latestDate);
            
            List<Regulation> regulations = new ArrayList<>();
            int processedCount = 0;
            
            for (String partNumber : partNumbers) {
                if (processedCount >= maxParts) {
                    logger.info("Reached maximum parts limit ({})", maxParts);
                    break;
                }
                
                try {
                    Regulation regulation = downloadPartContentFromAPI(titleNumber, partNumber);
                    if (regulation != null) {
                        regulations.add(regulation);
                        processedCount++;
                    }
                } catch (Exception e) {
                    logger.error("Error downloading Title {} Part {}: {}", titleNumber, partNumber, e.getMessage());
                }
                
                Thread.sleep(200); // Be respectful to API
            }
            
            return regulations;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Download interrupted for Title {}: {}", titleNumber, e.getMessage());
            throw new RuntimeException("Download interrupted: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error downloading Title {} from API: {}", titleNumber, e.getMessage());
            throw new RuntimeException("Failed to download from API: " + e.getMessage());
        }
    }
    
    /**
     * Download and save SPECIFIC parts only
     * CRUD: Selective CREATE/UPDATE
     */
    public Map<String, Object> downloadAndSaveSpecificParts(Integer titleNumber, List<String> partNumbers) {
        logger.info("Downloading and saving specific parts {} for Title {}", partNumbers, titleNumber);
        
        Map<String, Object> result = new HashMap<>();
        int created = 0, updated = 0, errors = 0;
        List<Regulation> savedRegulations = new ArrayList<>();
        
        for (String partNumber : partNumbers) {
            try {
                Regulation regulation = downloadPartContentFromAPI(titleNumber, partNumber);
                if (regulation != null) {
                    Optional<Regulation> existing = regulationRepository
                            .findByCfrTitleAndPartNumber(titleNumber, partNumber);
                    
                    if (existing.isPresent()) {
                        // UPDATE
                        Regulation existingReg = existing.get();
                        existingReg.setTitle(regulation.getTitle());
                        existingReg.setContent(regulation.getContent());
                        existingReg.setAgencyName(regulation.getAgencyName());
                        existingReg.setWordCount(regulation.getWordCount());
                        existingReg.setContentChecksum(regulation.getContentChecksum());
                        
                        savedRegulations.add(regulationRepository.save(existingReg)); // DATABASE HIT
                        updated++;
                    } else {
                        // CREATE
                        savedRegulations.add(regulationRepository.save(regulation)); // DATABASE HIT
                        created++;
                    }
                }
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Download interrupted for Title {} Part {}: {}", titleNumber, partNumber, e.getMessage());
                break;
            } catch (Exception e) {
                logger.error("Error with Title {} Part {}: {}", titleNumber, partNumber, e.getMessage());
                errors++;
            }
        }
        
        result.put("created", created);
        result.put("updated", updated);
        result.put("errors", errors);
        result.put("totalWordCount", savedRegulations.stream().mapToInt(r -> r.getWordCount() != null ? r.getWordCount() : 0).sum());
        
        return result;
    }
    
    /**
     * Check for changes without saving to database
     * CRUD: READ-only comparison
     */
    public Map<String, Object> checkForChanges(Integer titleNumber, List<String> partNumbers) {
        logger.info("Checking for changes in Title {} parts: {}", titleNumber, partNumbers);
        
        Map<String, Object> result = new HashMap<>();
        List<String> changedParts = new ArrayList<>();
        List<String> newParts = new ArrayList<>();
        List<String> unchangedParts = new ArrayList<>();
        
        for (String partNumber : partNumbers) {
            try {
                // Get current version from database
                Optional<Regulation> existingReg = regulationRepository.findByCfrTitleAndPartNumber(titleNumber, partNumber);
                
                // Download current version from API
                Regulation apiRegulation = downloadPartContentFromAPI(titleNumber, partNumber);
                
                if (existingReg.isPresent() && apiRegulation != null) {
                    if (!existingReg.get().getContentChecksum().equals(apiRegulation.getContentChecksum())) {
                        changedParts.add(partNumber);
                        logger.info("CHANGE DETECTED in Title {} Part {}", titleNumber, partNumber);
                    } else {
                        unchangedParts.add(partNumber);
                    }
                } else if (apiRegulation != null) {
                    newParts.add(partNumber);
                    logger.info("NEW PART detected: Title {} Part {}", titleNumber, partNumber);
                }
                
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Change check interrupted for Title {} Part {}: {}", titleNumber, partNumber, e.getMessage());
                break;
            } catch (Exception e) {
                logger.error("Error checking Title {} Part {}: {}", titleNumber, partNumber, e.getMessage());
            }
        }
        
        result.put("changedParts", changedParts);
        result.put("newParts", newParts);
        result.put("unchangedParts", unchangedParts);
        result.put("totalChecked", partNumbers.size());
        
        return result;
    }
    
    /**
     * Bulk delete regulations from database
     * CRUD: DELETE operations
     */
    public Map<String, Object> deleteRegulations(Integer titleNumber, List<String> partNumbers) {
        logger.info("Deleting regulations for Title {} parts: {}", titleNumber, partNumbers);
        
        Map<String, Object> result = new HashMap<>();
        int deleted = 0, notFound = 0;
        
        for (String partNumber : partNumbers) {
            try {
                Optional<Regulation> existing = regulationRepository.findByCfrTitleAndPartNumber(titleNumber, partNumber);
                if (existing.isPresent()) {
                    regulationRepository.delete(existing.get()); // DATABASE HIT - DELETE
                    deleted++;
                    logger.info("DELETED Title {} Part {} from database", titleNumber, partNumber);
                } else {
                    notFound++;
                    logger.warn("Title {} Part {} not found in database", titleNumber, partNumber);
                }
            } catch (Exception e) {
                logger.error("Error deleting Title {} Part {}: {}", titleNumber, partNumber, e.getMessage());
            }
        }
        
        result.put("deleted", deleted);
        result.put("notFound", notFound);
        
        return result;
    }
    
    /**
     * Get recent changes/amendments from eCFR API for a specific title
     * This uses the corrections/amendments endpoint to track changes over time
     */
    public List<Map<String, Object>> getRecentChangesFromECFR(Integer titleNumber) {
        try {
            String changesUrl = ECFR_BASE_URL + "/admin/v1/corrections.json";
            logger.info("Checking recent changes from eCFR API: {}", changesUrl);
            
            String changesResponse = restTemplate.getForObject(changesUrl, String.class);
            JsonNode changesJson = objectMapper.readTree(changesResponse);
            
            List<Map<String, Object>> recentChanges = new ArrayList<>();
            JsonNode correctionsNode = changesJson.get("corrections");
            
            if (correctionsNode != null && correctionsNode.isArray()) {
                for (JsonNode correction : correctionsNode) {
                    int correctionTitle = correction.path("title").asInt(-1);
                    if (correctionTitle == titleNumber) {
                        Map<String, Object> change = new HashMap<>();
                        change.put("title", correctionTitle);
                        change.put("part", correction.path("part").asText());
                        change.put("correctionDate", correction.path("correction_date").asText());
                        change.put("effectiveDate", correction.path("effective_date").asText());
                        change.put("description", correction.path("description").asText());
                        change.put("amendmentType", correction.path("amendment_type").asText());
                        change.put("documentNumber", correction.path("document_number").asText());
                        recentChanges.add(change);
                    }
                }
            }
            
            logger.info("Found {} recent changes for Title {}", recentChanges.size(), titleNumber);
            return recentChanges;
            
        } catch (Exception e) {
            logger.error("Error getting recent changes for Title {}: {}", titleNumber, e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Check for changes across all titles and return summary
     */
    public Map<String, Object> getOverallChangesSummary() {
        Map<String, Object> summary = new HashMap<>();
        List<Map<String, Object>> allChanges = new ArrayList<>();
        int totalChanges = 0;
        
        try {
            for (int titleNumber = 1; titleNumber <= 50; titleNumber++) {
                List<Map<String, Object>> titleChanges = getRecentChangesFromECFR(titleNumber);
                if (!titleChanges.isEmpty()) {
                    Map<String, Object> titleSummary = new HashMap<>();
                    titleSummary.put("titleNumber", titleNumber);
                    titleSummary.put("titleName", getTitleName(titleNumber));
                    titleSummary.put("changesCount", titleChanges.size());
                    titleSummary.put("changes", titleChanges);
                    allChanges.add(titleSummary);
                    totalChanges += titleChanges.size();
                }
                
                // Be respectful to API
                Thread.sleep(500);
            }
            
            summary.put("totalChanges", totalChanges);
            summary.put("titlesWithChanges", allChanges.size());
            summary.put("titleChanges", allChanges);
            summary.put("checkedAt", LocalDateTime.now());
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Changes summary interrupted: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Error getting overall changes summary: {}", e.getMessage());
        }
        
        return summary;
    }
    
    
    /**
     * LEGACY METHOD: Keep for backward compatibility with TestController
     * CRUD: CREATE + UPDATE (saves to database)
     */
    public String downloadTitle7Regulations() {
        Map<String, Object> result = downloadAndSaveTitle(7, 5);
        return String.format("Successfully processed %d regulations from CFR Title 7. Created: %d, Updated: %d, Total word count: %d", 
                (Integer)result.get("downloaded"), 
                (Integer)result.get("created"), 
                (Integer)result.get("updated"),
                (Integer)result.get("totalWordCount"));
    }
    
    // ================================
    // HELPER METHODS (No Database Hits)
    // ================================
    
    /**
     * Get the most recent available date for eCFR API calls
     */
    private String getLatestAvailableDate() {
        try {
            String titlesUrl = ECFR_BASE_URL + "/versioner/v1/titles";
            String titlesResponse = restTemplate.getForObject(titlesUrl, String.class);
            if (titlesResponse != null) {
                JsonNode titlesJson = objectMapper.readTree(titlesResponse);
                JsonNode titlesArray = titlesJson.get("titles");
                if (titlesArray != null && titlesArray.isArray() && titlesArray.size() > 0) {
                    // Get the up_to_date_as_of from the first title (should be consistent across all titles)
                    String upToDateAsOf = titlesArray.get(0).path("up_to_date_as_of").asText("");
                    if (!upToDateAsOf.isEmpty()) {
                        logger.debug("Using latest available date: {}", upToDateAsOf);
                        return upToDateAsOf;
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Could not get latest available date, falling back to current date: {}", e.getMessage());
        }
        
        // Fallback to current date
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    /**
     * Download single part content from API (no database save)
     * Now uses versioner API for better content retrieval
     */
    private Regulation downloadPartContentFromAPI(Integer titleNumber, String partNumber) {
        try {
            String latestDate = getLatestAvailableDate();
            
            // First try the versioner API for better content
            String versionerUrl = ECFR_BASE_URL + "/versioner/v1/structure/" + latestDate + "/title-" + titleNumber + ".json";
            logger.debug("Trying versioner API: {}", versionerUrl);
            
            try {
                String versionerResponse = restTemplate.getForObject(versionerUrl, String.class);
                if (versionerResponse != null) {
                    JsonNode versionerJson = objectMapper.readTree(versionerResponse);
                    Regulation regulation = extractFromVersionerAPI(versionerJson, titleNumber, partNumber, latestDate);
                    if (regulation != null) {
                        logger.info("Successfully retrieved content from versioner API for Title {} Part {}", titleNumber, partNumber);
                        return regulation;
                    }
                }
            } catch (Exception e) {
                logger.debug("Versioner API failed for Title {} Part {}: {}", titleNumber, partNumber, e.getMessage());
            }
            
            // Fallback to search API
            String searchUrl = ECFR_BASE_URL + "/search/v1/results?query=title:" + titleNumber + " part:" + partNumber + "&per_page=5";
            logger.debug("Fallback to search API: {}", searchUrl);
            
            String searchResponse = restTemplate.getForObject(searchUrl, String.class);
            if (searchResponse != null) {
                JsonNode searchJson = objectMapper.readTree(searchResponse);
                JsonNode resultsNode = searchJson.get("results");
                
                if (resultsNode != null && resultsNode.isArray() && resultsNode.size() > 0) {
                    JsonNode firstResult = resultsNode.get(0);
                    
                    String title = extractSearchTitle(firstResult, partNumber);
                    String content = extractSearchContent(firstResult);
                    String agencyName = getAgencyForTitle(titleNumber);
                    String checksum = generateChecksum(content);
                    Integer wordCount = calculateWordCount(content);
                    
                    Regulation regulation = new Regulation();
                    regulation.setCfrTitle(titleNumber);
                    regulation.setPartNumber(partNumber);
                    regulation.setTitle(title);
                    regulation.setContent(content);
                    regulation.setAgencyName(agencyName);
                    regulation.setWordCount(wordCount);
                    regulation.setContentChecksum(checksum);
                    regulation.setSourceUrl(searchUrl);
                    
                    return regulation;
                }
            }
            
            return createBasicRegulation(titleNumber, partNumber);
            
        } catch (Exception e) {
            logger.error("Error downloading Title {} Part {}: {}", titleNumber, partNumber, e.getMessage());
            return createBasicRegulation(titleNumber, partNumber);
        }
    }
    
    /**
     * Extract regulation from versioner API response for better content
     */
    private Regulation extractFromVersionerAPI(JsonNode versionerJson, Integer titleNumber, String partNumber, String apiDate) {
        try {
            // Navigate the versioner JSON structure to find the specific part
            JsonNode titleNode = versionerJson.get("title");
            if (titleNode != null) {
                // Look for the specific part in the structure
                JsonNode chaptersNode = titleNode.get("children");
                if (chaptersNode != null && chaptersNode.isArray()) {
                    for (JsonNode chapter : chaptersNode) {
                        JsonNode partsNode = chapter.get("children");
                        if (partsNode != null && partsNode.isArray()) {
                            for (JsonNode part : partsNode) {
                                // Check if this is the part we're looking for
                                String partId = part.path("identifier").asText("");
                                if (partId.contains("CFR " + partNumber) || partId.endsWith(" " + partNumber)) {
                                    
                                    String title = part.path("label").asText("CFR Title " + titleNumber + " Part " + partNumber);
                                    String content = extractContentFromVersionerPart(part);
                                    
                                    // Look for amendment information
                                    String lastAmended = part.path("last_amended").asText("");
                                    String lastIssued = part.path("last_issued").asText("");
                                    String lastUpdated = part.path("last_updated").asText("");
                                    
                                    if (!content.isEmpty() && !content.equals("Content not available")) {
                                        Regulation regulation = new Regulation();
                                        regulation.setCfrTitle(titleNumber);
                                        regulation.setPartNumber(partNumber);
                                        regulation.setTitle(title);
                                        regulation.setContent(content);
                                        regulation.setAgencyName(getAgencyForTitle(titleNumber));
                                        regulation.setWordCount(calculateWordCount(content));
                                        regulation.setContentChecksum(generateChecksum(content));
                                        
                                        // Set government amendment dates
                                        try {
                                            if (!lastAmended.isEmpty()) {
                                                regulation.setLatestAmendedOn(LocalDate.parse(lastAmended));
                                            }
                                            if (!lastIssued.isEmpty()) {
                                                regulation.setLatestIssueDate(LocalDate.parse(lastIssued));
                                            }
                                            if (!lastUpdated.isEmpty()) {
                                                regulation.setLastUpdatedOn(LocalDate.parse(lastUpdated));
                                            }
                                        } catch (Exception dateParseException) {
                                            logger.warn("Error parsing dates for Title {} Part {}: {}", titleNumber, partNumber, dateParseException.getMessage());
                                        }
                                        
                                        regulation.setSourceUrl(ECFR_BASE_URL + "/versioner/v1/structure/" + 
                                            apiDate + "/title-" + titleNumber + ".json");
                                        
                                        logger.debug("Extracted content from versioner API: {} words", regulation.getWordCount());
                                        return regulation;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Error extracting from versioner API for Title {} Part {}: {}", titleNumber, partNumber, e.getMessage());
        }
        return null;
    }
    
    /**
     * Extract content text from a versioner API part node
     */
    private String extractContentFromVersionerPart(JsonNode partNode) {
        StringBuilder content = new StringBuilder();
        
        try {
            // Try different fields that might contain content
            String text = partNode.path("text").asText("");
            if (!text.isEmpty()) {
                content.append(text);
            }
            
            String fullText = partNode.path("full_text").asText("");
            if (!fullText.isEmpty()) {
                if (content.length() > 0) content.append(" ");
                content.append(fullText);
            }
            
            String description = partNode.path("description").asText("");
            if (!description.isEmpty()) {
                if (content.length() > 0) content.append(" ");
                content.append(description);
            }
            
            // Try to extract from children nodes
            JsonNode childrenNode = partNode.get("children");
            if (childrenNode != null && childrenNode.isArray()) {
                for (JsonNode child : childrenNode) {
                    String childText = child.path("text").asText("");
                    if (!childText.isEmpty()) {
                        if (content.length() > 0) content.append(" ");
                        content.append(childText);
                    }
                }
            }
            
        } catch (Exception e) {
            logger.warn("Error extracting content from versioner part: {}", e.getMessage());
        }
        
        String result = content.toString().trim();
        return result.isEmpty() ? "Content not available" : result;
    }
    
    /**
     * Generate SHA-256 checksum for change detection
     */
    private String generateChecksum(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes());
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
            return "checksum-error-" + System.currentTimeMillis();
        }
    }
    
    /**
     * Calculate word count
     */
    private Integer calculateWordCount(String content) {
        if (content == null || content.trim().isEmpty()) {
            return 0;
        }
        return content.trim().split("\\s+").length;
    }
    
    /**
     * Get agency name for CFR title (all 50 titles mapped)
     */
    private String getAgencyForTitle(Integer titleNumber) {
        return switch (titleNumber) {
            case 1 -> "General Services Administration";
            case 2 -> "Office of Management and Budget";
            case 3 -> "Executive Office of the President";
            case 4 -> "Government Accountability Office";
            case 5 -> "Office of Personnel Management";
            case 6 -> "Department of Homeland Security";
            case 7 -> "Department of Agriculture";
            case 8 -> "Department of Homeland Security";
            case 9 -> "Department of Agriculture";
            case 10 -> "Department of Energy";
            case 11 -> "Federal Election Commission";
            case 12 -> "Federal Reserve System";
            case 13 -> "Small Business Administration";
            case 14 -> "Federal Aviation Administration";
            case 15 -> "Department of Commerce";
            case 16 -> "Federal Trade Commission";
            case 17 -> "Securities and Exchange Commission";
            case 18 -> "Conservation of Power and Water Resources";
            case 19 -> "U.S. Customs and Border Protection";
            case 20 -> "Department of Labor";
            case 21 -> "Food and Drug Administration";
            case 22 -> "Department of State";
            case 23 -> "Department of Transportation";
            case 24 -> "Department of Housing and Urban Development";
            case 25 -> "Bureau of Indian Affairs";
            case 26 -> "Internal Revenue Service";
            case 27 -> "Bureau of Alcohol, Tobacco, Firearms and Explosives";
            case 28 -> "Department of Justice";
            case 29 -> "Department of Labor";
            case 30 -> "Department of the Interior";
            case 31 -> "Department of the Treasury";
            case 32 -> "Department of Defense";
            case 33 -> "U.S. Coast Guard";
            case 34 -> "Department of Education";
            case 35 -> "Reserved";
            case 36 -> "National Park Service";
            case 37 -> "U.S. Patent and Trademark Office";
            case 38 -> "Department of Veterans Affairs";
            case 39 -> "U.S. Postal Service";
            case 40 -> "Environmental Protection Agency";
            case 41 -> "General Services Administration";
            case 42 -> "Department of Health and Human Services";
            case 43 -> "Department of the Interior";
            case 44 -> "Federal Emergency Management Agency";
            case 45 -> "Department of Health and Human Services";
            case 46 -> "Federal Maritime Commission";
            case 47 -> "Federal Communications Commission";
            case 48 -> "General Services Administration";
            case 49 -> "Department of Transportation";
            case 50 -> "U.S. Fish and Wildlife Service";
            default -> "Federal Government";
        };
    }
    
    /**
     * Get part numbers for any CFR title using the correct date
     */
    private List<String> getPartNumbersForTitle(Integer titleNumber, String apiDate) {
        List<String> partNumbers = new ArrayList<>();
        
        try {
            // Try structure API first
            String structureUrl = ECFR_BASE_URL + "/versioner/v1/structure/" + apiDate + "/title-" + titleNumber + ".json";
            logger.info("Fetching Title {} structure from: {}", titleNumber, structureUrl);
            
            String structureResponse = restTemplate.getForObject(structureUrl, String.class);
            if (structureResponse != null) {
                JsonNode structureJson = objectMapper.readTree(structureResponse);
                partNumbers = extractPartNumbersFromStructure(structureJson);
            }
        } catch (Exception e) {
            logger.warn("Could not fetch structure for Title {} from API: {}", titleNumber, e.getMessage());
        }
        
        // If structure API failed, use search API
        if (partNumbers.isEmpty()) {
            try {
                partNumbers = getPartNumbersFromSearch(titleNumber);
            } catch (Exception e) {
                logger.warn("Could not fetch parts for Title {} from search API: {}", titleNumber, e.getMessage());
            }
        }
        
        // If both APIs failed, use fallback
        if (partNumbers.isEmpty()) {
            logger.info("Using fallback part numbers for Title {}", titleNumber);
            partNumbers.addAll(getFallbackPartNumbers(titleNumber));
        }
        
        return partNumbers;
    }
    
    /**
     * Get part numbers using search API for any title
     */
    private List<String> getPartNumbersFromSearch(Integer titleNumber) {
        List<String> partNumbers = new ArrayList<>();
        
        try {
            String searchUrl = ECFR_BASE_URL + "/search/v1/results?query=title:" + titleNumber + "&per_page=50";
            logger.info("Searching for Title {} parts: {}", titleNumber, searchUrl);
            
            String searchResponse = restTemplate.getForObject(searchUrl, String.class);
            if (searchResponse != null) {
                JsonNode searchJson = objectMapper.readTree(searchResponse);
                JsonNode resultsNode = searchJson.get("results");
                
                if (resultsNode != null && resultsNode.isArray()) {
                    for (JsonNode result : resultsNode) {
                        JsonNode partNode = result.get("part");
                        if (partNode != null) {
                            String partNumber = partNode.asText().trim();
                            if (!partNumber.isEmpty() && !partNumbers.contains(partNumber)) {
                                partNumbers.add(partNumber);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Error getting parts for Title {} from search API: {}", titleNumber, e.getMessage());
        }
        
        return partNumbers;
    }
    
    /**
     * Fallback part numbers for when APIs fail
     */
    private List<String> getFallbackPartNumbers(Integer titleNumber) {
        return switch (titleNumber) {
            case 7 -> List.of("1", "2", "3", "4", "5", "7", "9", "10", "11", "12");
            case 10 -> List.of("1", "2", "3", "4", "5", "10", "20", "30");
            case 21 -> List.of("1", "2", "3", "4", "5", "10", "11", "50");
            case 40 -> List.of("1", "2", "3", "4", "5", "50", "60", "70");
            default -> List.of("1", "2", "3", "4", "5");
        };
    }
    
    
    /**
     * Extract part numbers from structure JSON
     */
    private List<String> extractPartNumbersFromStructure(JsonNode structureJson) {
        List<String> partNumbers = new ArrayList<>();
        
        try {
            // Navigate through the JSON structure to find parts
            JsonNode titleNode = structureJson.get("title");
            if (titleNode != null) {
                JsonNode chaptersNode = titleNode.get("children");
                if (chaptersNode != null && chaptersNode.isArray()) {
                    for (JsonNode chapter : chaptersNode) {
                        JsonNode partsNode = chapter.get("children");
                        if (partsNode != null && partsNode.isArray()) {
                            for (JsonNode part : partsNode) {
                                JsonNode identifierNode = part.get("identifier");
                                if (identifierNode != null) {
                                    String identifier = identifierNode.asText();
                                    // Extract part number from identifier like "7 CFR 1"
                                    String[] parts = identifier.split(" ");
                                    if (parts.length >= 3) {
                                        partNumbers.add(parts[2]); // The part number
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Error parsing structure JSON: {}", e.getMessage());
        }
        
        return partNumbers;
    }
    
    /**
     * Create a basic regulation entry when API data is not available
     */
    private Regulation createBasicRegulation(Integer titleNumber, String partNumber) {
        Regulation regulation = new Regulation();
        regulation.setCfrTitle(titleNumber);
        regulation.setPartNumber(partNumber);
        regulation.setTitle("CFR Title " + titleNumber + " Part " + partNumber);
        regulation.setContent("Content retrieval from eCFR API was not successful. This is a placeholder entry.");
        regulation.setAgencyName(getAgencyForTitle(titleNumber));
        regulation.setWordCount(calculateWordCount(regulation.getContent()));
        regulation.setContentChecksum(generateChecksum(regulation.getContent()));
        return regulation;
    }
    
    /**
     * Extract title from search result
     */
    private String extractSearchTitle(JsonNode resultNode, String partNumber) {
        // Try to get title from search result
        JsonNode titleNode = resultNode.get("title");
        if (titleNode != null && !titleNode.asText().trim().isEmpty()) {
            return titleNode.asText();
        }
        
        // Try heading
        JsonNode headingNode = resultNode.get("heading");
        if (headingNode != null && !headingNode.asText().trim().isEmpty()) {
            return headingNode.asText();
        }
        
        // Try label
        JsonNode labelNode = resultNode.get("label");
        if (labelNode != null && !labelNode.asText().trim().isEmpty()) {
            return labelNode.asText();
        }
        
        // Fallback
        return "CFR Part " + partNumber;
    }
    
    /**
     * Extract content from search result
     */
    private String extractSearchContent(JsonNode resultNode) {
        StringBuilder content = new StringBuilder();
        
        // Try to get content from search result
        JsonNode contentNode = resultNode.get("content");
        if (contentNode != null && !contentNode.asText().trim().isEmpty()) {
            content.append(contentNode.asText());
        }
        
        // Try text field
        JsonNode textNode = resultNode.get("text");
        if (textNode != null && !textNode.asText().trim().isEmpty()) {
            if (content.length() > 0) content.append(" ");
            content.append(textNode.asText());
        }
        
        // Try snippet
        JsonNode snippetNode = resultNode.get("snippet");
        if (snippetNode != null && !snippetNode.asText().trim().isEmpty()) {
            if (content.length() > 0) content.append(" ");
            content.append(snippetNode.asText());
        }
        
        String result = content.toString().trim();
        return result.isEmpty() ? "Content not available from search results" : result;
    }
    
    /**
     * Get human-readable title names for all 50 CFR titles
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
}