package com.smartfind.service;

import com.smartfind.dto.MatchResultDto;
import com.smartfind.dto.MatchingResponseDto;
import com.smartfind.entity.ItemReport;
import com.smartfind.entity.ItemStatus;
import com.smartfind.entity.ReportType;
import com.smartfind.exception.BadRequestException;
import com.smartfind.exception.ResourceNotFoundException;
import com.smartfind.repository.ItemReportRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Intelligent Matching Engine that matches LOST reports with FOUND reports.
 * Uses a transparent weighted similarity scoring algorithm (0 to 100):
 * - Item Name Similarity: 30%
 * - Description Keywords: 30%
 * - Location Similarity: 25%
 * - Date Proximity: 15%
 */
@Service
public class MatchingService {

    private final ItemReportRepository itemReportRepository;

    @Value("${matching.threshold:60.0}")
    private double defaultThreshold;

    private static final Set<String> STOP_WORDS = Set.of(
            "a", "an", "the", "and", "or", "in", "on", "at", "to", "for",
            "of", "with", "by", "from", "is", "was", "are", "were", "my",
            "i", "it", "this", "that", "there", "has", "have", "had", "near",
            "around", "found", "lost", "some", "please", "help"
    );

    public MatchingService(ItemReportRepository itemReportRepository) {
        this.itemReportRepository = itemReportRepository;
    }

    /**
     * Finds matching FOUND reports for a given LOST report ID.
     */
    public MatchingResponseDto findMatchesForLostItem(Long lostItemId) {
        ItemReport lostItem = itemReportRepository.findById(lostItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with ID: " + lostItemId));

        if (lostItem.getReportType() != ReportType.LOST) {
            throw new BadRequestException("Matching can only be performed on LOST item reports. Report ID "
                    + lostItemId + " is marked as " + lostItem.getReportType() + ".");
        }

        // Retrieve all active FOUND items (never compare against lost items or inactive reports)
        List<ItemReport> foundItems = itemReportRepository.findByReportTypeAndStatus(ReportType.FOUND, ItemStatus.ACTIVE);

        List<MatchResultDto> matches = new ArrayList<>();

        for (ItemReport foundItem : foundItems) {
            // Safety check: Never compare against itself
            if (foundItem.getId().equals(lostItem.getId())) {
                continue;
            }

            MatchCalculation calc = calculateSimilarity(lostItem, foundItem);

            if (calc.totalScore >= defaultThreshold) {
                MatchResultDto matchDto = new MatchResultDto(
                        foundItem.getId(),
                        foundItem.getItemName(),
                        foundItem.getDescription(),
                        foundItem.getLocation(),
                        foundItem.getItemDate(),
                        Math.round(calc.totalScore * 10.0) / 10.0,
                        calc.explanation,
                        foundItem.getUser() != null ? foundItem.getUser().getName() : "Anonymous",
                        foundItem.getUser() != null ? foundItem.getUser().getEmail() : "N/A"
                );
                matches.add(matchDto);
            }
        }

        // Sort matches descending by score
        matches.sort((a, b) -> Double.compare(b.getMatchScore(), a.getMatchScore()));

        String message;
        if (matches.isEmpty()) {
            message = "No matching found items currently reach the " + defaultThreshold + "% confidence threshold for '" + lostItem.getItemName() + "'.";
        } else {
            message = "Found " + matches.size() + " potential match" + (matches.size() > 1 ? "es" : "") + " exceeding " + defaultThreshold + "% similarity threshold.";
        }

        return new MatchingResponseDto(
                lostItem.getId(),
                lostItem.getItemName(),
                matches.size(),
                matches,
                message
        );
    }

    /**
     * Computes the total number of potential matches across all active lost items.
     * Used by the Dashboard card for real-time statistical accuracy.
     */
    public long countTotalPotentialMatches() {
        List<ItemReport> lostItems = itemReportRepository.findByReportTypeAndStatus(ReportType.LOST, ItemStatus.ACTIVE);
        List<ItemReport> foundItems = itemReportRepository.findByReportTypeAndStatus(ReportType.FOUND, ItemStatus.ACTIVE);

        long potentialMatchCount = 0;

        for (ItemReport lost : lostItems) {
            for (ItemReport found : foundItems) {
                if (lost.getId().equals(found.getId())) continue;
                MatchCalculation calc = calculateSimilarity(lost, found);
                if (calc.totalScore >= defaultThreshold) {
                    potentialMatchCount++;
                }
            }
        }

        return potentialMatchCount;
    }

    /**
     * Internal structure holding score components and human-readable explanation.
     */
    private static class MatchCalculation {
        double nameScore;
        double descScore;
        double locScore;
        double dateScore;
        double totalScore;
        String explanation;
    }

    /**
     * Calculates weighted similarity between a LOST and a FOUND report.
     */
    private MatchCalculation calculateSimilarity(ItemReport lost, ItemReport found) {
        // 1. Item Name Similarity (Weight: 30%)
        double nameRatio = calculateTextSimilarity(lost.getItemName(), found.getItemName());
        double nameScore = nameRatio * 30.0;

        // 2. Description Keyword Similarity (Weight: 30%)
        double descRatio = calculateKeywordSimilarity(lost.getDescription(), found.getDescription());
        double descScore = descRatio * 30.0;

        // 3. Location Similarity (Weight: 25%)
        double locRatio = calculateLocationSimilarity(lost.getLocation(), found.getLocation());
        double locScore = locRatio * 25.0;

        // 4. Date Proximity (Weight: 15%)
        double dateRatio = calculateDateProximity(lost.getItemDate(), found.getItemDate());
        double dateScore = dateRatio * 15.0;

        double totalScore = nameScore + descScore + locScore + dateScore;

        // Construct detailed breakdown explanation
        String explanation = String.format(
                "Name: %.1f/30 (%.0f%%), Description: %.1f/30 (%.0f%%), Location: %.1f/25 (%.0f%%), Date: %.1f/15 (%.0f%%)",
                nameScore, nameRatio * 100,
                descScore, descRatio * 100,
                locScore, locRatio * 100,
                dateScore, dateRatio * 100
        );

        MatchCalculation calc = new MatchCalculation();
        calc.nameScore = nameScore;
        calc.descScore = descScore;
        calc.locScore = locScore;
        calc.dateScore = dateScore;
        calc.totalScore = totalScore;
        calc.explanation = explanation;

        return calc;
    }

    /**
     * Calculates text similarity based on substring containment, word overlap, and Levenshtein distance.
     */
    private double calculateTextSimilarity(String textA, String textB) {
        if (textA == null || textB == null) return 0.0;
        String a = textA.trim().toLowerCase();
        String b = textB.trim().toLowerCase();

        if (a.equals(b)) return 1.0;

        // Substring check
        if (a.contains(b) || b.contains(a)) {
            int shorterLen = Math.min(a.length(), b.length());
            int longerLen = Math.max(a.length(), b.length());
            return Math.max(0.75, (double) shorterLen / longerLen);
        }

        // Word overlap
        Set<String> wordsA = tokenize(a);
        Set<String> wordsB = tokenize(b);
        double jaccard = calculateJaccard(wordsA, wordsB);

        // Normalized Levenshtein
        double levSim = calculateNormalizedLevenshtein(a, b);

        return Math.max(jaccard, levSim);
    }

    /**
     * Calculates keyword overlap for descriptions, filtering out common stop words.
     */
    private double calculateKeywordSimilarity(String descA, String descB) {
        if (descA == null || descB == null) return 0.0;

        Set<String> wordsA = extractKeywords(descA);
        Set<String> wordsB = extractKeywords(descB);

        if (wordsA.isEmpty() || wordsB.isEmpty()) {
            return calculateTextSimilarity(descA, descB);
        }

        Set<String> intersection = new HashSet<>(wordsA);
        intersection.retainAll(wordsB);

        if (intersection.isEmpty()) {
            return 0.0;
        }

        // Jaccard similarity and containment ratio
        double containment = (double) intersection.size() / Math.min(wordsA.size(), wordsB.size());
        double jaccard = (double) intersection.size() / (wordsA.size() + wordsB.size() - intersection.size());

        return (containment * 0.7) + (jaccard * 0.3);
    }

    /**
     * Calculates location similarity considering campus landmarks, room numbers, and substrings.
     */
    private double calculateLocationSimilarity(String locA, String locB) {
        if (locA == null || locB == null) return 0.0;
        String a = locA.trim().toLowerCase();
        String b = locB.trim().toLowerCase();

        if (a.equals(b)) return 1.0;

        if (a.contains(b) || b.contains(a)) {
            return 0.85;
        }

        Set<String> tokensA = tokenize(a);
        Set<String> tokensB = tokenize(b);
        Set<String> common = new HashSet<>(tokensA);
        common.retainAll(tokensB);

        if (!common.isEmpty()) {
            return (double) common.size() / Math.min(tokensA.size(), tokensB.size());
        }

        return calculateNormalizedLevenshtein(a, b) * 0.5;
    }

    /**
     * Calculates score based on temporal proximity between lost date and found date.
     */
    private double calculateDateProximity(LocalDate lostDate, LocalDate foundDate) {
        if (lostDate == null || foundDate == null) return 0.5;

        long daysDiff = Math.abs(ChronoUnit.DAYS.between(lostDate, foundDate));

        if (daysDiff == 0) return 1.0;
        if (daysDiff <= 2) return 0.85;
        if (daysDiff <= 5) return 0.70;
        if (daysDiff <= 10) return 0.50;
        if (daysDiff <= 20) return 0.30;
        if (daysDiff <= 30) return 0.15;
        return 0.0;
    }

    private Set<String> tokenize(String input) {
        if (input == null) return Collections.emptySet();
        return Arrays.stream(input.toLowerCase().split("[^a-zA-Z0-9]+"))
                .filter(w -> !w.isBlank())
                .collect(Collectors.toSet());
    }

    private Set<String> extractKeywords(String input) {
        if (input == null) return Collections.emptySet();
        return Arrays.stream(input.toLowerCase().split("[^a-zA-Z0-9]+"))
                .filter(w -> w.length() > 2 && !STOP_WORDS.contains(w))
                .collect(Collectors.toSet());
    }

    private double calculateJaccard(Set<String> setA, Set<String> setB) {
        if (setA.isEmpty() || setB.isEmpty()) return 0.0;
        Set<String> intersection = new HashSet<>(setA);
        intersection.retainAll(setB);
        Set<String> union = new HashSet<>(setA);
        union.addAll(setB);
        return (double) intersection.size() / union.size();
    }

    private double calculateNormalizedLevenshtein(String s1, String s2) {
        int maxLen = Math.max(s1.length(), s2.length());
        if (maxLen == 0) return 1.0;
        int distance = computeLevenshteinDistance(s1, s2);
        return 1.0 - ((double) distance / maxLen);
    }

    private int computeLevenshteinDistance(String s1, String s2) {
        int[] costs = new int[s2.length() + 1];
        for (int j = 0; j <= s2.length(); j++) {
            costs[j] = j;
        }
        for (int i = 1; i <= s1.length(); i++) {
            costs[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= s2.length(); j++) {
                int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]),
                        s1.charAt(i - 1) == s2.charAt(j - 1) ? nw : nw + 1);
                nw = costs[j];
                costs[j] = cj;
            }
        }
        return costs[s2.length()];
    }
}
