package com.lax.sme_manager.ui.view;

import com.lax.sme_manager.repository.ChequeBookRepository;
import com.lax.sme_manager.repository.ChequeConfigRepository;
import com.lax.sme_manager.repository.PrintQueueRepository;
import com.lax.sme_manager.repository.model.ChequeBook;
import com.lax.sme_manager.repository.model.ChequeConfig;
import com.lax.sme_manager.repository.model.PrintQueueItem;
import com.lax.sme_manager.service.ChequePrintService;
import com.lax.sme_manager.ui.component.AlertUtils;
import com.lax.sme_manager.util.DatabaseManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PrintQueueHubDialog extends Dialog<Void> {
    private static final Logger LOGGER = LoggerFactory.getLogger(PrintQueueHubDialog.class);

    private final PrintQueueRepository queueRepo = new PrintQueueRepository();
    private final ChequeBookRepository bookRepo = new ChequeBookRepository();
    private final ChequeConfigRepository configRepo = new ChequeConfigRepository();
    private final ChequePrintService printService = new ChequePrintService();
    private final Integer userId;

    private final ObservableList<PrintQueueItem> queueItems = FXCollections.observableArrayList();
    private TableView<PrintQueueItem> queueTable;

    private ComboBox<ChequeBook> bookSelector;
    private ComboBox<String> bankSelector;
    private Label totalAmountLabel;
    private Label itemCountLabel;
    private Label bookStatusLabel;

    private Runnable onQueueChanged;

    public PrintQueueHubDialog(Window owner) {
        this(owner, null);
    }

    public PrintQueueHubDialog(Window owner, Integer userId) {
        initOwner(owner);
        this.userId = userId;
        setTitle("Batch Printing Hub - Intelligent Cheque Management");
        getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        getDialogPane().setPrefSize(1100, 750);

        loadData();
        initUI();
    }

    public void setOnQueueChanged(Runnable callback) {
        this.onQueueChanged = callback;
    }

    private void loadData() {
        queueItems.setAll(queueRepo.getAllItems());
    }

    private void initUI() {
        HBox mainLayout = new HBox(20);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setStyle("-fx-background-color: #f8fafc;");

        // --- LEFT SIDE: QUEUE TABLE ---
        VBox tableContainer = new VBox(15);
        HBox.setHgrow(tableContainer, Priority.ALWAYS);

        HBox tableHeader = new HBox(10);
        tableHeader.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Pending Print Queue");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnClear = new Button("🗑️ Clear All");
        btnClear.setStyle(
                "-fx-background-color: #fee2e2; -fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 6; -fx-cursor: hand;");
        btnClear.setOnAction(e -> handleClearQueue());

        tableHeader.getChildren().addAll(title, spacer, btnClear);

        queueTable = createQueueTable();
        VBox.setVgrow(queueTable, Priority.ALWAYS);

        tableContainer.getChildren().addAll(tableHeader, queueTable);

        // --- RIGHT SIDE: CONTROLS ---
        VBox controls = new VBox(20);
        controls.setPrefWidth(320);
        controls.setPadding(new Insets(0, 0, 0, 10));

        // Group 1: Cheque Inventory
        VBox inventoryBox = createControlGroup("CHEQUE INVENTORY");
        bookSelector = new ComboBox<>();
        bookSelector.setMaxWidth(Double.MAX_VALUE);
        bookSelector.setPromptText("Select Cheque Book");
        loadBooks();

        bookStatusLabel = new Label();
        bookStatusLabel.setStyle("-fx-font-size: 11px; -fx-wrap-text: true;");
        updateBookStatus();

        bookSelector.setOnAction(e -> updateBookStatus());
        inventoryBox.getChildren().addAll(new Label("Active Cheque Book"), bookSelector, bookStatusLabel);

        // Group 2: Layout Configuration
        VBox configBox = createControlGroup("LAYOUT CONFIG");
        bankSelector = new ComboBox<>();
        bankSelector.setMaxWidth(Double.MAX_VALUE);
        loadBankTemplates();

        Button btnAlign = new Button("📏 Print Alignment Test");
        btnAlign.setMaxWidth(Double.MAX_VALUE);
        btnAlign.setStyle(
                "-fx-background-color: #f1f5f9; -fx-text-fill: #475569; -fx-font-weight: bold; -fx-padding: 8; -fx-background-radius: 6;");
        btnAlign.setOnAction(e -> handleAlignmentTest());

        configBox.getChildren().addAll(new Label("Bank Template"), bankSelector, btnAlign);

        // Group 3: Batch Stats
        VBox statsBox = createControlGroup("BATCH SUMMARY");
        itemCountLabel = new Label("Total Items: 0");
        itemCountLabel.setStyle("-fx-font-weight: bold;");
        totalAmountLabel = new Label("Total Amount: ₹0.00");
        totalAmountLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #0d9488;");
        updateStats();
        statsBox.getChildren().addAll(itemCountLabel, totalAmountLabel);

        // Group 4: Action!
        Button btnPrintAll = new Button("🖨️ PROCESS & PRINT BATCH");
        btnPrintAll.setMaxWidth(Double.MAX_VALUE);
        btnPrintAll.setPrefHeight(60);
        btnPrintAll.setStyle(
                "-fx-background-color: #0d9488; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: 800; -fx-background-radius: 10; -fx-cursor: hand;");
        btnPrintAll.setOnAction(e -> handlePrintAll());

        controls.getChildren().addAll(inventoryBox, configBox, statsBox, btnPrintAll);

        mainLayout.getChildren().addAll(tableContainer, controls);
        getDialogPane().setContent(mainLayout);
    }

    @SuppressWarnings("unchecked")
    private TableView<PrintQueueItem> createQueueTable() {
        TableView<PrintQueueItem> table = new TableView<>(queueItems);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #e2e8f0;");

        TableColumn<PrintQueueItem, String> payeeCol = new TableColumn<>("Payee");
        payeeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPayeeName()));

        TableColumn<PrintQueueItem, String> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(d -> new SimpleStringProperty(String.format("₹%,.2f", d.getValue().getAmount())));
        amountCol.setPrefWidth(120);

        TableColumn<PrintQueueItem, String> dateCol = new TableColumn<>("Cheque Date");
        dateCol.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getChequeDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));

        TableColumn<PrintQueueItem, String> acCol = new TableColumn<>("A/C Payee");
        acCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().isAcPayee() ? "Yes" : "No"));
        acCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    if ("Yes".equals(item))
                        setStyle("-fx-text-fill: #0d9488; -fx-font-weight: bold;");
                    else
                        setStyle("-fx-text-fill: #64748b;");
                }
            }
        });

        TableColumn<PrintQueueItem, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button btnRemove = new Button("❌");
            {
                btnRemove.setStyle("-fx-background-color: transparent; -fx-text-fill: #ef4444; -fx-cursor: hand;");
                btnRemove.setOnAction(e -> {
                    PrintQueueItem item = getTableView().getItems().get(getIndex());
                    queueRepo.removeItem(item.getId());
                    loadData();
                    updateStats();
                    if (onQueueChanged != null)
                        onQueueChanged.run();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty)
                    setGraphic(null);
                else
                    setGraphic(btnRemove);
            }
        });
        actionCol.setPrefWidth(70);

        table.getColumns().addAll(payeeCol, amountCol, dateCol, acCol, actionCol);
        return table;
    }

    private VBox createControlGroup(String title) {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(15));
        vbox.setStyle(
                "-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #e2e8f0; -fx-border-radius: 10;");

        Label lblTitle = new Label(title);
        lblTitle.setStyle(
                "-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #94a3b8; -fx-border-color: transparent transparent #f1f5f9 transparent; -fx-border-width: 0 0 1 0; -fx-padding: 0 0 5 0;");
        lblTitle.setMaxWidth(Double.MAX_VALUE);

        vbox.getChildren().add(lblTitle);
        return vbox;
    }

    private void loadBooks() {
        List<ChequeBook> books = bookRepo.getAllBooks().stream()
                .filter(b -> !b.isExhausted())
                .toList();
        bookSelector.setItems(FXCollections.observableArrayList(books));

        bookSelector.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(ChequeBook b) {
                return b == null ? "" : b.getBookName() + " (" + b.getRemainingLeaves() + ")";
            }

            @Override
            public ChequeBook fromString(String s) {
                return null;
            }
        });

        ChequeBook active = bookRepo.getActiveBook();
        if (active != null)
            bookSelector.setValue(active);
        else if (!books.isEmpty())
            bookSelector.setValue(books.get(0));
    }

    private void loadBankTemplates() {
        List<String> banks = new ArrayList<>();
        String sql = "SELECT bank_name FROM bank_templates ORDER BY bank_name";
        try (Connection conn = DatabaseManager.getConnection();
                ResultSet rs = conn.createStatement().executeQuery(sql)) {
            while (rs.next())
                banks.add(rs.getString("bank_name"));
        } catch (SQLException e) {
            LOGGER.error("Failed to load banks", e);
        }
        bankSelector.getItems().setAll(banks);

        ChequeConfig config = configRepo.getConfig();
        if (config != null && config.getBankName() != null)
            bankSelector.setValue(config.getBankName());
        else if (!banks.isEmpty())
            bankSelector.setValue(banks.get(0));
    }

    private void updateBookStatus() {
        ChequeBook b = bookSelector.getValue();
        if (b == null) {
            bookStatusLabel.setText("Please select a cheque book.");
            bookStatusLabel.setStyle("-fx-text-fill: #ef4444;");
            return;
        }

        if (b.getRemainingLeaves() < queueItems.size()) {
            bookStatusLabel.setText("⚠️ Warning: Only " + b.getRemainingLeaves()
                    + " leaves left. Cannot print full batch (" + queueItems.size() + ").");
            bookStatusLabel.setStyle("-fx-text-fill: #f59e0b;");
        } else {
            bookStatusLabel.setText("Ready: " + b.getRemainingLeaves() + " leaves available. Next No: "
                    + String.format("%06d", b.getNextNumber()));
            bookStatusLabel.setStyle("-fx-text-fill: #10b981;");
        }
    }

    private void updateStats() {
        double total = queueItems.stream().mapToDouble(PrintQueueItem::getAmount).sum();
        totalAmountLabel.setText(String.format("Total Amount: ₹%,.2f", total));
        itemCountLabel.setText("Total Items: " + queueItems.size());
    }

    private void handleClearQueue() {
        if (AlertUtils.showConfirmation("Clear Queue",
                "Are you sure you want to remove ALL items from the print queue?")) {
            queueRepo.clearQueue();
            loadData();
            updateStats();
            if (onQueueChanged != null)
                onQueueChanged.run();
        }
    }

    private void handleAlignmentTest() {
        String bank = bankSelector.getValue();
        if (bank == null)
            return;

        ChequeConfig config = configRepo.getConfigByBank(bank);
        if (config == null) {
            AlertUtils.showError("Error", "Template not found.");
            return;
        }

        com.lax.sme_manager.dto.ChequeData testData = new com.lax.sme_manager.dto.ChequeData(
                "SAMPLE ALIGNMENT TEST", java.math.BigDecimal.valueOf(1234.56), LocalDate.now(), true, null, "000000");

        try {
            printService.printSilent(config, testData, userId);
            AlertUtils.showInfo("Test Sent",
                    "Alignment test sent to printer. Please check the alignment on a blank paper.");
        } catch (Exception e) {
            AlertUtils.showError("Print Failed", e.getMessage());
        }
    }

    private void handlePrintAll() {
        if (queueItems.isEmpty())
            return;

        ChequeBook book = bookSelector.getValue();
        String bank = bankSelector.getValue();

        if (book == null || bank == null) {
            AlertUtils.showError("Selection Required", "Please select both a Cheque Book and a Bank Template.");
            return;
        }

        if (book.getRemainingLeaves() < queueItems.size()) {
            AlertUtils.showError("Insufficient Leaves", "The selected book only has " + book.getRemainingLeaves()
                    + " leaves left. Please select another book or reduce the queue.");
            return;
        }

        if (!AlertUtils.showConfirmation("Confirm Batch Print", "This will print " + queueItems.size()
                + " cheques and consume " + queueItems.size() + " leaves.\nContinue?")) {
            return;
        }

        ChequeConfig config = configRepo.getConfigByBank(bank);
        long startChqNum = bookRepo.consumeLeaves(book.getId(), queueItems.size());

        if (startChqNum == -1) {
            AlertUtils.showError("Error", "Failed to reserve leaves from book.");
            return;
        }

        List<com.lax.sme_manager.dto.ChequeData> batchData = new ArrayList<>();
        long currentNo = startChqNum;

        for (PrintQueueItem item : queueItems) {
            String chqNoStr = String.format("%06d", currentNo);
            batchData.add(new com.lax.sme_manager.dto.ChequeData(
                    item.getPayeeName(),
                    java.math.BigDecimal.valueOf(item.getAmount()),
                    item.getChequeDate(),
                    item.isAcPayee(),
                    item.getPurchaseId(),
                    chqNoStr));
            currentNo++;
        }

        try {
            printService.printBatch(config, batchData, userId);

            // Post-print: Update purchases and clear queue
            updatePurchasesInBatch(batchData);
            queueRepo.clearQueue();

            AlertUtils.showInfo("Batch Complete", "Successfully sent " + batchData.size() + " cheques to the printer.");

            if (onQueueChanged != null)
                onQueueChanged.run();
            close();

        } catch (Exception e) {
            LOGGER.error("Batch print failed", e);
            AlertUtils.showError("Batch Print Error", e.getMessage());
        }
    }

    private void updatePurchasesInBatch(List<com.lax.sme_manager.dto.ChequeData> batch) {
        String sql = "UPDATE purchase_entries SET status = 'PAID', cheque_number = ?, cheque_date = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (com.lax.sme_manager.dto.ChequeData data : batch) {
                if (data.purchaseId() != null) {
                    pstmt.setString(1, data.chequeNumber());
                    pstmt.setObject(2, data.date());
                    pstmt.setInt(3, data.purchaseId());
                    pstmt.addBatch();
                }
            }
            pstmt.executeBatch();
        } catch (SQLException e) {
            LOGGER.error("Failed to update purchases after batch print", e);
            javafx.application.Platform.runLater(() -> AlertUtils.showWarning("Update Warning",
                    "Cheques printed but some purchase records failed to update. Please check manually."));
        }
    }
}
