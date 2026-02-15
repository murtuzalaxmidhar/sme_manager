package com.lax.sme_manager.ui.view;

import com.lax.sme_manager.domain.Vendor;
import com.lax.sme_manager.repository.VendorRepository;
import com.lax.sme_manager.repository.model.PurchaseEntity;
import com.lax.sme_manager.service.ExportService;
import com.lax.sme_manager.ui.component.UIStyles;
import com.lax.sme_manager.ui.theme.LaxTheme;
import com.lax.sme_manager.util.i18n.AppLabel;
import com.lax.sme_manager.util.NumericTextFormatter;
import com.lax.sme_manager.viewmodel.PurchaseHistoryViewModel;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;

public class PurchaseHistoryView extends VBox implements RefreshableView {
    private final PurchaseHistoryViewModel viewModel;
    private final VendorRepository vendorRepository;
    private final ExportService exportService;
    private Consumer<PurchaseEntity> onPurchaseSelected;
    private Consumer<PurchaseEntity> onPurchaseEdit;
    private TableView<PurchaseEntity> purchaseTable;

    public PurchaseHistoryView(PurchaseHistoryViewModel viewModel, VendorRepository vendorRepository) {
        this.viewModel = viewModel;
        this.vendorRepository = vendorRepository;
        this.exportService = new ExportService(vendorRepository);
        initializeUI();
        viewModel.loadPurchases();
    }

    public void setOnPurchaseSelected(Consumer<PurchaseEntity> listener) {
        this.onPurchaseSelected = listener;
    }

    public void setOnPurchaseEdit(Consumer<PurchaseEntity> listener) {
        this.onPurchaseEdit = listener;
    }

    private void initializeUI() {
        setPadding(new Insets(12));
        setSpacing(LaxTheme.Spacing.SPACE_12);
        setStyle("-fx-background-color: transparent; -fx-background: " + LaxTheme.Colors.LIGHT_GRAY + ";");

        // Header
        HBox headerRow = new HBox(15);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label(AppLabel.TITLE_PURCHASE_HISTORY.get());
        title.setStyle(UIStyles.getTitleStyle());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnBatchPrint = new Button("🖨️ Batch Print");
        btnBatchPrint.setStyle(LaxTheme.getButtonStyle(LaxTheme.ButtonType.PRIMARY));
        btnBatchPrint.setOnAction(e -> handleBatchPrint());

        Button btnDeleteSelected = new Button("Delete Selected");
        btnDeleteSelected.setStyle(LaxTheme.getButtonStyle(LaxTheme.ButtonType.DANGER));
        btnDeleteSelected.setOnAction(e -> handleBulkDelete());

        var selectionEmpty = javafx.beans.binding.Bindings.isEmpty(viewModel.selectedPurchases);
        btnBatchPrint.visibleProperty().bind(selectionEmpty.not());
        btnBatchPrint.managedProperty().bind(btnBatchPrint.visibleProperty());
        btnDeleteSelected.visibleProperty().bind(selectionEmpty.not());
        btnDeleteSelected.managedProperty().bind(btnDeleteSelected.visibleProperty());

        Button exportBtn = new Button("Export to Excel");
        exportBtn.setStyle(LaxTheme.getButtonStyle(LaxTheme.ButtonType.SECONDARY));
        exportBtn.setOnAction(e -> handleExport());

        headerRow.getChildren().addAll(title, spacer, btnBatchPrint, btnDeleteSelected, exportBtn);

        // Filters
        VBox filterPanel = createFilterPanel();

        // Table
        purchaseTable = createTable();
        VBox.setVgrow(purchaseTable, Priority.ALWAYS);
        purchaseTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Footer
        HBox footer = createUnifiedFooter();

        getChildren().addAll(headerRow, filterPanel, purchaseTable, footer);
    }

    private HBox createUnifiedFooter() {
        HBox footer = new HBox(24);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(8, 16, 8, 16));
        footer.setStyle(UIStyles.getCardStyle() + "; -fx-background-color: #f8fafc;");

        Label lblBags = new Label("Total Bags: 0");
        lblBags.setStyle("-fx-font-weight: bold;");
        Label lblAmount = new Label("Total Amount: ₹0.00");
        lblAmount.setStyle("-fx-font-weight: bold; -fx-text-fill: " + LaxTheme.Colors.PRIMARY_TEAL + ";");

        viewModel.purchaseList.addListener((javafx.collections.ListChangeListener<PurchaseEntity>) c -> {
            int totalBags = viewModel.purchaseList.stream().mapToInt(PurchaseEntity::getBags).sum();
            double totalAmount = viewModel.purchaseList.stream()
                    .mapToDouble(p -> p.getGrandTotal() != null ? p.getGrandTotal().doubleValue() : 0.0).sum();
            lblBags.setText("Total Bags: " + totalBags);
            lblAmount.setText("Total Amount: ₹" + String.format("%,.2f", totalAmount));
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox pagination = new HBox(12);
        pagination.setAlignment(Pos.CENTER);
        Button prev = new Button("←");
        prev.setStyle(LaxTheme.getButtonStyle(LaxTheme.ButtonType.SECONDARY));
        prev.setOnAction(e -> viewModel.prevPage());

        Label pageLabel = new Label();
        pageLabel.textProperty().bind(viewModel.paginationLabel);

        Button next = new Button("→");
        next.setStyle(LaxTheme.getButtonStyle(LaxTheme.ButtonType.SECONDARY));
        next.setOnAction(e -> viewModel.nextPage());
        pagination.getChildren().addAll(prev, pageLabel, next);

        footer.getChildren().addAll(lblBags, lblAmount, spacer, pagination);
        return footer;
    }

    private VBox createFilterPanel() {
        VBox panel = new VBox(12);
        panel.setStyle(UIStyles.getCardStyle());
        panel.setPadding(new Insets(16));

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(12);
        grid.setAlignment(Pos.CENTER_LEFT);

        // Column 1: Date Range & Presets
        VBox dateCol = new VBox(8);
        HBox dateBox = new HBox(8, new Label("Date:"),
                new DatePicker() {
                    {
                        setPrefWidth(125);
                        valueProperty().bindBidirectional(viewModel.getFilterState().filterStartDate);
                    }
                },
                new Label("-"),
                new DatePicker() {
                    {
                        setPrefWidth(125);
                        valueProperty().bindBidirectional(viewModel.getFilterState().filterEndDate);
                    }
                });
        dateBox.setAlignment(Pos.CENTER_LEFT);

        HBox presetBox = new HBox(8);
        Button btnToday = new Button("Today");
        btnToday.setStyle(LaxTheme.getButtonStyle(LaxTheme.ButtonType.SECONDARY) + "; -fx-font-size: 11px;");
        btnToday.setOnAction(e -> viewModel.getFilterState().applyPresetToday());

        Button btnMonth = new Button("This Month");
        btnMonth.setStyle(LaxTheme.getButtonStyle(LaxTheme.ButtonType.SECONDARY) + "; -fx-font-size: 11px;");
        btnMonth.setOnAction(e -> viewModel.getFilterState().applyPresetLastMonth());
        presetBox.getChildren().addAll(btnToday, btnMonth);

        dateCol.getChildren().addAll(dateBox, presetBox);
        grid.add(dateCol, 0, 0);

        // Column 2: Vendor
        VBox vendorCol = new VBox(8);
        vendorCol.getChildren().add(new Label("Vendor:"));
        org.controlsfx.control.CheckComboBox<Vendor> vendorCheckCombo = new org.controlsfx.control.CheckComboBox<>();
        vendorCheckCombo.setPrefWidth(200);
        vendorCheckCombo.getItems().addAll(vendorRepository.findAllVendors());
        vendorCheckCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Vendor v) {
                return v == null ? "" : v.getName();
            }

            @Override
            public Vendor fromString(String s) {
                return null;
            }
        });
        vendorCheckCombo.getCheckModel().getCheckedItems()
                .addListener((javafx.collections.ListChangeListener<Vendor>) c -> {
                    viewModel.getFilterState().filterVendorIds.setAll(
                            vendorCheckCombo.getCheckModel().getCheckedItems().stream().map(Vendor::getId).toList());
                });
        vendorCol.getChildren().add(vendorCheckCombo);
        grid.add(vendorCol, 1, 0);

        // Column 3: Amount Range
        VBox amountCol = new VBox(8);
        amountCol.getChildren().add(new Label("Amount Range:"));
        HBox amountBox = new HBox(8);
        TextField minAmount = new TextField();
        minAmount.setPromptText("Min");
        minAmount.setPrefWidth(80);
        minAmount.setTextFormatter(NumericTextFormatter.decimalOnly(2));
        minAmount.textProperty().addListener((o, old, n) -> {
            viewModel.getFilterState().filterMinAmount.set(n.isEmpty() ? BigDecimal.ZERO : new BigDecimal(n));
        });

        TextField maxAmount = new TextField();
        maxAmount.setPromptText("Max");
        maxAmount.setPrefWidth(80);
        maxAmount.setTextFormatter(NumericTextFormatter.decimalOnly(2));
        maxAmount.textProperty().addListener((o, old, n) -> {
            viewModel.getFilterState().filterMaxAmount.set(n.isEmpty() ? null : new BigDecimal(n));
        });
        amountBox.getChildren().addAll(minAmount, new Label("-"), maxAmount);
        amountBox.setAlignment(Pos.CENTER_LEFT);
        amountCol.getChildren().add(amountBox);
        grid.add(amountCol, 2, 0);

        // Column 4: Buttons
        HBox btnBox = new HBox(12);
        btnBox.setAlignment(Pos.BOTTOM_RIGHT);
        Button apply = new Button("Apply");
        apply.setStyle(LaxTheme.getButtonStyle(LaxTheme.ButtonType.PRIMARY));
        apply.setOnAction(e -> viewModel.applyFilters());

        Button reset = new Button("Reset");
        reset.setStyle(LaxTheme.getButtonStyle(LaxTheme.ButtonType.SECONDARY));
        reset.setOnAction(e -> {
            vendorCheckCombo.getCheckModel().clearChecks();
            minAmount.clear();
            maxAmount.clear();
            viewModel.resetFilters();
        });
        btnBox.getChildren().addAll(apply, reset);
        grid.add(btnBox, 3, 0);

        panel.getChildren().add(grid);
        return panel;
    }

    private TableView<PurchaseEntity> createTable() {
        TableView<PurchaseEntity> table = new TableView<>();
        table.setItems(viewModel.purchaseList);
        table.setEditable(true);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Checkbox
        TableColumn<PurchaseEntity, Boolean> selectCol = new TableColumn<>("");
        CheckBox selectAll = new CheckBox();
        selectCol.setGraphic(selectAll);
        selectCol.setPrefWidth(40);
        selectCol.setCellValueFactory(data -> {
            PurchaseEntity p = data.getValue();
            BooleanProperty prop = new SimpleBooleanProperty(viewModel.selectedPurchases.contains(p));
            prop.addListener((obs, old, n) -> {
                if (n) {
                    if (!viewModel.selectedPurchases.contains(p))
                        viewModel.selectedPurchases.add(p);
                } else {
                    viewModel.selectedPurchases.remove(p);
                }
            });
            return prop;
        });
        selectCol.setCellFactory(javafx.scene.control.cell.CheckBoxTableCell.forTableColumn(selectCol));

        selectAll.setOnAction(e -> {
            if (selectAll.isSelected())
                viewModel.selectedPurchases.setAll(viewModel.purchaseList);
            else
                viewModel.selectedPurchases.clear();
        });

        // Columns
        TableColumn<PurchaseEntity, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().getEntryDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));

        TableColumn<PurchaseEntity, String> vendorCol = new TableColumn<>("Vendor");
        vendorCol.setCellValueFactory(
                d -> new javafx.beans.property.SimpleStringProperty(getVendorName(d.getValue().getVendorId())));

        TableColumn<PurchaseEntity, Number> bagsCol = new TableColumn<>("Bags");
        bagsCol.setCellValueFactory(d -> new javafx.beans.property.SimpleIntegerProperty(d.getValue().getBags()));

        TableColumn<PurchaseEntity, String> rateCol = new TableColumn<>("Rate");
        rateCol.setCellValueFactory(
                d -> new javafx.beans.property.SimpleStringProperty(String.format("%.2f", d.getValue().getRate())));

        TableColumn<PurchaseEntity, String> amountCol = new TableColumn<>("Amount");
        amountCol.setPrefWidth(120);
        amountCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                String.format("₹%,.2f", d.getValue().getGrandTotal())));

        TableColumn<PurchaseEntity, String> chequeCol = new TableColumn<>("Cheque No");
        chequeCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().getChequeNumber() != null ? d.getValue().getChequeNumber() : "-"));

        TableColumn<PurchaseEntity, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getStatus()));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("PAID".equalsIgnoreCase(item))
                        setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
                    else if ("UNPAID".equalsIgnoreCase(item))
                        setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                }
            }
        });

        // Actions with Crisp SVG Icons
        TableColumn<PurchaseEntity, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setCellFactory(cf -> new TableCell<>() {
            private final Button viewBtn = createSvgButton(
                    "M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z",
                    "#0ea5e9", "View");
            private final Button editBtn = createSvgButton(
                    "M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z",
                    "#f59e0b", "Edit");
            private final Button printBtn = createSvgButton(
                    "M19 8H5c-1.66 0-3 1.34-3 3v6h4v4h12v-4h4v-6c0-1.66-1.34-3-3-3zm-3 11H8v-5h8v5zm3-7c-.55 0-1-.45-1-1s.45-1 1-1 1 .45 1 1-.45 1-1 1zm-1-9H6v4h12V3z",
                    "#64748b", "Print Cheque");
            private final Button delBtn = createSvgButton(
                    "M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z", "#ef4444",
                    "Delete");

            {
                viewBtn.setOnAction(e -> {
                    if (onPurchaseSelected != null)
                        onPurchaseSelected.accept(getTableView().getItems().get(getIndex()));
                });
                editBtn.setOnAction(e -> {
                    if (onPurchaseEdit != null)
                        onPurchaseEdit.accept(getTableView().getItems().get(getIndex()));
                });
                printBtn.setOnAction(e -> handlePrintCheque(getTableView().getItems().get(getIndex())));
                delBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty)
                    setGraphic(null);
                else {
                    HBox box = new HBox(8, viewBtn, editBtn, printBtn, delBtn);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
            }
        });
        actionCol.setMinWidth(160);

        table.getColumns().addAll(selectCol, dateCol, vendorCol, bagsCol, rateCol, amountCol, chequeCol, statusCol,
                actionCol);
        return table;
    }

    private Button createSvgButton(String svgPath, String colorHex, String tooltip) {
        SVGPath path = new SVGPath();
        path.setContent(svgPath);
        path.setFill(Color.web(colorHex));
        path.setScaleX(1.0);
        path.setScaleY(1.0);

        Button btn = new Button();
        btn.setGraphic(path);
        btn.setTooltip(new Tooltip(tooltip));
        btn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 4;");
        return btn;
    }

    private void handlePrintCheque(PurchaseEntity p) {
        String vendorName = getVendorName(p.getVendorId());
        com.lax.sme_manager.dto.ChequeData data = new com.lax.sme_manager.dto.ChequeData(
                vendorName, p.getGrandTotal(), p.getChequeDate() != null ? p.getChequeDate() : LocalDate.now(), true);
        new ChequePreviewDialog(data).show();
    }

    private void handleBatchPrint() {
        List<PurchaseEntity> selected = viewModel.selectedPurchases;
        if (selected.isEmpty())
            return;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Print " + selected.size() + " cheques?", ButtonType.YES,
                ButtonType.NO);
        alert.showAndWait().ifPresent(r -> {
            if (r == ButtonType.YES) {
                var printService = new com.lax.sme_manager.service.ChequePrintService();
                var config = new com.lax.sme_manager.repository.ChequeConfigRepository().getConfig();
                int count = 0;
                for (PurchaseEntity p : selected) {
                    try {
                        String vName = getVendorName(p.getVendorId());
                        var data = new com.lax.sme_manager.dto.ChequeData(vName, p.getGrandTotal(),
                                p.getChequeDate() != null ? p.getChequeDate() : LocalDate.now(), true);
                        printService.printSilent(config, data);
                        count++;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                new Alert(Alert.AlertType.INFORMATION, "Sent " + count + " cheques to printer.").show();
            }
        });
    }

    private void handleBulkDelete() {
        int count = viewModel.selectedPurchases.size();
        if (count == 0)
            return;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete " + count + " selected entries?", ButtonType.YES,
                ButtonType.NO);
        alert.showAndWait().ifPresent(r -> {
            if (r == ButtonType.YES)
                viewModel.deleteSelectedPurchases();
        });
    }

    private void handleDelete(PurchaseEntity p) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete this purchase entry?", ButtonType.YES,
                ButtonType.NO);
        alert.showAndWait().ifPresent(r -> {
            if (r == ButtonType.YES)
                viewModel.deletePurchase(p);
        });
    }

    private String getVendorName(int id) {
        return vendorRepository.findAllVendors().stream().filter(v -> v.getId() == id).map(Vendor::getName).findFirst()
                .orElse("Unknown");
    }

    private void handleExport() {
        /* Use existing export logic */ }

    @Override
    public void refresh() {
        viewModel.loadPurchases();
    }
}
