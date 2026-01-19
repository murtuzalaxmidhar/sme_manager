package com.lax.sme_manager.util;

import javafx.scene.control.TextFormatter;

import java.util.function.UnaryOperator;

/**
 * Global numeric validation utility for amount and numeric fields.
 * Provides TextFormatters that reject non-numeric input in real-time.
 * 
 * Usage:
 * TextField amountField = new TextField();
 * amountField.setTextFormatter(NumericTextFormatter.decimalOnly(2));
 */
public class NumericTextFormatter {

    /**
     * Creates a TextFormatter that only allows decimal numbers.
     * 
     * @param maxDecimals Maximum decimal places allowed (e.g., 2 for currency)
     * @return TextFormatter for decimal input
     */
    public static TextFormatter<String> decimalOnly(int maxDecimals) {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();

            // Allow empty string
            if (newText.isEmpty()) {
                return change;
            }

            // Regex: optional digits, optional decimal point, max N decimal places
            String regex = "\\d*\\.?\\d{0," + maxDecimals + "}";

            if (newText.matches(regex)) {
                return change;
            }

            // Reject change that would result in invalid format
            return null;
        };

        return new TextFormatter<>(filter);
    }

    /**
     * Creates a TextFormatter that only allows integer numbers.
     * 
     * @return TextFormatter for integer input
     */
    public static TextFormatter<String> integerOnly() {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();

            // Allow empty string
            if (newText.isEmpty()) {
                return change;
            }

            // Only digits
            if (newText.matches("\\d*")) {
                return change;
            }

            return null;
        };

        return new TextFormatter<>(filter);
    }

    /**
     * Creates a TextFormatter for positive decimal numbers with optional sign.
     * Useful for fields that may need negative values.
     */
    public static TextFormatter<String> signedDecimal(int maxDecimals) {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();

            if (newText.isEmpty()) {
                return change;
            }

            // Allow optional minus at start, digits, optional decimal, max N decimals
            String regex = "-?\\d*\\.?\\d{0," + maxDecimals + "}";

            if (newText.matches(regex)) {
                return change;
            }

            return null;
        };

        return new TextFormatter<>(filter);
    }

    /**
     * Creates a TextFormatter specifically for percentage values (0-100)
     */
    public static TextFormatter<String> percentage() {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();

            if (newText.isEmpty()) {
                return change;
            }

            // Must be decimal
            if (newText.matches("\\d*\\.?\\d{0,2}")) {
                // Additional constraint: must be <= 100
                try {
                    double value = Double.parseDouble(newText);
                    if (value <= 100) {
                        return change;
                    }
                } catch (NumberFormatException e) {
                    // Partial input like "1." is valid during typing
                    return change;
                }
            }

            return null;
        };

        return new TextFormatter<>(filter);
    }
}
