package com.lax.sme_manager.ui.view;

import com.lax.sme_manager.domain.ChequeTemplate;
import com.lax.sme_manager.domain.Vendor;
import com.lax.sme_manager.repository.ChequeTemplateRepository;
import com.lax.sme_manager.repository.IPurchaseRepository;
import com.lax.sme_manager.repository.VendorRepository;
import com.lax.sme_manager.repository.model.PurchaseEntity;
import com.lax.sme_manager.ui.component.UIStyles;
import com.lax.sme_manager.ui.theme.LaxTheme;
import com.lax.sme_manager.util.AmountToWordsConverter;
import com.lax.sme_manager.util.DateCharacterTracker;
import com.lax.sme_manager.util.NumericTextFormatter;
import com.lax.sme_manager.util.i18n.AppLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.print.PrinterJob;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.StringConverter;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ChequeWizardView extends VBox implements RefreshableView {

    private final IPurchaseRepository purchaseRepository;
    private final VendorRepository vendorRepository;
    private final ChequeTemplateRepository templateRepository;

    private StackPane mainStack;
    private VBox selectionStep;
    private VBox writingStep;

    // Selection Data
    private ComboBox<Vendor> vendorCombo;
    private TableView<PurchaseSelectionModel> selectionTable;
    private final ObservableList<PurchaseSelectionModel> pendingEntries = FXCollections.observableArrayList();

    // Writing State
    private List<PurchaseEntity> entriesToProcess = new ArrayList<>();
    private int currentIndex = 0;

    // Writing UI Components
    private Label stepLabel;
    private TextField chequeNoField;
    private DatePicker chequeDatePicker;
    private Label entryInfoLabel;
    private ImageView chequeBg;
    private Pane previewOverlay;

    // Template Selection
    private ComboBox<String> bankSelector;
    private ComboBox<ChequeTemplate> formatSelector;
    private ChequeTemplate selectedTemplate;

    public ChequeWizardView(IPurchaseRepository purchaseRepository, VendorRepository vendorRepository) {
        this.purchaseRepository = purchaseRepository;
        this.vendorRepository = vendorRepository;
        this.templateRepository = new ChequeTemplateRepository();
        initialize();
        loadAllUnpaidEntries();
    }

    private void initialize() {
        setPadding(new Insets(LaxTheme.Layout.MAIN_CONTAINER_PADDING)); // Design Manifest: 25px
        setSpacing(LaxTheme.Spacing.SPACE_32);
        setStyle("-fx-background-color: transparent; -fx-background: " + LaxTheme.Colors.LIGHT_GRAY + ";");

        mainStack = new StackPane();
        VBox.setVgrow(mainStack, Priority.ALWAYS);

        selectionStep = createSelectionStep();
        writingStep = createWritingStep();
        writingStep.setVisible(false);

        mainStack.getChildren().addAll(selectionStep, writingStep);
        getChildren().add(mainStack);
    }

    private VBox createSelectionStep() {
        VBox step = new VBox(LaxTheme.Spacing.SPACE_32);

        Label title = new Label(AppLabel.TITLE_CHEQUE_WIZARD.get());
        title.setStyle(UIStyles.getTitleStyle());

        // Empty Status Message
        Label emptyLabel = new Label("No Pending Cheque Writing Purchase Entries Found...");
        emptyLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: " + LaxTheme.Colors.TEXT_SECONDARY + ";");
        emptyLabel.setVisible(false);
        emptyLabel.setManaged(false);

        // Selection Table
        selectionTable = new TableView<>(pendingEntries);
        selectionTable.setEditable(true);
        selectionTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        selectionTable.setStyle(UIStyles.getCardStyle());
        VBox.setVgrow(selectionTable, Priority.ALWAYS);

        TableColumn<PurchaseSelectionModel, Boolean> selectCol = new TableColumn<>("➔");
        selectCol.setCellValueFactory(d -> d.getValue().selectedProperty());
        selectCol.setCellFactory(CheckBoxTableCell.forTableColumn(selectCol));
        selectCol.setEditable(true);
        selectCol.setPrefWidth(60);

        TableColumn<PurchaseSelectionModel, Number> snoCol = new TableColumn<>("S.No");
        snoCol.setCellValueFactory(
                d -> new javafx.beans.property.SimpleIntegerProperty(pendingEntries.indexOf(d.getValue()) + 1));
        snoCol.setPrefWidth(60);

        TableColumn<PurchaseSelectionModel, String> vendorCol = new TableColumn<>(AppLabel.LBL_VENDOR.get());
        vendorCol.setCellValueFactory(d -> {
            Vendor v = vendorRepository.findAllVendors().stream()
                    .filter(ve -> ve.getId() == d.getValue().getEntity().getVendorId()).findFirst().orElse(null);
            return new javafx.beans.property.SimpleStringProperty(v != null ? v.getName() : "Unknown");
        });

        TableColumn<PurchaseSelectionModel, String> dateCol = new TableColumn<>(AppLabel.LBL_PURCHASE_DATE.get());
        dateCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().getEntity().getEntryDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));

        TableColumn<PurchaseSelectionModel, String> amountCol = new TableColumn<>(AppLabel.LBL_AMOUNT.get());
        amountCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                String.format("₹%,.2f", d.getValue().getEntity().getGrandTotal())));

        selectionTable.getColumns().addAll(selectCol, snoCol, vendorCol, dateCol, amountCol);

        // Buttons
        HBox btnRow = new HBox(16);
        btnRow.setAlignment(Pos.CENTER_LEFT);

        Button startBtn = new Button(AppLabel.ACTION_START_CHQ_WRITING.get());
        startBtn.setStyle(LaxTheme.getButtonStyle(LaxTheme.ButtonType.PRIMARY));
        startBtn.setPrefWidth(300);
        startBtn.setOnAction(e -> handleStartWriting());

        Button customBtn = new Button("➕ Custom Cheque");
        customBtn.setStyle(
                "-fx-background-color: #059669; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 6; -fx-cursor: hand;");
        customBtn.setOnAction(e -> handleCustomCheque());

        btnRow.getChildren().addAll(startBtn, customBtn);

        // Visibility Bindings for Empty State
        emptyLabel.visibleProperty().bind(javafx.beans.binding.Bindings.isEmpty(pendingEntries));
        emptyLabel.managedProperty().bind(emptyLabel.visibleProperty());
        selectionTable.visibleProperty().bind(javafx.beans.binding.Bindings.isNotEmpty(pendingEntries));
        selectionTable.managedProperty().bind(selectionTable.visibleProperty());
        btnRow.visibleProperty().bind(javafx.beans.binding.Bindings.isNotEmpty(pendingEntries));
        btnRow.managedProperty().bind(btnRow.visibleProperty());

        step.getChildren().addAll(title, emptyLabel, selectionTable, btnRow);
        return step;
    }

    private VBox createWritingStep() {
        VBox step = new VBox(LaxTheme.Spacing.SPACE_24);

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label(AppLabel.WIZARD_STEP_2.get());
        title.setStyle(UIStyles.getTitleStyle());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        stepLabel = new Label("Cheque 1 of N");
        stepLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + LaxTheme.Colors.TEXT_SECONDARY + ";");

        header.getChildren().addAll(title, spacer, stepLabel);

        // Sidebar Controls
        VBox controls = new VBox(LaxTheme.Spacing.SPACE_16);
        controls.setPadding(new Insets(32));
        controls.setStyle(UIStyles.getCardStyle());
        controls.setPrefWidth(400);

        chequeNoField = new TextField();
        chequeNoField.setPromptText(AppLabel.LBL_CHQ_NUMBER.get());

        chequeDatePicker = new DatePicker(LocalDate.now());
        chequeDatePicker.setPrefWidth(Double.MAX_VALUE);

        VBox infoBox = new VBox(8);
        infoBox.setPadding(new Insets(16));
        infoBox.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-radius: 8;");
        Label detailsLbl = new Label(AppLabel.TITLE_PURCHASE_DETAILS.get() + ":");
        detailsLbl.setStyle("-fx-font-weight: bold;");
        entryInfoLabel = new Label("-");
        infoBox.getChildren().addAll(detailsLbl, entryInfoLabel);

        HBox btnRow = new HBox(12);
        Button backBtn = new Button("←");
        backBtn.setStyle(LaxTheme.getButtonStyle(LaxTheme.ButtonType.SECONDARY));
        backBtn.setOnAction(e -> handleBack());

        // Template Selection
        VBox templateBox = new VBox(8);
        templateBox.setPadding(new Insets(16));
        templateBox.setStyle("-fx-background-color: #f1f5f9; -fx-border-color: #cbd5e1; -fx-border-radius: 8;");

        bankSelector = new ComboBox<>();
        bankSelector.setPromptText("Select Bank");
        bankSelector.setMaxWidth(Double.MAX_VALUE);
        bankSelector.setOnAction(e -> handleBankSelection());

        formatSelector = new ComboBox<>();
        formatSelector.setPromptText("Select Format");
        formatSelector.setMaxWidth(Double.MAX_VALUE);
        formatSelector.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(ChequeTemplate item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getTemplateName());
            }
        });
        formatSelector.setConverter(new StringConverter<>() {
            @Override
            public String toString(ChequeTemplate t) {
                return (t == null || t.getTemplateName() == null) ? "" : t.getTemplateName();
            }

            @Override
            public ChequeTemplate fromString(String s) {
                return null;
            }
        });
        formatSelector.setOnAction(e -> {
            selectedTemplate = formatSelector.getValue();
            if (entriesToProcess != null && !entriesToProcess.isEmpty()) {
                updateLivePreview(entriesToProcess.get(currentIndex));
            }
        });

        templateBox.getChildren().addAll(new Label("Select Template:"), bankSelector, formatSelector);

        Button nextBtn = new Button(AppLabel.ACTION_SUBMIT.get() + " & Next →");
        nextBtn.setStyle(LaxTheme.getButtonStyle(LaxTheme.ButtonType.PRIMARY));
        HBox.setHgrow(nextBtn, Priority.ALWAYS);
        nextBtn.setMaxWidth(Double.MAX_VALUE);
        nextBtn.setOnAction(e -> handleNext());

        btnRow.getChildren().addAll(backBtn, nextBtn);

        controls.getChildren().addAll(new Label(AppLabel.LBL_CHQ_NUMBER.get() + ":"), chequeNoField,
                new Label(AppLabel.LBL_CHEQUE_DATE.get() + ":"), chequeDatePicker,
                infoBox, templateBox, btnRow);

        // Preview Area
        StackPane previewStack = new StackPane();
        previewStack.setStyle("-fx-background-color: white; -fx-border-color: " + LaxTheme.Colors.BORDER_GRAY
                + "; -fx-border-radius: 12;");
        HBox.setHgrow(previewStack, Priority.ALWAYS);

        chequeBg = new ImageView();
        chequeBg.setPreserveRatio(true);
        chequeBg.setFitWidth(800);

        previewOverlay = new Pane();
        previewOverlay.setPickOnBounds(false);

        previewStack.getChildren().addAll(chequeBg, previewOverlay);

        HBox contentLayout = new HBox(LaxTheme.Spacing.SPACE_32, controls, previewStack);
        VBox.setVgrow(contentLayout, Priority.ALWAYS);

        step.getChildren().addAll(header, contentLayout);
        return step;
    }

    private void handleStartWriting() {
        entriesToProcess = pendingEntries.stream()
                .filter(PurchaseSelectionModel::isSelected)
                .map(PurchaseSelectionModel::getEntity)
                .collect(Collectors.toList());

        if (entriesToProcess.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Please select at least one entry.").show();
            return;
        }

        currentIndex = 0;
        selectionStep.setVisible(false);
        writingStep.setVisible(true);
        loadBanksToSelector();
        resetWritingForm(); // Clear previous data before showing new entry
        updateWritingForm();
    }

    /**
     * Resets all form fields to empty state
     * CRITICAL: Call this before updateWritingForm() when switching entry modes
     */
    private void resetWritingForm() {
        chequeNoField.clear();
        chequeDatePicker.setValue(LocalDate.now());
    }

    private void loadBanksToSelector() {
        List<String> banks = templateRepository.findAllBanks();
        if (banks.isEmpty()) {
            new Alert(Alert.AlertType.WARNING,
                    "No cheque templates found. Please create a template in the Cheque Designer first!").show();
            bankSelector.setItems(FXCollections.emptyObservableList());
            return;
        }
        bankSelector.setItems(FXCollections.observableArrayList(banks));
        bankSelector.setValue(banks.get(0));
        handleBankSelection();
    }

    private void handleBankSelection() {
        String bank = bankSelector.getValue();
        if (bank != null) {
            List<ChequeTemplate> templates = templateRepository.findByBank(bank);
            formatSelector.setItems(FXCollections.observableArrayList(templates));
            if (!templates.isEmpty()) {
                formatSelector.setValue(templates.get(0));
                selectedTemplate = templates.get(0);
            }
        }
    }

    private void updateWritingForm() {
        PurchaseEntity current = entriesToProcess.get(currentIndex);

        // Format step label: "Cheque 1 of 5" or localized version
        // The label format is "Cheque %d of %d" so we need both currentIndex and total
        stepLabel.setText(String.format("Cheque %d of %d", (currentIndex + 1), entriesToProcess.size()));

        chequeNoField.setText(current.getChequeNumber() != null ? current.getChequeNumber() : "");
        chequeDatePicker.setValue(current.getChequeDate() != null ? current.getChequeDate() : LocalDate.now());

        Vendor v = vendorRepository.findAllVendors().stream()
                .filter(ve -> ve.getId() == current.getVendorId()).findFirst().orElse(null);
        String vName = v != null ? v.getName() : "Unknown";

        entryInfoLabel.setText(String.format("Vendor: %s\nDate: %s\nBags: %d\nAmount: \u20B9%,.2f",
                vName,
                current.getEntryDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                current.getBags(), current.getGrandTotal()));

        // Update Preview
        updateLivePreview(current);
    }

    private void updateLivePreview(PurchaseEntity entity) {
        if (selectedTemplate == null) {
            previewOverlay.getChildren().clear();
            chequeBg.setImage(null);
            return;
        }

        previewOverlay.getChildren().clear();

        if (selectedTemplate.getBackgroundImagePath() != null && !selectedTemplate.getBackgroundImagePath().isEmpty()) {
            File f = new File(selectedTemplate.getBackgroundImagePath());
            if (f.exists()) {
                chequeBg.setImage(new Image(f.toURI().toString()));
            }
        }

        chequeBg.imageProperty().addListener((obs, oldV, newV) -> {
            if (newV != null)
                renderTemplateValues(selectedTemplate, entity);
        });

        if (chequeBg.getImage() != null) {
            renderTemplateValues(selectedTemplate, entity);
        }
    }

    private void renderTemplateValues(ChequeTemplate template, PurchaseEntity entity) {
        if (template == null)
            return;
        double w = chequeBg.getBoundsInParent().getWidth();
        double h = chequeBg.getBoundsInParent().getHeight();
        if (w == 0)
            w = 800; // Fallback

        Vendor v = vendorRepository.findAllVendors().stream().filter(ve -> ve.getId() == entity.getVendorId())
                .findFirst().orElse(null);
        String name = v != null ? v.getName() : "Unknown";

        Label previewDateLabel = createPreviewLabel(
                chequeDatePicker.getValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), template,
                template.getDateX(), template.getDateY(), w, h);
        Label previewNameLabel = createPreviewLabel(name, template, template.getPayeeX(), template.getPayeeY(), w, h);
        Label previewWordsLabel = createPreviewLabel(AmountToWordsConverter.convert(entity.getGrandTotal()), template,
                template.getAmountWordsX(), template.getAmountWordsY(), w, h);
        Label previewDigitsLabel = createPreviewLabel(String.format("₹%,.2f", entity.getGrandTotal()), template,
                template.getAmountDigitsX(), template.getAmountDigitsY(), w, h);

        previewOverlay.getChildren().clear();
        previewOverlay.getChildren().addAll(previewDateLabel, previewNameLabel, previewWordsLabel, previewDigitsLabel);

        // Add listeners for live updates from fields
        chequeDatePicker.valueProperty().addListener((o, ov, nv) -> {
            if (nv != null)
                previewDateLabel.setText(nv.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        });
    }

    private Label createPreviewLabel(String text, ChequeTemplate t, double xPct, double yPct, double w, double h) {
        Label lbl = new Label(text);
        lbl.setLayoutX(xPct * w);
        lbl.setLayoutY(yPct * h);
        lbl.setStyle(String.format(
                "-fx-font-family: '%s'; -fx-font-size: %d; -fx-text-fill: %s; -fx-font-weight: bold; -fx-background-color: transparent;",
                t.getFontFamily(), t.getFontSize(), t.getFontColor()));
        return lbl;
    }

    private void handleNext() {
        PurchaseEntity current = entriesToProcess.get(currentIndex);
        current.setChequeNumber(chequeNoField.getText());
        current.setChequeDate(chequeDatePicker.getValue());

        // Perform Actual Printing - WAIT FOR RESULT
        boolean printSuccess = printCheque(current);

        // CRITICAL: Only proceed if print succeeded
        if (!printSuccess) {
            // Error already shown by printCheque(), don't proceed to next
            return;
        }

        // Save progress (if not a manual custom cheque)
        if (!"CUSTOM".equals(current.getStatus())) {
            current.setStatus("PAID");
            purchaseRepository.save(current);
        }

        if (currentIndex < entriesToProcess.size() - 1) {
            currentIndex++;
            updateWritingForm();
        } else {
            // SUCCESS ALERT - Only shown if ALL prints succeeded
            new Alert(Alert.AlertType.INFORMATION, "All cheques processed and printed successfully!").show();
            selectionStep.setVisible(true);
            writingStep.setVisible(false);
            loadAllUnpaidEntries();
        }
    }

    /**
     * Print a single cheque
     * 
     * @return true if print succeeded, false if failed or cancelled
     */
    private boolean printCheque(PurchaseEntity entity) {
        Logger logger = LoggerFactory.getLogger(ChequeWizardView.class);
        logger.info("Attempting to print cheque for entity ID: {}", entity.getId());

        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null) {
            logger.error("Failed to create printer job. No printers available or driver issues.");
            new Alert(Alert.AlertType.ERROR,
                    "No printer found or unable to create print job. Please check your printer setup.").show();
            return false; // Print failed
        }

        // Check if a scene is available before showing print dialog
        if (getScene() == null || getScene().getWindow() == null) {
            logger.error("Cannot show print dialog: Scene or Window is null.");
            new Alert(Alert.AlertType.ERROR, "Application window not available for printing.").show();
            return false; // Print failed
        }

        if (job.showPrintDialog(getScene().getWindow())) {
            logger.info("Print dialog shown and accepted by user.");

            if (selectedTemplate == null) {
                logger.warn("No cheque template selected for printing entity ID: {}", entity.getId());
                new Alert(Alert.AlertType.WARNING, "No cheque template selected. Cannot print.").show();
                job.endJob(); // End job even if not printed
                return false; // Print failed
            }
            ChequeTemplate template = selectedTemplate;

            // Use a temporary Pane for printing to avoid UI artifacts
            Pane printPane = new Pane();
            // Set preferred size based on typical cheque dimensions or template's expected
            // print size
            // These values might need to be adjusted based on actual printer DPI and cheque
            // size
            printPane.setPrefSize(800, 300); // Example: 800px wide, 300px high at 96 DPI

            Vendor v = vendorRepository.findAllVendors().stream()
                    .filter(ve -> ve.getId() == entity.getVendorId())
                    .findFirst().orElse(null);
            String name = v != null ? v.getName() : "Unknown Vendor";

            // Use fixed dimensions for printing context, assuming template coordinates are
            // relative to this
            double w = 800;
            double h = 300;

            // Create labels for printing with character-tracked date
            String dateStr = entity.getChequeDate().format(DateTimeFormatter.ofPattern("ddMMyyyy"));
            HBox dateBox = DateCharacterTracker.createDateBoxes(
                    dateStr,
                    template.getFontSize(),
                    template.getFontColor());
            dateBox.setLayoutX(template.getDateX() * w);
            dateBox.setLayoutY(template.getDateY() * h);

            Label nameL = createPreviewLabel(name, template, template.getPayeeX(), template.getPayeeY(), w, h);
            Label wordsL = createPreviewLabel(AmountToWordsConverter.convert(entity.getGrandTotal()), template,
                    template.getAmountWordsX(), template.getAmountWordsY(), w, h);
            Label digitsL = createPreviewLabel(String.format("%,.2f /-", entity.getGrandTotal()), template,
                    template.getAmountDigitsX(), template.getAmountDigitsY(), w, h);

            printPane.getChildren().addAll(dateBox, nameL, wordsL, digitsL);

            logger.debug("Rendering print pane with cheque details for entity ID: {}", entity.getId());
            boolean success = job.printPage(printPane);
            if (success) {
                logger.info("Cheque for entity ID {} printed successfully.", entity.getId());
                job.endJob();
                return true; // ✅ SUCCESS
            } else {
                logger.error("Printing failed at the printer level for entity ID: {}. Printer returned false.",
                        entity.getId());
                new Alert(Alert.AlertType.ERROR,
                        "Printing failed at the printer level. Please check printer status and connection.").show();
                job.cancelJob(); // Attempt to cancel if printPage failed
                return false; // ❌ FAILED
            }
        } else {
            logger.info("Print dialog cancelled by user for entity ID: {}", entity.getId());
            job.cancelJob(); // User cancelled, so cancel the job
            return false; // ❌ CANCELLED
        }
    }

    private void handleBack() {
        if (currentIndex > 0) {
            currentIndex--;
            updateWritingForm();
        } else {
            selectionStep.setVisible(true);
            writingStep.setVisible(false);
        }
    }

    private void loadAllUnpaidEntries() {
        pendingEntries.clear();
        List<PurchaseEntity> entries = purchaseRepository.findAll().stream()
                .filter(p -> "UNPAID".equalsIgnoreCase(p.getStatus()))
                .collect(Collectors.toList());
        for (PurchaseEntity e : entries) {
            pendingEntries.add(new PurchaseSelectionModel(e));
        }
    }

    private void handleCustomCheque() {
        // Simple Input Dialog for Custom Cheque
        Dialog<PurchaseEntity> dialog = new Dialog<>();
        dialog.setTitle("Custom Cheque Creation");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        ComboBox<Vendor> vCombo = new ComboBox<>();
        vCombo.getItems().addAll(vendorRepository.findAllVendors());
        vCombo.setPromptText("Select Vendor");

        TextField amountField = new TextField();
        amountField.setTextFormatter(NumericTextFormatter.decimalOnly(2)); // CRITICAL: Financial precision
        amountField.setPromptText("Amount");

        DatePicker dp = new DatePicker(LocalDate.now());

        grid.add(new Label("Vendor:"), 0, 0);
        grid.add(vCombo, 1, 0);
        grid.add(new Label("Amount:"), 0, 1);
        grid.add(amountField, 1, 1);
        grid.add(new Label("Cheque Date:"), 0, 2);
        grid.add(dp, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                PurchaseEntity custom = new PurchaseEntity();
                custom.setVendorId(vCombo.getValue() != null ? vCombo.getValue().getId() : -1);
                custom.setGrandTotal(new java.math.BigDecimal(amountField.getText()));
                custom.setEntryDate(LocalDate.now());
                custom.setChequeDate(dp.getValue());
                custom.setStatus("CUSTOM"); // Mark as custom
                return custom;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(custom -> {
            entriesToProcess = new ArrayList<>();
            entriesToProcess.add(custom);
            currentIndex = 0;
            selectionStep.setVisible(false);
            writingStep.setVisible(true);
            loadBanksToSelector();
            resetWritingForm(); // Clear previous data before showing new entry
            updateWritingForm();
        });
    }

    private void loadPendingEntries(int vendorId) {
        pendingEntries.clear();
        List<PurchaseEntity> entries = purchaseRepository.findByVendorAndStatus(vendorId, "UNPAID");
        for (PurchaseEntity e : entries) {
            pendingEntries.add(new PurchaseSelectionModel(e));
        }
    }

    public static class PurchaseSelectionModel {
        private final PurchaseEntity entity;
        private final javafx.beans.property.BooleanProperty selected = new javafx.beans.property.SimpleBooleanProperty(
                false);

        public PurchaseSelectionModel(PurchaseEntity entity) {
            this.entity = entity;
        }

        public PurchaseEntity getEntity() {
            return entity;
        }

        public javafx.beans.property.BooleanProperty selectedProperty() {
            return selected;
        }

        public boolean isSelected() {
            return selected.get();
        }
    }

    @Override
    public void refresh() {
        loadAllUnpaidEntries();
    }
}
