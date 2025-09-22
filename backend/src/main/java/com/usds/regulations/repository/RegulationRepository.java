package com.usds.regulations.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.usds.regulations.entity.Regulation;

@Repository
public interface RegulationRepository extends JpaRepository<Regulation, Long> {
    
    /**
     * Find all regulations for a specific CFR title
     */
    List<Regulation> findByCfrTitle(Integer cfrTitle);
    
    /**
     * Find a specific regulation by CFR title and part number
     */
    Optional<Regulation> findByCfrTitleAndPartNumber(Integer cfrTitle, String partNumber);
    
    /**
     * Check if a regulation exists for the given CFR title and part number
     */
    boolean existsByCfrTitleAndPartNumber(Integer cfrTitle, String partNumber);
    
    /**
     * Find regulations by agency name
     */
    List<Regulation> findByAgencyNameContainingIgnoreCase(String agencyName);
    
    /**
     * Count regulations by CFR title
     */
    @Query("SELECT COUNT(r) FROM Regulation r WHERE r.cfrTitle = :title")
    Long countByTitle(@Param("title") Integer title);
    
    /**
     * Get total word count for all regulations in a specific CFR title
     */
    @Query("SELECT COALESCE(SUM(r.wordCount), 0) FROM Regulation r WHERE r.cfrTitle = :title")
    Long getTotalWordCountByTitle(@Param("title") Integer title);
    
    /**
     * Find regulations with word count greater than specified amount
     */
    List<Regulation> findByWordCountGreaterThan(Integer wordCount);
    
    /**
     * Get average word count for a CFR title
     */
    @Query("SELECT AVG(r.wordCount) FROM Regulation r WHERE r.cfrTitle = :title AND r.wordCount > 0")
    Double getAverageWordCountByTitle(@Param("title") Integer title);
    
    /**
     * Find regulations ordered by word count descending
     */
    List<Regulation> findByCfrTitleOrderByWordCountDesc(Integer cfrTitle);
    
    /**
     * Get agency statistics for analytics
     */
    @Query("SELECT r.agencyName, COUNT(r), COALESCE(SUM(r.wordCount), 0), COALESCE(AVG(r.wordCount), 0), MAX(r.updatedAt) " +
           "FROM Regulation r " +
           "WHERE r.agencyName IS NOT NULL " +
           "GROUP BY r.agencyName " +
           "ORDER BY SUM(r.wordCount) DESC")
    List<Object[]> getAgencyStatistics();
    
    /**
     * Get title statistics for analytics
     */
    @Query("SELECT r.cfrTitle, COUNT(r), COALESCE(SUM(r.wordCount), 0), COALESCE(AVG(r.wordCount), 0), MAX(r.updatedAt) " +
           "FROM Regulation r " +
           "GROUP BY r.cfrTitle " +
           "ORDER BY r.cfrTitle")
    List<Object[]> getTitleStatistics();
    
    /**
     * Get overall statistics
     */
    @Query("SELECT COUNT(r), COALESCE(SUM(r.wordCount), 0), COALESCE(AVG(r.wordCount), 0), " +
           "COUNT(DISTINCT r.agencyName), COUNT(DISTINCT r.cfrTitle), MAX(r.updatedAt) " +
           "FROM Regulation r")
    Object[] getOverallStatistics();
    
    /**
     * Find regulations that have been updated recently
     */
    @Query("SELECT r FROM Regulation r WHERE r.updatedAt > :since ORDER BY r.updatedAt DESC")
    List<Regulation> findRecentlyUpdated(@Param("since") LocalDateTime since);
    
    /**
     * Find regulations by checksum (for change detection)
     */
    List<Regulation> findByContentChecksum(String contentChecksum);
    
    /**
     * Find regulations with different checksums (potential changes)
     */
    @Query("SELECT r FROM Regulation r WHERE r.contentChecksum != :expectedChecksum " +
           "AND r.cfrTitle = :title AND r.partNumber = :partNumber")
    List<Regulation> findWithDifferentChecksum(@Param("title") Integer title, 
                                              @Param("partNumber") String partNumber, 
                                              @Param("expectedChecksum") String expectedChecksum);
    
    /**
     * Get word count distribution by agency
     */
    @Query("SELECT r.agencyName, " +
           "SUM(CASE WHEN r.wordCount BETWEEN 0 AND 100 THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN r.wordCount BETWEEN 101 AND 500 THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN r.wordCount BETWEEN 501 AND 1000 THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN r.wordCount > 1000 THEN 1 ELSE 0 END) " +
           "FROM Regulation r " +
           "WHERE r.agencyName IS NOT NULL " +
           "GROUP BY r.agencyName")
    List<Object[]> getWordCountDistributionByAgency();
    
    /**
     * Find top N regulations by word count
     */
    @Query("SELECT r FROM Regulation r WHERE r.wordCount IS NOT NULL ORDER BY r.wordCount DESC")
    List<Regulation> findTopByWordCount(org.springframework.data.domain.Pageable pageable);
}