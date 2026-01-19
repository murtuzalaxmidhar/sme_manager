//package com.lax.sme_manager.service;
//
//import javafx.concurrent.Task;
//import com.lax.sme_manager.repository.PurchaseRepository;
//import com.lax.sme_manager.repository.VendorRepository;
//import com.lax.sme_manager.repository.model.PurchaseEntity;
//import com.lax.sme_manager.domain.Vendor;
//import com.lax.sme_manager.util.*;
//
//import java.util.List;
//import java.util.logging.Logger;
//
///**
// * Business logic layer for purchases
// * Handles:
// * - Caching strategy
// * - Async data loading
// * - Complex business rules
// */
//public class PurchaseService {
//
//    private static final Logger LOGGER = AppLogger.getLogger(PurchaseService.class);
//    private final PurchaseRepository purchaseRepo;
//    private final VendorRepository vendorRepo;
//    private final DataCache cache;
//
//    public PurchaseService(PurchaseRepository purchaseRepo, VendorRepository vendorRepo) {
//        this.purchaseRepo = purchaseRepo;
//        this.vendorRepo = vendorRepo;
//        this.cache = DataCache.getInstance();
//    }
//
//    /**
//     * Save purchase (new or update)
//     * Invalidates related caches
//     */
//    public PurchaseEntity savePurchase(PurchaseEntity purchase) {
//        try {
//            LOGGER.info("Saving purchase: vendor_id={}, amount={}", purchase.getVendorId(), purchase.getGrandTotal());
//
//            // Handle new vendors (vendor_id = -1)
//            if (purchase.getVendorId() == -1) {
//                // Vendor creation should be handled at UI level before calling this
//                throw new IllegalArgumentException("Cannot save purchase with vendor_id = -1. Create vendor first.");
//            }
//
//            PurchaseEntity saved = purchaseRepo.save(purchase);
//
//            // Invalidate caches
//            cache.invalidatePrefix("purchase_list");
//            cache.invalidatePrefix("purchase_stats");
//
//            LOGGER.info("Purchase saved successfully: id={}", saved.getId());
//            return saved;
//
//        } catch (Exception e) {
//            LOGGER.error("Error saving purchase: {}", e.getMessage(), e);
//            throw new RuntimeException("Failed to save purchase", e);
//        }
//    }
//
//    /**
//     * Load purchases asynchronously with filters
//     * Returns a JavaFX Task that can be run on background thread
//     */
//    public Task<PurchaseRepositoryQueryResult> loadPurchasesAsync(PurchaseFilter filter) {
//        return new Task<PurchaseRepositoryQueryResult>() {
//            @Override
//            protected PurchaseRepositoryQueryResult call() throws Exception {
//                LOGGER.info("Async loading purchases with filter: page={}", filter.getPageNumber());
//                updateMessage("Loading purchases...");
//                updateProgress(-1, 1);  // Indeterminate progress
//
//                PurchaseRepositoryQueryResult result = purchaseRepo.findWithFilters(filter);
//
//                LOGGER.info("Loaded {} purchases (total: {}, page: {}/{})",
//                        result.getRecords().size(),
//                        result.getTotalCount(),
//                        result.getCurrentPage(),
//                        result.getTotalPages());
//
//                updateProgress(1, 1);  // Complete
//                return result;
//            }
//
//            @Override
//            protected void failed() {
//                LOGGER.error("Async purchase load failed: {}", getException().getMessage());
//            }
//        };
//    }
//
//    /**
//     * Load vendors asynchronously (usually on app startup)
//     */
//    public Task<List<Vendor>> loadVendorsAsync() {
//        return new Task<List<Vendor>>() {
//            @Override
//            protected List<Vendor> call() throws Exception {
//                LOGGER.info("Async loading vendors from database");
//                updateMessage("Loading vendors...");
//                updateProgress(-1, 1);
//
//                List<Vendor> vendors = vendorRepo.findAllVendors();
//
//                // Cache vendors for 24 hours (vendor list rarely changes during day)
//                cache.put("vendors_all", vendors, 24 * 60 * 60 * 1000);
//
//                LOGGER.info("Loaded {} vendors from database", vendors.size());
//                updateProgress(1, 1);
//                return vendors;
//            }
//        };
//    }
//
//    /**
//     * Get cached vendors if available, otherwise load fresh
//     */
//    public List<Vendor> getVendors() {
//        List<Vendor> cached = cache.get("vendors_all");
//        if (cached != null) {
//            LOGGER.debug("Returning cached vendors");
//            return cached;
//        }
//
//        LOGGER.debug("No cached vendors, fetching fresh");
//        return vendorRepo.findAllVendors();
//    }
//
//    /**
//     * Invalidate all purchase-related caches
//     * Call this after bulk operations or when data changes externally
//     */
//    public void invalidatePurchaseCache() {
//        cache.invalidatePrefix("purchase");
//        LOGGER.info("All purchase caches invalidated");
//    }
//}
