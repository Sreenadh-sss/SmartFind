package com.smartfind.service;

import com.smartfind.dto.DashboardStatsDto;
import com.smartfind.dto.ItemRequest;
import com.smartfind.dto.ItemResponse;
import com.smartfind.entity.ItemReport;
import com.smartfind.entity.ItemStatus;
import com.smartfind.entity.ReportType;
import com.smartfind.entity.User;
import com.smartfind.exception.ResourceNotFoundException;
import com.smartfind.exception.UnauthorizedException;
import com.smartfind.repository.ItemReportRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service managing item reports creation, retrieval, search, user-ownership validation, and administrative controls.
 */
@Service
public class ItemReportService {

    private final ItemReportRepository itemReportRepository;
    private final UserService userService;
    private final MatchingService matchingService;

    public ItemReportService(ItemReportRepository itemReportRepository,
                             UserService userService,
                             MatchingService matchingService) {
        this.itemReportRepository = itemReportRepository;
        this.userService = userService;
        this.matchingService = matchingService;
    }

    /**
     * Creates and persists a new item report (LOST or FOUND) associated with a user.
     */
    public ItemResponse createReport(ItemRequest request) {
        User user = userService.getUserById(request.getUserId());

        ItemReport report = new ItemReport();
        report.setItemName(request.getItemName().trim());
        report.setDescription(request.getDescription().trim());
        report.setLocation(request.getLocation().trim());
        report.setItemDate(request.getItemDate());
        report.setReportType(request.getReportType());
        report.setStatus(ItemStatus.ACTIVE);
        report.setUser(user);

        ItemReport saved = itemReportRepository.save(report);
        return ItemResponse.fromEntity(saved);
    }

    /**
     * Retrieves all reports across the system, newest first.
     */
    public List<ItemResponse> getAllReports() {
        return itemReportRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(ItemResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a single report by ID.
     */
    public ItemResponse getReportById(Long id) {
        ItemReport report = itemReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with ID: " + id));
        return ItemResponse.fromEntity(report);
    }

    /**
     * Searches items by keyword, report type, and location.
     */
    public List<ItemResponse> searchReports(String query, ReportType reportType, String location) {
        String cleanQuery = (query != null && !query.isBlank()) ? query.trim() : null;
        String cleanLocation = (location != null && !location.isBlank()) ? location.trim() : null;

        return itemReportRepository.searchItems(cleanQuery, reportType, cleanLocation)
                .stream()
                .map(ItemResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves reports created by a specific user.
     */
    public List<ItemResponse> getMyReports(Long userId) {
        // Validate user exists
        userService.getUserById(userId);

        return itemReportRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(ItemResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Deletes a report with ownership validation.
     * Only the user who created the report or an ADMIN may delete it.
     */
    public void deleteReport(Long reportId, Long requesterUserId) {
        ItemReport report = itemReportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with ID: " + reportId));

        User requester = userService.getUserById(requesterUserId);

        boolean isOwner = report.getUser() != null && report.getUser().getId().equals(requesterUserId);
        boolean isAdmin = "ADMIN".equalsIgnoreCase(requester.getRole());

        if (!isOwner && !isAdmin) {
            throw new UnauthorizedException("Access denied. You do not have permission to delete this report.");
        }

        itemReportRepository.delete(report);
    }

    /**
     * Administrative method to retrieve reports with role validation.
     */
    public List<ItemResponse> getAdminReports(Long adminUserId, ReportType filter) {
        validateAdmin(adminUserId);

        if (filter != null) {
            return itemReportRepository.findByReportTypeOrderByCreatedAtDesc(filter)
                    .stream()
                    .map(ItemResponse::fromEntity)
                    .collect(Collectors.toList());
        }
        return getAllReports();
    }

    /**
     * Administrative method to delete any inappropriate report with role validation.
     */
    public void deleteAdminReport(Long reportId, Long adminUserId) {
        validateAdmin(adminUserId);

        ItemReport report = itemReportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with ID: " + reportId));

        itemReportRepository.delete(report);
    }

    /**
     * Computes dashboard statistics with real potential match calculation.
     */
    public DashboardStatsDto getDashboardStats() {
        long totalReports = itemReportRepository.count();
        long totalLost = itemReportRepository.countByReportType(ReportType.LOST);
        long totalFound = itemReportRepository.countByReportType(ReportType.FOUND);
        long potentialMatches = matchingService.countTotalPotentialMatches();

        return new DashboardStatsDto(totalReports, totalLost, totalFound, potentialMatches);
    }

    /**
     * Verifies that the requester has administrative privileges.
     */
    private void validateAdmin(Long adminUserId) {
        if (adminUserId == null) {
            throw new UnauthorizedException("Admin user ID is required for administrative actions.");
        }
        User user = userService.getUserById(adminUserId);
        if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new UnauthorizedException("Access denied. Administrator privileges required.");
        }
    }
}
