package com.lax.sme_manager.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Enhanced Database Manager for Production Deployment.
 * Handles persistent paths, WAL mode, and connection pooling (basic).
 */
public class DatabaseManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseManager.class);
    private static String dbUrl;
    private static Path dbPath;

    static {
        setupPaths();
    }

    private static void setupPaths() {
        String home = System.getProperty("user.home");
        String os = System.getProperty("os.name").toLowerCase();

        Path appDataDir;
        if (os.contains("win")) {
            // Standard Windows Production Path: LocalAppData\LaxSMEManager
            String localAppData = System.getenv("LOCALAPPDATA");
            if (localAppData != null && !localAppData.isEmpty()) {
                appDataDir = Paths.get(localAppData).resolve("LaxSMEManager");
            } else {
                // Fallback if LOCALAPPDATA is missing (rare)
                appDataDir = Paths.get(home, "AppData", "Local", "LaxSMEManager");
            }
        } else {
            // For macOS/Linux development/production
            appDataDir = Paths.get(home, "LaxSMEManager");
        }

        dbPath = appDataDir.resolve("lax_data.db");
        dbUrl = "jdbc:sqlite:" + dbPath.toString();

        try {
            if (!Files.exists(appDataDir)) {
                Files.createDirectories(appDataDir);
                LOGGER.info("Created application data directory at: {}", appDataDir);
            }
            LOGGER.info("Database path standard set: {}", dbPath);
        } catch (Exception e) {
            LOGGER.error("CRITICAL: Failed to initialize database directory", e);
            throw new RuntimeException("Could not initialize storage directory: " + appDataDir, e);
        }
    }

    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(dbUrl);
        // Enable WAL mode for power-cut safety
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA journal_mode=WAL;");
        }
        return conn;
    }

    public static String getUrl() {
        return dbUrl;
    }

    public static File getDatabaseFile() {
        return dbPath.toFile();
    }

    public static Path getAppDataDir() {
        return dbPath.getParent();
    }
}
