package com.lax.sme_manager.repository;

import com.lax.sme_manager.repository.model.PurchaseEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * IPurchaseRepository - Repository interface for Purchase operations
 * Abstraction layer - allows multiple implementations (SQLite, PostgreSQL, etc)
 */
public interface IPurchaseRepository {

    /**
     * Save a new purchase or update existing
     */
    PurchaseEntity save(PurchaseEntity entity);

    /**
     * Find purchase by ID
     */
    Optional<PurchaseEntity> findById(Integer id);

    /**
     * Get all purchases
     */
    List<PurchaseEntity> findAll();

    /**
     * Find purchases by entry date
     */
    List<PurchaseEntity> findByDate(LocalDate date);

    /**
     * Find purchases by vendor ID
     */
    List<PurchaseEntity> findByVendorId(Integer vendorId);

    /**
     * Find purchases in date range
     */
    List<PurchaseEntity> findByDateRange(LocalDate startDate, LocalDate endDate);

    /**
     * Get today's purchases
     */
    List<PurchaseEntity> findTodaysPurchases();

    /**
     * Get this month's purchases
     */
    List<PurchaseEntity> findThisMonthPurchases();

    /**
     * Delete a purchase
     */
    /**
     * Get total bags count for date range
     */
    Integer getBagsCount(LocalDate startDate, LocalDate endDate);

    /**
     * Get total amount for date range
     */
    Double getTotalAmount(LocalDate startDate, LocalDate endDate);

    /**
     * Count pending cheques for date range
     */
    Integer countPendingCheques(LocalDate startDate, LocalDate endDate);

    /**
     * Count cheques with specific clearing date
     */
    Integer countChequesByClearingDate(LocalDate date);

    List<PurchaseEntity> findByVendorAndStatus(Integer vendorId, String status);

    int getLastInsertedId();

    void delete(Integer id);

    /**
     * Get all soft-deleted purchases
     */
    List<PurchaseEntity> findAllDeleted();

    /**
     * Restore a soft-deleted purchase
     */
    void restore(Integer id);

    void updateStatus(Integer id, String status);

    Integer countPendingClearing();

    /**
     * Archive purchases older than the specified date
     */
    int archiveOldData(LocalDate beforeDate);

    /**
     * Retrieve all archived records
     */
    List<PurchaseEntity> findAllArchived();

    /**
     * Restore a record from archive to active table
     */
    boolean restoreFromArchive(Integer id);

    /**
     * Fetch purchases with database-level filtering and pagination
     */
    List<PurchaseEntity> findFilteredPurchases(
            LocalDate startDate, LocalDate endDate, List<Integer> vendorIds,
            java.math.BigDecimal minAmount, java.math.BigDecimal maxAmount, Boolean chequeIssued,
            String searchQuery, int limit, int offset);

    /**
     * Get total count of filtered purchases
     */
    int countFilteredPurchases(
            LocalDate startDate, LocalDate endDate, List<Integer> vendorIds,
            java.math.BigDecimal minAmount, java.math.BigDecimal maxAmount, Boolean chequeIssued,
            String searchQuery);
}
