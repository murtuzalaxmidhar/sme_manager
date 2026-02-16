package com.lax.sme_manager.ui.view;

import com.lax.sme_manager.dto.ChequeData;
import com.lax.sme_manager.repository.ChequeConfigRepository;
import com.lax.sme_manager.repository.model.ChequeConfig;
import com.lax.sme_manager.service.ChequePrintService;
import com.lax.sme_manager.ui.theme.LaxTheme;
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
import java.time.format.DateTimeFormatter;
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

    // CTS-2010 Dimension: 202mm x 92mm
    private static final double PREVIEW_WIDTH_PX = 850; // Slightly larger for clarity
    private static final double PREVIEW_HEIGHT_PX = PREVIEW_WIDTH_PX * (92.0 / 202.0);
    private static final double MM_TO_PX = PREVIEW_WIDTH_PX / 202.0;

    public ChequePreviewDialog(ChequeData chequeData) {
        this.chequeData = chequeData;
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
        getDialogPane().setPrefSize(950, 650); // Increased height for taller buttons

        VBox root = new VBox(20);
        root.setPadding(new Insets(25));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: white;");

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

        // 2. Toolbar
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
                "Drag labels to align perfectly with your cheque leaf. Positions are saved in millimeters.");
        hint.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-font-style: italic;");

        root.getChildren().addAll(canvasContainer, hint, toolbar);
        getDialogPane().setContent(root);
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

        String words = IndianNumberToWords.convert(chequeData.amount());
        lblAmountWords = createField(words, config.getAmountWordsX() > 0 ? config.getAmountWordsX() : 25,
                config.getAmountWordsY() > 0 ? config.getAmountWordsY() : 48, "Amount Words");
        lblAmountWords.setMaxWidth(450 * MM_TO_PX);
        lblAmountWords.setWrapText(true);

        String digits = String.format("**%.2f/-", chequeData.amount());
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
            Label ac = new Label("A/C PAYEE ONLY");
            ac.setStyle(
                    "-fx-border-color: #1e293b; -fx-border-width: 2; -fx-padding: 4 8; -fx-font-weight: bold; -fx-rotate: -15; -fx-text-fill: #1e293b; -fx-background-color: white;");
            ac.setLayoutX(15 * MM_TO_PX);
            ac.setLayoutY(10 * MM_TO_PX);
            chequePane.getChildren().add(ac);
        }
    }

    private Label createField(String text, double xMm, double yMm, String hint) {
        if (text == null || text.isEmpty())
            text = "[" + hint + "]";
        Label l = new Label(text);
        
        // Transparent style (Printed look)
        String style = "-fx-text-fill: #1e1b4b; -fx-font-family: 'Courier New'; -fx-font-weight: bold; -fx-padding: 2 4; -fx-background-color: transparent;";
        
        // Default font size for all fields unless overridden later
        style += " -fx-font-size: 18px;";
        
        final String finalStyle = style; // Make it effectively final for lambda
        l.setStyle(finalStyle);
        l.setTooltip(new Tooltip(hint));
        l.setCursor(Cursor.MOVE);
        l.setLayoutX(xMm * MM_TO_PX);
        l.setLayoutY(yMm * MM_TO_PX);

        final Delta delta = new Delta();
        
        // Add border on hover so user knows it's draggable
        l.setOnMouseEntered(e -> l.setStyle(finalStyle + " -fx-border-color: #0d9488; -fx-border-width: 1; -fx-border-style: dashed; -fx-background-color: rgba(13, 148, 136, 0.05);"));
        l.setOnMouseExited(e -> l.setStyle(finalStyle));

        l.setOnMousePressed(e -> {
            delta.x = l.getLayoutX() - e.getSceneX();
            delta.y = l.getLayoutY() - e.getSceneY();
            l.toFront();
        });
        l.setOnMouseDragged(e -> {
            l.setLayoutX(e.getSceneX() + delta.x);
            l.setLayoutY(e.getSceneY() + delta.y);
        });

        return l;
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
    }

    private void save() {
        updateConfigFromUI();
        configRepo.saveConfig(config);
        Alert a = new Alert(Alert.AlertType.INFORMATION, "Alignment parameters saved for future use.");
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
        try {
            updateConfigFromUI(); // Ensure we print exactly what is seen
            printService.printSilent(config, chequeData);
            new Alert(Alert.AlertType.INFORMATION, "Print job sent successfully.").show();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Printing Failed: " + e.getMessage()).show();
        }
    }

    private static class Delta {
        double x, y;
    }
}
