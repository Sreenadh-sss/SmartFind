package com.smartfind.dto;

import java.time.LocalDate;

/**
 * DTO representing an individual matching FOUND report with similarity score and explanation.
 */
public class MatchResultDto {

    private Long foundReportId;
    private String itemName;
    private String description;
    private String location;
    private LocalDate date;
    private double matchScore;
    private String matchExplanation;
    private String contactName;
    private String contactEmail;

    public MatchResultDto() {
    }

    public MatchResultDto(Long foundReportId, String itemName, String description, String location,
                          LocalDate date, double matchScore, String matchExplanation,
                          String contactName, String contactEmail) {
        this.foundReportId = foundReportId;
        this.itemName = itemName;
        this.description = description;
        this.location = location;
        this.date = date;
        this.matchScore = matchScore;
        this.matchExplanation = matchExplanation;
        this.contactName = contactName;
        this.contactEmail = contactEmail;
    }

    // Getters and Setters

    public Long getFoundReportId() {
        return foundReportId;
    }

    public void setFoundReportId(Long foundReportId) {
        this.foundReportId = foundReportId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String location() {
        return location;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public double getMatchScore() {
        return matchScore;
    }

    public void setMatchScore(double matchScore) {
        this.matchScore = matchScore;
    }

    public String getMatchExplanation() {
        return matchExplanation;
    }

    public void setMatchExplanation(String matchExplanation) {
        this.matchExplanation = matchExplanation;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }
}
