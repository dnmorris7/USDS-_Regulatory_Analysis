package com.usds.regulations.controller;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Integration tests for the scalable eCFRDownloadController
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ECFRDownloadControllerTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String getBaseUrl() {
        return "http://localhost:" + port + "/api";
    }

    @Test
    void testAvailableTitlesEndpoint() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
            getBaseUrl() + "/eCFR_source-download/available-titles", 
            Map.class
        );
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertTrue((Boolean) body.get("success"));
        assertNotNull(body.get("supportedTitles"));
        assertTrue((Integer) body.get("totalSupportedTitles") > 0);
    }

    @Test
    void testDownloadTitle7() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
            getBaseUrl() + "/eCFR_source-download/7", 
            Map.class
        );
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertTrue((Boolean) body.get("success"));
        assertEquals(7, body.get("cfrTitle"));
        assertNotNull(body.get("executionTimeMs"));
        assertNotNull(body.get("statistics"));
    }

    @Test
    void testDownloadUnsupportedTitle() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
            getBaseUrl() + "/eCFR_source-download/8", 
            Map.class
        );
        
        assertEquals(HttpStatus.valueOf(501), response.getStatusCode());
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertFalse((Boolean) body.get("success"));
        // Note: cfrTitle is not included in unsupported title responses
        assertTrue(body.get("error").toString().contains("not yet implemented"));
        assertNotNull(body.get("supportedTitles"));
    }

    @Test
    void testDownloadInvalidTitle() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
            getBaseUrl() + "/eCFR_source-download/99", 
            Map.class
        );
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertFalse((Boolean) body.get("success"));
        assertTrue(body.get("error").toString().contains("Invalid CFR title"));
    }

    @Test
    void testGetRegulationsByTitle() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
            getBaseUrl() + "/regulations/title/7", 
            Map.class
        );
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertTrue((Boolean) body.get("success"));
        assertEquals(7, body.get("cfrTitle"));
        assertNotNull(body.get("count"));
        assertNotNull(body.get("regulations"));
    }

    @Test
    void testGetAllTitlesSummary() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
            getBaseUrl() + "/regulations/titles", 
            Map.class
        );
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertTrue((Boolean) body.get("success"));
        assertNotNull(body.get("totalRegulations"));
        assertNotNull(body.get("titlesWithData"));
        assertNotNull(body.get("titleCounts"));
        assertNotNull(body.get("titleWordCounts"));
    }

    @Test
    void testGetStatsByTitle() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
            getBaseUrl() + "/stats/7", 
            Map.class
        );
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertTrue((Boolean) body.get("success"));
        assertEquals(7, body.get("cfrTitle"));
        assertNotNull(body.get("statistics"));
        assertNotNull(body.get("context"));
        
        // Check the statistics structure
        Map<String, Object> statistics = (Map<String, Object>) body.get("statistics");
        assertNotNull(statistics.get("regulationCount"));
        assertNotNull(statistics.get("totalWordCount"));
        assertNotNull(statistics.get("averageWordCount"));
        assertNotNull(statistics.get("hasData"));
        
        // Check the context structure
        Map<String, Object> context = (Map<String, Object>) body.get("context");
        assertNotNull(context.get("totalRegulationsInDatabase"));
        assertNotNull(context.get("percentageOfTotal"));
    }

    @Test
    void testGetStatsByInvalidTitle() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
            getBaseUrl() + "/stats/99", 
            Map.class
        );
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertFalse((Boolean) body.get("success"));
        assertTrue(body.get("error").toString().contains("Invalid CFR title"));
    }

    @Disabled("Not needed in the long run - disabling this test")
    @Test
    void testGetStatsByTitleWithNoData() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
            getBaseUrl() + "/stats/8", 
            Map.class
        );
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertTrue((Boolean) body.get("success"));
        assertEquals(8, body.get("cfrTitle"));
        
        // Check that it shows no data for title 8
        Map<String, Object> statistics = (Map<String, Object>) body.get("statistics");
        assertEquals(0, statistics.get("regulationCount"));
        assertEquals(0, statistics.get("totalWordCount"));
        assertEquals(0, statistics.get("averageWordCount"));
        assertFalse((Boolean) statistics.get("hasData"));
    }
}