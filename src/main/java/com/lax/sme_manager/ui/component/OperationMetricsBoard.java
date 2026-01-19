//package com.lax.sme_manager.ui.component;
//
//import com.lax.sme_manager.service.PurchaseService.DashboardMetrics;
//import com.lax.sme_manager.service.PurchaseService.VendorPurchaseBreakdown;
//import javafx.geometry.Insets;
//import javafx.geometry.Pos;
//import javafx.scene.control.Label;
//import javafx.scene.layout.HBox;
//import javafx.scene.layout.Priority;
//import javafx.scene.layout.VBox;
//import javafx.scene.text.Font;
//import javafx.scene.text.FontWeight;
//
///**
// * OperationMetricsBoard - Displays key performance indicators
// * Shows today's and monthly metrics at a glance
// */
//public class OperationMetricsBoard {
//    private VBox root;
//
//    // Metric display components
//    private Label todaysBagsLabel;
//    private Label todaysAmountLabel;
//    private Label monthlyBagsLabel;
//    private Label monthlyAmountLabel;
//
//    public OperationMetricsBoard() {
//        initializeUI();
//    }
//
//    private void initializeUI() {
//        root = new VBox();
//        root.setSpacing(15);
//        root.setPadding(new Insets(20));
//        root.setStyle("-fx-background-color: #f5f5f5;");
//
//        // Title
//        Label titleLabel = new Label("Operations Metrics Board");
//        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
//        titleLabel.setStyle("-fx-text-fill: #333;");
//
//        // Today's metrics row
//        HBox todaysBox = createMetricRow("Today's Statistics");
//        todaysBagsLabel = createMetricLabel("0 Bags today");
//        todaysAmountLabel = createMetricLabel("₹0 Today");
//        todaysBox.getChildren().addAll(todaysBagsLabel, todaysAmountLabel);
//
//        // Monthly metrics row
//        HBox monthlyBox = createMetricRow("This Month's Statistics");
//        monthlyBagsLabel = createMetricCard("Bags", "0", "#45B7D1");
//        monthlyAmountLabel = createMetricCard("Amount", "₹0.00", "#FFA07A");
//        monthlyBox.getChildren().addAll(monthlyBagsLabel, monthlyAmountLabel);
//
//        // Vendor breakdown section (placeholder for now)
//        VBox vendorBox = createVendorBreakdownSection();
//
//        root.getChildren().addAll(
//            titleLabel,
//            createSeparator(),
//            todaysBox,
//            monthlyBox,
//            vendorBox
//        );
//    }
//
//    /**
//     * Create metric row container
//     */
//    private HBox createMetricRow(String title) {
//        VBox titleBox = new VBox();
//        Label titleLabel = new Label(title);
//        titleLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
//        titleLabel.setStyle("-fx-text-fill: #666;");
//        titleBox.getChildren().add(titleLabel);
//
//        HBox row = new HBox();
//        row.setSpacing(20);
//        row.setPadding(new Insets(10, 0, 0, 0));
//
//        return row;
//    }
//
//    /**
//     * Create individual metric card
//     */
//    private VBox createMetricCard(String label, String value, String color) {
//        VBox card = new VBox();
//        card.setSpacing(10);
//        card.setPadding(new Insets(15));
//        card.setStyle("-fx-border-radius: 8; -fx-background-color: white; " +
//                     "-fx-border-color: " + color + "; -fx-border-width: 2;");
//
//        Label valueLabel = new Label(value);
//        valueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
//        valueLabel.setStyle("-fx-text-fill: " + color + ";");
//
//        Label nameLabel = new Label(label);
//        nameLabel.setFont(Font.font("Segoe UI", 11));
//        nameLabel.setStyle("-fx-text-fill: #999;");
//
//        card.getChildren().addAll(valueLabel, nameLabel);
//        card.setStyle(card.getStyle() + "-fx-min-width: 150;");
//
//        return card;
//    }
//
//    /**
//     * Create vendor breakdown section
//     */
//    private VBox createVendorBreakdownSection() {
//        VBox box = new VBox();
//        box.setSpacing(10);
//        box.setPadding(new Insets(20, 0, 0, 0));
//
//        Label sectionLabel = new Label("Vendor-wise Breakdown (This Month)");
//        sectionLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
//        sectionLabel.setStyle("-fx-text-fill: #666;");
//
//        Label placeholderLabel = new Label("Vendor metrics will appear here");
//        placeholderLabel.setStyle("-fx-text-fill: #ccc; -fx-padding: 20;");
//
//        box.getChildren().addAll(sectionLabel, placeholderLabel);
//
//        return box;
//    }
//
//    /**
//     * Create visual separator
//     */
//    private HBox createSeparator() {
//        HBox separator = new HBox();
//        separator.setStyle("-fx-border-color: #ddd; -fx-border-width: 0 0 1 0;");
//        separator.setPrefHeight(1);
//        return separator;
//    }
//
//    /**
//     * Update metrics display with fresh data
//     */
//    public void updateMetrics(DashboardMetrics metrics) {
//        if (metrics != null) {
//            // Update today's metrics
//            todaysBagsLabel.setText(metrics.getTodaysBags() + " Bags");
//            todaysAmountLabel.setText("₹" + String.format("%.2f", metrics.getTodaysAmount()));
//
//            // Update monthly metrics
//            monthlyBagsLabel.setText(metrics.getMonthlyBags() + " Bags");
//            monthlyAmountLabel.setText("₹" + String.format("%.2f", metrics.getMonthlyAmount()));
//        }
//    }
//
//    /**
//     * Get root node
//     */
//    public VBox getRoot() {
//        return root;
//    }
//}
