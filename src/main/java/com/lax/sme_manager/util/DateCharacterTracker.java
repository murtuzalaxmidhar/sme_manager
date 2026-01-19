package com.lax.sme_manager.util;

import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.geometry.Insets;

/**
 * Date Character Tracking Utility for Cheque Printing
 * 
 * Renders date as 8 individual characters with fixed spacing to align with
 * physical cheque boxes.
 * 
 * MATHEMATICS:
 * - Physical cheque box width: ~6-7mm per digit
 * - Box spacing: ~1-2mm gap between boxes
 * - Total spacing: 7mm center-to-center
 * - Conversion: 7mm × 3.78px/mm ≈ 26px spacing
 */
public class DateCharacterTracker {

    private static final double BOX_SPACING_MM = 7.0; // Standard cheque digit box spacing
    private static final double MM_TO_PX = 3.78; // 1mm = 3.78px @ 96 DPI
    private static final double BOX_SPACING_PX = BOX_SPACING_MM * MM_TO_PX; // ≈ 26.46px

    /**
     * Create an HBox with 8 individual date characters (DDMMYYYY)
     * Uses monospace font and fixed spacing for perfect alignment
     * 
     * @param dateStr   Date string in DDMMYYYY format
     * @param fontSize  Font size
     * @param fontColor Font color (hex)
     * @return HBox containing 8 Text nodes
     */
    public static HBox createDateBoxes(String dateStr, int fontSize, String fontColor) {
        if (dateStr == null || dateStr.length() != 8) {
            throw new IllegalArgumentException("Date must be exactly 8 characters (DDMMYYYY)");
        }

        HBox dateBox = new HBox();
        dateBox.setSpacing(0); // We control spacing manually

        Font monoFont = Font.font("Courier New", FontWeight.BOLD, fontSize);

        for (int i = 0; i < 8; i++) {
            Text digitText = new Text(String.valueOf(dateStr.charAt(i)));
            digitText.setFont(monoFont);
            digitText.setStyle("-fx-fill: " + fontColor + ";");

            // Add right margin for spacing (except last character)
            if (i < 7) {
                HBox.setMargin(digitText, new Insets(0, BOX_SPACING_PX, 0, 0));
            }

            dateBox.getChildren().add(digitText);
        }

        return dateBox;
    }

    /**
     * Create date boxes with custom spacing (for variable cheque formats)
     * 
     * @param dateStr   Date string in DDMMYYYY format
     * @param fontSize  Font size
     * @param fontColor Font color
     * @param spacingMm Spacing in millimeters
     * @return HBox with custom spacing
     */
    public static HBox createDateBoxesWithSpacing(String dateStr, int fontSize, String fontColor, double spacingMm) {
        if (dateStr == null || dateStr.length() != 8) {
            throw new IllegalArgumentException("Date must be exactly 8 characters (DDMMYYYY)");
        }

        HBox dateBox = new HBox();
        dateBox.setSpacing(0);

        Font monoFont = Font.font("Courier New", FontWeight.BOLD, fontSize);
        double spacingPx = spacingMm * MM_TO_PX;

        for (int i = 0; i < 8; i++) {
            Text digitText = new Text(String.valueOf(dateStr.charAt(i)));
            digitText.setFont(monoFont);
            digitText.setStyle("-fx-fill: " + fontColor + ";");

            if (i < 7) {
                HBox.setMargin(digitText, new Insets(0, spacingPx, 0, 0));
            }

            dateBox.getChildren().add(digitText);
        }

        return dateBox;
    }

    /**
     * Get the total width of the date box in pixels
     * Useful for positioning calculations
     */
    public static double getTotalWidthPx(int fontSize) {
        // Approximate: 8 characters × character width + 7 × spacing
        // For Courier New @ 14pt: ~8px per char
        double charWidth = fontSize * 0.6; // Approximation
        return (8 * charWidth) + (7 * BOX_SPACING_PX);
    }
}
