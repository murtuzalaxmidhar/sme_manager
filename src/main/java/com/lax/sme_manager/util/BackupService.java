package com.lax.sme_manager.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Handles database backups on exit and manual triggers.
 */
public class BackupService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BackupService.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmm");

    public void performBackup() {
        File dbFile = DatabaseManager.getDatabaseFile();
        if (!dbFile.exists())
            return;

        // 1. Local Backup
        backupToPath(DatabaseManager.getAppDataDir().resolve("Backups"));

        // 2. Cloud/User defined Backup
        String customPath = ConfigManager.getInstance().getProperty(ConfigManager.KEY_BACKUP_PATH, null);
        if (customPath != null && !customPath.trim().isEmpty()) {
            backupToPath(Paths.get(customPath));
        }
    }

    private void backupToPath(Path targetDir) {
        try {
            Files.createDirectories(targetDir);
            String timestamp = LocalDateTime.now().format(FORMATTER);
            Path targetFile = targetDir.resolve("data_backup_" + timestamp + ".db");

            Files.copy(DatabaseManager.getDatabaseFile().toPath(), targetFile, StandardCopyOption.REPLACE_EXISTING);
            LOGGER.info("Backup created at: {}", targetFile);

            // Cleanup old backups (keep last 30)
            cleanupOldBackups(targetDir);

        } catch (IOException e) {
            LOGGER.error("Backup failed for path: {}", targetDir, e);
        }
    }

    private void cleanupOldBackups(Path targetDir) {
        try {
            LocalDateTime threshold = LocalDateTime.now().minusDays(30);
            Files.list(targetDir)
                    .filter(p -> p.getFileName().toString().startsWith("data_backup_"))
                    .forEach(p -> {
                        try {
                            // Extract date from filename: data_backup_yyyy-MM-dd_HHmm.db
                            String fileName = p.getFileName().toString();
                            String dateStr = fileName.replace("data_backup_", "").replace(".db", "");
                            LocalDateTime fileDate = LocalDateTime.parse(dateStr, FORMATTER);

                            if (fileDate.isBefore(threshold)) {
                                Files.delete(p);
                                LOGGER.info("Deleted old backup: {}", p.getFileName());
                            }
                        } catch (Exception ignored) {
                            // If parsing fails, use file last modified as fallback
                            try {
                                if (Files.getLastModifiedTime(p).toInstant()
                                        .isBefore(threshold.atZone(java.time.ZoneId.systemDefault()).toInstant())) {
                                    Files.delete(p);
                                    LOGGER.info("Deleted old backup (fallback): {}", p.getFileName());
                                }
                            } catch (IOException ignored2) {
                            }
                        }
                    });
        } catch (IOException e) {
            LOGGER.warn("Failed to cleanup old backups", e);
        }
    }
}
