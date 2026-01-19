package com.lax.sme_manager.util;

import com.lax.sme_manager.domain.Vendor;
import com.lax.sme_manager.repository.VendorRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Centralized vendor caching layer.
 * - Loads vendors ONCE at application start
 * - Provides filtered views without database calls
 * - Refreshable cache for new vendors created in session
 */
public class VendorCache {
    private static final Logger LOGGER = LoggerFactory.getLogger(VendorCache.class);

    private final VendorRepository vendorRepository;
    private ObservableList<Vendor> allVendors = FXCollections.observableArrayList();
    private LocalDateTime lastCacheRefresh;
    private static final long CACHE_VALIDITY_MINUTES = 30;

    public VendorCache(VendorRepository vendorRepository) {
        this.vendorRepository = vendorRepository;
    }

    /**
     * Initialize cache - call ONCE at app startup
     */
    public void initialize() {
        LOGGER.info("Initializing vendor cache");
        refreshCache();
    }

    /**
     * Get all vendors from cache (zero DB hit)
     */
    public ObservableList<Vendor> getAllVendors() {
        return FXCollections.observableArrayList(allVendors);
    }

    // Alias for consistence with some view usage
    public List<Vendor> getAll() {
        return allVendors;
    }

    /**
     * Search vendors by name (in-memory only)
     */
    public List<Vendor> searchByName(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            return List.copyOf(allVendors);
        }

        String search = searchText.toLowerCase().trim();
        return allVendors.stream()
                .filter(v -> v.getName().toLowerCase().contains(search))
                .sorted((v1, v2) -> {
                    // Sort by exact match first, then alphabetical
                    boolean v1Starts = v1.getName().toLowerCase().startsWith(search);
                    boolean v2Starts = v2.getName().toLowerCase().startsWith(search);

                    if (v1Starts && !v2Starts)
                        return -1;
                    if (!v1Starts && v2Starts)
                        return 1;
                    return v1.getName().compareTo(v2.getName());
                })
                .toList();
    }

    /**
     * Find exact vendor by name (case-insensitive)
     */
    public Vendor findByName(String name) {
        if (name == null)
            return null;
        return allVendors.stream()
                .filter(v -> v.getName().equalsIgnoreCase(name.trim()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Find vendor by ID
     */
    public Vendor findById(int id) {
        return allVendors.stream()
                .filter(v -> v.getId() == id)
                .findFirst()
                .orElse(null);
    }

    /**
     * Add newly created vendor to cache
     */
    public void addVendor(Vendor vendor) {
        if (!allVendors.contains(vendor)) {
            allVendors.add(vendor);
            LOGGER.debug("Added vendor to cache: {} (ID: {})", vendor.getName(), vendor.getId());
        }
    }

    /**
     * Refresh cache if needed (check validity)
     * Call this after creating new vendors
     */
    public void refreshCache() {
        try {
            List<Vendor> vendors = vendorRepository.findAllVendors();
            allVendors.setAll(vendors);
            lastCacheRefresh = LocalDateTime.now();
            LOGGER.info("Vendor cache refreshed. Total vendors: {}", vendors.size());
        } catch (Exception e) {
            LOGGER.error("Error refreshing vendor cache", e);
        }
    }

    /**
     * Check if cache is stale (optional - for advanced scenarios)
     */
    public boolean isCacheStale() {
        if (lastCacheRefresh == null)
            return true;
        return LocalDateTime.now().minusMinutes(CACHE_VALIDITY_MINUTES).isAfter(lastCacheRefresh);
    }
}
