package com.lax.sme_manager.ui.view;

import com.lax.sme_manager.dto.ChequeData;
import com.lax.sme_manager.repository.ChequeConfigRepository;
import com.lax.sme_manager.repository.model.ChequeConfig;
import com.lax.sme_manager.service.ChequePrintService;
import com.lax.sme_manager.ui.theme.LaxTheme;
import com.lax.sme_manager.util.DatabaseManager;
import com.lax.sme_manager.util.IndianNumberToWords;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.stage.FileChooser;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.WritableImage;
import javafx.scene.SnapshotParameters;
import javafx.scene.paint.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChequePreviewDialog extends Dialog<Void> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChequePreviewDialog.class);
    private final ChequeData chequeData;
    private final ChequeConfigRepository configRepo;
    private final ChequePrintService printService;

    private ChequeConfig config;

    private Label[] lblDateDigits = new Label[8];
    private Label lblPayee;
    private Label lblAmountWords;
    private Label lblAmountDigits;
    private Pane chequePane;
    private ImageView bgView; // Reference to hide during export
    private ImageView sigView; // Digital Signature view
    private com.lax.sme_manager.repository.SignatureRepository sigRepo;
    private com.lax.sme_manager.repository.model.SignatureConfig activeSig;

    // New UI Components for Multi-Bank & Integrity
    private ComboBox<String> bankSelector;
    private TextField chequeNumberField;
    private Runnable onPrintComplete; // Callback to refresh parent view after print

    // CTS-2010 Dimension: 202mm x 92mm
    private static final double PREVIEW_WIDTH_PX = 850; // Slightly larger for clarity
    private static final double PREVIEW_HEIGHT_PX = PREVIEW_WIDTH_PX * (92.0 / 202.0);
    private static final double MM_TO_PX = PREVIEW_WIDTH_PX / 202.0;

    public ChequePreviewDialog(ChequeData chequeData) {
        this(chequeData, null);
    }

    public ChequePreviewDialog(ChequeData chequeData, Runnable onPrintComplete) {
        this.chequeData = chequeData;
        this.onPrintComplete = onPrintComplete;
        this.configRepo = new ChequeConfigRepository();
        this.sigRepo = new com.lax.sme_manager.repository.SignatureRepository();
        this.printService = new ChequePrintService();
        this.config = configRepo.getConfig();
        if (this.config == null)
            this.config = new ChequeConfig();
        
        if (this.config.getActiveSignatureId() > 0) {
            this.activeSig = sigRepo.getSignatureById(this.config.getActiveSignatureId());
        }

        setTitle("Cheque Precision Preview");
        initUI();
    }

    private void initUI() {
        getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        getDialogPane().setPrefSize(950, 700);

        VBox root = new VBox(20);
        root.setPadding(new Insets(25));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: white;");

        // Top Toolbar: Bank Selection & Cheque Number
        HBox topToolbar = new HBox(15);
        topToolbar.setAlignment(Pos.CENTER_LEFT);
        topToolbar.setPadding(new Insets(0, 0, 10, 0));
        
        Label bankLbl = new Label("Bank Template:");
        bankLbl.setStyle("-fx-font-weight: bold;");
        
        bankSelector = new ComboBox<>();
        bankSelector.setEditable(true);
        bankSelector.setPromptText("Select or Type Bank Name");
        bankSelector.setPrefWidth(200);
        loadBankTemplates();
        bankSelector.setOnAction(e -> handleBankSelection());

        Label chqLbl = new Label("Cheque No:");
        chqLbl.setStyle("-fx-font-weight: bold;");

        chequeNumberField = new TextField();
        chequeNumberField.setPromptText("Enter 6-digit number");
        chequeNumberField.setPrefWidth(150);
        // If we extracted cheque number in future, set it here. For now, it's manual entry.

        Region spacerObj = new Region();
        HBox.setHgrow(spacerObj, Priority.ALWAYS);

        topToolbar.getChildren().addAll(bankLbl, bankSelector, new Label("  |  "), chqLbl, chequeNumberField, spacerObj);
        
        // 1. Cheque Canvas Container
        StackPane canvasContainer = new StackPane();
        canvasContainer.setPadding(new Insets(10));
        canvasContainer.setStyle(
                "-fx-background-color: #f1f5f9; -fx-border-color: #cbd5e1; -fx-border-radius: 8; -fx-background-radius: 8;");

        chequePane = new Pane();
        chequePane.setPrefSize(PREVIEW_WIDTH_PX, PREVIEW_HEIGHT_PX);
        chequePane.setMaxSize(PREVIEW_WIDTH_PX, PREVIEW_HEIGHT_PX);

        // Background Image
        try {
            Image bg = null;
            var is = getClass().getResourceAsStream("/images/standard_cheque.png");
            if (is != null)
                bg = new Image(is);

            if (bg == null) {
                File file = new File("src/main/resources/images/standard_cheque.png");
                if (file.exists())
                    bg = new Image(file.toURI().toString());
            }

            if (bg != null) {
                bgView = new ImageView(bg);
                bgView.setFitWidth(PREVIEW_WIDTH_PX);
                bgView.setFitHeight(PREVIEW_HEIGHT_PX);
                bgView.setPreserveRatio(false); // Force fit to our calculated aspect ratio
                chequePane.getChildren().add(bgView);
            }
        } catch (Exception e) {
            chequePane.setStyle("-fx-background-color: #f8fafc;");
        }

        addElements();
        canvasContainer.getChildren().add(chequePane);

        // 2. Bottom Toolbar
        HBox toolbar = new HBox(15);
        toolbar.setAlignment(Pos.CENTER);

        Button btnSave = new Button("💾 Save Alignment");
        btnSave.setStyle(LaxTheme.getButtonStyle(LaxTheme.ButtonType.SECONDARY) + "; -fx-padding: 8 15;");
        btnSave.setOnAction(e -> save());

        Button btnDownload = new Button("📥 Download Format");
        btnDownload.setStyle(LaxTheme.getButtonStyle(LaxTheme.ButtonType.SECONDARY) + "; -fx-padding: 8 15;");
        btnDownload.setOnAction(e -> downloadFormat());

        Button btnPrint = new Button("🖨️ Print Cheque");
        btnPrint.setStyle(LaxTheme.getButtonStyle(LaxTheme.ButtonType.PRIMARY) + "; -fx-padding: 8 20;");
        btnPrint.setOnAction(e -> print());

        toolbar.getChildren().addAll(btnSave, btnDownload, btnPrint);

        Label hint = new Label(
                "Drag labels to align perfectly with your cheque leaf. Positions are saved for the selected Bank.");
        hint.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-font-style: italic;");

        root.getChildren().addAll(topToolbar, canvasContainer, hint, toolbar);
        getDialogPane().setContent(root);
        
        // Load initial selection if available
        if (config.getBankName() != null && !config.getBankName().isEmpty()) {
            bankSelector.getSelectionModel().select(config.getBankName());
        }
    }

    private void addElements() {
        String dStr = chequeData.date() != null ? chequeData.date().format(DateTimeFormatter.ofPattern("ddMMyyyy")) : "14022026";
        
        // Handle Date Digits
        String[] positions = config.getDatePositions() != null ? config.getDatePositions().split(";") : null;
        
        for (int i = 0; i < 8; i++) {
            double x, y;
            if (positions != null && i < positions.length) {
                String[] xy = positions[i].split(",");
                x = Double.parseDouble(xy[0]);
                y = Double.parseDouble(xy[1]);
            } else {
                // Default: Start at dateX, dateY and space them out (e.g., 5.5mm spacing)
                x = (config.getDateX() > 0 ? config.getDateX() : 160) + (i * 5.5);
                y = (config.getDateY() > 0 ? config.getDateY() : 12);
            }
            lblDateDigits[i] = createField(String.valueOf(dStr.charAt(i)), x, y, "Date Digit " + (i+1));
            lblDateDigits[i].setFont(Font.font("Courier New", FontWeight.BOLD, 22));
            chequePane.getChildren().add(lblDateDigits[i]);
        }

        lblPayee = createField(chequeData.payeeName(), config.getPayeeX() > 0 ? config.getPayeeX() : 25,
                config.getPayeeY() > 0 ? config.getPayeeY() : 35, "Payee");

        relayoutAmountWords();

        String digits = String.format("%.2f/-", chequeData.amount());
        lblAmountDigits = createField(digits, config.getAmountDigitsX() > 0 ? config.getAmountDigitsX() : 155,
                config.getAmountDigitsY() > 0 ? config.getAmountDigitsY() : 54, "Amount Digits");

        chequePane.getChildren().addAll(lblPayee, lblAmountWords, lblAmountDigits);

        // Add Active Signature
        if (activeSig != null && activeSig.getPath() != null) {
            try {
                File f = new File(activeSig.getPath());
                if (f.exists()) {
                    Image img = new Image(f.toURI().toString());
                    sigView = new ImageView(img);
                    
                    double sigW = 40 * MM_TO_PX * activeSig.getScale();
                    sigView.setFitWidth(sigW);
                    sigView.setPreserveRatio(true);
                    
                    double x = config.getSignatureX() > 0 ? config.getSignatureX() : 150;
                    double y = config.getSignatureY() > 0 ? config.getSignatureY() : 65;
                    sigView.setLayoutX(x * MM_TO_PX);
                    sigView.setLayoutY(y * MM_TO_PX);
                    
                    // PEN-AUTHENTIC LOOK
                    sigView.setOpacity(activeSig.getOpacity());
                    sigView.setBlendMode(BlendMode.MULTIPLY); // Makes it look printed/inked
                    
                    // Dragging
                    final Delta delta = new Delta();
                    sigView.setCursor(Cursor.MOVE);
                    sigView.setOnMousePressed(e -> {
                        delta.x = sigView.getLayoutX() - e.getSceneX();
                        delta.y = sigView.getLayoutY() - e.getSceneY();
                        sigView.toFront();
                    });
                    sigView.setOnMouseDragged(e -> {
                        sigView.setLayoutX(e.getSceneX() + delta.x);
                        sigView.setLayoutY(e.getSceneY() + delta.y);
                    });
                    
                    chequePane.getChildren().add(sigView);
                }
            } catch (Exception e) {
                LOGGER.error("Error loading signature in preview", e);
            }
        }

        if (chequeData.isAcPayee()) {
            Label ac = new Label("A/C PAYEE");
            ac.setStyle(
                    "-fx-border-color: #1e293b; -fx-border-width: 1.5 0 1.5 0; -fx-padding: 4 12; -fx-font-weight: bold; -fx-rotate: -15; -fx-text-fill: #1e293b;");
            ac.setLayoutX(15 * MM_TO_PX);
            ac.setLayoutY(10 * MM_TO_PX);
            chequePane.getChildren().add(ac);
        }
    }

    private Label createField(String text, double xMm, double yMm, String hint) {
        return createField(text, xMm, yMm, hint, 18);
    }

    private Label createField(String text, double xMm, double yMm, String hint, double fontSize) {
        if (text == null || text.isEmpty())
            text = "[" + hint + "]";
        Label l = new Label(text);
        
        applyDraggableStyle(l, fontSize, hint);
        
        l.setLayoutX(xMm * MM_TO_PX);
        l.setLayoutY(yMm * MM_TO_PX);

        final Delta delta = new Delta();
        l.setOnMousePressed(e -> {
            delta.x = l.getLayoutX() - e.getSceneX();
            delta.y = l.getLayoutY() - e.getSceneY();
            l.toFront();
        });
        l.setOnMouseDragged(e -> {
            l.setLayoutX(e.getSceneX() + delta.x);
            l.setLayoutY(e.getSceneY() + delta.y);
            
            // If dragging amount words, trigger relayout to recalculate wrapping
            if (l == lblAmountWords) {
                relayoutAmountWords();
            }
        });

        return l;
    }

    private void applyDraggableStyle(Label l, double fontSize, String hint) {
        String baseStyle = "-fx-text-fill: #1e1b4b; -fx-font-family: 'Courier New'; -fx-font-weight: bold; "
                + "-fx-padding: 2 4; -fx-background-color: transparent; -fx-font-size: " + fontSize + "px;";
        
        l.setStyle(baseStyle);
        l.setFont(Font.font("Courier New", FontWeight.BOLD, fontSize));
        l.setTooltip(new Tooltip(hint));
        l.setCursor(Cursor.MOVE);
        
        l.setOnMouseEntered(e -> l.setStyle(baseStyle + " -fx-border-color: #0d9488; -fx-border-width: 1; -fx-border-style: dashed; -fx-background-color: rgba(13, 148, 136, 0.05);"));
        l.setOnMouseExited(e -> l.setStyle(baseStyle));
    }

    private void relayoutAmountWords() {
        if (lblAmountWords == null) {
            String words = IndianNumberToWords.convert(chequeData.amount());
            double x = config.getAmountWordsX() > 0 ? config.getAmountWordsX() : 25;
            double y = config.getAmountWordsY() > 0 ? config.getAmountWordsY() : 48;
            lblAmountWords = createField(words, x, y, "Amount Words");
        }

        double wordsXmm = lblAmountWords.getLayoutX() / MM_TO_PX;
        String words = lblAmountWords.getText();
        
        // Available width = from label X to the right edge of the cheque (with 5mm margin)
        double availableWidth = Math.max(50, (197 - wordsXmm) * MM_TO_PX);
        
        lblAmountWords.setMinWidth(0);
        lblAmountWords.setPrefWidth(availableWidth);
        lblAmountWords.setMaxWidth(availableWidth);
        lblAmountWords.setWrapText(true);
        
        // Auto-calculate font size
        int wordsLen = words.length();
        double singleLineFontSize = availableWidth / (wordsLen * 0.6);
        double amtFontSize;
        if (singleLineFontSize >= 12) {
            amtFontSize = Math.min(singleLineFontSize, 16);
        } else {
            double twoLineFontSize = (availableWidth * 2) / (wordsLen * 0.6);
            amtFontSize = Math.min(Math.max(twoLineFontSize, 8), 14);
        }
        amtFontSize = Math.round(amtFontSize);
        
        // Re-apply style to fix hover bug and update size
        applyDraggableStyle(lblAmountWords, amtFontSize, "Amount Words");
        lblAmountWords.setMinHeight(amtFontSize * 3.0);
    }

    private void updateConfigFromUI() {
        config.setPayeeX(lblPayee.getLayoutX() / MM_TO_PX);
        config.setPayeeY(lblPayee.getLayoutY() / MM_TO_PX);
        config.setAmountWordsX(lblAmountWords.getLayoutX() / MM_TO_PX);
        config.setAmountWordsY(lblAmountWords.getLayoutY() / MM_TO_PX);
        config.setAmountDigitsX(lblAmountDigits.getLayoutX() / MM_TO_PX);
        config.setAmountDigitsY(lblAmountDigits.getLayoutY() / MM_TO_PX);

        // Serialize Date Digit Positions
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            sb.append(String.format("%.2f,%.2f", lblDateDigits[i].getLayoutX() / MM_TO_PX, lblDateDigits[i].getLayoutY() / MM_TO_PX));
            if (i < 7) sb.append(";");
        }
        config.setDatePositions(sb.toString());
        // Also update the "base" dateX/Y as the position of the first digit
        config.setDateX(lblDateDigits[0].getLayoutX() / MM_TO_PX);
        config.setDateY(lblDateDigits[0].getLayoutY() / MM_TO_PX);

        if (sigView != null) {
            config.setSignatureX(sigView.getLayoutX() / MM_TO_PX);
            config.setSignatureY(sigView.getLayoutY() / MM_TO_PX);
        }
        
        String bName = bankSelector.getValue();
        if (bName != null && !bName.isBlank()) {
            config.setBankName(bName.trim());
        }
    }

    // --- Multi-Bank Features ---
    
    private void loadBankTemplates() {
        List<String> banks = new ArrayList<>();
        String sql = "SELECT bank_name FROM bank_templates ORDER BY bank_name";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                banks.add(rs.getString("bank_name"));
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to load bank templates", e);
        }
        bankSelector.getItems().setAll(banks);
    }
    
    private void handleBankSelection() {
        String selectedBank = bankSelector.getValue();
        if (selectedBank == null || selectedBank.isBlank()) return;
        
        String sql = "SELECT * FROM bank_templates WHERE bank_name = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, selectedBank);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                // Update UI elements from DB
                updateUIField(lblPayee, rs.getDouble("payee_x"), rs.getDouble("payee_y"));
                updateUIField(lblAmountWords, rs.getDouble("amount_words_x"), rs.getDouble("amount_words_y"));
                updateUIField(lblAmountDigits, rs.getDouble("amount_digits_x"), rs.getDouble("amount_digits_y"));
                updateUIField(lblDateDigits[0], rs.getDouble("date_x"), rs.getDouble("date_y")); // Approximation
                
                // Relayout date digits based on new start
                double startX = rs.getDouble("date_x") * MM_TO_PX;
                double startY = rs.getDouble("date_y") * MM_TO_PX;
                double spacingX = selectedBank.contains("State Bank of India") ? 4.5 : 5.5;
                
                for(int i=0; i<8; i++) {
                     lblDateDigits[i].setLayoutX(startX + (i * spacingX * MM_TO_PX));
                     lblDateDigits[i].setLayoutY(startY);
                }
                
                if (sigView != null) {
                    sigView.setLayoutX(rs.getDouble("signature_x") * MM_TO_PX);
                    sigView.setLayoutY(rs.getDouble("signature_y") * MM_TO_PX);
                }

                // After updating from DB, force amount words to relayout
                relayoutAmountWords();
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to load template for bank: " + selectedBank, e);
        }
    }
    
    private void updateUIField(javafx.scene.Node node, double xMm, double yMm) {
        if (xMm > 0) node.setLayoutX(xMm * MM_TO_PX);
        if (yMm > 0) node.setLayoutY(yMm * MM_TO_PX);
    }

    private void save() {
        updateConfigFromUI();
        configRepo.saveConfig(config); // Save as current global preference
        
        // Also save to bank_templates
        String bankName = config.getBankName();
        if (bankName != null && !bankName.isEmpty()) {
            String sqlCheck = "SELECT count(*) FROM bank_templates WHERE bank_name = ?";
            String sqlInsert = "INSERT INTO bank_templates (bank_name, date_x, date_y, payee_x, payee_y, amount_words_x, amount_words_y, amount_digits_x, amount_digits_y, signature_x, signature_y) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
            String sqlUpdate = "UPDATE bank_templates SET date_x=?, date_y=?, payee_x=?, payee_y=?, amount_words_x=?, amount_words_y=?, amount_digits_x=?, amount_digits_y=?, signature_x=?, signature_y=? WHERE bank_name=?";
            
            try (Connection conn = DatabaseManager.getConnection()) {
                boolean exists = false;
                try (PreparedStatement check = conn.prepareStatement(sqlCheck)) {
                   check.setString(1, bankName);
                   ResultSet rs = check.executeQuery();
                   if (rs.next() && rs.getInt(1) > 0) exists = true;
                }
                
                if (exists) {
                    try (PreparedStatement update = conn.prepareStatement(sqlUpdate)) {
                         update.setDouble(1, config.getDateX());
                         update.setDouble(2, config.getDateY());
                         update.setDouble(3, config.getPayeeX());
                         update.setDouble(4, config.getPayeeY());
                         update.setDouble(5, config.getAmountWordsX());
                         update.setDouble(6, config.getAmountWordsY());
                         update.setDouble(7, config.getAmountDigitsX());
                         update.setDouble(8, config.getAmountDigitsY());
                         update.setDouble(9, config.getSignatureX());
                         update.setDouble(10, config.getSignatureY());
                         update.setString(11, bankName);
                         update.executeUpdate();
                    }
                } else {
                    try (PreparedStatement insert = conn.prepareStatement(sqlInsert)) {
                         insert.setString(1, bankName);
                         insert.setDouble(2, config.getDateX());
                         insert.setDouble(3, config.getDateY());
                         insert.setDouble(4, config.getPayeeX());
                         insert.setDouble(5, config.getPayeeY());
                         insert.setDouble(6, config.getAmountWordsX());
                         insert.setDouble(7, config.getAmountWordsY());
                         insert.setDouble(8, config.getAmountDigitsX());
                         insert.setDouble(9, config.getAmountDigitsY());
                         insert.setDouble(10, config.getSignatureX());
                         insert.setDouble(11, config.getSignatureY());
                         insert.executeUpdate();
                    }
                }
                
                if (!bankSelector.getItems().contains(bankName)) {
                    bankSelector.getItems().add(bankName);
                }
            } catch (SQLException e) {
                LOGGER.error("Failed to save bank template", e);
                new Alert(Alert.AlertType.ERROR, "Could not save bank template: " + e.getMessage()).show();
                return;
            }
        }
        
        Alert a = new Alert(Alert.AlertType.INFORMATION, "Alignment parameters saved for " + (bankName != null ? bankName : "Default") + "+.");
        a.setHeaderText("Layout Saved");
        a.show();
    }

    private void downloadFormat() {
        try {
            if (bgView != null) bgView.setVisible(false); // Hide background for format-only export
            
            SnapshotParameters params = new SnapshotParameters();
            params.setFill(Color.TRANSPARENT);
            WritableImage image = chequePane.snapshot(params, null);
            
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Cheque Format");
            fileChooser.setInitialFileName("cheque_format_" + chequeData.payeeName().trim().replace(" ", "_") + ".png");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Files", "*.png"));
            
            File file = fileChooser.showSaveDialog(getDialogPane().getScene().getWindow());
            if (file != null) {
                BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);
                ImageIO.write(bImage, "png", file);
                new Alert(Alert.AlertType.INFORMATION, "Format saved to: " + file.getAbsolutePath()).show();
            }
            
            if (bgView != null) bgView.setVisible(true); // Restore background
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Download Failed: " + e.getMessage()).show();
        }
    }

    private void print() {
        String chqNo = chequeNumberField.getText().trim();
        
        // Validation
        if (chequeData.purchaseId() != null) {
             if (chqNo.isEmpty() || chqNo.length() < 6) {
                 new Alert(Alert.AlertType.ERROR, "Please enter a valid Cheque Number before printing.").show();
                 return;
             }
             
             // Feature 3: Fraud Protection Loop (Duplicate Check)
             if (isChequeNumberDuplicate(chqNo)) {
                 Alert alert = new Alert(Alert.AlertType.ERROR);
                 alert.setTitle("Security Warning");
                 alert.setHeaderText("Duplicate Cheque Number");
                 alert.setContentText("Cheque number " + chqNo + " has already been used. Please use a fresh leaf.");
                 alert.show();
                 return;
             }
        }
    
        try {
            updateConfigFromUI(); // Ensure we print exactly what is seen
            printService.printSilent(config, chequeData);
            
            // Feature 1: Auto-Reconciliation Loop
            if (chequeData.purchaseId() != null) {
                markPurchaseAsPaid(chequeData.purchaseId(), chqNo);
                new Alert(Alert.AlertType.INFORMATION, "Print successful! Purchase marked as PAID.").show();
            } else {
                new Alert(Alert.AlertType.INFORMATION, "Print job sent successfully.").show();
            }
            
            // Fire refresh callback to update the Purchase History table immediately
            if (onPrintComplete != null) {
                javafx.application.Platform.runLater(onPrintComplete);
            }
            
            close(); // Close dialog after successful print
            
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Printing Failed: " + e.getMessage()).show();
        }
    }
    
    private boolean isChequeNumberDuplicate(String chqNo) {
        String sql = "SELECT COUNT(*) FROM purchase_entries WHERE cheque_number = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, chqNo);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to check duplicate cheque", e);
        }
        return false;
    }
    
    private void markPurchaseAsPaid(int purchaseId, String chqNo) {
        String sql = "UPDATE purchase_entries SET status = 'PAID', cheque_number = ?, cheque_date = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, chqNo);
            stmt.setObject(2, java.time.LocalDate.now());
            stmt.setInt(3, purchaseId);
            stmt.executeUpdate();
            
            // UI Update is handled by PurchaseHistoryView listening to property/refresh, 
            // but since we are modifying DB directly, we should trigger a refresh if feasible, 
            // or assume the view refreshes on re-entry.
            // Ideally notify a listener or EventBus.
            
        } catch (SQLException e) {
            LOGGER.error("Failed to auto-reconcile purchase", e);
            javafx.application.Platform.runLater(() -> 
                new Alert(Alert.AlertType.WARNING, "Cheque printed but failed to update status: " + e.getMessage()).show()
            );
        }
    }

    private static class Delta {
        double x, y;
    }
}
