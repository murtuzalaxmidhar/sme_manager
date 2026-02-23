package com.lax.sme_manager.viewmodel;

import com.lax.sme_manager.repository.IVendorAnalyticsRepository;
import com.lax.sme_manager.util.AppLogger;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public class VendorAnalyticsViewModel {
    private static final Logger LOGGER = AppLogger.getLogger(VendorAnalyticsViewModel.class);
    private final IVendorAnalyticsRepository repository;
    private final int vendorId;

    // UI Properties
    public final StringProperty vendorName = new SimpleStringProperty("");
    public final StringProperty transCount = new SimpleStringProperty("0");
    public final StringProperty totalBags = new SimpleStringProperty("0");
    public final StringProperty avgRate = new SimpleStringProperty("₹0.00");
    public final StringProperty lastTransDate = new SimpleStringProperty("N/A");

    public final ObservableList<XYChart.Series<String, Number>> supplyTrendData = FXCollections.observableArrayList();
    public final ObservableList<XYChart.Series<String, Number>> priceHistoryData = FXCollections.observableArrayList();

    public final BooleanProperty isLoading = new SimpleBooleanProperty(false);

    public VendorAnalyticsViewModel(IVendorAnalyticsRepository repository, int vendorId, String name) {
        this.repository = repository;
        this.vendorId = vendorId;
        this.vendorName.set(name);
    }

    public void loadAnalytics() {
        isLoading.set(true);
        new Thread(() -> {
            try {
                // 1. Load Summary
                Map<String, Object> summary = repository.getVendorSummary(vendorId);

                // 2. Load Supply Trend
                Map<String, Integer> supplyTrend = repository.getMonthlySupplyTrend(vendorId);
                XYChart.Series<String, Number> supplySeries = new XYChart.Series<>();
                supplySeries.setName("Bags");
                // Records are DESC in repo for trend, let's reverse for chronological chart
                java.util.List<String> months = new java.util.ArrayList<>(supplyTrend.keySet());
                java.util.Collections.reverse(months);
                for (String month : months) {
                    supplySeries.getData().add(new XYChart.Data<>(month, supplyTrend.get(month)));
                }

                // 3. Load Price History
                Map<LocalDate, BigDecimal> priceHistory = repository.getPriceHistory(vendorId);
                XYChart.Series<String, Number> priceSeries = new XYChart.Series<>();
                priceSeries.setName("Rate per Bag");
                priceHistory.forEach((date, rate) -> {
                    priceSeries.getData().add(new XYChart.Data<>(date.toString(), rate));
                });

                Platform.runLater(() -> {
                    if (summary != null) {
                        transCount.set(String.valueOf(summary.getOrDefault("Transaction Count", 0)));
                        totalBags.set(String.valueOf(summary.getOrDefault("Total Bags", 0)));
                        BigDecimal avg = (BigDecimal) summary.get("Average Rate");
                        avgRate.set(avg != null ? String.format("₹%,.2f", avg) : "₹0.00");
                        LocalDate last = (LocalDate) summary.get("Last Transaction");
                        lastTransDate.set(last != null ? last.toString() : "N/A");
                    }

                    supplyTrendData.setAll(java.util.Collections.singletonList(supplySeries));
                    priceHistoryData.setAll(java.util.Collections.singletonList(priceSeries));
                    isLoading.set(false);
                    LOGGER.info("Vendor analytics loaded for vendor ID: {}", vendorId);
                });
            } catch (Exception e) {
                LOGGER.error("Error loading vendor analytics", e);
                Platform.runLater(() -> isLoading.set(false));
            }
        }).start();
    }
}
