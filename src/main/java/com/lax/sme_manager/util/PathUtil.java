package com.lax.sme_manager.util;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Cross-platform path utility for reliable file system operations.
 * Ensures consistent behavior across macOS, Windows, and Linux.
 */
public class PathUtil {

    private static final String APP_DIR_NAME = "LaxSMEManager";

    /**
     * Get the application data directory based on OS conventions.
     * - Windows: C:\Users\{user}\AppData\Roaming\LaxSMEManager
     * - macOS: /Users/{user}/LaxSMEManager
     * - Linux: /home/{user}/LaxSMEManager
     */
    public static Path getAppDataDirectory() {
        String home = System.getProperty("user.home");
        String os = System.getProperty("os.name").toLowerCase();

        Path appDataBase;
        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            appDataBase = (appData != null) ? Paths.get(appData) : Paths.get(home, "AppData", "Roaming");
        } else {
            appDataBase = Paths.get(home);
        }

        return appDataBase.resolve(APP_DIR_NAME);
    }

    /**
     * Get the database file path
     */
    public static Path getDatabasePath() {
        return getAppDataDirectory().resolve("data.db");
    }

    /**
     * Get the backup directory path
     */
    public static Path getBackupDirectory() {
        return getAppDataDirectory().resolve("backups");
    }

    /**
     * Get the cheque templates directory path
     */
    public static Path getChequeTemplatesDirectory() {
        return getAppDataDirectory().resolve("cheque_templates");
    }

    /**
     * Get the logs directory path
     */
    public static Path getLogsDirectory() {
        return getAppDataDirectory().resolve("logs");
    }

    /**
     * Get the exports directory path
     */
    public static Path getExportsDirectory() {
        return getAppDataDirectory().resolve("exports");
    }

    /**
     * Convert file separator for current OS
     * (Mainly for display purposes - Java handles path separators automatically)
     */
    public static String toOsPath(String path) {
        return path.replace("/", File.separator).replace("\\\\", File.separator);
    }

    /**
     * Get a safe filename by removing invalid characters
     */
    public static String sanitizeFilename(String filename) {
        // Remove characters invalid on Windows (most restrictive)
        return filename.replaceAll("[<>:\"/\\\\|?*]", "_");
    }
}
