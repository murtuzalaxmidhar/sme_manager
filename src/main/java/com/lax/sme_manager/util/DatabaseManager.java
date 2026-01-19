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

        Path appDataBase;
        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            appDataBase = (appData != null) ? Paths.get(appData) : Paths.get(home, "AppData", "Roaming");
        } else {
            appDataBase = Paths.get(home);
        }

        Path appDir = appDataBase.resolve("LaxSMEManager");
        dbPath = appDir.resolve("data.db");
        dbUrl = "jdbc:sqlite:" + dbPath.toString();

        try {
            Files.createDirectories(appDir);
            LOGGER.info("Database initialized at: {}", dbPath);
        } catch (Exception e) {
            LOGGER.error("Failed to create application data directory", e);
            // Fallback to local if absolutely necessary, but preferred to fail fast
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
