package com.lax.sme_manager.ui.view;

import com.lax.sme_manager.domain.Vendor;
import com.lax.sme_manager.repository.VendorRepository;
import com.lax.sme_manager.repository.model.VendorEntity;
import com.lax.sme_manager.ui.theme.LaxTheme;
import com.lax.sme_manager.util.i18n.AppLabel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Vendor Management View - Full CRUD operations for vendors
 * Features:
 * - Paginated table with all vendors
 * - Search/Filter by name or contact
 * - Add/Edit/Soft Delete operations
 */
public class VendorManagementView extends VBox {
    private static final Logger LOGGER = LoggerFactory.getLogger(VendorManagementView.class);

    private final VendorRepository repository;
    private final ObservableList<VendorEntity> vendors = FXCollections.observableArrayList();

    private TableView<VendorEntity> vendorTable;
    private TextField searchField;
    private Pagination pagination;
    private static final int ROWS_PER_PAGE = 20;

    public VendorManagementView() {
        this.repository = new VendorRepository();
        initializeUI();
        loadVendors();
    }

    private void initializeUI() {
        setPadding(new Insets(LaxTheme.Layout.MAIN_CONTAINER_PADDING));
        setSpacing(LaxTheme.Spacing.SPACE_24);
        setStyle("-fx-background-color: transparent; -fx-background: " + LaxTheme.Colors.LIGHT_GRAY + ";");

        // Header section
        Label title = new Label("👥 Vendor Management");
        title.setStyle(String.format(
                "-fx-font-size: %d; -fx-font-weight: %d; -fx-text-fill: %s;",
                LaxTheme.Typography.FONT_SIZE_2XL,
                LaxTheme.Typography.WEIGHT_BOLD,
                LaxTheme.Colors.TEXT_PRIMARY));

        // Controls row
        HBox controls = new HBox(LaxTheme.Spacing.SPACE_16);
        controls.setAlignment(Pos.CENTER_LEFT);

        searchField = new TextField();
        searchField.setPromptText("Search vendors by name...");
        searchField.setPrefWidth(300);
        searchField.textProperty().addListener((obs, old, val) -> filterVendors(val));

        Button addButton = new Button("➕ New Vendor");
        addButton.setStyle(LaxTheme.getButtonStyle(LaxTheme.ButtonType.PRIMARY));
        addButton.setPrefHeight(40);
        addButton.setOnAction(e -> handleAddVendor());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        controls.getChildren().addAll(searchField, spacer, addButton);

        // Vendor table
        vendorTable = createVendorTable();
        VBox.setVgrow(vendorTable, Priority.ALWAYS);

        getChildren().addAll(title, controls, vendorTable);
    }

    private TableView<VendorEntity> createVendorTable() {
        TableView<VendorEntity> table = new TableView<>(vendors);
        table.setStyle(LaxTheme.getCardStyle());
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // ID Column
        TableColumn<VendorEntity, Number> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(d -> new javafx.beans.property.SimpleIntegerProperty(d.getValue().getId()));
        idCol.setPrefWidth(60);

        // Name Column
        TableColumn<VendorEntity, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getName()));
        nameCol.setPrefWidth(200);

        // Contact Person Column
        TableColumn<VendorEntity, String> contactCol = new TableColumn<>("Contact Person");
        contactCol.setCellValueFactory(
                d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getContactPerson()));

        // Phone Column
        TableColumn<VendorEntity, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getPhone()));

        // Email Column
        TableColumn<VendorEntity, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getEmail()));

        // Actions Column
        TableColumn<VendorEntity, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(150);
        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");

            {
                editBtn.setStyle("-fx-background-color: " + LaxTheme.Colors.PRIMARY_TEAL
                        + "; -fx-text-fill: white; -fx-cursor: hand;");
                deleteBtn.setStyle(
                        "-fx-background-color: " + LaxTheme.Colors.ERROR + "; -fx-text-fill: white; -fx-cursor: hand;");

                editBtn.setOnAction(e -> {
                    VendorEntity vendor = getTableView().getItems().get(getIndex());
                    handleEditVendor(vendor);
                });

                deleteBtn.setOnAction(e -> {
                    VendorEntity vendor = getTableView().getItems().get(getIndex());
                    handleDeleteVendor(vendor);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(8, editBtn, deleteBtn);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
            }
        });

        table.getColumns().addAll(idCol, nameCol, contactCol, phoneCol, emailCol, actionsCol);
        return table;
    }

    private void loadVendors() {
        try {
            List<VendorEntity> allVendors = repository.findAll();
            vendors.setAll(allVendors);
            LOGGER.info("Loaded {} vendors", allVendors.size());
        } catch (Exception e) {
            LOGGER.error("Failed to load vendors", e);
            new Alert(Alert.AlertType.ERROR, "Failed to load vendors: " + e.getMessage()).show();
        }
    }

    private void filterVendors(String query) {
        if (query == null || query.trim().isEmpty()) {
            loadVendors();
            return;
        }

        try {
            List<VendorEntity> allVendors = repository.findAll();
            String lowerQuery = query.toLowerCase();
            List<VendorEntity> filtered = allVendors.stream()
                    .filter(v -> v.getName().toLowerCase().contains(lowerQuery) ||
                            (v.getContactPerson() != null && v.getContactPerson().toLowerCase().contains(lowerQuery)))
                    .toList();
            vendors.setAll(filtered);
            LOGGER.debug("Filtered to {} vendors matching '{}'", filtered.size(), query);
        } catch (Exception e) {
            LOGGER.error("Failed to filter vendors", e);
        }
    }

    private void handleAddVendor() {
        VendorEditDialog dialog = new VendorEditDialog(null);
        Optional<VendorEntity> result = dialog.showAndWait();
        result.ifPresent(vendor -> {
            try {
                repository.insert(vendor);
                loadVendors();
                new Alert(Alert.AlertType.INFORMATION, "Vendor added successfully!").show();
                LOGGER.info("Added new vendor: {}", vendor.getName());
            } catch (Exception e) {
                LOGGER.error("Failed to add vendor", e);
                new Alert(Alert.AlertType.ERROR, "Failed to add vendor: " + e.getMessage()).show();
            }
        });
    }

    private void handleEditVendor(VendorEntity vendor) {
        VendorEditDialog dialog = new VendorEditDialog(vendor);
        Optional<VendorEntity> result = dialog.showAndWait();
        result.ifPresent(updated -> {
            try {
                repository.update(updated);
                loadVendors();
                new Alert(Alert.AlertType.INFORMATION, "Vendor updated successfully!").show();
                LOGGER.info("Updated vendor ID: {}", updated.getId());
            } catch (Exception e) {
                LOGGER.error("Failed to update vendor", e);
                new Alert(Alert.AlertType.ERROR, "Failed to update vendor: " + e.getMessage()).show();
            }
        });
    }

    private void handleDeleteVendor(VendorEntity vendor) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to delete vendor '" + vendor.getName() + "'?\n\n" +
                        "This will not remove the vendor from the database, just mark as deleted.",
                ButtonType.YES, ButtonType.NO);
        confirmation.setTitle("Confirm Delete");
        confirmation.setHeaderText("Soft Delete Vendor");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    repository.softDelete(vendor.getId());
                    loadVendors();
                    new Alert(Alert.AlertType.INFORMATION, "Vendor deleted successfully!").show();
                    LOGGER.info("Soft-deleted vendor ID: {}", vendor.getId());
                } catch (Exception e) {
                    LOGGER.error("Failed to delete vendor", e);
                    new Alert(Alert.AlertType.ERROR, "Failed to delete vendor: " + e.getMessage()).show();
                }
            }
        });
    }
}
