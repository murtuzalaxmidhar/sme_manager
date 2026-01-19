package com.lax.sme_manager.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Manages application configuration persistence (e.g., language, backup path).
 * Stores settings in a .properties file in the AppData directory.
 */
public class ConfigManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigManager.class);
    private static final String CONFIG_FILE = "config.properties";
    private static ConfigManager instance;

    private final Properties properties = new Properties();
    private final Path configPath;

    private ConfigManager() {
        this.configPath = DatabaseManager.getAppDataDir().resolve(CONFIG_FILE);
        load();
    }

    public static synchronized ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    private void load() {
        File file = configPath.toFile();
        if (file.exists()) {
            try (InputStream is = new FileInputStream(file)) {
                properties.load(is);
                LOGGER.info("Config loaded from {}", configPath);
            } catch (IOException e) {
                LOGGER.error("Failed to load config", e);
            }
        }
    }

    public void save() {
        try (OutputStream os = new FileOutputStream(configPath.toFile())) {
            properties.store(os, "Lax SME Manager Settings");
            LOGGER.info("Config saved to {}", configPath);
        } catch (IOException e) {
            LOGGER.error("Failed to save config", e);
        }
    }

    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
        save();
    }

    // --- Cheque Template Helpers ---
    public void saveChequeTemplate(com.lax.sme_manager.domain.ChequeTemplate template) {
        properties.setProperty("cheque.name",
                template.getTemplateName() != null ? template.getTemplateName() : "Default");
        properties.setProperty("cheque.bg",
                template.getBackgroundImagePath() != null ? template.getBackgroundImagePath() : "");
        properties.setProperty("cheque.dateX", String.valueOf(template.getDateX()));
        properties.setProperty("cheque.dateY", String.valueOf(template.getDateY()));
        properties.setProperty("cheque.payeeX", String.valueOf(template.getPayeeX()));
        properties.setProperty("cheque.payeeY", String.valueOf(template.getPayeeY()));
        properties.setProperty("cheque.amountWordsX", String.valueOf(template.getAmountWordsX()));
        properties.setProperty("cheque.amountWordsY", String.valueOf(template.getAmountWordsY()));
        properties.setProperty("cheque.amountDigitsX", String.valueOf(template.getAmountDigitsX()));
        properties.setProperty("cheque.amountDigitsY", String.valueOf(template.getAmountDigitsY()));
        properties.setProperty("cheque.signatureX", String.valueOf(template.getSignatureX()));
        properties.setProperty("cheque.signatureY", String.valueOf(template.getSignatureY()));
        properties.setProperty("cheque.fontSize", String.valueOf(template.getFontSize()));
        properties.setProperty("cheque.fontColor",
                template.getFontColor() != null ? template.getFontColor() : "#000000");
        save();
    }

    public com.lax.sme_manager.domain.ChequeTemplate loadChequeTemplate() {
        if (!properties.containsKey("cheque.dateX")) {
            return com.lax.sme_manager.domain.ChequeTemplate.createDefault();
        }
        try {
            return com.lax.sme_manager.domain.ChequeTemplate.builder()
                    .templateName(properties.getProperty("cheque.name", "Default"))
                    .backgroundImagePath(properties.getProperty("cheque.bg", ""))
                    .dateX(Double.parseDouble(properties.getProperty("cheque.dateX", "0.82")))
                    .dateY(Double.parseDouble(properties.getProperty("cheque.dateY", "0.08")))
                    .payeeX(Double.parseDouble(properties.getProperty("cheque.payeeX", "0.12")))
                    .payeeY(Double.parseDouble(properties.getProperty("cheque.payeeY", "0.22")))
                    .amountWordsX(Double.parseDouble(properties.getProperty("cheque.amountWordsX", "0.12")))
                    .amountWordsY(Double.parseDouble(properties.getProperty("cheque.amountWordsY", "0.32")))
                    .amountDigitsX(Double.parseDouble(properties.getProperty("cheque.amountDigitsX", "0.82")))
                    .amountDigitsY(Double.parseDouble(properties.getProperty("cheque.amountDigitsY", "0.42")))
                    .signatureX(Double.parseDouble(properties.getProperty("cheque.signatureX", "0.75")))
                    .signatureY(Double.parseDouble(properties.getProperty("cheque.signatureY", "0.80")))
                    .fontSize(Integer.parseInt(properties.getProperty("cheque.fontSize", "16")))
                    .fontColor(properties.getProperty("cheque.fontColor", "#1e293b"))
                    .fontFamily("Inter")
                    .build();
        } catch (Exception e) {
            LOGGER.error("Failed to parse cheque template config", e);
            return com.lax.sme_manager.domain.ChequeTemplate.createDefault();
        }
    }

    // Convenience Keys
    public static final String KEY_LANGUAGE = "app.language";
    public static final String KEY_BACKUP_PATH = "app.backup.path";
}
