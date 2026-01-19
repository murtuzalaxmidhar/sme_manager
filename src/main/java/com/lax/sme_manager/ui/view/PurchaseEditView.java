package com.lax.sme_manager.ui.view;

import com.lax.sme_manager.domain.Vendor;
import com.lax.sme_manager.repository.model.PurchaseEntity;
import com.lax.sme_manager.ui.component.UIComponents;
import com.lax.sme_manager.ui.component.UIStyles;
import com.lax.sme_manager.ui.theme.LaxTheme;
import com.lax.sme_manager.util.i18n.AppLabel;
import com.lax.sme_manager.util.VendorCache;
import com.lax.sme_manager.viewmodel.PurchaseEntryViewModel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

/**
 * Professional Single-Column Edit View for Purchase Entries.
 * Designed to look like an editable digital receipt.
 */
public class PurchaseEditView extends ScrollPane {

    private final PurchaseEntryViewModel viewModel;
    private final PurchaseEntity originalEntity;
    private Runnable onSave;
    private Runnable onCancel;

    public PurchaseEditView(PurchaseEntity entity, VendorCache vendorCache) {
        this.originalEntity = entity;
        this.viewModel = new PurchaseEntryViewModel(vendorCache);
        this.viewModel.setPurchaseData(entity);
        initializeUI();
    }

    public void setOnSave(Runnable onSave) {
        this.onSave = onSave;
    }

    public void setOnCancel(Runnable onCancel) {
        this.onCancel = onCancel;
    }

    private void initializeUI() {
        setFitToWidth(true);
        setStyle("-fx-background-color: #f8fafc;");
        setPadding(new Insets(24));

        VBox content = new VBox(0);
        content.setAlignment(Pos.TOP_CENTER);

        VBox paper = new VBox(0);
        paper.setMaxWidth(600);
        paper.setStyle(
                "-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #e2e8f0; -fx-border-radius: 8;");
        paper.setEffect(new DropShadow(15, Color.rgb(0, 0, 0, 0.08)));

        // Header
        VBox header = new VBox(8);
        header.setPadding(new Insets(32));
        header.setStyle("-fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0;");
        Label lblTitle = new Label("Edit Purchase Entry");
        lblTitle.setStyle(
                "-fx-font-size: 22; -fx-font-weight: bold; -fx-text-fill: " + LaxTheme.Colors.TEXT_PRIMARY + ";");
        header.getChildren().add(lblTitle);

        // Body
        VBox body = new VBox(20);
        body.setPadding(new Insets(32));

        // Date Picker
        DatePicker datePicker = new DatePicker();
        datePicker.valueProperty().bindBidirectional(viewModel.entryDate);
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(java.time.LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isAfter(java.time.LocalDate.now()));
            }
        });
        body.getChildren().add(createEditRow(AppLabel.LBL_PURCHASE_DATE.get(), datePicker));

        // Vendor (Display Only for edit usually, or editable if needed)
        TextField vendorField = new TextField(viewModel.selectedVendor.get().getName());
        vendorField.setEditable(false);
        vendorField.setStyle("-fx-background-color: #f1f5f9;");
        body.getChildren().add(createEditRow(AppLabel.LBL_VENDOR.get(), vendorField));

        // Bags
        TextField bagsField = new TextField();
        bagsField.textProperty().bindBidirectional(viewModel.bags);
        body.getChildren().add(createEditRow(AppLabel.LBL_BAGS.get(), bagsField));

        // Rate
        TextField rateField = new TextField();
        rateField.textProperty().bindBidirectional(viewModel.rate);
        body.getChildren().add(createEditRow(AppLabel.LBL_RATE.get(), rateField));

        // Weight
        TextField weightField = new TextField();
        weightField.textProperty().bindBidirectional(viewModel.weight);
        weightField.disableProperty().bind(viewModel.isLumpsum);
        body.getChildren().add(createEditRow(AppLabel.LBL_WEIGHT.get(), weightField));

        // Lumpsum
        CheckBox lumpsumCheck = new CheckBox();
        lumpsumCheck.selectedProperty().bindBidirectional(viewModel.isLumpsum);
        body.getChildren().add(createEditRow(AppLabel.LBL_LUMPSUM.get(), lumpsumCheck));

        // Mode
        ComboBox<String> modeCombo = new ComboBox<>();
        modeCombo.getItems().addAll("CHEQUE", "CASH", "BANK TRANSFER", "UPI");
        modeCombo.valueProperty().bindBidirectional(viewModel.paymentMode);
        body.getChildren().add(createEditRow(AppLabel.LBL_PAYMENT_MODE.get(), modeCombo));

        body.getChildren().add(new Separator());

        // Calculation Summary (Read Only)
        VBox calcPanel = new VBox(10);
        calcPanel.setPadding(new Insets(10, 0, 0, 0));
        calcPanel.getChildren().add(createSummaryRow("Base Amount", viewModel.baseAmountProperty()));
        calcPanel.getChildren().add(createSummaryRow("Grand Total", viewModel.grandTotalProperty()));
        body.getChildren().add(calcPanel);

        // Footer Actions
        HBox actions = new HBox(12);
        actions.setPadding(new Insets(32));
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle(LaxTheme.getButtonStyle(LaxTheme.ButtonType.SECONDARY));
        cancelBtn.setOnAction(e -> {
            if (onCancel != null)
                onCancel.run();
        });

        Button saveBtn = new Button("Save Changes");
        saveBtn.setStyle(LaxTheme.getButtonStyle(LaxTheme.ButtonType.PRIMARY));
        saveBtn.setOnAction(e -> {
            // Re-apply original ID to the entity for update
            // ViewModel creates a new entity usually on submit, we need to ensure update
            viewModel.submitEntry(); // This updates the DB
            if (onSave != null)
                onSave.run();
        });

        actions.getChildren().addAll(cancelBtn, saveBtn);

        paper.getChildren().addAll(header, body, actions);
        content.getChildren().add(paper);
        setContent(content);
    }

    private HBox createEditRow(String label, javafx.scene.Node control) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        Label lbl = new Label(label + ":");
        lbl.setPrefWidth(160);
        lbl.setStyle("-fx-text-fill: #64748b; -fx-font-weight: 500;");

        if (control instanceof Region) {
            ((Region) control).setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(control, Priority.ALWAYS);
        }

        row.getChildren().addAll(lbl, control);
        return row;
    }

    private HBox createSummaryRow(String label, javafx.beans.value.ObservableValue<Number> val) {
        HBox row = new HBox();
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: #64748b;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label valLbl = new Label();
        valLbl.setStyle("-fx-font-weight: bold;");
        valLbl.textProperty().bind(val.map(n -> String.format("₹%.2f", n.doubleValue())));
        row.getChildren().addAll(lbl, spacer, valLbl);
        return row;
    }
}
