package com.smartfind.repository;

import com.smartfind.entity.ItemReport;
import com.smartfind.entity.ItemStatus;
import com.smartfind.entity.ReportType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA Repository for ItemReport entity operations.
 */
@Repository
public interface ItemReportRepository extends JpaRepository<ItemReport, Long> {

    List<ItemReport> findAllByOrderByCreatedAtDesc();

    List<ItemReport> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<ItemReport> findByReportTypeOrderByCreatedAtDesc(ReportType reportType);

    List<ItemReport> findByReportTypeAndStatus(ReportType reportType, ItemStatus status);

    long countByReportType(ReportType reportType);

    @Query("SELECT i FROM ItemReport i WHERE " +
           "(:reportType IS NULL OR i.reportType = :reportType) AND " +
           "(:query IS NULL OR :query = '' OR LOWER(i.itemName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(i.description) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:location IS NULL OR :location = '' OR LOWER(i.location) LIKE LOWER(CONCAT('%', :location, '%'))) " +
           "ORDER BY i.createdAt DESC")
    List<ItemReport> searchItems(
            @Param("query") String query,
            @Param("reportType") ReportType reportType,
            @Param("location") String location
    );
}
