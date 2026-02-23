package com.lax.sme_manager.ui.view;

import com.lax.sme_manager.repository.PurchaseRepository;
import com.lax.sme_manager.repository.model.PurchaseEntity;
import com.lax.sme_manager.service.ReportService;
import com.lax.sme_manager.ui.component.AlertUtils;
import com.lax.sme_manager.util.VendorCache;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ReportsView extends VBox implements RefreshableView {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReportsView.class);
    private final PurchaseRepository purchaseRepo = new PurchaseRepository();
    private final ReportService reportService;

    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private Label totalBagsLbl;
    private Label totalAmountLbl;
    private Label totalCommLbl;
    private Label transactionCountLbl;

    public ReportsView(VendorCache vendorCache) {
        this.reportService = new ReportService(vendorCache);
        initializeUI();
        updateSummary();
    }

    private void initializeUI() {
        setPadding(new Insets(24, 32, 24, 32));
        setSpacing(28);
        setStyle("-fx-background-color: #f8fafc;");

        // --- HEADER ---
        VBox header = new VBox(2);
        Label titleLbl = new Label("Accountant Reports");
        titleLbl.setStyle("-fx-font-size: 28px; -fx-font-weight: 800; -fx-text-fill: #0F172A;");
        Label subtitleLbl = new Label("Generate monthly tax summaries and business performance reports");
        subtitleLbl.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748B;");
        header.getChildren().addAll(titleLbl, subtitleLbl);

        // --- FILTER SECTION ---
        HBox filterCard = new HBox(24);
        filterCard.setAlignment(Pos.CENTER_LEFT);
        filterCard.setPadding(new Insets(20));
        filterCard.setStyle(
                "-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 2);");

        VBox startBox = createFilterField("Start Date",
                startDatePicker = new DatePicker(LocalDate.now().withDayOfMonth(1)));
        VBox endBox = createFilterField("End Date", endDatePicker = new DatePicker(LocalDate.now()));

        startDatePicker.valueProperty().addListener((obs, oldV, newV) -> updateSummary());
        endDatePicker.valueProperty().addListener((obs, oldV, newV) -> updateSummary());

        Region filterSpacer = new Region();
        HBox.setHgrow(filterSpacer, Priority.ALWAYS);

        filterCard.getChildren().addAll(startBox, endBox, filterSpacer);

        // --- SUMMARY DASHBOARD ---
        GridPane summaryGrid = new GridPane();
        summaryGrid.setHgap(20);
        summaryGrid.setVgap(20);

        summaryGrid.add(createSummaryCard("Total Bags", "0", totalBagsLbl = new Label("0"), "#3B82F6"), 0, 0);
        summaryGrid.add(createSummaryCard("Turnover (₹)", "₹0.00", totalAmountLbl = new Label("₹0.00"), "#10B981"), 1,
                0);
        summaryGrid.add(createSummaryCard("Agency Commission", "₹0.00", totalCommLbl = new Label("₹0.00"), "#F59E0B"),
                2, 0);
        summaryGrid.add(createSummaryCard("Transactions", "0", transactionCountLbl = new Label("0"), "#6366F1"), 3, 0);

        ColumnConstraints col = new ColumnConstraints();
        col.setPercentWidth(25);
        summaryGrid.getColumnConstraints().addAll(col, col, col, col);

        // --- ACTION BUTTONS ---
        HBox actionPanel = new HBox(16);
        actionPanel.setAlignment(Pos.CENTER_RIGHT);

        Button btnExcel = new Button("📥 Export to Excel");
        btnExcel.setStyle(
                "-fx-background-color: #0D9488; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 24; -fx-background-radius: 8; -fx-cursor: hand;");
        btnExcel.setOnAction(e -> handleExcelExport());

        Button btnPDF = new Button("📄 Export to PDF");
        btnPDF.setStyle(
                "-fx-background-color: #475569; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 24; -fx-background-radius: 8; -fx-cursor: hand;");
        btnPDF.setOnAction(e -> handlePDFExport());

        actionPanel.getChildren().addAll(btnExcel, btnPDF);

        getChildren().addAll(header, filterCard, summaryGrid, actionPanel);
    }

    private VBox createFilterField(String label, DatePicker picker) {
        VBox box = new VBox(8);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #475569; -fx-font-size: 13px;");
        picker.setPrefWidth(180);
        picker.setStyle("-fx-background-radius: 8;");
        box.getChildren().addAll(lbl, picker);
        return box;
    }

    private VBox createSummaryCard(String title, String initialValue, Label valueLbl, String color) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #f1f5f9; -fx-border-width: 1;");

        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748B; -fx-font-weight: 600;");

        valueLbl.setStyle("-fx-font-size: 22px; -fx-font-weight: 800; -fx-text-fill: " + color + ";");
        valueLbl.setText(initialValue);

        card.getChildren().addAll(titleLbl, valueLbl);
        return card;
    }

    private void updateSummary() {
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();
        if (start == null || end == null)
            return;

        Platform.runLater(() -> {
            List<PurchaseEntity> purchases = purchaseRepo.findByDateRange(start, end);

            int totalBags = 0;
            BigDecimal totalAmount = BigDecimal.ZERO;
            BigDecimal totalComm = BigDecimal.ZERO;

            for (PurchaseEntity p : purchases) {
                totalBags += p.getBags();
                totalAmount = totalAmount.add(p.getGrandTotal());
                totalComm = totalComm.add(p.getCommissionFeeAmount());
            }

            totalBagsLbl.setText(String.valueOf(totalBags));
            totalAmountLbl.setText(String.format("₹%,.2f", totalAmount.doubleValue()));
            totalCommLbl.setText(String.format("₹%,.2f", totalComm.doubleValue()));
            transactionCountLbl.setText(String.valueOf(purchases.size()));
        });
    }

    private void handleExcelExport() {
        List<PurchaseEntity> data = purchaseRepo.findByDateRange(startDatePicker.getValue(), endDatePicker.getValue());
        if (data.isEmpty()) {
            AlertUtils.showWarning("No Data", "There are no transactions for the selected date range.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Accountant Report (Excel)");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Workbook", "*.xlsx"));
        fileChooser.setInitialFileName(
                "Accountant_Report_" + startDatePicker.getValue() + "_to_" + endDatePicker.getValue() + ".xlsx");

        File file = fileChooser.showSaveDialog(getScene().getWindow());
        if (file != null) {
            try {
                reportService.exportToExcel(data, file);
                AlertUtils.showInfo("Export Success",
                        "Excel report generated successfully at:\n" + file.getAbsolutePath());
            } catch (Exception e) {
                LOGGER.error("Excel export failed", e);
                AlertUtils.showError("Export Failed", "Could not generate Excel report: " + e.getMessage());
            }
        }
    }

    private void handlePDFExport() {
        List<PurchaseEntity> data = purchaseRepo.findByDateRange(startDatePicker.getValue(), endDatePicker.getValue());
        if (data.isEmpty()) {
            AlertUtils.showWarning("No Data", "There are no transactions for the selected date range.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Accountant Report (PDF)");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Document", "*.pdf"));
        fileChooser.setInitialFileName(
                "Accountant_Report_" + startDatePicker.getValue() + "_to_" + endDatePicker.getValue() + ".pdf");

        File file = fileChooser.showSaveDialog(getScene().getWindow());
        if (file != null) {
            try {
                reportService.exportToPDF(data, file);
                AlertUtils.showInfo("Export Success",
                        "PDF summary generated successfully at:\n" + file.getAbsolutePath());
            } catch (Exception e) {
                LOGGER.error("PDF export failed", e);
                AlertUtils.showError("Export Failed", "Could not generate PDF report: " + e.getMessage());
            }
        }
    }

    @Override
    public void refresh() {
        updateSummary();
    }
}
