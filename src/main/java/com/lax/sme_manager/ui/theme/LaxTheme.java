package com.lax.sme_manager.ui.theme;

/**
 * LaxTheme - Centralized theme configuration
 * Single source of truth for all design tokens
 * Change one value here, it updates everywhere
 */
public class LaxTheme {

    // ============ COLOR PALETTE ============
    public static class Colors {
        // Primary Colors (Professional Teal)
        public static final String PRIMARY_TEAL = "#0D9488";
        public static final String PRIMARY_TEAL_DARK = "#0F766E";
        public static final String PRIMARY_TEAL_LIGHT = "#F0FDFA";

        // Neutral Colors
        public static final String DARK_NAVY = "#1F2937";
        public static final String LIGHT_GRAY = "#F8FAFC"; // Updated from #F9FAFB
        public static final String WHITE = "#FFFFFF";
        public static final String BORDER_GRAY = "#E5E7EB";

        // Text Colors
        public static final String TEXT_PRIMARY = "#1F2937";
        public static final String TEXT_SECONDARY = "#64748B"; // Updated from #6B7280
        public static final String TEXT_LIGHT = "#9CA3AF";

        // Semantic Colors
        public static final String SUCCESS = "#10B981"; // Updated from #059669
        public static final String WARNING = "#F59E0B"; // Updated from #D97706
        public static final String ERROR = "#EF4444"; // Updated from #DC2626
        public static final String INFO = "#3B82F6"; // Updated from #0284C7

        // Cheque Specific Colors
        public static final String CHEQUE_BLUE = "#F0F9FF"; // Pale Blue
        public static final String CHEQUE_CREAM = "#FEFCE8"; // Pale Cream (Standard CTS-2010)
        public static final String CHEQUE_WHITE = "#FAFAFA"; // Clean White
        public static final String CHEQUE_GREY = "#F1F5F9"; // Light Grey
        public static final String CHEQUE_YELLOW = "#FFF9E6"; // Pale Yellow
    }

    // ============ TYPOGRAPHY ============
    public static class Typography {
        // Font Family
        public static final String FONT_FAMILY = "-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif";

        // Font Sizes
        public static final int FONT_SIZE_XS = 11; // Labels, small text
        public static final int FONT_SIZE_SM = 12; // Default body, buttons
        public static final int FONT_SIZE_MD = 13; // Form labels, secondary text
        public static final int FONT_SIZE_LG = 16; // Section headers
        public static final int FONT_SIZE_XL = 18; // Page titles
        public static final int FONT_SIZE_2XL = 22; // Major titles
        public static final int FONT_SIZE_3XL = 28; // App title

        // Font Weights
        public static final int WEIGHT_NORMAL = 400; // Regular text
        public static final int WEIGHT_MEDIUM = 500; // Emphasized text
        public static final int WEIGHT_SEMIBOLD = 600; // Labels, headers
        public static final int WEIGHT_BOLD = 700; // Values, strong emphasis
    }

    // ============ SPACING ============
    public static class Spacing {
        public static final int SPACE_2 = 2;
        public static final int SPACE_4 = 4;
        public static final int SPACE_6 = 6;
        public static final int SPACE_8 = 8;
        public static final int SPACE_12 = 12;
        public static final int SPACE_16 = 16;
        public static final int SPACE_20 = 20;
        public static final int SPACE_24 = 24;
        public static final int SPACE_32 = 32;
        public static final int SPACE_40 = 40;
        public static final int SPACE_48 = 48;
    }

    // ============ LAYOUT HIERARCHY (Design Manifest) ============
    public static class Layout {
        public static final int MAIN_CONTAINER_PADDING = 25;
        public static final int SUB_PANEL_PADDING = 20;
        public static final int MIN_BORDER_DISTANCE = 15;
        public static final int LABEL_FIELD_GAP = 8;
    }

    // ============ SIDEBAR SPECIFIC ============
    public static class Sidebar {
        public static final int FONT_SIZE = 16;
        public static final int BUTTON_MIN_HEIGHT = 48;
        public static final int BUTTON_PADDING_V = 14; // Vertical padding for centering
        public static final int BUTTON_PADDING_H = 16; // Horizontal padding
    }

    // ============ BORDER RADIUS ============
    public static class BorderRadius {
        public static final int RADIUS_SM = 4;
        public static final int RADIUS_MD = 6;
        public static final int RADIUS_LG = 12; // Increased for premium cards
        public static final int RADIUS_XL = 16;
    }

    // ============ SHADOWS ============
    public static class Shadows {
        public static final String SHADOW_SM = "0 1px 2px rgba(0,0,0,0.05)";
        public static final String SHADOW_MD = "0 4px 12px rgba(0,0,0,0.08)";
        public static final String SHADOW_LG = "0 20px 60px rgba(0,0,0,0.1);"; // New premium deep shadow
    }

    // ============ COMPONENT SIZES ============
    public static class ComponentSizes {
        // Sidebar
        public static final int SIDEBAR_WIDTH = 220;
        public static final int SIDEBAR_WIDTH_COLLAPSED = 70;

        // Buttons
        public static final int BUTTON_HEIGHT_SM = 32;
        public static final int BUTTON_HEIGHT_MD = 40;
        public static final int BUTTON_HEIGHT_LG = 48;

        // Input fields
        public static final int INPUT_HEIGHT = 36;
        public static final int INPUT_PADDING_H = 12;
        public static final int INPUT_PADDING_V = 9;

        // Metric cards
        public static final int METRIC_CARD_MIN_WIDTH = 200;
        public static final int METRIC_CARD_PADDING = 18;
    }

    // ============ Z-INDEX HIERARCHY ============
    public static class ZIndex {
        public static final int BACKGROUND = 0;
        public static final int CONTENT = 10;
        public static final int OVERLAY = 100;
        public static final int MODAL = 1000;
        public static final int TOOLTIP = 10000;
    }

    // ============ ANIMATION ============
    public static class Animation {
        public static final int DURATION_FAST = 150; // ms
        public static final int DURATION_NORMAL = 250; // ms
        public static final int DURATION_SLOW = 350; // ms
        public static final String EASING = "cubic-bezier(0.16, 1, 0.3, 1)";
    }

    // ============ RESPONSIVE BREAKPOINTS ============
    public static class Breakpoints {
        public static final int MOBILE = 480;
        public static final int TABLET = 768;
        public static final int DESKTOP = 1024;
        public static final int WIDE = 1280;
    }

    // ============ HELPER METHODS ============

    /**
     * Get color with opacity
     * Example: getColorWithOpacity("#000000", 0.5) -> "rgba(0,0,0,0.5)"
     */
    public static String getColorWithOpacity(String hexColor, double opacity) {
        String hex = hexColor.replace("#", "");
        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);
        return String.format("rgba(%d,%d,%d,%.2f)", r, g, b, opacity);
    }

    /**
     * Get focus ring styling
     */
    public static String getFocusRing() {
        return String.format("0 0 0 3px %s", getColorWithOpacity(Colors.PRIMARY_TEAL, 0.1));
    }

    /**
     * Get button styling CSS
     */
    public static String getButtonStyle(ButtonType type) {
        switch (type) {
            case PRIMARY:
                return String.format(
                        "-fx-background-color: %s; -fx-text-fill: %s; -fx-font-weight: %d; -fx-cursor: hand;",
                        Colors.PRIMARY_TEAL, Colors.WHITE, Typography.WEIGHT_SEMIBOLD);
            case SECONDARY:
                return String.format(
                        "-fx-background-color: %s; -fx-text-fill: %s; -fx-font-weight: %d; -fx-cursor: hand;",
                        Colors.BORDER_GRAY, Colors.TEXT_PRIMARY, Typography.WEIGHT_SEMIBOLD);
            case DANGER:
                return String.format(
                        "-fx-background-color: %s; -fx-text-fill: %s; -fx-font-weight: %d; -fx-cursor: hand;",
                        Colors.ERROR, Colors.WHITE, Typography.WEIGHT_SEMIBOLD);
            default:
                return "";
        }
    }

    /**
     * Get input field styling CSS (static so it can be used in other static
     * helpers)
     */
    public static String getInputStyle() {
        return String.format(
                "-fx-background-radius: %d;" +
                        "-fx-border-radius: %d;" +
                        "-fx-background-color: %s;" +
                        "-fx-border-color: %s;" +
                        "-fx-padding: %d %d;" +
                        "-fx-font-size: %d;",
                BorderRadius.RADIUS_MD,
                BorderRadius.RADIUS_MD,
                Colors.WHITE,
                Colors.BORDER_GRAY,
                ComponentSizes.INPUT_PADDING_V,
                ComponentSizes.INPUT_PADDING_H,
                Typography.FONT_SIZE_SM);
    }

    /**
     * Get card styling CSS
     */
    public static String getCardStyle() {
        return String.format(
                "-fx-background-color: %s; " +
                        "-fx-border-radius: %d; " +
                        "-fx-padding: %d; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 3, 0, 0, 1);",
                Colors.WHITE,
                BorderRadius.RADIUS_LG,
                Spacing.SPACE_16);
    }

    /**
     * Get metric card styling CSS
     */
    public static String getMetricCardStyle() {
        return String.format(
                "-fx-background-color: %s; " +
                        "-fx-border-color: %s; " +
                        "-fx-border-width: 0 0 0 4; " +
                        "-fx-border-radius: %d; " +
                        "-fx-padding: %d; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.02), 2, 0, 0, 1);",
                Colors.WHITE,
                Colors.PRIMARY_TEAL,
                BorderRadius.RADIUS_MD,
                ComponentSizes.METRIC_CARD_PADDING);
    }

    /**
     * Button type enum
     */
    public enum ButtonType {
        PRIMARY,
        SECONDARY,
        DANGER
    }

    /**
     * Get complete CSS stylesheet as string
     * (Can be used for WebView or external CSS)
     */
    public static String getCompleteStylesheet() {
        return String.format(
                "* { -fx-font-family: '%s'; }\n" +
                        ".root { -fx-background-color: %s; -fx-text-fill: %s; }\n" +
                        ".button { -fx-padding: 8 16; -fx-font-size: %dpx; -fx-border-radius: %d; }\n" +
                        ".text-field { %s }\n" +
                        ".combo-box { %s }\n",
                Typography.FONT_FAMILY,
                Colors.LIGHT_GRAY,
                Colors.TEXT_PRIMARY,
                Typography.FONT_SIZE_SM,
                BorderRadius.RADIUS_MD,
                getInputStyle(),
                getInputStyle());
    }
}
