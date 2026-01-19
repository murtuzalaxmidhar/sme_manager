package com.lax.sme_manager.ui.view;

import com.lax.sme_manager.repository.model.VendorEntity;
import com.lax.sme_manager.ui.theme.LaxTheme;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.time.LocalDateTime;

/**
 * Modal dialog for adding/editing vendors
 */
public class VendorEditDialog extends Dialog<VendorEntity> {

    private final VendorEntity existingVendor;

    private TextField nameField;
    private TextField contactPersonField;
    private TextField phoneField;
    private TextField emailField;
    private TextArea addressArea;
    private TextArea notesArea;

    public VendorEditDialog(VendorEntity vendor) {
        this.existingVendor = vendor;

        setTitle(vendor == null ? "Add New Vendor" : "Edit Vendor");
        setHeaderText(vendor == null ? "Enter vendor details" : "Update vendor: " + vendor.getName());

        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(LaxTheme.Layout.SUB_PANEL_PADDING));
        grid.setPrefWidth(600);

        // Name (required)
        Label nameLbl = new Label("Name *:");
        nameLbl.setStyle("-fx-font-weight: " + LaxTheme.Typography.WEIGHT_SEMIBOLD + ";");
        nameField = new TextField();
        nameField.setPromptText("Vendor name (required)");
        GridPane.setHgrow(nameField, Priority.ALWAYS);

        // Contact Person
        Label contactPersonLbl = new Label("Contact Person:");
        contactPersonField = new TextField();
        contactPersonField.setPromptText("Primary contact name");
        GridPane.setHgrow(contactPersonField, Priority.ALWAYS);

        // Phone
        Label phoneLbl = new Label("Phone:");
        phoneField = new TextField();
        phoneField.setPromptText("Contact phone number");
        GridPane.setHgrow(phoneField, Priority.ALWAYS);

        // Email
        Label emailLbl = new Label("Email:");
        emailField = new TextField();
        emailField.setPromptText("Email address");
        GridPane.setHgrow(emailField, Priority.ALWAYS);

        // Address
        Label addressLbl = new Label("Address:");
        addressArea = new TextArea();
        addressArea.setPromptText("Full address");
        addressArea.setPrefRowCount(3);
        GridPane.setHgrow(addressArea, Priority.ALWAYS);

        // Notes
        Label notesLbl = new Label("Notes:");
        notesArea = new TextArea();
        notesArea.setPromptText("Additional notes");
        notesArea.setPrefRowCount(3);
        GridPane.setHgrow(notesArea, Priority.ALWAYS);

        // Add all to grid
        int row = 0;
        grid.add(nameLbl, 0, row);
        grid.add(nameField, 1, row++);

        grid.add(contactPersonLbl, 0, row);
        grid.add(contactPersonField, 1, row++);

        grid.add(phoneLbl, 0, row);
        grid.add(phoneField, 1, row++);

        grid.add(emailLbl, 0, row);
        grid.add(emailField, 1, row++);

        grid.add(addressLbl, 0, row);
        grid.add(addressArea, 1, row++);

        grid.add(notesLbl, 0, row);
        grid.add(notesArea, 1, row);

        getDialogPane().setContent(grid);

        // Populate fields if editing
        if (vendor != null) {
            nameField.setText(vendor.getName());
            contactPersonField.setText(vendor.getContactPerson());
            phoneField.setText(vendor.getPhone());
            emailField.setText(vendor.getEmail());
            addressArea.setText(vendor.getAddress());
            notesArea.setText(vendor.getNotes());
        }

        // Validation: name is required
        Button okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDisable(vendor == null); // Disable for new vendor until name entered

        nameField.textProperty().addListener((obs, old, val) -> {
            okButton.setDisable(val == null || val.trim().isEmpty());
        });

        // Result converter
        setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                VendorEntity result;
                if (existingVendor != null) {
                    // Update existing
                    result = existingVendor;
                } else {
                    // Create new
                    result = new VendorEntity();
                    result.setCreatedAt(LocalDateTime.now());
                }

                result.setName(nameField.getText().trim());
                result.setContactPerson(contactPersonField.getText().trim());
                result.setPhone(phoneField.getText().trim());
                result.setEmail(emailField.getText().trim());
                result.setAddress(addressArea.getText().trim());
                result.setNotes(notesArea.getText().trim());
                result.setUpdatedAt(LocalDateTime.now());

                return result;
            }
            return null;
        });
    }
}
