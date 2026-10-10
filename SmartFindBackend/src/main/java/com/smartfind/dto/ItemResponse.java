package com.smartfind.dto;

import com.smartfind.entity.ItemReport;
import com.smartfind.entity.ItemStatus;
import com.smartfind.entity.ReportType;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO returned for item reports.
 */
public class ItemResponse {

    private Long id;
    private String itemName;
    private String description;
    private String location;
    private LocalDate itemDate;
    private ReportType reportType;
    private ItemStatus status;
    private Long userId;
    private String userName;
    private String userEmail;
    private LocalDateTime createdAt;

    public ItemResponse() {
    }

    public static ItemResponse fromEntity(ItemReport item) {
        ItemResponse res = new ItemResponse();
        res.setId(item.getId());
        res.setItemName(item.getItemName());
        res.setDescription(item.getDescription());
        res.setLocation(item.getLocation());
        res.setItemDate(item.getItemDate());
        res.setReportType(item.getReportType());
        res.setStatus(item.getStatus());
        if (item.getUser() != null) {
            res.setUserId(item.getUser().getId());
            res.setUserName(item.getUser().getName());
            res.setUserEmail(item.getUser().getEmail());
        }
        res.setCreatedAt(item.getCreatedAt());
        return res;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public ItemStatus getStatus() {
        return status;
    }

    public void setStatus(ItemStatus status) {
        this.status = status;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
