package com.usds.regulations.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for analytics responses containing various metrics and statistics
 */
public class AnalyticsResponse {
    
    private LocalDateTime generatedAt;
    private OverallStats overallStats;
    private List<AgencyStats> agencyStats;
    private List<TitleStats> titleStats;
    private List<RecentChange> recentChanges;
    
    public AnalyticsResponse() {
        this.generatedAt = LocalDateTime.now();
    }
    
    // Nested classes for structured analytics data
    
    public static class OverallStats {
        private long totalRegulations;
        private long totalWordCount;
        private double averageWordCount;
        private int totalAgencies;
        private int totalTitles;
        private LocalDateTime lastUpdate;
        
        public OverallStats() {}
        
        public OverallStats(long totalRegulations, long totalWordCount, double averageWordCount, 
                           int totalAgencies, int totalTitles, LocalDateTime lastUpdate) {
            this.totalRegulations = totalRegulations;
            this.totalWordCount = totalWordCount;
            this.averageWordCount = averageWordCount;
            this.totalAgencies = totalAgencies;
            this.totalTitles = totalTitles;
            this.lastUpdate = lastUpdate;
        }
        
        // Getters and setters
        public long getTotalRegulations() { return totalRegulations; }
        public void setTotalRegulations(long totalRegulations) { this.totalRegulations = totalRegulations; }
        
        public long getTotalWordCount() { return totalWordCount; }
        public void setTotalWordCount(long totalWordCount) { this.totalWordCount = totalWordCount; }
        
        public double getAverageWordCount() { return averageWordCount; }
        public void setAverageWordCount(double averageWordCount) { this.averageWordCount = averageWordCount; }
        
        public int getTotalAgencies() { return totalAgencies; }
        public void setTotalAgencies(int totalAgencies) { this.totalAgencies = totalAgencies; }
        
        public int getTotalTitles() { return totalTitles; }
        public void setTotalTitles(int totalTitles) { this.totalTitles = totalTitles; }
        
        public LocalDateTime getLastUpdate() { return lastUpdate; }
        public void setLastUpdate(LocalDateTime lastUpdate) { this.lastUpdate = lastUpdate; }
    }
    
    public static class AgencyStats {
        private String agencyName;
        private long regulationCount;
        private long totalWordCount;
        private double averageWordCount;
        private double percentageOfTotal;
        private LocalDateTime lastUpdate;
        
        public AgencyStats() {}
        
        public AgencyStats(String agencyName, long regulationCount, long totalWordCount, 
                          double averageWordCount, double percentageOfTotal, LocalDateTime lastUpdate) {
            this.agencyName = agencyName;
            this.regulationCount = regulationCount;
            this.totalWordCount = totalWordCount;
            this.averageWordCount = averageWordCount;
            this.percentageOfTotal = percentageOfTotal;
            this.lastUpdate = lastUpdate;
        }
        
        // Getters and setters
        public String getAgencyName() { return agencyName; }
        public void setAgencyName(String agencyName) { this.agencyName = agencyName; }
        
        public long getRegulationCount() { return regulationCount; }
        public void setRegulationCount(long regulationCount) { this.regulationCount = regulationCount; }
        
        public long getTotalWordCount() { return totalWordCount; }
        public void setTotalWordCount(long totalWordCount) { this.totalWordCount = totalWordCount; }
        
        public double getAverageWordCount() { return averageWordCount; }
        public void setAverageWordCount(double averageWordCount) { this.averageWordCount = averageWordCount; }
        
        public double getPercentageOfTotal() { return percentageOfTotal; }
        public void setPercentageOfTotal(double percentageOfTotal) { this.percentageOfTotal = percentageOfTotal; }
        
        public LocalDateTime getLastUpdate() { return lastUpdate; }
        public void setLastUpdate(LocalDateTime lastUpdate) { this.lastUpdate = lastUpdate; }
    }
    
    public static class TitleStats {
        private int titleNumber;
        private String titleName;
        private long regulationCount;
        private long totalWordCount;
        private double averageWordCount;
        private LocalDateTime lastUpdate;
        
        public TitleStats() {}
        
        public TitleStats(int titleNumber, String titleName, long regulationCount, 
                         long totalWordCount, double averageWordCount, LocalDateTime lastUpdate) {
            this.titleNumber = titleNumber;
            this.titleName = titleName;
            this.regulationCount = regulationCount;
            this.totalWordCount = totalWordCount;
            this.averageWordCount = averageWordCount;
            this.lastUpdate = lastUpdate;
        }
        
        // Getters and setters
        public int getTitleNumber() { return titleNumber; }
        public void setTitleNumber(int titleNumber) { this.titleNumber = titleNumber; }
        
        public String getTitleName() { return titleName; }
        public void setTitleName(String titleName) { this.titleName = titleName; }
        
        public long getRegulationCount() { return regulationCount; }
        public void setRegulationCount(long regulationCount) { this.regulationCount = regulationCount; }
        
        public long getTotalWordCount() { return totalWordCount; }
        public void setTotalWordCount(long totalWordCount) { this.totalWordCount = totalWordCount; }
        
        public double getAverageWordCount() { return averageWordCount; }
        public void setAverageWordCount(double averageWordCount) { this.averageWordCount = averageWordCount; }
        
        public LocalDateTime getLastUpdate() { return lastUpdate; }
        public void setLastUpdate(LocalDateTime lastUpdate) { this.lastUpdate = lastUpdate; }
    }
    
    public static class RecentChange {
        private Long regulationId;
        private String title;
        private String partNumber;
        private String changeType; // CREATED, UPDATED, CONTENT_CHANGED
        private LocalDateTime changeDate;
        private String checksum;
        private String previousChecksum;
        
        public RecentChange() {}
        
        public RecentChange(Long regulationId, String title, String partNumber, 
                           String changeType, LocalDateTime changeDate, String checksum, String previousChecksum) {
            this.regulationId = regulationId;
            this.title = title;
            this.partNumber = partNumber;
            this.changeType = changeType;
            this.changeDate = changeDate;
            this.checksum = checksum;
            this.previousChecksum = previousChecksum;
        }
        
        // Getters and setters
        public Long getRegulationId() { return regulationId; }
        public void setRegulationId(Long regulationId) { this.regulationId = regulationId; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getPartNumber() { return partNumber; }
        public void setPartNumber(String partNumber) { this.partNumber = partNumber; }
        
        public String getChangeType() { return changeType; }
        public void setChangeType(String changeType) { this.changeType = changeType; }
        
        public LocalDateTime getChangeDate() { return changeDate; }
        public void setChangeDate(LocalDateTime changeDate) { this.changeDate = changeDate; }
        
        public String getChecksum() { return checksum; }
        public void setChecksum(String checksum) { this.checksum = checksum; }
        
        public String getPreviousChecksum() { return previousChecksum; }
        public void setPreviousChecksum(String previousChecksum) { this.previousChecksum = previousChecksum; }
    }
    
    // Main class getters and setters
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    
    public OverallStats getOverallStats() { return overallStats; }
    public void setOverallStats(OverallStats overallStats) { this.overallStats = overallStats; }
    
    public List<AgencyStats> getAgencyStats() { return agencyStats; }
    public void setAgencyStats(List<AgencyStats> agencyStats) { this.agencyStats = agencyStats; }
    
    public List<TitleStats> getTitleStats() { return titleStats; }
    public void setTitleStats(List<TitleStats> titleStats) { this.titleStats = titleStats; }
    
    public List<RecentChange> getRecentChanges() { return recentChanges; }
    public void setRecentChanges(List<RecentChange> recentChanges) { this.recentChanges = recentChanges; }
}