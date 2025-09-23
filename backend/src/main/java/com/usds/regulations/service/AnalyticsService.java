package com.usds.regulations.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.usds.regulations.dto.AnalyticsResponse;
import com.usds.regulations.dto.AnalyticsResponse.AgencyStats;
import com.usds.regulations.dto.AnalyticsResponse.OverallStats;
import com.usds.regulations.dto.AnalyticsResponse.RecentChange;
import com.usds.regulations.dto.AnalyticsResponse.TitleStats;
import com.usds.regulations.entity.Regulation;
import com.usds.regulations.repository.RegulationRepository;

@Service
public class AnalyticsService {
    
    private static final Logger logger = LoggerFactory.getLogger(AnalyticsService.class);
    
    @Autowired
    private RegulationRepository regulationRepository;
    
    /**
     * Generate comprehensive analytics report
     */
    public AnalyticsResponse generateAnalyticsReport() {
        logger.info("Generating comprehensive analytics report");
        
        AnalyticsResponse response = new AnalyticsResponse();
        
        try {
            // Generate overall statistics
            response.setOverallStats(generateOverallStats());
            
            // Generate agency statistics
            response.setAgencyStats(generateAgencyStats());
            
            // Generate title statistics
            response.setTitleStats(generateTitleStats());
            
            // Generate recent changes
            response.setRecentChanges(generateRecentChanges());
            
            logger.info("Analytics report generated successfully");
            return response;
            
        } catch (Exception e) {
            logger.error("Error generating analytics report: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate analytics report", e);
        }
    }
    
    /**
     * Generate overall statistics
     */
    private OverallStats generateOverallStats() {
        Object[] stats = regulationRepository.getOverallStatistics();
        
        if (stats != null && stats.length >= 6) {
            long totalRegulations = stats[0] != null ? ((Number) stats[0]).longValue() : 0;
            long totalWordCount = stats[1] != null ? ((Number) stats[1]).longValue() : 0;
            double averageWordCount = stats[2] != null ? ((Number) stats[2]).doubleValue() : 0.0;
            int totalAgencies = stats[3] != null ? ((Number) stats[3]).intValue() : 0;
            int totalTitles = stats[4] != null ? ((Number) stats[4]).intValue() : 0;
            LocalDateTime lastUpdate = (LocalDateTime) stats[5];
            
            return new OverallStats(totalRegulations, totalWordCount, averageWordCount, 
                                   totalAgencies, totalTitles, lastUpdate);
        }
        
        return new OverallStats(0, 0, 0.0, 0, 0, null);
    }
    
    /**
     * Generate agency statistics with word counts per agency
     */
    private List<AgencyStats> generateAgencyStats() {
        List<Object[]> agencyData = regulationRepository.getAgencyStatistics();
        List<AgencyStats> agencyStats = new ArrayList<>();
        
        // Calculate total word count for percentage calculations
        long totalWordCount = regulationRepository.getOverallStatistics()[1] != null ? 
            ((Number) regulationRepository.getOverallStatistics()[1]).longValue() : 0;
        
        for (Object[] data : agencyData) {
            if (data.length >= 5) {
                String agencyName = (String) data[0];
                long regulationCount = data[1] != null ? ((Number) data[1]).longValue() : 0;
                long agencyWordCount = data[2] != null ? ((Number) data[2]).longValue() : 0;
                double averageWordCount = data[3] != null ? ((Number) data[3]).doubleValue() : 0.0;
                LocalDateTime lastUpdate = (LocalDateTime) data[4];
                
                double percentageOfTotal = totalWordCount > 0 ? 
                    (double) agencyWordCount / totalWordCount * 100 : 0.0;
                
                agencyStats.add(new AgencyStats(agencyName, regulationCount, agencyWordCount, 
                                              averageWordCount, percentageOfTotal, lastUpdate));
            }
        }
        
        return agencyStats;
    }
    
    /**
     * Generate title statistics
     */
    private List<TitleStats> generateTitleStats() {
        List<Object[]> titleData = regulationRepository.getTitleStatistics();
        List<TitleStats> titleStats = new ArrayList<>();
        
        // Map of CFR title numbers to names
        Map<Integer, String> titleNames = getTitleNames();
        
        for (Object[] data : titleData) {
            if (data.length >= 5) {
                int titleNumber = data[0] != null ? ((Number) data[0]).intValue() : 0;
                long regulationCount = data[1] != null ? ((Number) data[1]).longValue() : 0;
                long totalWordCount = data[2] != null ? ((Number) data[2]).longValue() : 0;
                double averageWordCount = data[3] != null ? ((Number) data[3]).doubleValue() : 0.0;
                LocalDateTime lastUpdate = (LocalDateTime) data[4];
                
                String titleName = titleNames.getOrDefault(titleNumber, "Unknown Title");
                
                titleStats.add(new TitleStats(titleNumber, titleName, regulationCount, 
                                            totalWordCount, averageWordCount, lastUpdate));
            }
        }
        
        return titleStats;
    }
    
    /**
     * Generate recent changes list
     */
    private List<RecentChange> generateRecentChanges() {
        // Get regulations updated in the last 7 days
        try {
            LocalDateTime since = LocalDateTime.now().minusDays(7);
            List<Regulation> recentlyUpdated = regulationRepository.findRecentlyUpdated(since);
            
            List<RecentChange> recentChanges = new ArrayList<>();
            
            for (Regulation regulation : recentlyUpdated) {
                String changeType = determineChangeType(regulation);
                
                recentChanges.add(new RecentChange(
                    regulation.getId(),
                    regulation.getTitle(),
                    regulation.getPartNumber(),
                    changeType,
                    regulation.getUpdatedAt(),
                    regulation.getContentChecksum(),
                    null // Previous checksum would require change history tracking
                ));
            }
            
            return recentChanges;
        } catch (Exception e) {
            logger.warn("Error generating recent changes, returning empty list: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Determine change type for a regulation
     */
    private String determineChangeType(Regulation regulation) {
        // If created and updated times are very close, it's likely a new creation
        if (regulation.getCreatedAt() != null && regulation.getUpdatedAt() != null) {
            long diffSeconds = java.time.Duration.between(regulation.getCreatedAt(), regulation.getUpdatedAt()).getSeconds();
            if (diffSeconds < 60) { // Less than a minute difference
                return "CREATED";
            }
        }
        return "UPDATED";
    }
    
    /**
     * Get agency-specific analytics
     */
    public AnalyticsResponse getAgencyAnalytics(String agencyName) {
        logger.info("Generating analytics for agency: {}", agencyName);
        
        List<Regulation> agencyRegulations = regulationRepository.findByAgencyNameContainingIgnoreCase(agencyName);
        
        AnalyticsResponse response = new AnalyticsResponse();
        
        // Calculate agency-specific stats
        long regulationCount = agencyRegulations.size();
        long totalWordCount = agencyRegulations.stream()
            .mapToInt(r -> r.getWordCount() != null ? r.getWordCount() : 0)
            .sum();
        double averageWordCount = regulationCount > 0 ? (double) totalWordCount / regulationCount : 0.0;
        
        LocalDateTime lastUpdate = agencyRegulations.stream()
            .map(Regulation::getUpdatedAt)
            .max(LocalDateTime::compareTo)
            .orElse(null);
        
        // Create single agency stats
        AgencyStats agencyStats = new AgencyStats(agencyName, regulationCount, totalWordCount, 
                                                 averageWordCount, 100.0, lastUpdate);
        response.setAgencyStats(List.of(agencyStats));
        
        return response;
    }
    
    /**
     * Get word count distribution analytics
     */
    public Map<String, Object> getWordCountDistribution() {
        logger.info("Generating word count distribution analytics");
        
        List<Object[]> distribution = regulationRepository.getWordCountDistributionByAgency();
        Map<String, Object> result = new HashMap<>();
        
        for (Object[] data : distribution) {
            if (data.length >= 5) {
                String agencyName = (String) data[0];
                Map<String, Long> agencyDistribution = new HashMap<>();
                agencyDistribution.put("0-100", data[1] != null ? ((Number) data[1]).longValue() : 0);
                agencyDistribution.put("101-500", data[2] != null ? ((Number) data[2]).longValue() : 0);
                agencyDistribution.put("501-1000", data[3] != null ? ((Number) data[3]).longValue() : 0);
                agencyDistribution.put("1000+", data[4] != null ? ((Number) data[4]).longValue() : 0);
                
                result.put(agencyName, agencyDistribution);
            }
        }
        
        return result;
    }
    
    /**
     * Get top regulations by word count
     */
    public List<Regulation> getTopRegulationsByWordCount(int limit) {
        logger.info("Getting top {} regulations by word count", limit);
        return regulationRepository.findTopByWordCount(PageRequest.of(0, limit));
    }
    
    /**
     * Map of CFR title numbers to human-readable names
     */
    private Map<Integer, String> getTitleNames() {
        Map<Integer, String> titleNames = new HashMap<>();
        titleNames.put(1, "General Provisions");
        titleNames.put(2, "Federal Financial Assistance");
        titleNames.put(3, "The President");
        titleNames.put(4, "Accounts");
        titleNames.put(5, "Administrative Personnel");
        titleNames.put(6, "Domestic Security");
        titleNames.put(7, "Agriculture");
        titleNames.put(8, "Aliens and Nationality");
        titleNames.put(9, "Animals and Animal Products");
        titleNames.put(10, "Energy");
        titleNames.put(11, "Federal Elections");
        titleNames.put(12, "Banks and Banking");
        titleNames.put(13, "Business Credit and Assistance");
        titleNames.put(14, "Aeronautics and Space");
        titleNames.put(15, "Commerce and Foreign Trade");
        titleNames.put(16, "Commercial Practices");
        titleNames.put(17, "Commodity and Securities Exchanges");
        titleNames.put(18, "Conservation of Power and Water Resources");
        titleNames.put(19, "Customs Duties");
        titleNames.put(20, "Employees' Benefits");
        titleNames.put(21, "Food and Drugs");
        titleNames.put(22, "Foreign Relations");
        titleNames.put(23, "Highways");
        titleNames.put(24, "Housing and Urban Development");
        titleNames.put(25, "Indians");
        titleNames.put(26, "Internal Revenue");
        titleNames.put(27, "Alcohol, Tobacco Products and Firearms");
        titleNames.put(28, "Judicial Administration");
        titleNames.put(29, "Labor");
        titleNames.put(30, "Mineral Resources");
        titleNames.put(31, "Money and Finance: Treasury");
        titleNames.put(32, "National Defense");
        titleNames.put(33, "Navigation and Navigable Waters");
        titleNames.put(34, "Education");
        titleNames.put(35, "Reserved");
        titleNames.put(36, "Parks, Forests, and Public Property");
        titleNames.put(37, "Patents, Trademarks, and Copyrights");
        titleNames.put(38, "Pensions, Bonuses, and Veterans' Relief");
        titleNames.put(39, "Postal Service");
        titleNames.put(40, "Protection of Environment");
        titleNames.put(41, "Public Contracts and Property Management");
        titleNames.put(42, "Public Health");
        titleNames.put(43, "Public Lands");
        titleNames.put(44, "Emergency Management and Assistance");
        titleNames.put(45, "Public Welfare");
        titleNames.put(46, "Shipping");
        titleNames.put(47, "Telecommunication");
        titleNames.put(48, "Federal Acquisition Regulation");
        titleNames.put(49, "Transportation");
        titleNames.put(50, "Wildlife and Fisheries");
        
        return titleNames;
    }
}