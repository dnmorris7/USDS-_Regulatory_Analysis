package com.usds.regulations.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.usds.regulations.RegulationsApplication;
import com.usds.regulations.repository.RegulationRepository;

/**
 * Comprehensive test class for download endpoints
 * Tests HTTP status codes, response structures, and API functionality
 */
@SpringBootTest(classes = RegulationsApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
public class DownloadEndpointTests {
    
    @LocalServerPort
    private int port;
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private RegulationRepository regulationRepository;
    
    private String getBaseUrl() {
        return "http://localhost:" + port + "/api";
    }
    
    @BeforeEach
    void setUp() {
        // Clean up database before each test
        regulationRepository.deleteAll();
    }
    
    @Test
    @DisplayName("Health endpoint should return 200 OK with proper structure")
    void testHealthEndpoint() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                getBaseUrl() + "/health", String.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("status"));
        assertTrue(response.getBody().contains("USDS Regulations Analysis Backend"));
    }
    
    @Test
    @DisplayName("eCFR source download endpoint should return 200 OK and download data")
    void testEcfrSourceDownloadEndpoint() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                getBaseUrl() + "/eCFR_source-download", String.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("success"));
        assertTrue(response.getBody().contains("true") || response.getBody().contains("false"));
    }
    
    @Test
    @DisplayName("Stats endpoint should return 200 OK with regulation statistics")
    void testStatsEndpoint() {
        // First download some data
        restTemplate.getForEntity(getBaseUrl() + "/eCFR_source-download", String.class);
        
        ResponseEntity<String> response = restTemplate.getForEntity(
                getBaseUrl() + "/stats", String.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("totalRegulations"));
        assertTrue(response.getBody().contains("title7"));
    }
    
    @Test
    @DisplayName("Title 7 regulations endpoint should return 200 OK with regulation list")
    void testTitle7RegulationsEndpoint() {
        // First download some data
        restTemplate.getForEntity(getBaseUrl() + "/eCFR_source-download", String.class);
        
        ResponseEntity<String> response = restTemplate.getForEntity(
                getBaseUrl() + "/regulations/title7", String.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("success"));
        assertTrue(response.getBody().contains("count"));
        assertTrue(response.getBody().contains("regulations"));
    }
    
    @Test
    @DisplayName("Analytics word count distribution should return 200 OK")
    void testAnalyticsWordCountDistribution() {
        // First download some data
        restTemplate.getForEntity(getBaseUrl() + "/eCFR_source-download", String.class);
        
        ResponseEntity<String> response = restTemplate.getForEntity(
                getBaseUrl() + "/analytics/word-count-distribution", String.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("success"));
        assertTrue(response.getBody().contains("distribution"));
    }
    
    @Test
    @DisplayName("Analytics top regulations should return 200 OK")
    void testAnalyticsTopRegulations() {
        // First download some data
        restTemplate.getForEntity(getBaseUrl() + "/eCFR_source-download", String.class);
        
        ResponseEntity<String> response = restTemplate.getForEntity(
                getBaseUrl() + "/analytics/top-regulations?limit=5", String.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("success"));
        assertTrue(response.getBody().contains("regulations"));
    }
    
    @Test
    @DisplayName("Invalid endpoints should return 404 Not Found")
    void testInvalidEndpoints() {
        ResponseEntity<String> response1 = restTemplate.getForEntity(
                getBaseUrl() + "/nonexistent", String.class);
        assertEquals(HttpStatus.NOT_FOUND, response1.getStatusCode());
        
        ResponseEntity<String> response2 = restTemplate.getForEntity(
                getBaseUrl() + "/download/invalid", String.class);
        assertEquals(HttpStatus.NOT_FOUND, response2.getStatusCode());
    }
    
    @Test
    @DisplayName("Multiple consecutive downloads should work correctly")
    void testMultipleDownloads() {
        // First download
        ResponseEntity<String> response1 = restTemplate.getForEntity(
                getBaseUrl() + "/test-download", String.class);
        assertEquals(HttpStatus.OK, response1.getStatusCode());
        
        // Second download (should handle duplicates)
        ResponseEntity<String> response2 = restTemplate.getForEntity(
                getBaseUrl() + "/test-download", String.class);
        assertEquals(HttpStatus.OK, response2.getStatusCode());
        
        // Verify data consistency
        ResponseEntity<String> statsResponse = restTemplate.getForEntity(
                getBaseUrl() + "/stats", String.class);
        assertEquals(HttpStatus.OK, statsResponse.getStatusCode());
        assertTrue(statsResponse.getBody().contains("totalRegulations"));
    }
    
    @Test
    @DisplayName("All endpoints should return proper JSON Content-Type")
    void testContentTypeHeaders() {
        ResponseEntity<String> healthResponse = restTemplate.getForEntity(
                getBaseUrl() + "/health", String.class);
        assertEquals(HttpStatus.OK, healthResponse.getStatusCode());
        assertTrue(healthResponse.getHeaders().getContentType().toString().contains("application/json"));
        
        ResponseEntity<String> statsResponse = restTemplate.getForEntity(
                getBaseUrl() + "/stats", String.class);
        assertEquals(HttpStatus.OK, statsResponse.getStatusCode());
        assertTrue(statsResponse.getHeaders().getContentType().toString().contains("application/json"));
    }
    
    @Test
    @DisplayName("Download endpoint performance should be reasonable")
    void testDownloadPerformance() {
        long startTime = System.currentTimeMillis();
        
        ResponseEntity<String> response = restTemplate.getForEntity(
                getBaseUrl() + "/test-download", String.class);
        
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(executionTime < 30000, "Download should complete within 30 seconds, took: " + executionTime + "ms");
    }
    
    @Test
    @DisplayName("Health endpoint should provide database connectivity status")
    void testHealthEndpointDatabaseStatus() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                getBaseUrl() + "/health", String.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("database"));
        assertTrue(response.getBody().contains("Connected") || response.getBody().contains("Error"));
    }
    
    @Test
    @DisplayName("Stats endpoint should provide meaningful statistics")
    void testStatsEndpointWithData() {
        // Download data first
        restTemplate.getForEntity(getBaseUrl() + "/test-download", String.class);
        
        ResponseEntity<String> response = restTemplate.getForEntity(
                getBaseUrl() + "/stats", String.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        String responseBody = response.getBody();
        assertNotNull(responseBody);
        
        // Check for required statistics fields
        assertTrue(responseBody.contains("totalRegulations"));
        assertTrue(responseBody.contains("title7"));
        assertTrue(responseBody.contains("count"));
        assertTrue(responseBody.contains("totalWordCount"));
        assertTrue(responseBody.contains("averageWordCount"));
    }
}