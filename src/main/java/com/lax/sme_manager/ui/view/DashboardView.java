package com.lax.sme_manager.ui.view;

import com.lax.sme_manager.service.MetricsService;
import com.lax.sme_manager.ui.component.UIStyles;
import com.lax.sme_manager.ui.theme.LaxTheme;
import com.lax.sme_manager.util.i18n.AppLabel;
import com.lax.sme_manager.viewmodel.DashboardViewModel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.application.Platform;

public class DashboardView extends ScrollPane implements RefreshableView {

    private final DashboardViewModel viewModel;

    public DashboardView(MetricsService metricsService) {
        this.viewModel = new DashboardViewModel(metricsService);
        initializeUI();
        viewModel.loadMetrics();
    }

    private void initializeUI() {
        setFitToWidth(true);
        setStyle("-fx-background-color: transparent; -fx-background: " + LaxTheme.Colors.LIGHT_GRAY + ";");

        VBox content = new VBox(LaxTheme.Spacing.SPACE_32);
        content.setPadding(new Insets(LaxTheme.Layout.MAIN_CONTAINER_PADDING)); // Design Manifest: 25px

        // Title
        Label title = new Label(AppLabel.TITLE_DASHBOARD.get());
        title.setStyle(UIStyles.getTitleStyle());

        // Sections
        VBox purchaseSection = createSection("📦 Purchase Overview");
        GridPane purchaseGrid = createPaddingGrid();
        purchaseGrid.add(createMetricCard(AppLabel.METRIC_BAGS_TODAY.get(), viewModel.bagsToday, "BLUE"), 0, 0);
        purchaseGrid.add(createMetricCard(AppLabel.METRIC_BAGS_WEEK.get(), viewModel.bagsThisWeek, "BLUE"), 1, 0);
        purchaseGrid.add(createMetricCard(AppLabel.METRIC_BAGS_MONTH.get(), viewModel.bagsThisMonth, "BLUE"), 2, 0);
        purchaseSection.getChildren().add(purchaseGrid);

        VBox financialSection = createSection("💰 Financial Status");
        GridPane financialGrid = createPaddingGrid();
        financialGrid.add(createMetricCard("Total (Month)", viewModel.amountThisMonth, "BLUE"), 0, 0);
        financialGrid.add(createMetricCard("Avg Rate/Bag", viewModel.avgRateThisMonth, "BLUE"), 1, 0);
        financialSection.getChildren().add(financialGrid);

        VBox criticalSection = createSection("⚠️ Critical Handlers");
        GridPane criticalGrid = createPaddingGrid();
        criticalGrid.add(createMetricCard(AppLabel.METRIC_UNPAID_CHEQUES.get(),
                viewModel.unpaidChequesCount.asString(), "RED"), 0, 0);
        criticalGrid.add(createMetricCard("Cheques Clearing Today", viewModel.clearingTodayCount, "ORANGE"), 1, 0);
        criticalSection.getChildren().add(criticalGrid);

        content.getChildren().addAll(title, purchaseSection, financialSection, criticalSection);
        setContent(content);
    }

    private VBox createSection(String title) {
        VBox section = new VBox(LaxTheme.Spacing.SPACE_16);
        Label lbl = new Label(title.toUpperCase());
        lbl.setStyle("-fx-font-size: 13; -fx-font-weight: 700; -fx-text-fill: " + LaxTheme.Colors.TEXT_SECONDARY
                + "; -fx-letter-spacing: 1px;");
        section.getChildren().add(lbl);
        return section;
    }

    private GridPane createPaddingGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(LaxTheme.Spacing.SPACE_32); // Increased gutter
        grid.setVgap(LaxTheme.Spacing.SPACE_32);
        return grid;
    }

    private VBox createMetricCard(String title, javafx.beans.value.ObservableStringValue valueProperty,
            String accentColor) {
        VBox card = new VBox(LaxTheme.Spacing.SPACE_12);
        card.setPadding(new Insets(24));
        card.setPrefWidth(280);
        card.setPrefHeight(140);
        card.setAlignment(Pos.CENTER_LEFT);

        String accentHex;
        String bgHex;
        switch (accentColor) {
            case "RED":
                accentHex = LaxTheme.Colors.ERROR;
                bgHex = "#FEF2F2";
                break;
            case "ORANGE":
                accentHex = LaxTheme.Colors.WARNING;
                bgHex = "#FFFBEB";
                break;
            case "GREEN":
                accentHex = LaxTheme.Colors.SUCCESS;
                bgHex = "#F0FDF4";
                break;
            default:
                accentHex = LaxTheme.Colors.PRIMARY_TEAL;
                bgHex = "#FFFFFF";
                break;
        }

        card.setStyle(String.format("-fx-background-color: %s; " +
                "-fx-background-radius: " + LaxTheme.BorderRadius.RADIUS_LG + "; " +
                "-fx-border-color: %s; -fx-border-width: 0 0 0 6; -fx-border-radius: 2; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 40, 0, 0, 10);",
                bgHex, accentHex));

        Label titleLabel = new Label(title);
        titleLabel.setStyle(
                "-fx-text-fill: " + LaxTheme.Colors.TEXT_SECONDARY + "; -fx-font-size: 13; -fx-font-weight: 500;");

        Label valueLabel = new Label();
        valueLabel.textProperty().bind(valueProperty);
        valueLabel.setStyle("-fx-text-fill: " + LaxTheme.Colors.TEXT_PRIMARY
                + "; -fx-font-size: 32; -fx-font-weight: 700; -fx-letter-spacing: -1px;");

        card.getChildren().addAll(titleLabel, valueLabel);

        // Add indicator for updates
        viewModel.isLoading.addListener((obs, old, loading) -> {
            Platform.runLater(() -> card.setOpacity(loading ? 0.7 : 1.0));
        });

        return card;
    }

    @Override
    public void refresh() {
        if (viewModel != null) {
            viewModel.loadMetrics();
        }
    }
}
