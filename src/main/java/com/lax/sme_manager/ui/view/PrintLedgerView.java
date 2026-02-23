package com.lax.sme_manager.ui.view;

import com.lax.sme_manager.repository.PrintLedgerRepository;
import com.lax.sme_manager.repository.model.PrintLedgerEntry;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.format.DateTimeFormatter;

public class PrintLedgerView extends VBox implements RefreshableView {
    private final PrintLedgerRepository ledgerRepo = new PrintLedgerRepository();
    private final ObservableList<PrintLedgerEntry> logs = FXCollections.observableArrayList();
    private TableView<PrintLedgerEntry> logTable;
    private TextField searchField;

    public PrintLedgerView() {
        initializeUI();
        refresh();
    }

    private void initializeUI() {
        setPadding(new Insets(24, 32, 24, 32));
        setSpacing(24);
        setStyle("-fx-background-color: #f8fafc;");

        // --- HEADER ---
        VBox header = new VBox(2);
        Label titleLbl = new Label("Consolidated Print Ledger");
        titleLbl.setStyle("-fx-font-size: 28px; -fx-font-weight: 800; -fx-text-fill: #0F172A;");
        Label subtitleLbl = new Label("Audit trail and accountability records for all printed cheques");
        subtitleLbl.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748B;");
        header.getChildren().addAll(titleLbl, subtitleLbl);

        // --- FILTER BAR ---
        HBox filterBar = new HBox(15);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        searchField = new TextField();
        searchField.setPromptText("Search by Payee, Cheque No, or User...");
        searchField.setPrefWidth(350);
        searchField.setStyle(
                "-fx-background-radius: 8; -fx-padding: 10 15; -fx-border-color: #e2e8f0; -fx-border-radius: 8;");

        Button btnRefresh = new Button("🔄 Refresh");
        btnRefresh.setStyle(
                "-fx-background-color: #f1f5f9; -fx-text-fill: #475569; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand;");
        btnRefresh.setOnAction(e -> refresh());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        filterBar.getChildren().addAll(searchField, btnRefresh, spacer);

        // --- TABLE ---
        logTable = createTable();
        VBox.setVgrow(logTable, Priority.ALWAYS);

        getChildren().addAll(header, filterBar, logTable);
    }

    @SuppressWarnings("unchecked")
    private TableView<PrintLedgerEntry> createTable() {
        TableView<PrintLedgerEntry> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setStyle(
                "-fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: #e2e8f0; -fx-overflow-x: hidden;");

        TableColumn<PrintLedgerEntry, String> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getPrintedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
        timeCol.setPrefWidth(150);

        TableColumn<PrintLedgerEntry, String> userCol = new TableColumn<>("User");
        userCol.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getUsername() != null ? d.getValue().getUsername() : "System"));
        userCol.setPrefWidth(120);

        TableColumn<PrintLedgerEntry, String> payeeCol = new TableColumn<>("Payee");
        payeeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPayeeName()));
        payeeCol.setPrefWidth(200);

        TableColumn<PrintLedgerEntry, String> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(d -> new SimpleStringProperty(String.format("₹%,.2f", d.getValue().getAmount())));
        amountCol.setPrefWidth(120);

        TableColumn<PrintLedgerEntry, String> chqCol = new TableColumn<>("Cheque No");
        chqCol.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getChequeNumber() != null ? d.getValue().getChequeNumber() : "-"));
        chqCol.setPrefWidth(100);

        TableColumn<PrintLedgerEntry, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPrintStatus()));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    if ("SUCCESS".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                    }
                }
            }
        });

        TableColumn<PrintLedgerEntry, String> remarksCol = new TableColumn<>("Remarks");
        remarksCol.setCellValueFactory(
                d -> new SimpleStringProperty(d.getValue().getRemarks() != null ? d.getValue().getRemarks() : ""));

        table.getColumns().addAll(timeCol, userCol, payeeCol, amountCol, chqCol, statusCol, remarksCol);

        // Setup Filtering
        FilteredList<PrintLedgerEntry> filteredData = new FilteredList<>(logs, p -> true);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(entry -> {
                if (newVal == null || newVal.isEmpty())
                    return true;
                String low = newVal.toLowerCase();
                return entry.getPayeeName().toLowerCase().contains(low) ||
                        (entry.getChequeNumber() != null && entry.getChequeNumber().contains(low)) ||
                        (entry.getUsername() != null && entry.getUsername().toLowerCase().contains(low));
            });
        });
        table.setItems(filteredData);

        return table;
    }

    @Override
    public void refresh() {
        Platform.runLater(() -> {
            logs.setAll(ledgerRepo.getAllLogs());
        });
    }
}
