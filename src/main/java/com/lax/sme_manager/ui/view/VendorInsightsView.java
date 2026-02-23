package com.lax.sme_manager.ui.view;

import com.lax.sme_manager.repository.VendorAnalyticsRepository;
import com.lax.sme_manager.viewmodel.VendorAnalyticsViewModel;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class VendorInsightsView extends Stage {

    private final VendorAnalyticsViewModel viewModel;

    public VendorInsightsView(int vendorId, String vendorName) {
        this.viewModel = new VendorAnalyticsViewModel(new VendorAnalyticsRepository(), vendorId, vendorName);

        initModality(Modality.APPLICATION_MODAL);
        setTitle("Vendor Insights: " + vendorName);

        initializeUI();
        viewModel.loadAnalytics();
    }

    private void initializeUI() {
        VBox root = new VBox(25);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #f8fafc;");

        // Header
        Label header = new Label("Analytics for " + viewModel.vendorName.get());
        header.setStyle("-fx-font-size: 24px; -fx-font-weight: 800; -fx-text-fill: #0F172A;");

        // Summary Cards Row
        HBox summaryRow = new HBox(20);
        summaryRow.getChildren().addAll(
                createMiniCard("Total Transactions", viewModel.transCount),
                createMiniCard("Total Volume (Bags)", viewModel.totalBags),
                createMiniCard("Average Rate", viewModel.avgRate),
                createMiniCard("Last Transaction", viewModel.lastTransDate));

        // Charts Row
        HBox chartsRow = new HBox(25);
        VBox.setVgrow(chartsRow, Priority.ALWAYS);

        VBox supplyBox = createChartCard("📦 Monthly Supply Trend", createSupplyChart());
        HBox.setHgrow(supplyBox, Priority.ALWAYS);

        VBox priceBox = createChartCard("📈 Price History (Rate/Bag)", createPriceChart());
        HBox.setHgrow(priceBox, Priority.ALWAYS);

        chartsRow.getChildren().addAll(supplyBox, priceBox);

        root.getChildren().addAll(header, summaryRow, chartsRow);

        Scene scene = new Scene(root, 1100, 750);
        setScene(scene);
    }

    private VBox createMiniCard(String title, javafx.beans.value.ObservableStringValue value) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(15, 20, 15, 20));
        card.setPrefWidth(250);
        card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #E2E8F0; -fx-border-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.03), 10, 0, 0, 4);");

        Label titleLbl = new Label(title.toUpperCase());
        titleLbl.setStyle("-fx-font-size: 11px; -fx-font-weight: 700; -fx-text-fill: #64748B;");

        Label valLbl = new Label();
        valLbl.textProperty().bind(value);
        valLbl.setStyle("-fx-font-size: 20px; -fx-font-weight: 800; -fx-text-fill: #0F172A;");

        card.getChildren().addAll(titleLbl, valLbl);
        return card;
    }

    private VBox createChartCard(String title, Chart chart) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #E2E8F0; -fx-border-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 15, 0, 0, 6);");

        Label lbl = new Label(title.toUpperCase());
        lbl.setStyle("-fx-font-size: 13px; -fx-font-weight: 800; -fx-text-fill: #475569;");

        card.getChildren().addAll(lbl, chart);
        return card;
    }

    private Chart createSupplyChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Month");
        yAxis.setLabel("Bags");

        BarChart<String, Number> bc = new BarChart<>(xAxis, yAxis);
        bc.setData(viewModel.supplyTrendData);
        bc.setAnimated(true);
        bc.setLegendVisible(false);
        bc.setPrefHeight(400);
        return bc;
    }

    private Chart createPriceChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Date");
        yAxis.setLabel("Rate (₹)");

        yAxis.setForceZeroInRange(false); // Focus on fluctuation

        LineChart<String, Number> lc = new LineChart<>(xAxis, yAxis);
        lc.setData(viewModel.priceHistoryData);
        lc.setAnimated(true);
        lc.setLegendVisible(false);
        lc.setCreateSymbols(true);
        lc.setPrefHeight(400);
        return lc;
    }
}
