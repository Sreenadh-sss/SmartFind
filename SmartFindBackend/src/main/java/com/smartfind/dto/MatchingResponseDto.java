package com.smartfind.dto;

import java.util.List;

/**
 * DTO returned by GET /api/matching/{lostItemId} containing all matches found.
 */
public class MatchingResponseDto {

    private Long lostReportId;
    private String lostItemName;
    private int totalMatches;
    private List<MatchResultDto> matches;
    private String message;

    public MatchingResponseDto() {
    }

    public MatchingResponseDto(Long lostReportId, String lostItemName, int totalMatches,
                               List<MatchResultDto> matches, String message) {
        this.lostReportId = lostReportId;
        this.lostItemName = lostItemName;
        this.totalMatches = totalMatches;
        this.matches = matches;
        this.message = message;
    }

    public Long getLostReportId() {
        return lostReportId;
    }

    public void setLostReportId(Long lostReportId) {
        this.lostReportId = lostReportId;
    }

    public String getLostItemName() {
        return lostItemName;
    }

    public void setLostItemName(String lostItemName) {
        this.lostItemName = lostItemName;
    }

    public int getTotalMatches() {
        return totalMatches;
    }

    public void setTotalMatches(int totalMatches) {
        this.totalMatches = totalMatches;
    }

    public List<MatchResultDto> getMatches() {
        return matches;
    }

    public void setMatches(List<MatchResultDto> matches) {
        this.matches = matches;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
