package com.lax.sme_manager.ui.view;

import com.lax.sme_manager.domain.Vendor;
import com.lax.sme_manager.repository.VendorRepository;
import com.lax.sme_manager.repository.model.PurchaseEntity;
import com.lax.sme_manager.service.ExportService;
import com.lax.sme_manager.ui.component.UIStyles;
import com.lax.sme_manager.ui.theme.LaxTheme;
import com.lax.sme_manager.util.i18n.AppLabel;
import com.lax.sme_manager.util.NumericTextFormatter;
import com.lax.sme_manager.util.VendorCache;
import com.lax.sme_manager.viewmodel.PurchaseHistoryViewModel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public class PurchaseHistoryView extends VBox {
    private final PurchaseHistoryViewModel viewModel;
    private final VendorRepository vendorRepository;
    private final ExportService exportService;
    private final VendorCache vendorCache;
    private Consumer<PurchaseEntity> onPurchaseSelected;
    private Consumer<PurchaseEntity> onPurchaseEdit;

    public PurchaseHistoryView(PurchaseHistoryViewModel viewModel, VendorRepository vendorRepository,
            VendorCache vendorCache) {
        this.viewModel = viewModel;
        this.vendorRepository = vendorRepository;
        this.vendorCache = vendorCache;
        this.exportService = new ExportService(vendorRepository);
        initializeUI();
        viewModel.loadPurchases(); // Initial load
    }

    public void setOnPurchaseSelected(Consumer<PurchaseEntity> listener) {
        this.onPurchaseSelected = listener;
    }

    public void setOnPurchaseEdit(Consumer<PurchaseEntity> listener) {
        this.onPurchaseEdit = listener;
    }

    private void initializeUI() {
        setPadding(new Insets(LaxTheme.Spacing.SPACE_40)); // Premium Outer Padding
        setSpacing(LaxTheme.Spacing.SPACE_32); // Increased spacing between sections
        setStyle("-fx-background-color: transparent; -fx-background: " + LaxTheme.Colors.LIGHT_GRAY + ";");

        // Header Row with Title and Export Button
        HBox headerRow = new HBox();
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label(AppLabel.TITLE_PURCHASE_HISTORY.get());
        title.setStyle(UIStyles.getTitleStyle());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button exportBtn = new Button(AppLabel.ACTION_EXPORT.get());
        exportBtn.setStyle(LaxTheme.getButtonStyle(LaxTheme.ButtonType.SECONDARY));
        exportBtn.setGraphic(new Label("\uD83D\uDCE4")); // Export icon
        exportBtn.setOnAction(e -> handleExport());

        headerRow.getChildren().addAll(title, spacer, exportBtn);

        // Filters
        VBox filterPanel = createFilterPanel();

        // Table
        TableView<PurchaseEntity> table = createTable();
        VBox.setVgrow(table, Priority.ALWAYS);
        table.setPrefHeight(600); // Encourage more height

        // Unified Footer (Summary + Pagination + Status)
        HBox footer = createUnifiedFooter();

        getChildren().addAll(headerRow, filterPanel, table, footer);
    }

    private HBox createUnifiedFooter() {
        HBox footer = new HBox(24);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(12, 24, 12, 24));
        footer.setStyle(UIStyles.getCardStyle() + "; -fx-background-color: #f8fafc;");

        // Summary Labels
        Label lblBags = new Label("Total Bags: 0");
        lblBags.setStyle("-fx-font-weight: bold;");
        Label lblAmount = new Label("Total Amount: \u20B90.00");
        lblAmount.setStyle("-fx-font-weight: bold; -fx-text-fill: " + LaxTheme.Colors.PRIMARY_TEAL + ";");

        viewModel.purchaseList.addListener((javafx.collections.ListChangeListener<PurchaseEntity>) c -> {
            int totalBags = viewModel.purchaseList.stream().mapToInt(PurchaseEntity::getBags).sum();
            double totalAmount = viewModel.purchaseList.stream()
                    .mapToDouble(p -> p.getGrandTotal() != null ? p.getGrandTotal().doubleValue() : 0.0).sum();
            lblBags.setText("Total Bags: " + totalBags);
            lblAmount.setText("Total Amount: \u20B9" + String.format("%,.2f", totalAmount));
        });

        // Pagination Controls
        HBox pagination = new HBox(12);
        pagination.setAlignment(Pos.CENTER);
        Button prev = new Button("\u2190"); // left arrow
        prev.setStyle(LaxTheme.getButtonStyle(LaxTheme.ButtonType.SECONDARY) + "; -fx-padding: 4 10;");
        prev.setOnAction(e -> viewModel.prevPage());

        Label pageLabel = new Label();
        pageLabel.textProperty().bind(viewModel.paginationLabel);
        pageLabel.setStyle("-fx-font-weight: 600;");

        Button next = new Button("\u2192"); // right arrow
        next.setStyle(LaxTheme.getButtonStyle(LaxTheme.ButtonType.SECONDARY) + "; -fx-padding: 4 10;");
        next.setOnAction(e -> viewModel.nextPage());
        pagination.getChildren().addAll(prev, pageLabel, next);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Status
        Label status = new Label();
        status.setStyle("-fx-text-fill: #94a3b8; -fx-font-style: italic; -fx-font-size: 13;");
        status.textProperty().bind(viewModel.statusMessage);

        footer.getChildren().addAll(lblBags, lblAmount, spacer, pagination,
                new Separator(javafx.geometry.Orientation.VERTICAL), status);
        return footer;
    }

    private VBox createFilterPanel() {
        VBox panel = new VBox(LaxTheme.Spacing.SPACE_24); // Increased vertical spacing
        panel.setStyle(UIStyles.getCardStyle());
        panel.setPadding(new Insets(32)); // Increased interior padding

        // Row 1: Date Range & Presets
        HBox row1 = new HBox(LaxTheme.Spacing.SPACE_12);
        row1.setAlignment(Pos.CENTER_LEFT);

        DatePicker startPicker = new DatePicker();
        startPicker.setPrefWidth(140);
        startPicker.valueProperty().bindBidirectional(viewModel.getFilterState().filterStartDate);

        DatePicker endPicker = new DatePicker();
        endPicker.setPrefWidth(140);
        endPicker.valueProperty().bindBidirectional(viewModel.getFilterState().filterEndDate);

        Button btnToday = new Button("Today");
        btnToday.setOnAction(e -> viewModel.getFilterState().applyPresetToday());
        btnToday.setStyle(LaxTheme.getButtonStyle(LaxTheme.ButtonType.SECONDARY));

        Button btnWeek = new Button("Last 7 Days");
        btnWeek.setOnAction(e -> viewModel.getFilterState().applyPresetLast7Days());
        btnWeek.setStyle(LaxTheme.getButtonStyle(LaxTheme.ButtonType.SECONDARY));

        Button btnMonth = new Button("Last Month");
        btnMonth.setOnAction(e -> viewModel.getFilterState().applyPresetLastMonth());
        btnMonth.setStyle(LaxTheme.getButtonStyle(LaxTheme.ButtonType.SECONDARY));

        row1.getChildren().addAll(
                new Label(AppLabel.LBL_PURCHASE_DATE.get() + ":"), startPicker,
                new Label("to"), endPicker,
                btnToday, btnWeek, btnMonth);

        // Row 2: Vendor & Amount Range
        HBox row2 = new HBox(LaxTheme.Spacing.SPACE_24);
        row2.setAlignment(Pos.CENTER_LEFT);

        // Vendor Sub-group
        HBox vendorGroup = new HBox(12);
        vendorGroup.setAlignment(Pos.CENTER_LEFT);
        ComboBox<Vendor> vendorCombo = new ComboBox<>();
        vendorCombo.setPrefWidth(200);
        vendorCombo.setPromptText("All Vendors");
        vendorCombo.getItems().add(new Vendor(-1, "All Vendors"));
        vendorCombo.getItems().addAll(vendorRepository.findAllVendors());
        vendorCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Vendor v) {
                return v == null ? "" : v.getName();
            }

            @Override
            public Vendor fromString(String s) {
                return null;
            }
        });
        vendorCombo.getSelectionModel().selectFirst();
        vendorCombo.valueProperty().addListener((o, old, v) -> {
            if (v != null)
                viewModel.getFilterState().filterVendorId.set(v.getId());
        });
        vendorGroup.getChildren().addAll(new Label(AppLabel.LBL_VENDOR.get() + ":"), vendorCombo);

        // Amount Sub-group
        HBox amountGroup = new HBox(12);
        amountGroup.setAlignment(Pos.CENTER_LEFT);
        TextField minAmount = new TextField();
        minAmount.setTextFormatter(NumericTextFormatter.decimalOnly(2)); // MANDATORY: Financial precision
        minAmount.setPromptText("Min");
        minAmount.setPrefWidth(100);
        minAmount.textProperty().addListener((obs, o, n) -> {
            if (n == null || n.isEmpty())
                viewModel.getFilterState().filterMinAmount.set(BigDecimal.ZERO);
            else
                try {
                    viewModel.getFilterState().filterMinAmount.set(new BigDecimal(n));
                } catch (NumberFormatException ignored) {
                }
        });

        TextField maxAmount = new TextField();
        maxAmount.setTextFormatter(NumericTextFormatter.decimalOnly(2)); // MANDATORY: Financial precision
        maxAmount.setPromptText("Max");
        maxAmount.setPrefWidth(100);
        maxAmount.textProperty().addListener((obs, o, n) -> {
            if (n == null || n.isEmpty())
                viewModel.getFilterState().filterMaxAmount.set(null);
            else
                try {
                    viewModel.getFilterState().filterMaxAmount.set(new BigDecimal(n));
                } catch (NumberFormatException ignored) {
                }
        });
        amountGroup.getChildren().addAll(new Label(AppLabel.LBL_AMOUNT.get() + ":"), minAmount, new Label("-"),
                maxAmount);

        row2.getChildren().addAll(vendorGroup, amountGroup);

        // Row 3: Action Buttons (Centralized)
        HBox row3 = new HBox(20); // Better gap
        row3.setAlignment(Pos.CENTER_RIGHT); // Align to right
        row3.setPadding(new Insets(12, 0, 0, 0));

        Button applyBtn = new Button(AppLabel.ACTION_APPLY.get());
        applyBtn.setPrefWidth(120);
        applyBtn.setStyle(LaxTheme.getButtonStyle(LaxTheme.ButtonType.PRIMARY));
        applyBtn.setOnAction(e -> viewModel.applyFilters());

        Button resetBtn = new Button(AppLabel.ACTION_RESET.get());
        resetBtn.setPrefWidth(120);
        resetBtn.setStyle(LaxTheme.getButtonStyle(LaxTheme.ButtonType.SECONDARY));
        resetBtn.setOnAction(e -> {
            vendorCombo.getSelectionModel().selectFirst();
            minAmount.clear();
            maxAmount.clear();
            viewModel.resetFilters();
        });

        row3.getChildren().addAll(applyBtn, resetBtn);

        panel.getChildren().addAll(row1, row2, row3);
        return panel;
    }

    private TableView<PurchaseEntity> createTable() {
        TableView<PurchaseEntity> table = new TableView<>();
        table.setItems(viewModel.purchaseList);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setStyle("-fx-background-color: white; -fx-border-color: " + LaxTheme.Colors.BORDER_GRAY
                + "; -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 1;");

        // Purchase Date Column (Center Aligned)
        TableColumn<PurchaseEntity, String> dateCol = new TableColumn<>(AppLabel.LBL_PURCHASE_DATE.get());
        dateCol.setCellValueFactory(data -> {
            PurchaseEntity p = data.getValue();
            String dateStr = p.getEntryDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            String timeStr = p.getCreatedAt() != null
                    ? " " + p.getCreatedAt().format(DateTimeFormatter.ofPattern("HH:mm"))
                    : "";
            return new javafx.beans.property.SimpleStringProperty(dateStr + timeStr);
        });
        dateCol.setStyle("-fx-alignment: CENTER;");
        dateCol.setPrefWidth(130);

        // Vendor Column (Left Aligned)
        TableColumn<PurchaseEntity, String> vendorCol = new TableColumn<>(AppLabel.LBL_VENDOR.get());
        vendorCol.setCellValueFactory(data -> {
            int vid = data.getValue().getVendorId();
            Vendor v = vendorRepository.findAllVendors().stream().filter(ve -> ve.getId() == vid).findFirst()
                    .orElse(null);
            return new javafx.beans.property.SimpleStringProperty(v != null ? v.getName() : "?");
        });
        vendorCol.setStyle("-fx-alignment: CENTER-LEFT;");
        vendorCol.setPrefWidth(150);

        // Bags Column (Right Aligned)
        TableColumn<PurchaseEntity, Number> bagsCol = new TableColumn<>(AppLabel.LBL_BAGS.get());
        bagsCol.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getBags()));
        bagsCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        bagsCol.setPrefWidth(70);

        // Rate Column (Right Aligned)
        TableColumn<PurchaseEntity, String> rateCol = new TableColumn<>(AppLabel.LBL_RATE.get());
        rateCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                String.format("%.2f", data.getValue().getRate())));
        rateCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        rateCol.setPrefWidth(80);

        // Amount Column (Right Aligned)
        TableColumn<PurchaseEntity, String> amountCol = new TableColumn<>(AppLabel.LBL_AMOUNT.get());
        amountCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                String.format("\u20B9%.2f", data.getValue().getGrandTotal())));
        amountCol.setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold;");
        amountCol.setPrefWidth(120);

        // Weight Column (Right Aligned)
        TableColumn<PurchaseEntity, String> weightCol = new TableColumn<>("Weight (kg)");
        weightCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getWeightKg() != null ? String.format("%.2f", data.getValue().getWeightKg()) : "0.00"));
        weightCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        weightCol.setPrefWidth(90);

        // Cheque No Column (Left Aligned)
        TableColumn<PurchaseEntity, String> chequeCol = new TableColumn<>("Cheque No");
        chequeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getChequeNumber() != null ? data.getValue().getChequeNumber() : "-"));
        chequeCol.setStyle("-fx-alignment: CENTER-LEFT;");
        chequeCol.setPrefWidth(100);

        // Cheque Date Column (Center Aligned)
        TableColumn<PurchaseEntity, String> chqDateCol = new TableColumn<>(AppLabel.LBL_CHEQUE_DATE.get());
        chqDateCol.setCellValueFactory(data -> {
            LocalDate d = data.getValue().getChequeDate();
            return new javafx.beans.property.SimpleStringProperty(
                    d != null ? d.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "-");
        });
        chqDateCol.setStyle("-fx-alignment: CENTER;");
        chqDateCol.setPrefWidth(110);

        // Status Column (Left Aligned with Color)
        TableColumn<PurchaseEntity, String> statusCol = new TableColumn<>(AppLabel.LBL_STATUS.get());
        statusCol.setCellValueFactory(
                data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus()));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("PAID".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold; -fx-alignment: CENTER-LEFT;"); // Green
                    } else if ("UNPAID".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-alignment: CENTER-LEFT;"); // Red
                    } else {
                        setStyle("-fx-text-fill: " + LaxTheme.Colors.TEXT_PRIMARY + "; -fx-alignment: CENTER-LEFT;");
                    }
                }
            }
        });
        statusCol.setPrefWidth(100);

        // Action Column (Center Aligned)
        TableColumn<PurchaseEntity, Void> actionCol = new TableColumn<>("Action");
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = createIconButton("\uD83D\uDC41\uFE0F", AppLabel.ACTION_VIEW_DETAILS.get(),
                    LaxTheme.Colors.PRIMARY_TEAL);
            private final Button editBtn = createIconButton("\u270F\uFE0F", AppLabel.ACTION_EDIT.get(), "#f59e0b");
            private final Button deleteBtn = createIconButton("\uD83D\uDDD1\uFE0F", AppLabel.ACTION_DELETE.get(),
                    "#ef4444");

            {
                viewBtn.setOnAction(e -> {
                    PurchaseEntity p = getTableView().getItems().get(getIndex());
                    if (onPurchaseSelected != null)
                        onPurchaseSelected.accept(p);
                });
                editBtn.setOnAction(e -> {
                    PurchaseEntity p = getTableView().getItems().get(getIndex());
                    handleEdit(p);
                });
                deleteBtn.setOnAction(e -> {
                    PurchaseEntity p = getTableView().getItems().get(getIndex());
                    handleDelete(p);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(8, viewBtn, editBtn, deleteBtn);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
            }
        });
        actionCol.setStyle("-fx-alignment: CENTER;");
        actionCol.setPrefWidth(120);

        table.getColumns().addAll(dateCol, vendorCol, bagsCol, rateCol, weightCol, amountCol, chequeCol, chqDateCol,
                statusCol, actionCol);
        return table;
    }

    private HBox createSummaryRow() {
        HBox box = new HBox(32);
        box.setPadding(new Insets(16, 24, 16, 24));
        box.setAlignment(Pos.CENTER_RIGHT);
        box.setStyle(UIStyles.getCardStyle() + "; -fx-background-color: #f8fafc;");

        Label lblBags = new Label();
        lblBags.setStyle("-fx-font-weight: bold;");
        Label lblWeight = new Label();
        lblWeight.setStyle("-fx-font-weight: bold;");
        Label lblAmount = new Label();
        lblAmount.setStyle("-fx-font-weight: bold; -fx-text-fill: " + LaxTheme.Colors.PRIMARY_TEAL + ";");

        viewModel.purchaseList.addListener((javafx.collections.ListChangeListener<PurchaseEntity>) c -> {
            int totalBags = viewModel.purchaseList.stream().mapToInt(PurchaseEntity::getBags).sum();
            double totalWeight = viewModel.purchaseList.stream()
                    .mapToDouble(p -> p.getWeightKg() != null ? p.getWeightKg().doubleValue() : 0.0).sum();
            double totalAmount = viewModel.purchaseList.stream()
                    .mapToDouble(p -> p.getGrandTotal() != null ? p.getGrandTotal().doubleValue() : 0.0).sum();

            lblBags.setText("Total Bags: " + totalBags);
            lblWeight.setText("Total Weight: " + String.format("%.2f kg", totalWeight));
            lblAmount.setText("Total Amount: ₹" + String.format("%,.2f", totalAmount));
        });

        box.getChildren().addAll(lblBags, lblWeight, lblAmount);
        return box;
    }

    private void handleExport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Purchase History");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName("purchase_history_" + java.time.LocalDate.now() + ".csv");

        File file = fileChooser.showSaveDialog(getScene().getWindow());
        if (file != null) {
            try {
                exportService.exportToCsv(viewModel.purchaseList, file);
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Export successful!");
                alert.show();
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Export failed: " + e.getMessage());
                alert.show();
            }
        }
    }

    private HBox createPagination() {
        HBox box = new HBox(LaxTheme.Spacing.SPACE_12);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(10));
        box.setStyle(UIStyles.getCardStyle());

        Button prev = new Button("Previous");
        prev.setOnAction(e -> viewModel.prevPage());

        Label pageLabel = new Label();
        pageLabel.textProperty().bind(viewModel.paginationLabel);

        Button next = new Button("Next");
        next.setOnAction(e -> viewModel.nextPage());

        box.getChildren().addAll(prev, pageLabel, next);
        return box;
    }

    private Button createIconButton(String icon, String tooltip, String color) {
        Button btn = new Button(icon);
        btn.setTooltip(new Tooltip(tooltip));
        btn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: " + color
                        + "; -fx-font-size: 16px; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: " + color
                + "; -fx-font-size: 16px; -fx-cursor: hand; -fx-background-radius: 4;"));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: " + color
                        + "; -fx-font-size: 16px; -fx-cursor: hand;"));
        return btn;
    }

    private void handleEdit(PurchaseEntity p) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Edit Confirmation");
        alert.setHeaderText("Do you want to edit this entry?");
        alert.setContentText("This will open the record in a single-column edit form.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (onPurchaseEdit != null)
                    onPurchaseEdit.accept(p);
            }
        });
    }

    private void handleDelete(PurchaseEntity p) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Confirmation");
        alert.setHeaderText("Are you sure you want to delete this purchase entry?");
        alert.setContentText("Vendor: " + getVendorName(p.getVendorId()) + "\nAmount: \u20B9" + p.getGrandTotal());

        // Set Warning style (best effort without image)
        alert.getDialogPane().setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                viewModel.deletePurchase(p);
            }
        });
    }

    private String getVendorName(int id) {
        return vendorRepository.findAllVendors().stream()
                .filter(v -> v.getId() == id)
                .map(Vendor::getName)
                .findFirst()
                .orElse("Unknown Vendor");
    }
}
