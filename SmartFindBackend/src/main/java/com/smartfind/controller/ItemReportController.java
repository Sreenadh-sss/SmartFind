package com.smartfind.controller;

import com.smartfind.dto.ApiResponse;
import com.smartfind.dto.DashboardStatsDto;
import com.smartfind.dto.ItemRequest;
import com.smartfind.dto.ItemResponse;
import com.smartfind.entity.ReportType;
import com.smartfind.exception.BadRequestException;
import com.smartfind.service.ItemReportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller handling item reporting, search, user-specific reports, and deletion.
 */
@RestController
@RequestMapping("/api/items")
public class ItemReportController {

    private final ItemReportService itemReportService;

    public ItemReportController(ItemReportService itemReportService) {
        this.itemReportService = itemReportService;
    }

    /**
     * Reports a new Lost or Found item.
     */
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<ItemResponse>> addItem(@Valid @RequestBody ItemRequest request) {
        ItemResponse response = itemReportService.createReport(request);
        String msg = request.getReportType() == ReportType.LOST
                ? "Lost item report registered successfully!"
                : "Found item report submitted successfully! Thank you for helping your peers.";
        return new ResponseEntity<>(ApiResponse.success(msg, response), HttpStatus.CREATED);
    }

    /**
     * Retrieves all reported items (both LOST and FOUND).
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<ItemResponse>>> getAllItems() {
        List<ItemResponse> items = itemReportService.getAllReports();
        return ResponseEntity.ok(ApiResponse.success("Reports retrieved successfully.", items));
    }

    /**
     * Searches items by keyword, report type filter, and location.
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ItemResponse>>> searchItems(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) ReportType reportType,
            @RequestParam(required = false) String location) {
        List<ItemResponse> results = itemReportService.searchReports(query, reportType, location);
        return ResponseEntity.ok(ApiResponse.success("Found " + results.size() + " report(s).", results));
    }

    /**
     * Retrieves all reports submitted by a specific user.
     */
    @GetMapping("/my-reports/{userId}")
    public ResponseEntity<ApiResponse<List<ItemResponse>>> getMyReports(@PathVariable Long userId) {
        List<ItemResponse> reports = itemReportService.getMyReports(userId);
        return ResponseEntity.ok(ApiResponse.success("User reports retrieved successfully.", reports));
    }

    /**
     * Deletes a report with ownership validation.
     * Requester user ID can be passed as query param or in X-User-Id header.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteItem(
            @PathVariable Long id,
            @RequestParam(required = false) Long userId,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId) {

        Long effectiveUserId = userId != null ? userId : headerUserId;
        if (effectiveUserId == null) {
            throw new BadRequestException("User ID is required to verify ownership before deletion (pass ?userId=X or header X-User-Id).");
        }

        itemReportService.deleteReport(id, effectiveUserId);
        return ResponseEntity.ok(ApiResponse.success("Report #" + id + " has been successfully deleted."));
    }

    /**
     * Retrieves dashboard statistics with live calculated potential matches.
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStatsDto>> getStats() {
        DashboardStatsDto stats = itemReportService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success("Dashboard statistics loaded.", stats));
    }
}
