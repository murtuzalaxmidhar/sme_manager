package com.lax.sme_manager.ui.view;

import com.lax.sme_manager.ui.component.UIStyles;
import com.lax.sme_manager.ui.theme.LaxTheme;
import com.lax.sme_manager.util.i18n.AppLabel;
import com.lax.sme_manager.util.i18n.LanguageManager;
import com.lax.sme_manager.util.i18n.LanguageMode;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class SettingsView extends VBox {

    public SettingsView() {
        initializeUI();
    }

    private void initializeUI() {
        setPadding(new Insets(LaxTheme.Layout.MAIN_CONTAINER_PADDING)); // Design Manifest: 25px
        setSpacing(LaxTheme.Spacing.SPACE_32);
        setStyle("-fx-background-color: transparent; -fx-background: " + LaxTheme.Colors.LIGHT_GRAY + ";");

        // Header
        Label title = new Label(AppLabel.TITLE_SETTINGS.get()); // Initial load text
        title.setStyle(UIStyles.getTitleStyle());

        // Listen for language changes to update title
        LanguageManager.getInstance().currentModeProperty()
                .addListener((obs, old, mode) -> title.setText(AppLabel.TITLE_SETTINGS.get()));

        // Language Section (Categorized as "General / \u0aad\u0abe\u0ab7\u0abe")
        VBox langSection = createSection("🌍 Language / \u0aad\u0abe\u0ab7\u0abe");
        Label langHint = new Label("Choose your preferred language for the interface.");
        langHint.setStyle("-fx-text-fill: " + LaxTheme.Colors.TEXT_SECONDARY + "; -fx-font-size: 11px;");

        ToggleGroup langGroup = new ToggleGroup();
        RadioButton rbEnglish = createRadioButton("English", LanguageMode.ENGLISH, langGroup);
        RadioButton rbGujarati = createRadioButton("Gujarati (\u0a97\u0ac1\u0a9c\u0ab0\u0abe\u0aa4\u0ac0)",
                LanguageMode.GUJARATI, langGroup);
        RadioButton rbCombine = createRadioButton("English / \u0a97\u0ac1\u0a9c\u0ab0\u0abe\u0aa4\u0ac0 (Bilingual)",
                LanguageMode.BILINGUAL, langGroup);

        // Select current
        LanguageMode current = LanguageManager.getInstance().getMode();
        if (current == LanguageMode.ENGLISH)
            rbEnglish.setSelected(true);
        else if (current == LanguageMode.GUJARATI)
            rbGujarati.setSelected(true);
        else
            rbCombine.setSelected(true);

        // Listener
        langGroup.selectedToggleProperty().addListener((obs, old, toggle) -> {
            if (toggle != null) {
                LanguageMode selected = (LanguageMode) toggle.getUserData();
                LanguageManager.getInstance().setMode(selected);
            }
        });

        VBox rbBox = new VBox(16, rbEnglish, rbGujarati, rbCombine); // Increased spacing
        rbBox.setPadding(new Insets(16, 0, 0, 8));
        langSection.getChildren().addAll(langHint, rbBox);

        // Backup Section (Categorized as "System & Security")
        VBox backupSection = createSection("💾 System & Backup");
        Label backupDesc = new Label("Automated local backups occur on exit. Set a secondary location for cloud sync.");
        backupDesc.setStyle("-fx-text-fill: " + LaxTheme.Colors.TEXT_SECONDARY + "; -fx-font-size: 11px;");

        VBox backupForm = new VBox(12);
        backupForm.setPadding(new Insets(10, 0, 0, 0));

        Label pathLabel = new Label("Secondary Backup Path:");
        pathLabel.setStyle(
                "-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: " + LaxTheme.Colors.TEXT_PRIMARY + ";");

        HBox backupRow = new HBox(12);
        backupRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        TextField pathField = new TextField();
        pathField.setEditable(false);
        pathField.setPrefWidth(350);
        pathField.setStyle(
                "-fx-background-color: #f1f5f9; -fx-border-color: #e2e8f0; -fx-border-radius: 4; -fx-padding: 8;");
        pathField.setText(com.lax.sme_manager.util.ConfigManager.getInstance()
                .getProperty(com.lax.sme_manager.util.ConfigManager.KEY_BACKUP_PATH, ""));

        Button browseBtn = new Button("Browse Folder");
        browseBtn.setStyle(LaxTheme.getButtonStyle(LaxTheme.ButtonType.SECONDARY));
        browseBtn.setOnAction(e -> {
            javafx.stage.DirectoryChooser chooser = new javafx.stage.DirectoryChooser();
            chooser.setTitle("Select Backup Folder");
            java.io.File selected = chooser.showDialog(getScene().getWindow());
            if (selected != null) {
                pathField.setText(selected.getAbsolutePath());
                com.lax.sme_manager.util.ConfigManager.getInstance().setProperty(
                        com.lax.sme_manager.util.ConfigManager.KEY_BACKUP_PATH, selected.getAbsolutePath());
            }
        });

        Button clearBtn = new Button("Clear");
        clearBtn.setStyle(LaxTheme.getButtonStyle(LaxTheme.ButtonType.SECONDARY));
        clearBtn.setOnAction(e -> {
            pathField.setText("");
            com.lax.sme_manager.util.ConfigManager.getInstance()
                    .setProperty(com.lax.sme_manager.util.ConfigManager.KEY_BACKUP_PATH, "");
        });

        backupRow.getChildren().addAll(pathField, browseBtn, clearBtn);
        backupForm.getChildren().addAll(pathLabel, backupRow);
        backupSection.getChildren().addAll(backupDesc, backupForm);

        getChildren().addAll(title, langSection, backupSection);
    }

    private VBox createSection(String title) {
        VBox box = new VBox(LaxTheme.Spacing.SPACE_16);
        box.setStyle(UIStyles.getCardStyle());
        box.setPadding(new Insets(LaxTheme.Layout.SUB_PANEL_PADDING)); // Design Manifest: 20px

        Label lbl = new Label(title);
        lbl.setStyle(
                "-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: " + LaxTheme.Colors.TEXT_PRIMARY + ";");

        box.getChildren().add(lbl);
        return box;
    }

    private RadioButton createRadioButton(String text, LanguageMode mode, ToggleGroup group) {
        RadioButton rb = new RadioButton(text);
        rb.setToggleGroup(group);
        rb.setUserData(mode);
        rb.setStyle("-fx-font-size: 13px; -fx-text-fill: " + LaxTheme.Colors.TEXT_PRIMARY + ";");
        return rb;
    }
}
