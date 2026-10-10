package com.smartfind.controller;

import com.smartfind.dto.ApiResponse;
import com.smartfind.dto.ItemResponse;
import com.smartfind.entity.ReportType;
import com.smartfind.exception.UnauthorizedException;
import com.smartfind.service.ItemReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller providing administrative operations with strict role validation.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final ItemReportService itemReportService;

    public AdminController(ItemReportService itemReportService) {
        this.itemReportService = itemReportService;
    }

    /**
     * Retrieves all items for administrative review.
     * Requires admin userId passed as query param or in X-User-Id header.
     */
    @GetMapping("/items")
    public ResponseEntity<ApiResponse<List<ItemResponse>>> getAdminItems(
            @RequestParam(required = false) Long adminUserId,
            @RequestHeader(value = "X-User-Id", required = false) Long headerAdminId,
            @RequestParam(required = false) ReportType reportType) {

        Long effectiveAdminId = adminUserId != null ? adminUserId : headerAdminId;
        if (effectiveAdminId == null) {
            throw new UnauthorizedException("Administrator identity required. Pass adminUserId parameter or X-User-Id header.");
        }

        List<ItemResponse> items = itemReportService.getAdminReports(effectiveAdminId, reportType);
        return ResponseEntity.ok(ApiResponse.success("Admin reports loaded successfully.", items));
    }

    /**
     * Deletes an inappropriate or resolved item as an administrator.
     */
    @DeleteMapping("/items/{id}")
    public ResponseEntity<ApiResponse<String>> deleteAdminItem(
            @PathVariable Long id,
            @RequestParam(required = false) Long adminUserId,
            @RequestHeader(value = "X-User-Id", required = false) Long headerAdminId) {

        Long effectiveAdminId = adminUserId != null ? adminUserId : headerAdminId;
        if (effectiveAdminId == null) {
            throw new UnauthorizedException("Administrator identity required. Pass adminUserId parameter or X-User-Id header.");
        }

        itemReportService.deleteAdminReport(id, effectiveAdminId);
        return ResponseEntity.ok(ApiResponse.success("Report #" + id + " has been removed by administrator."));
    }
}
