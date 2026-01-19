//package com.lax.sme_manager.util;
//
//import java.util.*;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.logging.Logger;
//
///**
// * Thread-safe, in-memory cache for application data
// * Prevents repeated DB queries for frequently accessed data
// *
// * Strategy:
// * - Cache vendors on app startup (small dataset)
// * - Cache recent purchases (last 30 days)
// * - Cache on-demand for older data
// * - TTL-based invalidation for real-time data
// */
//public class DataCache {
//
//    private static final Logger LOGGER = AppLogger.getLogger(DataCache.class);
//    private static volatile DataCache INSTANCE;
//
//    private static final long VENDOR_CACHE_TTL = 24 * 60 * 60 * 1000;  // 24 hours
//    private static final long PURCHASE_CACHE_TTL = 30 * 60 * 1000;     // 30 minutes
//
//    private final Map<String, CacheEntry<?>> cache = new ConcurrentHashMap<>();
//
//    private DataCache() {}
//
//    /**
//     * Lazy-loaded singleton
//     */
//    public static DataCache getInstance() {
//        if (INSTANCE == null) {
//            synchronized (DataCache.class) {
//                if (INSTANCE == null) {
//                    INSTANCE = new DataCache();
//                    LOGGER.info("DataCache initialized");
//                }
//            }
//        }
//        return INSTANCE;
//    }
//
//    /**
//     * Get cached value if valid, otherwise return null
//     */
//    public <T> T get(String key) {
//        CacheEntry<?> entry = cache.get(key);
//        if (entry != null && !entry.isExpired()) {
//            LOGGER.debug("Cache HIT: {}", key);
//            return (T) entry.value;
//        }
//        LOGGER.debug("Cache MISS: {}", key);
//        return null;
//    }
//
//    /**
//     * Put value with default TTL based on data type
//     */
//    public <T> void put(String key, T value) {
//        long ttl = key.contains("vendor") ? VENDOR_CACHE_TTL : PURCHASE_CACHE_TTL;
//        put(key, value, ttl);
//    }
//
//    /**
//     * Put value with custom TTL
//     */
//    public <T> void put(String key, T value, long ttlMillis) {
//        cache.put(key, new CacheEntry<>(value, ttlMillis));
//        LOGGER.debug("Cache PUT: {} (TTL: {}ms)", key, ttlMillis);
//    }
//
//    /**
//     * Invalidate specific cache entry
//     */
//    public void invalidate(String key) {
//        cache.remove(key);
//        LOGGER.debug("Cache INVALIDATE: {}", key);
//    }
//
//    /**
//     * Invalidate all entries matching prefix
//     */
//    public void invalidatePrefix(String prefix) {
//        cache.keySet().removeIf(k -> k.startsWith(prefix));
//        LOGGER.debug("Cache INVALIDATE PREFIX: {}", prefix);
//    }
//
//    /**
//     * Clear entire cache
//     */
//    public void clear() {
//        cache.clear();
//        LOGGER.info("Cache CLEARED");
//    }
//
//    /**
//     * Inner class: Cache entry with TTL
//     */
//    private static class CacheEntry<T> {
//        final T value;
//        final long expiresAt;
//
//        CacheEntry(T value, long ttlMillis) {
//            this.value = value;
//            this.expiresAt = System.currentTimeMillis() + ttlMillis;
//        }
//
//        boolean isExpired() {
//            return System.currentTimeMillis() > expiresAt;
//        }
//    }
//}
