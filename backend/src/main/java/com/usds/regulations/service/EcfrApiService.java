package com.usds.regulations.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.usds.regulations.entity.Regulation;
import com.usds.regulations.repository.RegulationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class EcfrApiService {
    
    private static final Logger logger = LoggerFactory.getLogger(EcfrApiService.class);
    private static final String ECFR_BASE_URL = "https://www.ecfr.gov/api";
    
    @Autowired
    private RegulationRepository regulationRepository;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public EcfrApiService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Download Title 7 (Agriculture) regulations from eCFR API
     */
    public String downloadTitle7Regulations() {
        logger.info("Starting download of CFR Title 7 (Agriculture) regulations using eCFR API");
        
        try {
            // Get current date for versioner API
            String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            
            // First, try to get the structure of Title 7 to understand what parts are available
            List<String> partNumbers = getTitle7PartNumbers(currentDate);
            logger.info("Found {} parts to process in Title 7", partNumbers.size());
            
            List<Regulation> savedRegulations = new ArrayList<>();
            int processedCount = 0;
            int maxParts = 5; // Limit for testing - can be removed later
            
            // For each part, get the detailed content
            for (String partNumber : partNumbers) {
                if (processedCount >= maxParts) {
                    logger.info("Reached maximum parts limit ({}) for testing", maxParts);
                    break;
                }
                
                try {
                    logger.info("Processing Part {}", partNumber);
                    Regulation regulation = downloadPartContent(7, partNumber);
                    
                    if (regulation != null) {
                        // Check if regulation already exists
                        var existing = regulationRepository.findByCfrTitleAndPartNumber(7, partNumber);
                        if (existing.isPresent()) {
                            logger.info("Part {} already exists, updating", partNumber);
                            Regulation existingReg = existing.get();
                            existingReg.setTitle(regulation.getTitle());
                            existingReg.setContent(regulation.getContent());
                            existingReg.setAgencyName(regulation.getAgencyName());
                            savedRegulations.add(regulationRepository.save(existingReg));
                        } else {
                            savedRegulations.add(regulationRepository.save(regulation));
                        }
                        processedCount++;
                    }
                } catch (Exception e) {
                    logger.error("Error processing Part {}: {}", partNumber, e.getMessage());
                    // Continue with next part
                }
                
                // Be respectful to the API - small delay between requests
                Thread.sleep(200);
            }
            
            logger.info("Successfully processed {} regulations for Title 7", savedRegulations.size());
            return String.format("Successfully downloaded %d regulations from CFR Title 7. Total word count: %d", 
                    savedRegulations.size(), 
                    savedRegulations.stream().mapToInt(r -> r.getWordCount() != null ? r.getWordCount() : 0).sum());
            
        } catch (Exception e) {
            logger.error("Error downloading Title 7 regulations: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to download regulations: " + e.getMessage());
        }
    }
    
    /**
     * Get list of part numbers for Title 7 using multiple approaches
     */
    private List<String> getTitle7PartNumbers(String currentDate) {
        List<String> partNumbers = new ArrayList<>();
        
        try {
            // Try structure API first
            String structureUrl = ECFR_BASE_URL + "/versioner/v1/structure/" + currentDate + "/title-7.json";
            logger.info("Fetching Title 7 structure from: {}", structureUrl);
            
            String structureResponse = restTemplate.getForObject(structureUrl, String.class);
            if (structureResponse != null) {
                JsonNode structureJson = objectMapper.readTree(structureResponse);
                partNumbers = extractPartNumbersFromStructure(structureJson);
            }
        } catch (Exception e) {
            logger.warn("Could not fetch structure from API: {}", e.getMessage());
        }
        
        // If structure API failed or returned no parts, use search API
        if (partNumbers.isEmpty()) {
            try {
                partNumbers = getPartNumbersFromSearch();
            } catch (Exception e) {
                logger.warn("Could not fetch parts from search API: {}", e.getMessage());
            }
        }
        
        // If both APIs failed, use fallback list of common Title 7 parts
        if (partNumbers.isEmpty()) {
            logger.info("Using fallback part numbers for Title 7");
            partNumbers.addAll(List.of("1", "2", "3", "4", "5", "7", "9", "10", "11", "12", 
                                     "15", "16", "17", "18", "20", "25", "30", "35", "40", "50"));
        }
        
        return partNumbers;
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
     * Get part numbers using search API
     */
    private List<String> getPartNumbersFromSearch() {
        List<String> partNumbers = new ArrayList<>();
        
        try {
            String searchUrl = ECFR_BASE_URL + "/search/v1/results?query=title:7&per_page=50";
            logger.info("Searching for Title 7 parts: {}", searchUrl);
            
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
            logger.warn("Error getting parts from search API: {}", e.getMessage());
        }
        
        return partNumbers;
    }
    
    /**
     * Download content for a specific part using search API
     */
    private Regulation downloadPartContent(int titleNumber, String partNumber) {
        try {
            // Use search API to find content for this specific part
            String searchUrl = ECFR_BASE_URL + "/search/v1/results?query=title:" + titleNumber + " part:" + partNumber + "&per_page=5";
            logger.debug("Searching for part content: {}", searchUrl);
            
            String searchResponse = restTemplate.getForObject(searchUrl, String.class);
            if (searchResponse != null) {
                JsonNode searchJson = objectMapper.readTree(searchResponse);
                
                // Extract the first relevant result
                JsonNode resultsNode = searchJson.get("results");
                if (resultsNode != null && resultsNode.isArray() && resultsNode.size() > 0) {
                    JsonNode firstResult = resultsNode.get(0);
                    
                    String title = extractSearchTitle(firstResult, partNumber);
                    String content = extractSearchContent(firstResult);
                    String agencyName = "Department of Agriculture"; // Title 7 is always USDA
                    
                    logger.debug("Extracted from search - Title: {}, Content length: {}", 
                            title, content != null ? content.length() : 0);
                    
                    return new Regulation(titleNumber, partNumber, title, content, agencyName);
                }
            }
            
            // If no search results, create basic regulation entry
            logger.warn("No search results for Part {}, creating basic entry", partNumber);
            return createBasicRegulation(titleNumber, partNumber);
            
        } catch (HttpClientErrorException e) {
            logger.error("HTTP error searching for part {}: {} - {}", partNumber, e.getStatusCode(), e.getResponseBodyAsString());
            return createBasicRegulation(titleNumber, partNumber);
        } catch (RestClientException e) {
            logger.error("REST error searching for part {}: {}", partNumber, e.getMessage());
            return createBasicRegulation(titleNumber, partNumber);
        } catch (Exception e) {
            logger.error("Error parsing search results for part {}: {}", partNumber, e.getMessage());
            return createBasicRegulation(titleNumber, partNumber);
        }
    }
    
    /**
     * Create a basic regulation entry when API data is not available
     */
    private Regulation createBasicRegulation(int titleNumber, String partNumber) {
        return new Regulation(titleNumber, partNumber, 
                "CFR Title " + titleNumber + " Part " + partNumber, 
                "Content retrieval from eCFR API was not successful. This is a placeholder entry.", 
                "Department of Agriculture");
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
        return "CFR Title 7 Part " + partNumber;
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
}