package com.smartfind.controller;

import com.smartfind.dto.ApiResponse;
import com.smartfind.dto.MatchingResponseDto;
import com.smartfind.service.MatchingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller handling smart AI-based lost-and-found matching requests.
 */
@RestController
@RequestMapping("/api/matching")
public class MatchingController {

    private final MatchingService matchingService;

    public MatchingController(MatchingService matchingService) {
        this.matchingService = matchingService;
    }

    /**
     * Executes the intelligent matching engine for a given LOST report ID.
     * Evaluates item name (30%), description keywords (30%), location (25%), and date proximity (15%).
     */
    @GetMapping("/{lostItemId}")
    public ResponseEntity<ApiResponse<MatchingResponseDto>> getMatches(@PathVariable Long lostItemId) {
        MatchingResponseDto response = matchingService.findMatchesForLostItem(lostItemId);
        return ResponseEntity.ok(ApiResponse.success("Matching evaluation completed.", response));
    }
}
