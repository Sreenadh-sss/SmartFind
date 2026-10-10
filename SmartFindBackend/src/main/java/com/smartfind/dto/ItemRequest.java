package com.smartfind.dto;

import com.smartfind.entity.ReportType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * DTO for creating a new Lost or Found item report.
 */
public class ItemRequest {

    @NotBlank(message = "Item name is required")
    @Size(min = 2, max = 150, message = "Item name must be between 2 and 150 characters")
    private String itemName;

    @NotBlank(message = "Description is required")
    @Size(min = 5, message = "Description must provide sufficient details (at least 5 characters)")
    private String description;

    @NotBlank(message = "Location is required")
    @Size(min = 2, max = 150, message = "Location must be between 2 and 150 characters")
    private String location;

    @NotNull(message = "Date is required")
    private LocalDate itemDate;

    @NotNull(message = "Report type must be LOST or FOUND")
    private ReportType reportType;

    @NotNull(message = "User ID is required to associate the report")
    private Long userId;

    public ItemRequest() {
    }

    public ItemRequest(String itemName, String description, String location, LocalDate itemDate, ReportType reportType, Long userId) {
        this.itemName = itemName;
        this.description = description;
        this.location = location;
        this.itemDate = itemDate;
        this.reportType = reportType;
        this.userId = userId;
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDate getItemDate() {
        return itemDate;
    }

    public void setItemDate(LocalDate itemDate) {
        this.itemDate = itemDate;
    }

    public ReportType getReportType() {
        return reportType;
    }

    public void setReportType(ReportType reportType) {
        this.reportType = reportType;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
