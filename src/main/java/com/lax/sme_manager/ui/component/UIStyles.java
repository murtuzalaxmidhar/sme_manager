package com.lax.sme_manager.ui.component;

import com.lax.sme_manager.ui.theme.LaxTheme;

/**
 * POINT 2: UI Styling Constants & Style Generators
 *
 * Centralized styling for consistent, clean, professional UI
 * - No rounded rectangles on individual values (clean look)
 * - Proper text wrapping to prevent cropping
 * - Consistent spacing and alignment
 * - Theme-aware colors
 */
public class UIStyles {

    // ========== SCREEN STYLES ==========

    public static String getScreenBackgroundStyle() {
        return "-fx-background-color: " + LaxTheme.Colors.LIGHT_GRAY + ";";
    }

    public static String getCardStyle() {
        return "-fx-background-color: white; " +
                "-fx-border-width: 1; " +
                "-fx-border-color: " + LaxTheme.Colors.BORDER_GRAY + "; " +
                "-fx-border-radius: " + LaxTheme.BorderRadius.RADIUS_LG + "; " +
                // REMOVED: "-fx-padding: 0;" - this was overriding VBox padding!
                "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.05), 30, 0, 0, 10)"; // Deep premium shadow
    }

    public static String getSummaryBoxStyle() {
        return "-fx-background-color: " + LaxTheme.Colors.LIGHT_GRAY + "; " +
                "-fx-border-width: 1; " +
                "-fx-border-color: " + LaxTheme.Colors.BORDER_GRAY + "; " +
                "-fx-border-radius: 6";
    }

    public static String getCalcPanelStyle() {
        return "-fx-background-color: #f0f4f8; " +
                "-fx-border-width: 1; " +
                "-fx-border-color: #d0d9e8; " +
                "-fx-border-radius: 6";
    }

    // ========== TEXT STYLES ==========

    public static String getTitleStyle() {
        return "-fx-font-size: 24; " +
                "-fx-font-weight: bold; " +
                "-fx-text-fill: " + LaxTheme.Colors.DARK_NAVY + ";";
    }

    public static String getCalcTitleStyle() {
        return "-fx-font-size: 14; " +
                "-fx-font-weight: bold; " +
                "-fx-text-fill: #1f2937";
    }

    public static String getCalcSectionHeaderStyle() {
        return "-fx-font-size: 12; " +
                "-fx-font-weight: bold; " +
                "-fx-text-fill: #374151";
    }

    public static String getLargeAmountStyle() {
        return "-fx-font-size: 20; " +
                "-fx-font-weight: bold; " +
                "-fx-text-fill: " + LaxTheme.Colors.PRIMARY_TEAL + ";";
    }

    // ========== STATUS STYLES ==========

    public static String getStatusStyle() {
        return "-fx-font-size: 12; " +
                "-fx-padding: 8 12 8 12; " +
                "-fx-text-fill: #374151";
    }

    public static String getSuccessStatusStyle() {
        return getStatusStyle() +
                " -fx-background-color: #d1fae5; " +
                " -fx-text-fill: #065f46; " +
                " -fx-border-width: 1; " +
                " -fx-border-color: #6ee7b7";
    }

    public static String getErrorStatusStyle() {
        return getStatusStyle() +
                " -fx-background-color: #fee2e2; " +
                " -fx-text-fill: #7f1d1d; " +
                " -fx-border-width: 1; " +
                " -fx-border-color: #fca5a5";
    }

    public static String getWarningStatusStyle() {
        return getStatusStyle() +
                " -fx-background-color: #fef3c7; " +
                " -fx-text-fill: #78350f; " +
                " -fx-border-width: 1; " +
                " -fx-border-color: #fcd34d";
    }

    // ========== INPUT STYLES ==========

    public static String getInputFieldStyle() {
        return "-fx-font-size: 12; " +
                "-fx-padding: 10 14 10 14; " + // Increased padding
                "-fx-border-width: 1; " +
                "-fx-border-color: " + LaxTheme.Colors.BORDER_GRAY + "; " +
                "-fx-border-radius: 6; " +
                "-fx-background-color: #F8FAFC; " + // Muted background for idle state
                "-fx-text-fill: " + LaxTheme.Colors.TEXT_PRIMARY + ";";
    }

    public static String getInputFieldFocusedStyle() {
        return getInputFieldStyle() +
                " -fx-border-color: " + LaxTheme.Colors.PRIMARY_TEAL + "; " +
                " -fx-border-width: 1.5;";
    }

    // ========== LABEL STYLES ==========

    public static String getLabelStyle() {
        return "-fx-font-size: 11; " +
                "-fx-font-weight: bold; " +
                "-fx-text-fill: #4b5563";
    }

    public static String getSmallLabelStyle() {
        return "-fx-font-size: 10; " +
                "-fx-text-fill: #6b7280";
    }

    public static String getValueLabelStyle() {
        return "-fx-font-size: 12; " +
                "-fx-text-fill: #1f2937";
    }

    // ========== BUTTON STYLES ==========

    public static String getPrimaryButtonStyle() {
        return "-fx-font-size: 12; " +
                "-fx-padding: 8 20 8 20; " +
                "-fx-background-color: " + LaxTheme.Colors.PRIMARY_TEAL + "; " +
                "-fx-text-fill: white; " +
                "-fx-border-radius: 6; " +
                "-fx-cursor: hand; " +
                "-fx-font-weight: bold";
    }

    public static String getSecondaryButtonStyle() {
        return "-fx-font-size: 12; " +
                "-fx-padding: 8 16 8 16; " +
                "-fx-background-color: #e5e7eb; " +
                "-fx-text-fill: #1f2937; " +
                "-fx-border-radius: 4; " +
                "-fx-cursor: hand";
    }

    public static String getDangerButtonStyle() {
        return "-fx-font-size: 12; " +
                "-fx-padding: 8 16 8 16; " +
                "-fx-background-color: #dc2626; " +
                "-fx-text-fill: white; " +
                "-fx-border-radius: 4; " +
                "-fx-cursor: hand; " +
                "-fx-font-weight: bold";
    }

    // ========== SEPARATOR STYLES ==========

    public static String getSeparatorStyle() {
        return "-fx-padding: 0; " +
                "-fx-text-fill: #d1d5db";
    }

    // ========== TABLE STYLES ==========

    public static String getTableStyle() {
        return "-fx-font-size: 12; " +
                "-fx-background-color: white; " +
                "-fx-control-inner-background: white; " +
                "-fx-text-fill: #1f2937";
    }

    public static String getTableHeaderStyle() {
        return "-fx-font-size: 11; " +
                "-fx-font-weight: bold; " +
                "-fx-background-color: #f3f4f6; " +
                "-fx-text-fill: #374151";
    }

    // ========== FORM ROW STYLES ==========

    /**
     * Style for form row containers - no shadows, clean borders
     */
    public static String getFormRowStyle() {
        return "-fx-background-color: transparent; " +
                "-fx-padding: 16 0 16 0; " +
                "-fx-spacing: 24"; // Increased gutter
    }

    /**
     * Style for labeled form field containers
     */
    public static String getFormFieldContainerStyle() {
        return "-fx-background-color: transparent; " +
                "-fx-spacing: 4";
    }

    // ========== TEXT WRAPPING UTILITIES ==========

    /**
     * Wrappable label style - prevents text cropping
     */
    public static String getWrappableLabelStyle() {
        return "-fx-text-alignment: left; " +
                "-fx-wrap-text: true";
    }

    /**
     * Value display style - text wrapping, no background
     */
    public static String getValueDisplayStyle() {
        return getWrappableLabelStyle() +
                " -fx-background-color: transparent; " +
                " -fx-padding: 4 0 4 0";
    }

    // ========== CHECKBOX & RADIO STYLES ==========

    public static String getCheckBoxStyle() {
        return "-fx-font-size: 12; " +
                "-fx-text-fill: #1f2937";
    }

    // ========== COMBOBOX STYLES ==========

    public static String getComboBoxStyle() {
        return "-fx-font-size: 12; " +
                "-fx-background-color: white; " +
                "-fx-border-width: 1; " +
                "-fx-border-color: #d1d5db; " +
                "-fx-border-radius: 4; " +
                "-fx-text-fill: #1f2937; " +
                "-fx-padding: 8 12 8 12";
    }

    // ========== DATEPICKER STYLES ==========

    public static String getDatePickerStyle() {
        return "-fx-font-size: 12; " +
                "-fx-background-color: white; " +
                "-fx-border-width: 1; " +
                "-fx-border-color: #d1d5db; " +
                "-fx-border-radius: 4; " +
                "-fx-text-fill: #1f2937; " +
                "-fx-padding: 8 12 8 12";
    }

    // ========== TOOLTIP STYLES ==========

    public static String getTooltipStyle() {
        return "-fx-font-size: 11; " +
                "-fx-text-fill: white; " +
                "-fx-background-color: #1f2937; " +
                "-fx-background-radius: 4";
    }

    // ========== FLEX & SPACING UTILITIES ==========

    public static String getFlexRowStyle() {
        return "-fx-spacing: 16; " +
                "-fx-alignment: CENTER_LEFT; " +
                "-fx-background-color: transparent";
    }

    public static String getFlexColumnStyle() {
        return "-fx-spacing: 12; " +
                "-fx-alignment: TOP_LEFT; " +
                "-fx-background-color: transparent";
    }

    // ========== NO-DECORATION STYLES ==========

    /**
     * For individual value displays - no box shadow, no rounded corners
     * This prevents the "rounded rectangle around every value" issue (POINT 2)
     */
    public static String getCleanValueStyle() {
        return "-fx-background-color: transparent; " +
                "-fx-border-width: 0; " +
                "-fx-padding: 0; " +
                "-fx-text-fill: #1f2937; " +
                "-fx-font-size: 12";
    }

    /**
     * Section separator style - subtle divider
     */
    public static String getSectionSeparatorStyle() {
        return "-fx-text-fill: #e5e7eb; " +
                "-fx-padding: 8 0 8 0";
    }
}
