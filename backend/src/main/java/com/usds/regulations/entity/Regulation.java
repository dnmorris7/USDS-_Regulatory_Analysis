package com.usds.regulations.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "regulations", indexes = {
    @Index(name = "idx_cfr_title", columnList = "cfr_title"),
    @Index(name = "idx_part_number", columnList = "part_number"),
    @Index(name = "idx_cfr_title_part", columnList = "cfr_title,part_number")
})
public class Regulation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @Column(name = "cfr_title", nullable = false)
    private Integer cfrTitle;
    
    @NotNull
    @Size(max = 50)
    @Column(name = "part_number", nullable = false, length = 50)
    private String partNumber;
    
    @NotNull
    @Size(max = 1000)
    @Column(name = "title", nullable = false, length = 1000)
    private String title;
    
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;
    
    @Size(max = 255)
    @Column(name = "agency_name", length = 255)
    private String agencyName;
    
    @Column(name = "word_count")
    private Integer wordCount;
    
    @Size(max = 64)
    @Column(name = "content_checksum", length = 64)
    private String contentChecksum;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public Regulation() {
    }
    
    public Regulation(Integer cfrTitle, String partNumber, String title, String content, String agencyName) {
        this.cfrTitle = cfrTitle;
        this.partNumber = partNumber;
        this.title = title;
        this.content = content;
        this.agencyName = agencyName;
        this.wordCount = calculateWordCount(content);
        this.contentChecksum = generateChecksum(content);
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (wordCount == null) {
            wordCount = calculateWordCount(content);
        }
        if (contentChecksum == null) {
            contentChecksum = generateChecksum(content);
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        wordCount = calculateWordCount(content);
        contentChecksum = generateChecksum(content);
    }
    
    private Integer calculateWordCount(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        String[] words = text.trim().split("\\s+");
        return words.length;
    }
    
    /**
     * Generate SHA-256 checksum for content change detection
     */
    private String generateChecksum(String text) {
        if (text == null) {
            text = "";
        }
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(text.getBytes("UTF-8"));
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
            // Fallback to simple hash if SHA-256 fails
            return String.valueOf(text.hashCode());
        }
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Integer getCfrTitle() {
        return cfrTitle;
    }
    
    public void setCfrTitle(Integer cfrTitle) {
        this.cfrTitle = cfrTitle;
    }
    
    public String getPartNumber() {
        return partNumber;
    }
    
    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
        this.wordCount = calculateWordCount(content);
        this.contentChecksum = generateChecksum(content);
    }
    
    public String getAgencyName() {
        return agencyName;
    }
    
    public void setAgencyName(String agencyName) {
        this.agencyName = agencyName;
    }
    
    public Integer getWordCount() {
        return wordCount;
    }
    
    public void setWordCount(Integer wordCount) {
        this.wordCount = wordCount;
    }
    
    public String getContentChecksum() {
        return contentChecksum;
    }
    
    public void setContentChecksum(String contentChecksum) {
        this.contentChecksum = contentChecksum;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public String toString() {
        return "Regulation{" +
                "id=" + id +
                ", cfrTitle=" + cfrTitle +
                ", partNumber='" + partNumber + '\'' +
                ", title='" + title + '\'' +
                ", agencyName='" + agencyName + '\'' +
                ", wordCount=" + wordCount +
                '}';
    }
}