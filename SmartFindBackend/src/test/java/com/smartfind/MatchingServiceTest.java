package com.smartfind;

import com.smartfind.dto.MatchingResponseDto;
import com.smartfind.entity.ItemReport;
import com.smartfind.entity.ItemStatus;
import com.smartfind.entity.ReportType;
import com.smartfind.entity.User;
import com.smartfind.repository.ItemReportRepository;
import com.smartfind.service.MatchingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MatchingServiceTest {

    @Mock
    private ItemReportRepository itemReportRepository;

    @InjectMocks
    private MatchingService matchingService;

    private User testUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(matchingService, "defaultThreshold", 60.0);

        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Alex Student");
        testUser.setEmail("alex@smartfind.com");
    }

    @Test
    @DisplayName("Should find strong match when iPhone 13 is reported lost and found")
    void testMatchingAlgorithmStrongMatch() {
        LocalDate today = LocalDate.now();

        // 1. Lost Item
        ItemReport lostItem = new ItemReport();
        lostItem.setId(10L);
        lostItem.setItemName("Apple iPhone 13");
        lostItem.setDescription("Black Apple iPhone 13 with matte case lost in reading room");
        lostItem.setLocation("Central Library");
        lostItem.setItemDate(today.minusDays(1));
        lostItem.setReportType(ReportType.LOST);
        lostItem.setStatus(ItemStatus.ACTIVE);
        lostItem.setUser(testUser);

        // 2. Found Item (Very similar)
        ItemReport foundItem = new ItemReport();
        foundItem.setId(20L);
        foundItem.setItemName("Black iPhone 13");
        foundItem.setDescription("Found an Apple black iPhone 13 with silicone case on desk");
        foundItem.setLocation("Central Library 2nd Floor");
        foundItem.setItemDate(today);
        foundItem.setReportType(ReportType.FOUND);
        foundItem.setStatus(ItemStatus.ACTIVE);
        foundItem.setUser(testUser);

        when(itemReportRepository.findById(10L)).thenReturn(Optional.of(lostItem));
        when(itemReportRepository.findByReportTypeAndStatus(ReportType.FOUND, ItemStatus.ACTIVE))
                .thenReturn(List.of(foundItem));

        MatchingResponseDto response = matchingService.findMatchesForLostItem(10L);

        assertNotNull(response);
        assertEquals(1, response.getTotalMatches());
        assertTrue(response.getMatches().get(0).getMatchScore() >= 60.0);
        assertTrue(response.getMatches().get(0).getMatchExplanation().contains("Name:"));
    }

    @Test
    @DisplayName("Should reject matches below the 60% threshold")
    void testMatchingAlgorithmRejectsUnrelatedItem() {
        LocalDate today = LocalDate.now();

        // 1. Lost Item
        ItemReport lostItem = new ItemReport();
        lostItem.setId(10L);
        lostItem.setItemName("Apple iPhone 13");
        lostItem.setDescription("Black Apple iPhone 13");
        lostItem.setLocation("Central Library");
        lostItem.setItemDate(today.minusDays(1));
        lostItem.setReportType(ReportType.LOST);
        lostItem.setStatus(ItemStatus.ACTIVE);
        lostItem.setUser(testUser);

        // 2. Found Item (Completely unrelated)
        ItemReport foundItem = new ItemReport();
        foundItem.setId(30L);
        foundItem.setItemName("Red Water Bottle");
        foundItem.setDescription("Red metallic gym water bottle left on grass");
        foundItem.setLocation("Sports Complex");
        foundItem.setItemDate(today.minusDays(15));
        foundItem.setReportType(ReportType.FOUND);
        foundItem.setStatus(ItemStatus.ACTIVE);
        foundItem.setUser(testUser);

        when(itemReportRepository.findById(10L)).thenReturn(Optional.of(lostItem));
        when(itemReportRepository.findByReportTypeAndStatus(ReportType.FOUND, ItemStatus.ACTIVE))
                .thenReturn(List.of(foundItem));

        MatchingResponseDto response = matchingService.findMatchesForLostItem(10L);

        assertNotNull(response);
        assertEquals(0, response.getTotalMatches());
        assertTrue(response.getMatches().isEmpty());
    }
}
