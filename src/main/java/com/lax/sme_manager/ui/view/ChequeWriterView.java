package com.lax.sme_manager.ui.view;

import com.lax.sme_manager.dto.ChequeData;
import com.lax.sme_manager.ui.component.UIStyles;
import com.lax.sme_manager.ui.theme.LaxTheme;
import com.lax.sme_manager.util.IndianNumberToWords;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ChequeWriterView extends VBox {

    private final TextField payeeField;
    private final TextField amountField;
    private final DatePicker datePicker;
    private final CheckBox acPayeeCheck;
    private final Label amountWordsLabel;

    public ChequeWriterView() {
        setSpacing(20);
        setPadding(new Insets(30));
        setStyle("-fx-background-color: white;");

        // Header
        Label title = new Label("Cheque Writer");
        title.setStyle(UIStyles.getTitleStyle());

        // Form Container
        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(15);
        form.setMaxWidth(600);
        form.setAlignment(Pos.CENTER_LEFT);

        // Fields
        payeeField = new TextField();
        payeeField.setPromptText("Enter Payee Name");
        payeeField.setStyle(UIStyles.getInputFieldStyle());
        payeeField.setPrefWidth(300);

        amountField = new TextField();
        amountField.setPromptText("Enter Amount");
        amountField.setStyle(UIStyles.getInputFieldStyle());
        amountField.setPrefWidth(200);

        datePicker = new DatePicker(LocalDate.now());
        datePicker.setStyle(UIStyles.getDatePickerStyle());

        acPayeeCheck = new CheckBox("A/C Payee Only");
        acPayeeCheck.setSelected(true);
        acPayeeCheck.setStyle(UIStyles.getCheckBoxStyle());

        amountWordsLabel = new Label("");
        amountWordsLabel.setStyle("-fx-text-fill: #666; -fx-font-style: italic; -fx-wrap-text: true;");
        amountWordsLabel.setMaxWidth(400);

        // Layout
        // ROW 0: Payee
        Label lblPayee = new Label("Payee Name:");
        lblPayee.setStyle(UIStyles.getLabelStyle());
        form.add(lblPayee, 0, 0);
        form.add(payeeField, 1, 0);

        // ROW 1: Amount
        Label lblAmount = new Label("Amount (₹):");
        lblAmount.setStyle(UIStyles.getLabelStyle());
        form.add(lblAmount, 0, 1);
        form.add(amountField, 1, 1);

        // ROW 2: Date
        Label lblDate = new Label("Cheque Date:");
        lblDate.setStyle(UIStyles.getLabelStyle());
        form.add(lblDate, 0, 2);
        form.add(datePicker, 1, 2);

        // ROW 3: Checks
        form.add(acPayeeCheck, 1, 3);

        // ROW 4: Words
        Label lblWords = new Label("In Words:");
        lblWords.setStyle(UIStyles.getLabelStyle());
        form.add(lblWords, 0, 4);
        form.add(amountWordsLabel, 1, 4);

        // Live amount to words conversion
        amountField.textProperty().addListener((obs, oldVal, newVal) -> updateAmountWords());

        // Buttons
        Button btnPreview = new Button("Preview & Print");
        btnPreview.setStyle(LaxTheme.getButtonStyle(LaxTheme.ButtonType.PRIMARY));
        btnPreview.setOnAction(e -> handlePreview());

        Button btnClear = new Button("Clear");
        btnClear.setStyle(LaxTheme.getButtonStyle(LaxTheme.ButtonType.SECONDARY));
        btnClear.setOnAction(e -> clearForm());

        HBox actions = new HBox(15, btnPreview, btnClear);
        actions.setAlignment(Pos.CENTER_LEFT);
        actions.setPadding(new Insets(20, 0, 0, 0));

        getChildren().addAll(title, form, actions);
    }

    private void updateAmountWords() {
        try {
            if (amountField.getText().isEmpty()) {
                amountWordsLabel.setText("");
                return;
            }
            BigDecimal amount = new BigDecimal(amountField.getText());
            amountWordsLabel.setText(IndianNumberToWords.convert(amount));
        } catch (NumberFormatException e) {
            amountWordsLabel.setText("Invalid Amount");
        }
    }

    private void handlePreview() {
        try {
            String payee = payeeField.getText();
            BigDecimal amount = new BigDecimal(amountField.getText());
            LocalDate date = datePicker.getValue();
            boolean isAcPayee = acPayeeCheck.isSelected();

            ChequeData data = new ChequeData(payee, amount, date, isAcPayee, null);
            new ChequePreviewDialog(data).show();

        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR, "Please enter a valid amount.").show();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error: " + e.getMessage()).show();
        }
    }

    private void clearForm() {
        payeeField.clear();
        amountField.clear();
        datePicker.setValue(LocalDate.now());
        amountWordsLabel.setText("");
    }
}
