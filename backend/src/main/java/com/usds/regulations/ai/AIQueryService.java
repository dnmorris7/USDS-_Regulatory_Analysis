package com.usds.regulations.ai;

import com.usds.regulations.entity.Regulation;
import com.usds.regulations.repository.RegulationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service for safely querying the database based on user questions
 * Uses ONLY parameterized JPA queries - NO SQL INJECTION POSSIBLE
 */
@Service
public class AIQueryService {

    private static final Logger logger = LoggerFactory.getLogger(AIQueryService.class);

    @Autowired
    private RegulationRepository regulationRepository;

    /**
     * Process a user question and query the database safely
     * 
     * @param userQuestion The user's natural language question
     * @return Query results with metadata
     */
    public QueryResult processQuestion(String userQuestion) {
        logger.info("Processing question: {}", userQuestion);
        
        String question = userQuestion.toLowerCase().trim();
        
        try {
            // Pattern matching for safe query routing
            if (matchesPattern(question, "how many", "count", "total")) {
                return countTitles();
            }
            
            if (matchesPattern(question, "list", "show", "all", "titles")) {
                if (containsAgency(question)) {
                    String agency = extractAgency(question);
                    return getTitlesByAgency(agency);
                }
                return getAllTitles();
            }
            
            if (matchesPattern(question, "agency", "department")) {
                String agency = extractAgency(question);
                return getTitlesByAgency(agency);
            }
            
            if (matchesPattern(question, "title") && containsNumber(question)) {
                Integer titleNumber = extractNumber(question);
                if (titleNumber != null) {
                    return getTitleByNumber(titleNumber);
                }
            }
            
            // No specific pattern matched
            return QueryResult.noData("I can help you query the CFR database. Try asking: 'How many CFR titles are there?' or 'Show DHS regulations'");
            
        } catch (Exception e) {
            logger.error("Error processing question: {}", e.getMessage(), e);
            return QueryResult.error("Error querying database: " + e.getMessage());
        }
    }

    /**
     * Count all CFR titles (count unique CFR titles from regulations)
     */
    private QueryResult countTitles() {
        List<Regulation> allRegulations = regulationRepository.findAll();
        long uniqueTitleCount = allRegulations.stream()
                .map(Regulation::getCfrTitle)
                .distinct()
                .count();
        
        Map<String, Object> data = new HashMap<>();
        data.put("uniqueTitles", uniqueTitleCount);
        data.put("totalRegulations", allRegulations.size());
        data.put("type", "count");
        
        String summary = String.format("There are %d unique CFR titles with %d total regulations in the database.", 
                uniqueTitleCount, allRegulations.size());
        
        return QueryResult.success(data, summary);
    }

    /**
     * Get all regulations grouped by CFR title (limited to 50 for safety)
     */
    private QueryResult getAllTitles() {
        List<Regulation> regulations = regulationRepository.findAll();
        
        // Group by CFR title
        Map<Integer, List<Regulation>> groupedByTitle = regulations.stream()
                .collect(Collectors.groupingBy(Regulation::getCfrTitle));
        
        // Create summary info for each title
        List<Map<String, Object>> titleSummaries = groupedByTitle.entrySet().stream()
                .limit(50)
                .map(entry -> {
                    Map<String, Object> titleInfo = new HashMap<>();
                    titleInfo.put("titleNumber", entry.getKey());
                    titleInfo.put("regulationCount", entry.getValue().size());
                    titleInfo.put("agencies", entry.getValue().stream()
                            .map(Regulation::getAgencyName)
                            .distinct()
                            .collect(Collectors.toList()));
                    return titleInfo;
                })
                .collect(Collectors.toList());
        
        Map<String, Object> data = new HashMap<>();
        data.put("titles", titleSummaries);
        data.put("count", titleSummaries.size());
        data.put("type", "list");
        
        String summary = String.format("Found %d CFR titles (showing first 50).", titleSummaries.size());
        
        return QueryResult.success(data, summary);
    }

    /**
     * Get regulations by agency (safe parameterized query)
     */
    private QueryResult getTitlesByAgency(String agency) {
        if (agency == null || agency.isBlank()) {
            return QueryResult.error("Agency name not specified");
        }
        
        // Use parameterized query - NO SQL INJECTION
        List<Regulation> regulations = regulationRepository.findByAgencyNameContainingIgnoreCase(agency);
        
        // Group by CFR title
        Map<Integer, Long> titleCounts = regulations.stream()
                .collect(Collectors.groupingBy(Regulation::getCfrTitle, Collectors.counting()));
        
        Map<String, Object> data = new HashMap<>();
        data.put("regulations", regulations.stream().limit(20).collect(Collectors.toList()));
        data.put("agency", agency);
        data.put("totalRegulations", regulations.size());
        data.put("uniqueTitles", titleCounts.size());
        data.put("titleBreakdown", titleCounts);
        data.put("type", "agency");
        
        String summary = regulations.isEmpty() 
            ? String.format("No regulations found for agency: %s", agency)
            : String.format("Found %d regulations across %d CFR titles for %s.", 
                    regulations.size(), titleCounts.size(), agency);
        
        return QueryResult.success(data, summary);
    }

    /**
     * Get regulations by CFR title number (safe parameterized query)
     */
    private QueryResult getTitleByNumber(Integer titleNumber) {
        // Use parameterized query - NO SQL INJECTION
        List<Regulation> regulations = regulationRepository.findByCfrTitle(titleNumber);
        
        Map<String, Object> data = new HashMap<>();
        data.put("regulations", regulations.stream().limit(20).collect(Collectors.toList()));
        data.put("titleNumber", titleNumber);
        data.put("count", regulations.size());
        data.put("type", "title");
        
        // Get unique agencies
        List<String> agencies = regulations.stream()
                .map(Regulation::getAgencyName)
                .distinct()
                .collect(Collectors.toList());
        data.put("agencies", agencies);
        
        String summary = regulations.isEmpty()
            ? String.format("CFR Title %d not found.", titleNumber)
            : String.format("Found %d regulations in CFR Title %d (showing first 20).", 
                    regulations.size(), titleNumber);
        
        return QueryResult.success(data, summary);
    }

    // ==================== Helper Methods ====================

    /**
     * Check if question matches any of the given patterns
     */
    private boolean matchesPattern(String question, String... keywords) {
        for (String keyword : keywords) {
            if (question.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if question contains an agency reference
     */
    private boolean containsAgency(String question) {
        String[] agencies = {"dhs", "homeland", "security", "defense", "dod", "epa", "fda", "agriculture", "usda"};
        return matchesPattern(question, agencies);
    }

    /**
     * Extract agency name from question
     */
    private String extractAgency(String question) {
        // Map of keywords to agency names
        Map<String, String> agencyMap = Map.of(
            "dhs", "Homeland Security",
            "homeland", "Homeland Security",
            "security", "Homeland Security",
            "defense", "Defense",
            "dod", "Defense",
            "epa", "Environmental Protection",
            "fda", "Food and Drug",
            "agriculture", "Agriculture",
            "usda", "Agriculture"
        );
        
        for (Map.Entry<String, String> entry : agencyMap.entrySet()) {
            if (question.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        
        return null;
    }

    /**
     * Check if question contains a number
     */
    private boolean containsNumber(String question) {
        return Pattern.compile("\\d+").matcher(question).find();
    }

    /**
     * Extract number from question
     */
    private Integer extractNumber(String question) {
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(question);
        
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        
        return null;
    }

    /**
     * Result object for database queries
     */
    public static class QueryResult {
        private final Map<String, Object> data;
        private final String summary;
        private final boolean success;
        private final String error;

        private QueryResult(Map<String, Object> data, String summary, boolean success, String error) {
            this.data = data;
            this.summary = summary;
            this.success = success;
            this.error = error;
        }

        public static QueryResult success(Map<String, Object> data, String summary) {
            return new QueryResult(data, summary, true, null);
        }

        public static QueryResult error(String error) {
            return new QueryResult(null, null, false, error);
        }

        public static QueryResult noData(String message) {
            return new QueryResult(new HashMap<>(), message, true, null);
        }

        // Getters
        public Map<String, Object> getData() { return data; }
        public String getSummary() { return summary; }
        public boolean isSuccess() { return success; }
        public String getError() { return error; }
        public boolean hasData() { return data != null && !data.isEmpty(); }
    }
}
