package com.lax.sme_manager.ui.view;

import com.lax.sme_manager.repository.PurchaseRepository;
import com.lax.sme_manager.repository.model.PurchaseEntity;
import com.lax.sme_manager.ui.component.AlertUtils;
import com.lax.sme_manager.ui.theme.LaxTheme;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class ArchiveExplorerView extends VBox {

    private final PurchaseRepository repo = new PurchaseRepository();
    private final TableView<PurchaseEntity> table = new TableView<>();
    private final ObservableList<PurchaseEntity> masterData = FXCollections.observableArrayList();
    private final ObservableList<PurchaseEntity> filteredData = FXCollections.observableArrayList();

    private final TextField searchField = new TextField();
    private final DatePicker fromDate = new DatePicker();
    private final DatePicker toDate = new DatePicker();

    public ArchiveExplorerView() {
        initializeUI();
        refreshData();
    }

    private void initializeUI() {
        setPadding(new Insets(20));
        setSpacing(20);
        setStyle("-fx-background-color: white;");

        // Header
        Label title = new Label("🔎 Historical Archive Explorer (Cold Storage)");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        Label subTitle = new Label(
                "Viewing all records moved to archive for performance. Use 'Restore' to move them back to active business.");
        subTitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");

        // Toolbar
        HBox toolbar = new HBox(15);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(10));
        toolbar.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 8; -fx-border-color: #e2e8f0;");

        searchField.setPromptText("Search Vendor/Notes...");
        searchField.setPrefWidth(200);
        searchField.setStyle(LaxTheme.getInputStyle());
        searchField.textProperty().addListener((obs, old, n) -> applyFilters());

        fromDate.setPromptText("From Date");
        toDate.setPromptText("To Date");
        fromDate.valueProperty().addListener((obs, old, n) -> applyFilters());
        toDate.valueProperty().addListener((obs, old, n) -> applyFilters());

        Button btnRefresh = new Button("🔄 Refresh");
        btnRefresh.setStyle(LaxTheme.getButtonStyle(LaxTheme.ButtonType.SECONDARY));
        btnRefresh.setOnAction(e -> refreshData());

        toolbar.getChildren().addAll(new Label("Filters:"), searchField, fromDate, toDate, btnRefresh);

        // Table
        setupTable();

        getChildren().addAll(title, subTitle, toolbar, table);
        VBox.setVgrow(table, Priority.ALWAYS);
    }

    @SuppressWarnings("unchecked")
    private void setupTable() {
        TableColumn<PurchaseEntity, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(60);

        TableColumn<PurchaseEntity, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("entryDate"));
        dateCol.setPrefWidth(100);

        TableColumn<PurchaseEntity, Integer> vendorCol = new TableColumn<>("Vendor ID");
        vendorCol.setCellValueFactory(new PropertyValueFactory<>("vendorId"));
        vendorCol.setPrefWidth(80);

        TableColumn<PurchaseEntity, Integer> bagsCol = new TableColumn<>("Bags");
        bagsCol.setCellValueFactory(new PropertyValueFactory<>("bags"));

        TableColumn<PurchaseEntity, Double> amountCol = new TableColumn<>("Grand Total");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("grandTotal"));

        TableColumn<PurchaseEntity, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(120);
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button btnRestore = new Button("♻️ Restore");
            {
                btnRestore.setStyle(
                        "-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 4 8;");
                btnRestore.setOnAction(e -> {
                    PurchaseEntity entity = getTableView().getItems().get(getIndex());
                    handleRestore(entity);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty)
                    setGraphic(null);
                else
                    setGraphic(btnRestore);
            }
        });

        table.getColumns().addAll(dateCol, vendorCol, bagsCol, amountCol, actionCol);
        table.setItems(filteredData);
        table.setPlaceholder(new Label("No archived records found."));
    }

    private void refreshData() {
        masterData.setAll(repo.findAllArchived());
        applyFilters();
    }

    private void applyFilters() {
        String query = searchField.getText().toLowerCase();
        LocalDate start = fromDate.getValue();
        LocalDate end = toDate.getValue();

        List<PurchaseEntity> results = masterData.stream()
                .filter(p -> {
                    boolean matchesSearch = query.isEmpty() ||
                            (p.getNotes() != null && p.getNotes().toLowerCase().contains(query)) ||
                            (String.valueOf(p.getVendorId()).contains(query));

                    boolean matchesDate = true;
                    if (start != null && p.getEntryDate().isBefore(start))
                        matchesDate = false;
                    if (end != null && p.getEntryDate().isAfter(end))
                        matchesDate = false;

                    return matchesSearch && matchesDate;
                })
                .collect(Collectors.toList());

        filteredData.setAll(results);
    }

    private void handleRestore(PurchaseEntity entity) {
        if (AlertUtils.showConfirmation("Confirm Restoration",
                "Bring Record #" + entity.getId() + " back to active business?\n" +
                        "It will reappear in your normal history and dashboards.")) {

            if (repo.restoreFromArchive(entity.getId())) {
                AlertUtils.showInfo("Restored", "Record moved back to active list.");
                refreshData();
            } else {
                AlertUtils.showError("Failed", "Could not restore record. Check logs.");
            }
        }
    }
}
