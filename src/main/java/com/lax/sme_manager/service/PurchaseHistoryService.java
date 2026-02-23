package com.lax.sme_manager.service;

import com.lax.sme_manager.repository.PurchaseRepository;
import com.lax.sme_manager.repository.VendorRepository;
import com.lax.sme_manager.repository.model.PurchaseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Service layer for purchase history with advanced filtering, pagination, and
 * performance optimization.
 * 
 * Design Decisions:
 * - Filtering happens at DB level (not in-memory) for large datasets
 * - Pagination prevents loading all records into memory
 * - Async loading via background threads (optional)
 * - Caching of recent records only
 */
public class PurchaseHistoryService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PurchaseHistoryService.class);

    private final PurchaseRepository purchaseRepository;
    private final VendorRepository vendorRepository;
    private static final int PAGE_SIZE = 50; // Records per page

    public PurchaseHistoryService(PurchaseRepository purchaseRepository, VendorRepository vendorRepository) {
        this.purchaseRepository = purchaseRepository;
        this.vendorRepository = vendorRepository;
    }

    /**
     * Fetch purchase history with filtering and pagination
     */
    public List<PurchaseEntity> fetchPurchases(
            LocalDate startDate,
            LocalDate endDate,
            List<Integer> vendorIds,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            Boolean chequeIssued,
            String searchQuery,
            int pageNumber) {

        try {
            LOGGER.info("Fetching purchases - Page: {}, Vendors: {}, Range: {} to {}, Search: {}",
                    pageNumber, vendorIds, startDate, endDate, searchQuery);

            int offset = pageNumber * PAGE_SIZE;

            // Fetch natively filtered and paginated results from DB
            List<PurchaseEntity> filtered = purchaseRepository.findFilteredPurchases(
                    startDate, endDate, vendorIds, minAmount, maxAmount, chequeIssued,
                    searchQuery, PAGE_SIZE, offset);

            LOGGER.debug("Loaded: {} purchases for page {}", filtered.size(), pageNumber);
            return filtered;

        } catch (Exception e) {
            LOGGER.error("Error fetching purchases with filters", e);
            return new ArrayList<>();
        }
    }

    /**
     * Get total filtered record count (for pagination UI)
     */
    public int getTotalFilteredCount(
            LocalDate startDate,
            LocalDate endDate,
            List<Integer> vendorIds,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            Boolean chequeIssued,
            String searchQuery) {

        try {
            return purchaseRepository.countFilteredPurchases(
                    startDate, endDate, vendorIds, minAmount, maxAmount, chequeIssued, searchQuery);
        } catch (Exception e) {
            LOGGER.error("Error calculating total filtered count", e);
            return 0;
        }
    }

    /**
     * Get purchases for TODAY
     */
    public List<PurchaseEntity> getPurchasesToday() {
        LocalDate today = LocalDate.now();
        return fetchPurchases(today, today, null, null, null, null, null, 0);
    }

    /**
     * Get purchases for last N days
     */
    public List<PurchaseEntity> getPurchasesLastNDays(int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minus(days, ChronoUnit.DAYS);
        return fetchPurchases(startDate, endDate, null, null, null, null, null, 0);
    }

    /**
     * Get single purchase with vendor details
     */
    public PurchaseEntity getPurchaseById(int id) {
        try {
            return purchaseRepository.findAll().stream()
                    .filter(p -> p.getId() == id)
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            LOGGER.error("Error fetching purchase by ID: {}", id, e);
            return null;
        }
    }

    public void deletePurchase(int id) {
        try {
            purchaseRepository.delete(id);
            LOGGER.info("Soft-deleted purchase ID: {}", id);
        } catch (Exception e) {
            LOGGER.error("Error soft-deleting purchase ID: {}", id, e);
            throw e;
        }
    }

    public void deletePurchases(List<Integer> ids) {
        if (ids == null || ids.isEmpty())
            return;
        try {
            for (Integer id : ids) {
                purchaseRepository.delete(id);
            }
            LOGGER.info("Soft-deleted {} purchases", ids.size());
        } catch (Exception e) {
            LOGGER.error("Error bulk soft-deleting purchases", e);
            throw e;
        }
    }

    public void updateStatus(int id, String status) {
        try {
            purchaseRepository.updateStatus(id, status);
            LOGGER.info("Updated purchase ID: {} to status: {}", id, status);
        } catch (Exception e) {
            LOGGER.error("Error updating status for purchase ID: {}", id, e);
            throw e;
        }
    }

    public String getVendorName(int vendorId) {
        return vendorRepository.findAllVendors().stream()
                .filter(v -> v.getId() == vendorId)
                .map(com.lax.sme_manager.domain.Vendor::getName)
                .findFirst()
                .orElse("Unknown Vendor");
    }

}
