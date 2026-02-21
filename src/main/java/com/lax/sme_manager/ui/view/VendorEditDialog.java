package com.lax.sme_manager.ui.view;

import com.lax.sme_manager.repository.model.VendorEntity;
import com.lax.sme_manager.ui.component.AlertUtils;
import com.lax.sme_manager.ui.theme.LaxTheme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDateTime;

/**
 * Modern, premium modal dialog for adding/editing vendors.
 * Features frosted glass header, spacious form, and styled buttons.
 */
public class VendorEditDialog extends Dialog<VendorEntity> {

    private final VendorEntity existingVendor;

    private TextField nameField;
    private TextField contactPersonField;
    private TextField phoneField;
    private TextField emailField;
    private TextField defaultAmountField;
    private TextArea addressArea;
    private TextArea notesArea;

    public VendorEditDialog(VendorEntity vendor) {
        this.existingVendor = vendor;

        setTitle(vendor == null ? "Add New Vendor" : "Edit Vendor");
        setHeaderText(vendor == null ? "Register a new vendor to your network"
                : "Update information for " + vendor.getName());

        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Apply modern CSS styling
        AlertUtils.styleDialog(this);

        // --- Premium Form Layout ---
        VBox formContainer = new VBox(20);
        formContainer.setPadding(new Insets(24, 32, 24, 32));
        formContainer.setPrefWidth(600);
        formContainer.setStyle("-fx-background-color: #ffffff;");

        // Section: Basic Info
        Label basicHeader = createSectionHeader("Basic Information");
        GridPane basicGrid = createFormGrid();

        nameField = createStyledField("Enter full legal name");
        contactPersonField = createStyledField("Primary contact person name");

        addFormRow(basicGrid, 0, "VENDOR NAME *", nameField);
        addFormRow(basicGrid, 1, "CONTACT PERSON", contactPersonField);

        // Section: Contact Details
        Label contactHeader = createSectionHeader("Contact Details");
        GridPane contactGrid = createFormGrid();

        phoneField = createStyledField("+91 00000 00000");
        emailField = createStyledField("vendor@example.com");

        addFormRow(contactGrid, 0, "PHONE NUMBER", phoneField);
        addFormRow(contactGrid, 1, "EMAIL ADDRESS", emailField);

        // Section: Address & Amount
        Label addressHeader = createSectionHeader("Address & Defaults");
        GridPane addressGrid = createFormGrid();

        addressArea = new TextArea();
        addressArea.setPromptText("Enter full physical or mailing address");
        addressArea.setPrefRowCount(2);
        addressArea.setStyle(getTextAreaStyle());

        defaultAmountField = createStyledField("Auto-populate amount in Cheque Writer");

        addFormRow(addressGrid, 0, "OFFICE ADDRESS", addressArea);
        addFormRow(addressGrid, 1, "DEFAULT AMOUNT (\u20B9)", defaultAmountField);

        // Section: Notes
        Label notesHeader = createSectionHeader("Internal Notes");
        GridPane notesGrid = createFormGrid();

        notesArea = new TextArea();
        notesArea.setPromptText("Bank details, terms, or internal remarks");
        notesArea.setPrefRowCount(2);
        notesArea.setStyle(getTextAreaStyle());

        addFormRow(notesGrid, 0, "NOTES", notesArea);

        formContainer.getChildren().addAll(
                basicHeader, basicGrid,
                createSeparator(),
                contactHeader, contactGrid,
                createSeparator(),
                addressHeader, addressGrid,
                createSeparator(),
                notesHeader, notesGrid);

        ScrollPane scrollPane = new ScrollPane(formContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(500);
        scrollPane.setStyle("-fx-background-color: transparent;");

        getDialogPane().setContent(scrollPane);

        // Populate fields if editing
        if (vendor != null) {
            nameField.setText(vendor.getName());
            contactPersonField.setText(vendor.getContactPerson());
            phoneField.setText(vendor.getPhone());
            emailField.setText(vendor.getEmail());
            addressArea.setText(vendor.getAddress());
            defaultAmountField
                    .setText(vendor.getDefaultAmount() != null ? vendor.getDefaultAmount().toPlainString() : "");
            notesArea.setText(vendor.getNotes());
        }

        // Validation: name required
        Button okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
        okButton.setText(vendor == null ? "Add Vendor" : "Save Changes");
        okButton.setDisable(vendor == null);

        nameField.textProperty().addListener((obs, old, val) -> {
            okButton.setDisable(val == null || val.trim().isEmpty());
        });

        // Result converter
        setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                VendorEntity result;
                if (existingVendor != null) {
                    result = existingVendor;
                } else {
                    result = new VendorEntity();
                    result.setCreatedAt(LocalDateTime.now());
                }

                result.setName(nameField.getText().trim());
                result.setContactPerson(contactPersonField.getText().trim());
                result.setPhone(phoneField.getText().trim());
                result.setEmail(emailField.getText().trim());
                result.setAddress(addressArea.getText().trim());
                try {
                    String amt = defaultAmountField.getText().trim();
                    result.setDefaultAmount(amt.isEmpty() ? java.math.BigDecimal.ZERO : new java.math.BigDecimal(amt));
                } catch (Exception e) {
                    result.setDefaultAmount(java.math.BigDecimal.ZERO);
                }
                result.setNotes(notesArea.getText().trim());
                result.setUpdatedAt(LocalDateTime.now());

                return result;
            }
            return null;
        });
    }

    private Label createSectionHeader(String text) {
        Label header = new Label(text);
        header.setStyle(
                "-fx-font-size: 14px; -fx-font-weight: 700; -fx-text-fill: #0f172a; " +
                        "-fx-padding: 4 0 0 0;");
        return header;
    }

    private GridPane createFormGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(14);
        return grid;
    }

    private TextField createStyledField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setStyle(
                "-fx-pref-height: 40px; -fx-background-color: #f8fafc; " +
                        "-fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-background-radius: 8; " +
                        "-fx-padding: 8 14; -fx-font-size: 13px;");
        field.focusedProperty().addListener((obs, old, focused) -> {
            if (focused) {
                field.setStyle(
                        "-fx-pref-height: 40px; -fx-background-color: #ffffff; " +
                                "-fx-border-color: #0d9488; -fx-border-radius: 8; -fx-background-radius: 8; " +
                                "-fx-border-width: 1.5; -fx-padding: 8 14; -fx-font-size: 13px; " +
                                "-fx-effect: dropshadow(gaussian, rgba(13,148,136,0.15), 8, 0, 0, 0);");
            } else {
                field.setStyle(
                        "-fx-pref-height: 40px; -fx-background-color: #f8fafc; " +
                                "-fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-background-radius: 8; " +
                                "-fx-padding: 8 14; -fx-font-size: 13px;");
            }
        });
        GridPane.setHgrow(field, Priority.ALWAYS);
        return field;
    }

    private String getTextAreaStyle() {
        return "-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; " +
                "-fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 13px; " +
                "-fx-padding: 8 14;";
    }

    private void addFormRow(GridPane grid, int row, String labelText, javafx.scene.Node field) {
        Label label = new Label(labelText);
        label.setStyle(
                "-fx-font-weight: 700; -fx-text-fill: #64748b; -fx-font-size: 11px; " +
                        "-fx-letter-spacing: 0.5;");
        label.setMinWidth(140);
        grid.add(label, 0, row);
        grid.add(field, 1, row);
        GridPane.setHgrow(field, Priority.ALWAYS);
    }

    private Separator createSeparator() {
        Separator sep = new Separator();
        sep.setStyle("-fx-padding: 4 0;");
        return sep;
    }
}
