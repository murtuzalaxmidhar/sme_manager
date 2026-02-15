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

public class ChequePreviewDialog extends Dialog<Void> {

    private final ChequeData chequeData;
    private final ChequeConfigRepository configRepo;
    private final ChequePrintService printService;

    private ChequeConfig config;

    private Label lblDate;
    private Label lblPayee;
    private Label lblAmountWords;
    private Label lblAmountDigits;
    private Pane chequePane;

    // CTS-2010 Dimension: 202mm x 92mm
    private static final double PREVIEW_WIDTH_PX = 850; // Slightly larger for clarity
    private static final double PREVIEW_HEIGHT_PX = PREVIEW_WIDTH_PX * (92.0 / 202.0);
    private static final double MM_TO_PX = PREVIEW_WIDTH_PX / 202.0;

    public ChequePreviewDialog(ChequeData chequeData) {
        this.chequeData = chequeData;
        this.configRepo = new ChequeConfigRepository();
        this.printService = new ChequePrintService();
        this.config = configRepo.getConfig();
        if (this.config == null)
            this.config = new ChequeConfig();

        setTitle("Cheque Precision Preview");
        initUI();
    }

    private void initUI() {
        getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        getDialogPane().setPrefSize(950, 600);

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
                ImageView bgView = new ImageView(bg);
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
        HBox toolbar = new HBox(20);
        toolbar.setAlignment(Pos.CENTER);

        Button btnSave = new Button("💾 Save Alignment");
        btnSave.setStyle(LaxTheme.getButtonStyle(LaxTheme.ButtonType.SECONDARY) + "; -fx-padding: 8 20;");
        btnSave.setOnAction(e -> save());

        Button btnPrint = new Button("🖨️ Print Cheque");
        btnPrint.setStyle(LaxTheme.getButtonStyle(LaxTheme.ButtonType.PRIMARY) + "; -fx-padding: 8 20;");
        btnPrint.setOnAction(e -> print());

        toolbar.getChildren().addAll(btnSave, btnPrint);

        Label hint = new Label(
                "Drag labels to align perfectly with your cheque leaf. Positions are saved in millimeters.");
        hint.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-font-style: italic;");

        root.getChildren().addAll(canvasContainer, hint, toolbar);
        getDialogPane().setContent(root);
    }

    private void addElements() {
        double dateX = config.getDateX() > 0 ? config.getDateX() : 160;
        double dateY = config.getDateY() > 0 ? config.getDateY() : 12;

        String dStr = chequeData.date() != null ? chequeData.date().format(DateTimeFormatter.ofPattern("ddMMyyyy"))
                : "14022026";
        lblDate = createField(dStr, dateX, dateY, "Date");
        lblDate.setStyle(lblDate.getStyle() + "; -fx-letter-spacing: 6;");
        lblDate.setFont(Font.font("Courier New", FontWeight.BOLD, 22));

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

        chequePane.getChildren().addAll(lblDate, lblPayee, lblAmountWords, lblAmountDigits);

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
        l.setStyle(
                "-fx-text-fill: #1e1b4b; -fx-font-weight: bold; -fx-background-color: rgba(254, 240, 138, 0.5); -fx-padding: 2 4; -fx-border-color: #eab308; -fx-border-width: 1; -fx-border-radius: 3;");
        l.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        l.setTooltip(new Tooltip(hint));
        l.setCursor(Cursor.MOVE);
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
        });

        return l;
    }

    private void save() {
        config.setDateX(lblDate.getLayoutX() / MM_TO_PX);
        config.setDateY(lblDate.getLayoutY() / MM_TO_PX);
        config.setPayeeX(lblPayee.getLayoutX() / MM_TO_PX);
        config.setPayeeY(lblPayee.getLayoutY() / MM_TO_PX);
        config.setAmountWordsX(lblAmountWords.getLayoutX() / MM_TO_PX);
        config.setAmountWordsY(lblAmountWords.getLayoutY() / MM_TO_PX);
        config.setAmountDigitsX(lblAmountDigits.getLayoutX() / MM_TO_PX);
        config.setAmountDigitsY(lblAmountDigits.getLayoutY() / MM_TO_PX);

        configRepo.saveConfig(config);
        Alert a = new Alert(Alert.AlertType.INFORMATION, "Alignment parameters saved for future use.");
        a.setHeaderText("Layout Saved");
        a.show();
    }

    private void print() {
        try {
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
