package com.lax.sme_manager.ui.view;

import com.lax.sme_manager.repository.SignatureRepository;
import com.lax.sme_manager.repository.ChequeConfigRepository;
import com.lax.sme_manager.repository.model.SignatureConfig;
import com.lax.sme_manager.repository.model.ChequeConfig;
import com.lax.sme_manager.ui.component.UIStyles;
import com.lax.sme_manager.ui.theme.LaxTheme;
import com.lax.sme_manager.util.ImageUtils;
import com.lax.sme_manager.util.BackupService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;

public class SettingsView extends VBox {

    private final SignatureRepository sigRepo = new SignatureRepository();
    private final ChequeConfigRepository configRepo = new ChequeConfigRepository();
    private final VBox signatureList = new VBox(10);
    private final ImageView previewView = new ImageView();
    private SignatureConfig activeConfig;
    private ChequeConfig chequeConfig;

    public SettingsView() {
        this.chequeConfig = configRepo.getConfig();
        if (this.chequeConfig == null)
            this.chequeConfig = new ChequeConfig();
        initializeUI();
        loadSignatures();
    }

    private void initializeUI() {
        setPadding(new Insets(25));
        setSpacing(32);
        setStyle("-fx-background-color: transparent;");

        Label titleLabel = new Label("⚙️ Settings");
        titleLabel.setStyle(UIStyles.getTitleStyle());

        // --- 1. GENERAL SETTINGS (Restored Language) ---
        VBox generalSection = createSection("🔧 General & Localization");
        GridPane generalGrid = new GridPane();
        generalGrid.setHgap(20);
        generalGrid.setVgap(15);

        generalGrid.add(new Label("Application Language:"), 0, 0);
        ComboBox<com.lax.sme_manager.util.i18n.LanguageMode> langCombo = new ComboBox<>();
        langCombo.getItems().addAll(com.lax.sme_manager.util.i18n.LanguageMode.values());
        langCombo.setValue(com.lax.sme_manager.util.i18n.LanguageManager.getInstance().getMode());
        langCombo.getSelectionModel().selectedItemProperty().addListener((obs, old, n) -> {
            if (n != null)
                com.lax.sme_manager.util.i18n.LanguageManager.getInstance().setMode(n);
        });
        generalGrid.add(langCombo, 1, 0);

        generalGrid.add(new Label("Database Path:"), 0, 1);
        TextField dbPath = new TextField("data/lax_manager.db");
        dbPath.setEditable(false);
        dbPath.setPrefWidth(300);
        generalGrid.add(dbPath, 1, 1);

        Button btnBackup = new Button("📦 Create Backup Now");
        btnBackup.setStyle(LaxTheme.getButtonStyle(LaxTheme.ButtonType.SECONDARY));
        btnBackup.setOnAction(e -> {
            new BackupService().performBackup();
            new Alert(Alert.AlertType.INFORMATION, "Manual Backup Successful").show();
        });
        generalGrid.add(btnBackup, 1, 2);
        generalSection.getChildren().add(generalGrid);

        // --- 2. CHEQUE CONFIGURATION ---
        VBox chequeSection = createSection("🏦 Cheque Print Defaults");
        GridPane chequeGrid = new GridPane();
        chequeGrid.setHgap(20);
        chequeGrid.setVgap(15);

        chequeGrid.add(new Label("Default Bank Name:"), 0, 0);
        TextField bankName = new TextField(chequeConfig.getBankName());
        bankName.setPrefWidth(250);
        chequeGrid.add(bankName, 1, 0);

        chequeGrid.add(new Label("Default Font Size:"), 0, 1);
        Slider fontSlider = new Slider(8, 24, chequeConfig.getFontSize() > 0 ? chequeConfig.getFontSize() : 12);
        fontSlider.setShowTickLabels(true);
        fontSlider.setShowTickMarks(true);
        chequeGrid.add(fontSlider, 1, 1);

        Button btnSaveCheque = new Button("Save Cheque Defaults");
        btnSaveCheque.setStyle(LaxTheme.getButtonStyle(LaxTheme.ButtonType.PRIMARY));
        btnSaveCheque.setOnAction(e -> {
            chequeConfig.setBankName(bankName.getText());
            chequeConfig.setFontSize((int) fontSlider.getValue());
            configRepo.saveConfig(chequeConfig);
            new Alert(Alert.AlertType.INFORMATION, "Cheque defaults saved.").show();
        });
        chequeGrid.add(btnSaveCheque, 1, 2);
        chequeSection.getChildren().add(chequeGrid);

        // --- 3. SIGNATURE SECTION ---
        VBox sigSection = createSection("🖋️ Digital Signature Management");
        Label rbiNote = new Label(
                "RBI Compliance: Ensure scanned signatures look 'pen-authentic'. Check opacity and transparency.");
        rbiNote.setStyle(
                "-fx-text-fill: #b45309; -fx-background-color: #fffbeb; -fx-padding: 10; -fx-border-color: #f59e0b; -fx-font-size: 11px;");

        HBox sigLayout = new HBox(30);
        VBox left = new VBox(15);
        Button btnAdd = new Button("+ Add New Signature");
        btnAdd.setStyle(LaxTheme.getButtonStyle(LaxTheme.ButtonType.PRIMARY));
        btnAdd.setOnAction(e -> handleAdd());

        signatureList.setPrefWidth(250);
        left.getChildren().addAll(btnAdd, new Label("Saved Signatures:"), signatureList);

        VBox right = new VBox(15);
        right.setStyle(
                "-fx-background-color: #f8fafc; -fx-padding: 15; -fx-border-color: #e2e8f0; -fx-background-radius: 8;");
        Label tuningLabel = new Label("Signature Tuning");
        tuningLabel.setStyle("-fx-font-weight: bold;");

        Slider opacitySlider = new Slider(0.1, 1.0, 1.0);
        CheckBox cbTransparent = new CheckBox("Remove Background (Make Transparent)");
        cbTransparent.setSelected(true);

        opacitySlider.valueProperty().addListener((obs, old, n) -> {
            if (activeConfig != null) {
                activeConfig.setOpacity(n.doubleValue());
                updatePreview();
            }
        });
        cbTransparent.selectedProperty().addListener((obs, old, n) -> {
            if (activeConfig != null) {
                activeConfig.setTransparent(n);
                updatePreview();
            }
        });

        StackPane previewBox = new StackPane(previewView);
        previewBox.setPrefSize(200, 70);
        previewBox.setStyle(
                "-fx-border-color: #64748b; -fx-border-style: dashed; -fx-background-color: white; -fx-padding: 5;");

        Button btnSaveSig = new Button("Apply Tuning & Save");
        btnSaveSig.setStyle(LaxTheme.getButtonStyle(LaxTheme.ButtonType.SECONDARY));
        btnSaveSig.setOnAction(e -> {
            if (activeConfig != null) {
                sigRepo.saveSignature(activeConfig);
                new Alert(Alert.AlertType.INFORMATION, "Signature tuning saved.").show();
            }
        });

        right.getChildren().addAll(tuningLabel, new Label("Opacity:"), opacitySlider, cbTransparent,
                new Label("Actual Size Visualizer (Scale 1:1):"), previewBox, btnSaveSig);

        sigLayout.getChildren().addAll(left, right);
        sigSection.getChildren().addAll(rbiNote, sigLayout);

        getChildren().addAll(titleLabel, generalSection, chequeSection, sigSection);
    }

    private void loadSignatures() {
        signatureList.getChildren().clear();
        List<SignatureConfig> list = sigRepo.getAllSignatures();
        for (SignatureConfig sig : list) {
            HBox item = new HBox(10);
            item.setAlignment(Pos.CENTER_LEFT);
            item.setStyle(
                    "-fx-padding: 8; -fx-border-color: #e2e8f0; -fx-background-color: white; -fx-background-radius: 4;");

            Label name = new Label(sig.getName());
            name.setPrefWidth(120);
            name.setStyle("-fx-font-size: 12px;");

            Button btnActive = new Button(chequeConfig.getActiveSignatureId() == sig.getId() ? "✅ Active" : "Set Active");
            btnActive.setStyle(chequeConfig.getActiveSignatureId() == sig.getId() 
                ? "-fx-background-color: #0d9488; -fx-text-fill: white;" 
                : LaxTheme.getButtonStyle(LaxTheme.ButtonType.SECONDARY));
            btnActive.setOnAction(e -> {
                chequeConfig.setActiveSignatureId(sig.getId());
                configRepo.saveConfig(chequeConfig);
                loadSignatures();
                new Alert(Alert.AlertType.INFORMATION, "Signature set as active for printing.").show();
            });

            Button btnSelect = new Button("⚙️");
            btnSelect.setTooltip(new Tooltip("Edit Tuning"));
            btnSelect.setOnAction(e -> selectSignature(sig));

            Button btnDel = new Button("🗑️");
            btnDel.setStyle("-fx-text-fill: #ef4444; -fx-background-color: transparent;");
            btnDel.setOnAction(e -> {
                sigRepo.deleteSignature(sig.getId());
                if (chequeConfig.getActiveSignatureId() == sig.getId()) {
                    chequeConfig.setActiveSignatureId(0);
                    configRepo.saveConfig(chequeConfig);
                }
                loadSignatures();
            });

            item.getChildren().addAll(name, btnActive, btnSelect, btnDel);
            signatureList.getChildren().add(item);
        }
    }

    private void selectSignature(SignatureConfig sig) {
        this.activeConfig = sig;
        updatePreview();
    }

    private void handleAdd() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose Signature Image");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File file = chooser.showOpenDialog(getScene().getWindow());
        if (file != null) {
            try {
                SignatureConfig sig = SignatureConfig.builder()
                        .name(file.getName())
                        .path(file.getAbsolutePath())
                        .opacity(1.0)
                        .isTransparent(true)
                        .scale(1.0)
                        .thickness(1.0)
                        .build();
                sigRepo.saveSignature(sig);

                // Immediately reload and select the new one
                loadSignatures();
                List<SignatureConfig> updatedList = sigRepo.getAllSignatures();
                SignatureConfig added = updatedList.stream()
                        .filter(s -> s.getPath().equals(file.getAbsolutePath()))
                        .findFirst().orElse(null);
                if (added != null)
                    selectSignature(added);

                new Alert(Alert.AlertType.INFORMATION, "Signature uploaded and selected for tuning.").show();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Failed to upload: " + e.getMessage()).show();
            }
        }
    }

    private void updatePreview() {
        if (activeConfig == null)
            return;
        var img = ImageUtils.processSignature(activeConfig.getPath(), activeConfig.getOpacity(),
                activeConfig.isTransparent());
        if (img != null) {
            previewView.setImage(img);
            previewView.setFitWidth(180);
            previewView.setFitHeight(60);
            previewView.setPreserveRatio(true);
        } else {
            previewView.setImage(null);
            Label error = new Label("Image load fail");
            error.setStyle("-fx-text-fill: red;");
            // stackpane will show it
        }
    }

    private VBox createSection(String title) {
        VBox box = new VBox(15);
        box.setStyle(UIStyles.getCardStyle());
        box.setPadding(new Insets(20));
        Label lbl = new Label(title);
        lbl.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #334155;");
        box.getChildren().add(lbl);
        return box;
    }
}
