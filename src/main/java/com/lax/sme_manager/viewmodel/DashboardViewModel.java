package com.lax.sme_manager.viewmodel;

import com.lax.sme_manager.service.MetricsService;
import com.lax.sme_manager.util.AppLogger;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import org.slf4j.Logger;

public class DashboardViewModel {
    private static final Logger LOGGER = AppLogger.getLogger(DashboardViewModel.class);
    private final MetricsService metricsService;

    // UI Properties
    public final StringProperty bagsToday = new SimpleStringProperty("0");
    public final StringProperty bagsThisWeek = new SimpleStringProperty("0");
    public final StringProperty bagsThisMonth = new SimpleStringProperty("0");
    public final StringProperty amountThisMonth = new SimpleStringProperty("₹0.00");
    public final IntegerProperty unpaidChequesCount = new SimpleIntegerProperty(0);
    public final StringProperty avgRateThisMonth = new SimpleStringProperty("₹0.00");
    public final StringProperty clearingTodayCount = new SimpleStringProperty("0");
    public final StringProperty pendingClearingCount = new SimpleStringProperty("0");

    // Chart Collections
    public final ObservableList<XYChart.Series<String, Number>> weeklyTrendData = FXCollections.observableArrayList();
    public final ObservableList<PieChart.Data> paymentModeData = FXCollections.observableArrayList();
    public final ObservableList<String> topVendorsData = FXCollections.observableArrayList();

    public final BooleanProperty isLoading = new SimpleBooleanProperty(false);
    public final StringProperty statusMessage = new SimpleStringProperty("");

    public DashboardViewModel(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    public void loadMetrics() {
        isLoading.set(true);
        statusMessage.set("Loading metrics...");

        metricsService.getDashboardMetricsAsync()
                .thenAccept(metrics -> Platform.runLater(() -> {
                    bagsToday.set(String.valueOf(metrics.getBagsToday()));
                    bagsThisWeek.set(String.valueOf(metrics.getBagsThisWeek()));
                    bagsThisMonth.set(String.valueOf(metrics.getBagsThisMonth()));

                    amountThisMonth.set(String.format("₹%,.2f", metrics.getAmountThisMonth()));

                    clearingTodayCount.set(String.valueOf(metrics.getChequesClearingToday()));
                    pendingClearingCount.set(String.valueOf(metrics.getPendingClearingTotal()));
                    avgRateThisMonth.set(String.format("₹%,.2f", metrics.getAvgRateThisMonth()));

                    // Update Trends Chart
                    XYChart.Series<String, Number> series = new XYChart.Series<>();
                    series.setName("Bags");
                    metrics.getWeeklyBagsTrend().forEach((date, count) -> {
                        series.getData().add(new XYChart.Data<>(
                                date.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM")), count));
                    });
                    weeklyTrendData.setAll(java.util.Collections.singletonList(series));

                    // Update Distribution Chart
                    paymentModeData.clear();
                    metrics.getPaymentDistribution().forEach((mode, count) -> {
                        paymentModeData.add(new PieChart.Data(mode, count));
                    });

                    // Update Top Vendors
                    topVendorsData.clear();
                    metrics.getTopVendors().forEach((name, count) -> {
                        topVendorsData.add(name + " (" + count + " bags)");
                    });

                    isLoading.set(false);
                    statusMessage.set("Dashboard updated");
                    LOGGER.info("Dashboard metrics loaded successfully");
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        isLoading.set(false);
                        statusMessage.set("Error loading metrics");
                        LOGGER.error("Failed to load dashboard metrics", ex);
                    });
                    return null;
                });
    }
}
