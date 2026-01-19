package com.lax.sme_manager.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * POINT 6: Centralized Logging Utility
 *
 * Purpose:
 * - Provides consistent logging across the application
 * - Ensures proper logger initialization with class names
 * - Standardized logging patterns
 *
 * Usage:
 * private static final Logger LOGGER = AppLogger.getLogger(ClassName.class);
 *
 * Logging Levels:
 * - TRACE: Fine-grained debugging (disabled by default)
 * - DEBUG: Detailed information for debugging
 * - INFO: General informational messages (major operations)
 * - WARN: Warning messages (recoverable issues)
 * - ERROR: Error messages (failed operations)
 */
public class AppLogger {

    /**
     * Get a logger for the given class
     * This method should be used at the top of each class:
     *
     * private static final Logger LOGGER = AppLogger.getLogger(MyClass.class);
     */
    public static Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }

    /**
     * Get a logger for the given class name
     */
    public static Logger getLogger(String name) {
        return LoggerFactory.getLogger(name);
    }

    /**
     * Log application startup information
     */
    public static void logApplicationStartup(String appName, String version) {
        Logger logger = LoggerFactory.getLogger("APPLICATION");
        logger.info("========================================");
        logger.info("{} v{} starting...", appName, version);
        logger.info("========================================");
    }

    /**
     * Log application shutdown information
     */
    public static void logApplicationShutdown(String appName) {
        Logger logger = LoggerFactory.getLogger("APPLICATION");
        logger.info("========================================");
        logger.info("{} shutting down...", appName);
        logger.info("========================================");
    }

    /**
     * Log screen navigation
     */
    public static void logScreenNavigation(String fromScreen, String toScreen) {
        Logger logger = LoggerFactory.getLogger("NAVIGATION");
        logger.info("Navigating from {} to {}", fromScreen, toScreen);
    }

    /**
     * Log database operation start
     */
    public static void logDatabaseOperationStart(String operation, String details) {
        Logger logger = LoggerFactory.getLogger("DATABASE");
        logger.debug("DB Operation [{}] - Details: {}", operation, details);
    }

    /**
     * Log database operation completion
     */
    public static void logDatabaseOperationComplete(String operation, long durationMs, boolean success) {
        Logger logger = LoggerFactory.getLogger("DATABASE");
        String status = success ? "SUCCESS" : "FAILED";
        logger.info("DB Operation [{}] completed - Status: {} ({}ms)", operation, status, durationMs);
    }

    /**
     * Log user action
     */
    public static void logUserAction(String action, String details) {
        Logger logger = LoggerFactory.getLogger("USER_ACTION");
        logger.info("User Action: {} - {}", action, details);
    }

    /**
     * Log validation error
     */
    public static void logValidationError(String fieldName, String reason) {
        Logger logger = LoggerFactory.getLogger("VALIDATION");
        logger.warn("Validation Error - Field: {}, Reason: {}", fieldName, reason);
    }

    /**
     * Log exception with context
     */
    public static void logException(Class<?> clazz, String context, Throwable exception) {
        Logger logger = LoggerFactory.getLogger(clazz);
        logger.error("Exception in {}: {}", context, exception.getMessage(), exception);
    }

    /**
     * Log performance timing
     */
    public static void logPerformance(String operation, long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        Logger logger = LoggerFactory.getLogger("PERFORMANCE");
        if (duration > 1000) {
            logger.warn("Slow operation detected - {}: {}ms", operation, duration);
        } else {
            logger.debug("Performance - {}: {}ms", operation, duration);
        }
    }

    /**
     * Log cache operation
     */
    public static void logCacheOperation(String operation, String details, int hitCount, int missCount) {
        Logger logger = LoggerFactory.getLogger("CACHE");
        logger.debug("Cache [{}] - Details: {}, Hits: {}, Misses: {}", operation, details, hitCount, missCount);
    }
}
