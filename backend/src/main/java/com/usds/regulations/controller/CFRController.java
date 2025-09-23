package com.usds.regulations.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cfr")
@CrossOrigin(origins = "*")
public class CFRController {
    
    private static final Logger logger = LoggerFactory.getLogger(CFRController.class);
    
    /**
     * Get all CFR titles with their agency mappings
     * This matches what our Angular RegulationService expects
     */
    @GetMapping("/titles")
    public ResponseEntity<List<Map<String, Object>>> getAllCFRTitles() {
        logger.info("CFR titles endpoint called");
        
        List<Map<String, Object>> titles = new ArrayList<>();
        
        // All 50 CFR titles with their agency mappings
        // This matches the structure expected by our Angular RegulationService
        
        titles.add(createTitleMap(1, "General Provisions", "General Services Administration"));
        titles.add(createTitleMap(2, "Federal Financial Assistance", "Office of Management and Budget"));
        titles.add(createTitleMap(3, "The President", "Executive Office of the President"));
        titles.add(createTitleMap(4, "Accounts", "Government Accountability Office"));
        titles.add(createTitleMap(5, "Administrative Personnel", "Office of Personnel Management"));
        titles.add(createTitleMap(6, "Domestic Security", "Department of Homeland Security"));
        titles.add(createTitleMap(7, "Agriculture", "Department of Agriculture"));
        titles.add(createTitleMap(8, "Aliens and Nationality", "Department of Homeland Security"));
        titles.add(createTitleMap(9, "Animals and Animal Products", "Department of Agriculture"));
        titles.add(createTitleMap(10, "Energy", "Department of Energy"));
        titles.add(createTitleMap(11, "Federal Elections", "Federal Election Commission"));
        titles.add(createTitleMap(12, "Banks and Banking", "Federal Reserve System"));
        titles.add(createTitleMap(13, "Business Credit and Assistance", "Small Business Administration"));
        titles.add(createTitleMap(14, "Aeronautics and Space", "Federal Aviation Administration"));
        titles.add(createTitleMap(15, "Commerce and Foreign Trade", "Department of Commerce"));
        titles.add(createTitleMap(16, "Commercial Practices", "Federal Trade Commission"));
        titles.add(createTitleMap(17, "Commodity and Securities Exchanges", "Securities and Exchange Commission"));
        titles.add(createTitleMap(18, "Conservation of Power and Water Resources", "Federal Energy Regulatory Commission"));
        titles.add(createTitleMap(19, "Customs Duties", "Department of Homeland Security"));
        titles.add(createTitleMap(20, "Employees' Benefits", "Department of Labor"));
        titles.add(createTitleMap(21, "Food and Drugs", "Food and Drug Administration"));
        titles.add(createTitleMap(22, "Foreign Relations", "Department of State"));
        titles.add(createTitleMap(23, "Highways", "Department of Transportation"));
        titles.add(createTitleMap(24, "Housing and Urban Development", "Department of Housing and Urban Development"));
        titles.add(createTitleMap(25, "Indians", "Department of the Interior"));
        titles.add(createTitleMap(26, "Internal Revenue", "Internal Revenue Service"));
        titles.add(createTitleMap(27, "Alcohol, Tobacco Products and Firearms", "Department of the Treasury"));
        titles.add(createTitleMap(28, "Judicial Administration", "Department of Justice"));
        titles.add(createTitleMap(29, "Labor", "Department of Labor"));
        titles.add(createTitleMap(30, "Mineral Resources", "Department of the Interior"));
        titles.add(createTitleMap(31, "Money and Finance: Treasury", "Department of the Treasury"));
        titles.add(createTitleMap(32, "National Defense", "Department of Defense"));
        titles.add(createTitleMap(33, "Navigation and Navigable Waters", "Department of Homeland Security"));
        titles.add(createTitleMap(34, "Education", "Department of Education"));
        titles.add(createTitleMap(35, "Reserved", "Reserved"));
        titles.add(createTitleMap(36, "Parks, Forests, and Public Property", "Department of the Interior"));
        titles.add(createTitleMap(37, "Patents, Trademarks, and Copyrights", "Department of Commerce"));
        titles.add(createTitleMap(38, "Pensions, Bonuses, and Veterans' Relief", "Department of Veterans Affairs"));
        titles.add(createTitleMap(39, "Postal Service", "United States Postal Service"));
        titles.add(createTitleMap(40, "Protection of Environment", "Environmental Protection Agency"));
        titles.add(createTitleMap(41, "Public Contracts and Property Management", "General Services Administration"));
        titles.add(createTitleMap(42, "Public Health", "Department of Health and Human Services"));
        titles.add(createTitleMap(43, "Public Lands: Interior", "Department of the Interior"));
        titles.add(createTitleMap(44, "Emergency Management and Assistance", "Federal Emergency Management Agency"));
        titles.add(createTitleMap(45, "Public Welfare", "Department of Health and Human Services"));
        titles.add(createTitleMap(46, "Shipping", "Department of Transportation"));
        titles.add(createTitleMap(47, "Telecommunication", "Federal Communications Commission"));
        titles.add(createTitleMap(48, "Federal Acquisition Regulations System", "General Services Administration"));
        titles.add(createTitleMap(49, "Transportation", "Department of Transportation"));
        titles.add(createTitleMap(50, "Wildlife and Fisheries", "Department of the Interior"));
        
        logger.info("Returning {} CFR titles", titles.size());
        return ResponseEntity.ok(titles);
    }
    
    /**
     * Get unique agencies from CFR titles
     */
    @GetMapping("/agencies")
    public ResponseEntity<List<String>> getUniqueAgencies() {
        logger.info("CFR agencies endpoint called");
        
        List<String> agencies = List.of(
            "Department of Agriculture",
            "Department of Commerce", 
            "Department of Defense",
            "Department of Education",
            "Department of Energy",
            "Department of Health and Human Services",
            "Department of Homeland Security",
            "Department of Housing and Urban Development",
            "Department of Justice",
            "Department of Labor",
            "Department of State",
            "Department of the Interior",
            "Department of the Treasury",
            "Department of Transportation",
            "Department of Veterans Affairs",
            "Environmental Protection Agency",
            "Federal Aviation Administration",
            "Federal Communications Commission",
            "Federal Election Commission",
            "Federal Emergency Management Agency",
            "Federal Energy Regulatory Commission",
            "Federal Reserve System",
            "Federal Trade Commission",
            "Food and Drug Administration",
            "General Services Administration",
            "Government Accountability Office",
            "Internal Revenue Service",
            "Office of Management and Budget",
            "Office of Personnel Management",
            "Securities and Exchange Commission",
            "Small Business Administration",
            "United States Postal Service",
            "Executive Office of the President"
        );
        
        logger.info("Returning {} unique agencies", agencies.size());
        return ResponseEntity.ok(agencies);
    }
    
    /**
     * Get detailed metrics for a specific CFR title
     */
    @GetMapping("/titles/{titleNumber}/metrics")
    public ResponseEntity<Map<String, Object>> getTitleMetrics(@PathVariable int titleNumber) {
        logger.info("CFR title {} metrics endpoint called", titleNumber);
        
        if (titleNumber < 1 || titleNumber > 50) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid title number"));
        }
        
        Map<String, Object> metrics = new HashMap<>();
        
        // Mock metrics based on title number (in production, this would query the relationship database)
        int baseWordCount = 30000 + (titleNumber * 1200);
        int avgWordCount = 42000;
        
        metrics.put("wordCount", Map.of(
            "total", baseWordCount,
            "vsAverage", avgWordCount,
            "percentageDifference", Math.round(((double)(baseWordCount - avgWordCount) / avgWordCount * 100) * 10.0) / 10.0
        ));
        
        metrics.put("historicalChanges", Map.of(
            "count", 3 + (titleNumber % 8),
            "period", "Last 2 Years",
            "status", titleNumber % 3 == 0 ? "Growing" : titleNumber % 2 == 0 ? "Declining" : "Stable"
        ));
        
        metrics.put("redundancyScore", Map.of(
            "score", 1 + (titleNumber % 9),
            "maxScore", 10,
            "level", titleNumber % 4 == 0 ? "High" : titleNumber % 3 == 0 ? "Medium" : "Low"
        ));
        
        metrics.put("deregulationPotential", Map.of(
            "opportunities", 1 + (titleNumber % 5),
            "redundantSections", 3 + (titleNumber % 15),
            "conflicts", 2 + (titleNumber % 10)
        ));
        
        logger.info("Returning metrics for CFR title {}", titleNumber);
        return ResponseEntity.ok(metrics);
    }

    private Map<String, Object> createTitleMap(int number, String title, String agency) {
        Map<String, Object> titleMap = new HashMap<>();
        titleMap.put("number", number);
        titleMap.put("title", title);
        titleMap.put("agency", agency);
        titleMap.put("status", "Active"); // Default status
        return titleMap;
    }
}