package com.lax.sme_manager.util;

import com.lax.sme_manager.ui.theme.LaxTheme;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility for drawing realistic cheque backgrounds per bank.
 */
public class ChequeTemplateUIUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChequeTemplateUIUtil.class);

    // =====================================================================
    // LAYOUT DATA
    // =====================================================================
    public static class ChequeLayout {
        // Paper color
        public String paperColor = LaxTheme.Colors.CHEQUE_CREAM;

        // NEW: Stores the image file name (e.g., "axis.png")
        public String backgroundImageName = null;

        // Date Boxes (8 boxes: DD MM YYYY)
        public double dateBoxX = 159.79;
        public double dateBoxY = 9.04;
        public double dateBoxSize = 5.0;

        // "Pay" Label
        public double payLabelX = 20;
        public double payLabelY = 22;

        // Pay Line (the dashed line after "Pay")
        public double payLineStartX = 30;
        public double payLineEndX = 150;
        public double payLineY = 28;

        // "Rupees" Label
        public double rupeesLabelX = 20;
        public double rupeesLabelY = 32;

        // Amount Words Line 1
        public double wordsLine1StartX = 38;
        public double wordsLine1EndX = 150;
        public double wordsLine1Y = 38;

        // Amount Words Line 2
        public double wordsLine2StartX = 20;
        public double wordsLine2EndX = 150;
        public double wordsLine2Y = 46;

        // Rupee Symbol Box (Figures)
        public double rupeeBoxX = 160;
        public double rupeeBoxY = 38;
        public double rupeeBoxW = 35;
        public double rupeeBoxH = 10;

        // MICR Band
        public double micrBandHeight = 19;
    }

    /**
     * Returns the layout for a specific bank.
     */
    public static ChequeLayout getBankLayout(String bankName) {
        ChequeLayout layout = new ChequeLayout();

        if (bankName == null)
            return layout;

        switch (bankName.toUpperCase()) {

            // ============================================================
            // 1. AXIS BANK (With Image Support)
            // ============================================================
            case "AXIS BANK":
                // >>> SET IMAGE NAME HERE <<<

                layout.paperColor = LaxTheme.Colors.CHEQUE_CREAM;
                layout.dateBoxX = 160.2;
                layout.dateBoxY = 8.5; // Axis dates are usually high
                layout.payLabelX = 26;
                layout.payLabelY = 21;
                layout.payLineStartX = 36;
                layout.payLineEndX = 158;
                layout.payLineY = 26;
                layout.rupeesLabelX = 20;
                layout.rupeesLabelY = 32;
                layout.wordsLine1StartX = 36;
                layout.wordsLine1EndX = 148;
                layout.wordsLine1Y = 37;
                layout.wordsLine2StartX = 20;
                layout.wordsLine2EndX = 148;
                layout.wordsLine2Y = 45;
                layout.rupeeBoxX = 162;
                layout.rupeeBoxY = 36;
                break;

            // ============================================================
            // 2. CANARA BANK (Standard Baseline)
            // ============================================================
            case "CANARA BANK":
                layout.paperColor = LaxTheme.Colors.CHEQUE_BLUE;
                layout.dateBoxX = 159.79;
                layout.dateBoxY = 9.04;
                layout.payLabelX = 28;
                layout.payLabelY = 22;
                layout.payLineStartX = 37;
                layout.payLineEndX = 157;
                layout.payLineY = 27;
                layout.rupeesLabelX = 24;
                layout.rupeesLabelY = 30;
                layout.wordsLine1StartX = 37;
                layout.wordsLine1EndX = 147;
                layout.wordsLine1Y = 38;
                layout.wordsLine2StartX = 37;
                layout.wordsLine2EndX = 147;
                layout.wordsLine2Y = 46;
                layout.rupeeBoxX = 160;
                layout.rupeeBoxY = 37;
                break;

            // ============================================================
            // 3. STATE BANK OF INDIA (SBI)
            // ============================================================
            case "STATE BANK OF INDIA":
            case "SBI":
                layout.paperColor = LaxTheme.Colors.CHEQUE_WHITE;
                layout.dateBoxX = 161.5;
                layout.dateBoxY = 9.0;
                layout.payLabelX = 28;
                layout.payLabelY = 22;
                layout.payLineStartX = 42;
                layout.payLineEndX = 155;
                layout.payLineY = 26;
                layout.rupeesLabelX = 22;
                layout.rupeesLabelY = 32;
                layout.wordsLine1StartX = 42;
                layout.wordsLine1EndX = 145;
                layout.wordsLine1Y = 37;
                layout.wordsLine2StartX = 22;
                layout.wordsLine2EndX = 145;
                layout.wordsLine2Y = 45;
                layout.rupeeBoxX = 160;
                layout.rupeeBoxY = 36.5;
                break;

            // ============================================================
            // 4. HDFC BANK
            // ============================================================
            case "HDFC BANK":
            case "HDFC":
                layout.backgroundImageName = "HDFC.png";

                layout.paperColor = LaxTheme.Colors.CHEQUE_GREY;
                layout.dateBoxX = 160.5;
                layout.dateBoxY = 9.5;
                layout.payLabelX = 26;
                layout.payLabelY = 21;
                layout.payLineStartX = 38;
                layout.payLineEndX = 158;
                layout.payLineY = 27;
                layout.rupeesLabelX = 20;
                layout.rupeesLabelY = 32;
                layout.wordsLine1StartX = 38;
                layout.wordsLine1EndX = 148;
                layout.wordsLine1Y = 38;
                layout.wordsLine2StartX = 20;
                layout.wordsLine2EndX = 148;
                layout.wordsLine2Y = 46;
                layout.rupeeBoxX = 162;
                layout.rupeeBoxY = 37;
                break;

            // ============================================================
            // 5. ICICI BANK
            // ============================================================
            case "ICICI BANK":
                layout.paperColor = LaxTheme.Colors.CHEQUE_GREY;
                layout.dateBoxX = 160.0;
                layout.dateBoxY = 9.2;
                layout.payLabelX = 26;
                layout.payLabelY = 21;
                layout.payLineStartX = 36;
                layout.payLineEndX = 158;
                layout.payLineY = 27.5;
                layout.rupeesLabelX = 20;
                layout.rupeesLabelY = 32;
                layout.wordsLine1StartX = 36;
                layout.wordsLine1EndX = 148;
                layout.wordsLine1Y = 38.5;
                layout.wordsLine2StartX = 20;
                layout.wordsLine2EndX = 148;
                layout.wordsLine2Y = 46.5;
                layout.rupeeBoxX = 161;
                layout.rupeeBoxY = 37;
                break;

            // ============================================================
            // 6. PUNJAB NATIONAL BANK (PNB)
            // ============================================================
            case "PUNJAB NATIONAL BANK":
            case "PNB":
                layout.paperColor = LaxTheme.Colors.CHEQUE_YELLOW;
                layout.dateBoxX = 159.8;
                layout.dateBoxY = 9.0;
                layout.payLabelX = 28;
                layout.payLabelY = 22;
                layout.payLineStartX = 38;
                layout.payLineEndX = 155;
                layout.payLineY = 27;
                layout.rupeesLabelX = 24;
                layout.rupeesLabelY = 30;
                layout.wordsLine1StartX = 38;
                layout.wordsLine1EndX = 150;
                layout.wordsLine1Y = 38;
                layout.wordsLine2StartX = 38;
                layout.wordsLine2EndX = 150;
                layout.wordsLine2Y = 46;
                layout.rupeeBoxX = 160;
                layout.rupeeBoxY = 38;
                break;

            // ============================================================
            // 7. BANK OF BARODA
            // ============================================================
            case "BANK OF BARODA":
            case "BOB":
                layout.paperColor = LaxTheme.Colors.CHEQUE_CREAM;
                layout.dateBoxX = 159.5;
                layout.dateBoxY = 9.5;
                layout.payLabelX = 28;
                layout.payLabelY = 22;
                layout.payLineStartX = 38;
                layout.payLineEndX = 150;
                layout.payLineY = 28;
                layout.rupeesLabelX = 22;
                layout.rupeesLabelY = 32;
                layout.wordsLine1StartX = 38;
                layout.wordsLine1EndX = 150;
                layout.wordsLine1Y = 38;
                layout.wordsLine2StartX = 22;
                layout.wordsLine2EndX = 150;
                layout.wordsLine2Y = 46;
                layout.rupeeBoxX = 159;
                layout.rupeeBoxY = 38;
                break;

            // ============================================================
            // 8. KOTAK MAHINDRA BANK
            // ============================================================
            case "KOTAK MAHINDRA BANK":
            case "KOTAK":
                layout.paperColor = LaxTheme.Colors.CHEQUE_BLUE;
                layout.dateBoxX = 160.5;
                layout.dateBoxY = 9.0;
                layout.payLabelX = 26;
                layout.payLabelY = 21;
                layout.payLineStartX = 36;
                layout.payLineEndX = 158;
                layout.payLineY = 27;
                layout.rupeesLabelX = 20;
                layout.rupeesLabelY = 32;
                layout.wordsLine1StartX = 36;
                layout.wordsLine1EndX = 148;
                layout.wordsLine1Y = 38;
                layout.wordsLine2StartX = 20;
                layout.wordsLine2EndX = 148;
                layout.wordsLine2Y = 46;
                layout.rupeeBoxX = 162;
                layout.rupeeBoxY = 37;
                break;

            // ============================================================
            // 9. UNION BANK OF INDIA
            // ============================================================
            case "UNION BANK OF INDIA":
            case "UNION":
                layout.paperColor = LaxTheme.Colors.CHEQUE_WHITE;
                layout.dateBoxX = 159.8;
                layout.dateBoxY = 9.0;
                layout.payLabelX = 28;
                layout.payLabelY = 22;
                layout.payLineStartX = 37;
                layout.payLineEndX = 157;
                layout.payLineY = 27;
                layout.rupeesLabelX = 24;
                layout.rupeesLabelY = 30;
                layout.wordsLine1StartX = 37;
                layout.wordsLine1EndX = 147;
                layout.wordsLine1Y = 38;
                layout.wordsLine2StartX = 37;
                layout.wordsLine2EndX = 147;
                layout.wordsLine2Y = 46;
                layout.rupeeBoxX = 160;
                layout.rupeeBoxY = 37;
                break;

            // ============================================================
            // DEFAULT
            // ============================================================
            default:
                layout.paperColor = LaxTheme.Colors.CHEQUE_WHITE;
                layout.dateBoxX = 159.79;
                layout.dateBoxY = 9.04;
                layout.payLabelX = 28;
                layout.payLabelY = 22;
                layout.payLineStartX = 37;
                layout.payLineEndX = 157;
                layout.payLineY = 27;
                layout.rupeesLabelX = 24;
                layout.rupeesLabelY = 30;
                layout.wordsLine1StartX = 37;
                layout.wordsLine1EndX = 147;
                layout.wordsLine1Y = 38;
                layout.wordsLine2StartX = 37;
                layout.wordsLine2EndX = 147;
                layout.wordsLine2Y = 46;
                layout.rupeeBoxX = 160;
                layout.rupeeBoxY = 37;
                break;
        }

        return layout;
    }

    // =====================================================================
    // DRAWING LOGIC (With Image Loader)
    // =====================================================================
    public static void drawBankSpecificBackground(Pane pane, String bankName, double mmToPx, double widthPx,
            double heightPx) {

        pane.getChildren().clear();
        ChequeLayout L = getBankLayout(bankName);
        boolean imageLoaded = false;

        ImageView finalImgView = null;

        // ---------------------------------------------------------
        // 1. ATTEMPT TO LOAD IMAGE
        // ---------------------------------------------------------
        if (L.backgroundImageName != null && !L.backgroundImageName.isEmpty()) {
            try {
                String imagePath = "/images/" + L.backgroundImageName;
                LOGGER.info("Attempting to load bank image: {} for bank: {}", imagePath, bankName);
                InputStream is = ChequeTemplateUIUtil.class.getResourceAsStream(imagePath);

                if (is != null) {
                    Image fxImage = new Image(is);
                    if (!fxImage.isError()) {
                        finalImgView = new ImageView(fxImage);
                        finalImgView.setFitWidth(widthPx);
                        finalImgView.setFitHeight(heightPx);
                        finalImgView.setPreserveRatio(false);
                        imageLoaded = true;
                        LOGGER.info("Successfully loaded image for bank: {}", bankName);
                    } else {
                        LOGGER.warn("Image loaded but has error for bank: {}", bankName);
                    }
                } else {
                    LOGGER.warn("Image NOT found at classpath: {}", imagePath);
                }
            } catch (Exception e) {
                LOGGER.error("Error loading image for bank: {}", bankName, e);
            }
        }

        // ---------------------------------------------------------
        // 2. FALLBACK (Standard Drawn Background)
        // ---------------------------------------------------------
        if (!imageLoaded) {
            pane.setStyle("-fx-background-color: " + L.paperColor + "; " +
                    "-fx-border-color: #cbd5e1; -fx-border-width: 1; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 10, 0, 0, 4); " +
                    "-fx-background-radius: 4; -fx-border-radius: 4;");

            Rectangle waterMark = new Rectangle(widthPx, heightPx);
            waterMark.setFill(Color.web("#000000", 0.02));
            pane.getChildren().add(waterMark);
        }

        // ---------------------------------------------------------
        // 3. DRAW GUIDES
        // (RED if image loaded for easy alignment, GREY otherwise)
        // ---------------------------------------------------------
        Color guideColor = imageLoaded ? Color.RED : Color.web("#94a3b8");
        double strokeWidth = imageLoaded ? 1.0 : 0.5;

        // Date Boxes
        double boxSize = L.dateBoxSize * mmToPx;
        HBox dateBoxes = new HBox(2);
        dateBoxes.setLayoutX(L.dateBoxX * mmToPx);
        dateBoxes.setLayoutY(L.dateBoxY * mmToPx);

        for (int i = 0; i < 8; i++) {
            Rectangle rect = new Rectangle(boxSize, boxSize);
            rect.setFill(Color.TRANSPARENT);
            rect.setStroke(guideColor);
            rect.setStrokeWidth(strokeWidth);
            dateBoxes.getChildren().add(rect);
        }

        // Pay Label
        Label payLabel = new Label("Pay");
        payLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: "
                + (imageLoaded ? "red" : "#475569") + "; -fx-font-family: 'Serif';");
        payLabel.setLayoutX(L.payLabelX * mmToPx);
        payLabel.setLayoutY(L.payLabelY * mmToPx);

        // Pay Line
        Line payLine = new Line(L.payLineStartX * mmToPx, L.payLineY * mmToPx, L.payLineEndX * mmToPx,
                L.payLineY * mmToPx);
        payLine.setStroke(guideColor);
        payLine.getStrokeDashArray().addAll(2d, 2d);

        // Rupees Label
        Label amountLabel = new Label("Rupees");
        amountLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: "
                + (imageLoaded ? "red" : "#475569") + "; -fx-font-family: 'Serif';");
        amountLabel.setLayoutX(L.rupeesLabelX * mmToPx);
        amountLabel.setLayoutY(L.rupeesLabelY * mmToPx);

        // Words Lines
        Line wordsLine1 = new Line(L.wordsLine1StartX * mmToPx, L.wordsLine1Y * mmToPx, L.wordsLine1EndX * mmToPx,
                L.wordsLine1Y * mmToPx);
        wordsLine1.setStroke(guideColor);
        wordsLine1.getStrokeDashArray().addAll(2d, 2d);

        Line wordsLine2 = new Line(L.wordsLine2StartX * mmToPx, L.wordsLine2Y * mmToPx, L.wordsLine2EndX * mmToPx,
                L.wordsLine2Y * mmToPx);
        wordsLine2.setStroke(guideColor);
        wordsLine2.getStrokeDashArray().addAll(2d, 2d);

        // MICR Band
        Rectangle micrBand = new Rectangle(widthPx, L.micrBandHeight * mmToPx);
        micrBand.setLayoutY(heightPx - (L.micrBandHeight * mmToPx));
        micrBand.setFill(Color.web("#ffffff", 0.4)); // Semi-transparent
        micrBand.setStroke(Color.web("#e2e8f0"));

        // Rupee Box
        Label rupeeSymbol = new Label(" ₹ ");
        rupeeSymbol.setStyle(
                "-fx-border-color: " + (imageLoaded ? "red" : "#94a3b8") +
                        "; -fx-border-width: 1; -fx-padding: 4; -fx-font-size: 16px; -fx-font-weight: bold;" +
                        (imageLoaded ? "-fx-text-fill: red;" : "-fx-background-color: white;"));
        rupeeSymbol.setLayoutX(L.rupeeBoxX * mmToPx);
        rupeeSymbol.setLayoutY(L.rupeeBoxY * mmToPx);
        rupeeSymbol.setPrefSize(L.rupeeBoxW * mmToPx, L.rupeeBoxH * mmToPx);
        rupeeSymbol.setAlignment(Pos.CENTER_LEFT);

        pane.getChildren().addAll(dateBoxes, payLabel, payLine, amountLabel, wordsLine1, wordsLine2, micrBand,
                rupeeSymbol);

        // ---------------------------------------------------------
        // 4. ADD IMAGE ON TOP
        // If an image was loaded, add it LAST so it sits on top of
        // the drawn guides, hiding them for a cleaner look.
        // ---------------------------------------------------------
        if (imageLoaded && finalImgView != null) {
            pane.getChildren().add(finalImgView);
        }
    }
}