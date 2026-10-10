package com.smartfind.dto;

/**
 * DTO for Dashboard statistical overview cards.
 */
public class DashboardStatsDto {

    private long totalReports;
    private long totalLost;
    private long totalFound;
    private long potentialMatches;

    public DashboardStatsDto() {
    }

    public DashboardStatsDto(long totalReports, long totalLost, long totalFound, long potentialMatches) {
        this.totalReports = totalReports;
        this.totalLost = totalLost;
        this.totalFound = totalFound;
        this.potentialMatches = potentialMatches;
    }

    public long getTotalReports() {
        return totalReports;
    }

    public void setTotalReports(long totalReports) {
        this.totalReports = totalReports;
    }

    public long getTotalLost() {
        return totalLost;
    }

    public void setTotalLost(long totalLost) {
        this.totalLost = totalLost;
    }

    public long getTotalFound() {
        return totalFound;
    }

    public void setTotalFound(long totalFound) {
        this.totalFound = totalFound;
    }

    public long getPotentialMatches() {
        return potentialMatches;
    }

    public void setPotentialMatches(long potentialMatches) {
        this.potentialMatches = potentialMatches;
    }
}
