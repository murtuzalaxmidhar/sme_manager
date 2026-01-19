package com.lax.sme_manager.util.i18n;

import com.lax.sme_manager.util.ConfigManager;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Singleton to manage global language Settings.
 */
public class LanguageManager {
    private static final LanguageManager INSTANCE = new LanguageManager();

    private final ObjectProperty<LanguageMode> currentMode = new SimpleObjectProperty<>();

    private LanguageManager() {
        // Load from config
        String saved = ConfigManager.getInstance().getProperty(ConfigManager.KEY_LANGUAGE,
                LanguageMode.BILINGUAL.name());
        try {
            currentMode.set(LanguageMode.valueOf(saved));
        } catch (Exception e) {
            currentMode.set(LanguageMode.BILINGUAL);
        }

        // Save on change
        currentMode.addListener((obs, old, newVal) -> {
            if (newVal != null) {
                ConfigManager.getInstance().setProperty(ConfigManager.KEY_LANGUAGE, newVal.name());
            }
        });
    }

    public static LanguageManager getInstance() {
        return INSTANCE;
    }

    public ObjectProperty<LanguageMode> currentModeProperty() {
        return currentMode;
    }

    public LanguageMode getMode() {
        return currentMode.get();
    }

    public void setMode(LanguageMode mode) {
        currentMode.set(mode);
    }
}
