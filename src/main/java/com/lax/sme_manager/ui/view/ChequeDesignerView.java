package com.lax.sme_manager.ui.view;

import com.lax.sme_manager.domain.ChequeTemplate;
import com.lax.sme_manager.repository.ChequeTemplateRepository;
import com.lax.sme_manager.ui.component.UIStyles;
import com.lax.sme_manager.ui.theme.LaxTheme;
import com.lax.sme_manager.util.CoordinateMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * Professional Cheque Designer 2.0.
 * Supports Bank/Format management, vertical dragging, and sample cheque
 * fallback.
 */
public class ChequeDesignerView extends HBox {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChequeDesignerView.class);

    private final ChequeTemplateRepository repository = new ChequeTemplateRepository();
    private ChequeTemplate currentTemplate;

    // Left Sidebar Components
    private ComboBox<String> bankCombo;
    private ListView<ChequeTemplate> formatList;
    private final ObservableList<ChequeTemplate> templates = FXCollections.observableArrayList();

    // Designer Components
    private ImageView chequePreview;
    private Pane overlayPane;
    private StackPane designerStack;
    private Label dateLabel, payeeLabel, amountWordsLabel, amountDigitsLabel, signatureLabel;

    // Controls
    private Slider fsSlider;
    private ColorPicker colorPicker;
    private Label coordLabel; // Display MM coordinates

    // Coordinate mapping
    private CoordinateMapper mapper;

    public ChequeDesignerView() {
        this.currentTemplate = ChequeTemplate.createDefault();
        initializeUI();
        loadBanks();
    }

    private void initializeUI() {
        setPadding(new Insets(24));
        setSpacing(24);
        setStyle("-fx-background-color: transparent; -fx-background: " + LaxTheme.Colors.LIGHT_GRAY + ";");

        // LEFT: Sidebar
        VBox sidebar = new VBox(16);
        sidebar.setPrefWidth(300);
        sidebar.setPadding(new Insets(20));
        sidebar.setStyle(UIStyles.getCardStyle());

        Label sideTitle = new Label("Bank Formats");
        sideTitle.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");

        bankCombo = new ComboBox<>();
        bankCombo.setMaxWidth(Double.MAX_VALUE);
        bankCombo.setPromptText("Select Bank");
        bankCombo.setOnAction(e -> loadTemplatesForBank(bankCombo.getValue()));

        formatList = new ListView<>(templates);
        formatList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(ChequeTemplate item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getTemplateName());
            }
        });
        formatList.getSelectionModel().selectedItemProperty().addListener((obs, old, nv) -> {
            if (nv != null) {
                currentTemplate = nv;
                updatePreview();
            }
        });

        Button newBankBtn = new Button("+ New Bank");
        newBankBtn.setMaxWidth(Double.MAX_VALUE);
        newBankBtn.setOnAction(e -> handleAddBank());

        Button newFormatBtn = new Button("+ New Format");
        newFormatBtn.setMaxWidth(Double.MAX_VALUE);
        newFormatBtn.setOnAction(e -> handleAddFormat());

        sidebar.getChildren().addAll(sideTitle, bankCombo, newBankBtn, new Separator(), formatList, newFormatBtn);

        // RIGHT: Designer
        VBox designerArea = new VBox(20);
        HBox.setHgrow(designerArea, Priority.ALWAYS);

        // Header
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("🎨 Cheque Designer");
        title.setStyle(UIStyles.getTitleStyle());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button uploadBtn = new Button("Upload Image");
        uploadBtn.setStyle(LaxTheme.getButtonStyle(LaxTheme.ButtonType.SECONDARY));
        uploadBtn.setOnAction(e -> handleFileUpload());

        Button saveBtn = new Button("Save Changes");
        saveBtn.setStyle(LaxTheme.getButtonStyle(LaxTheme.ButtonType.PRIMARY));
        saveBtn.setOnAction(e -> handleSave());

        header.getChildren().addAll(title, spacer, uploadBtn, saveBtn);

        // Stack
        designerStack = new StackPane();
        designerStack.setStyle(
                "-fx-background-color: #f1f5f9; -fx-border-color: #e2e8f0; -fx-border-radius: 12; -fx-background-radius: 12;");
        designerStack.setPrefHeight(400);

        chequePreview = new ImageView();
        chequePreview.setPreserveRatio(true);
        chequePreview.setFitWidth(850);

        overlayPane = new Pane();
        overlayPane.setPickOnBounds(false);

        designerStack.getChildren().addAll(chequePreview, overlayPane);

        // Sliders
        HBox controls = new HBox(24);
        controls.setPadding(new Insets(16));
        controls.setStyle(UIStyles.getCardStyle());

        fsSlider = new Slider(8, 48, 16);
        fsSlider.valueProperty().addListener((o, ov, nv) -> {
            currentTemplate.setFontSize(nv.intValue());
            positionLabels();
        });

        colorPicker = new ColorPicker(javafx.scene.paint.Color.web("#1e293b"));
        colorPicker.setOnAction(e -> {
            String hex = String.format("#%02X%02X%02X",
                    (int) (colorPicker.getValue().getRed() * 255),
                    (int) (colorPicker.getValue().getGreen() * 255),
                    (int) (colorPicker.getValue().getBlue() * 255));
            currentTemplate.setFontColor(hex);
            positionLabels();
        });

        // Coordinate display label
        coordLabel = new Label("Position: (hover to see coordinates)");
        coordLabel.setStyle("-fx-font-size: 12; -fx-text-fill: " + LaxTheme.Colors.TEXT_SECONDARY +
                "; -fx-padding: 8; -fx-background-color: #f8fafc; -fx-border-radius: 4;");

        controls.getChildren().addAll(new Label("Font Size:"), fsSlider, new Label("Color:"), colorPicker,
                new Region(), coordLabel);

        designerArea.getChildren().addAll(header, designerStack, controls);

        getChildren().addAll(sidebar, designerArea);
    }

    private void loadBanks() {
        List<String> banks = repository.findAllBanks();
        bankCombo.setItems(FXCollections.observableArrayList(banks));
        if (!banks.isEmpty()) {
            bankCombo.setValue(banks.get(0));
            loadTemplatesForBank(banks.get(0));
        }
    }

    private void loadTemplatesForBank(String bank) {
        if (bank == null)
            return;
        templates.setAll(repository.findByBank(bank));
        if (!templates.isEmpty()) {
            formatList.getSelectionModel().select(0);
        }
    }

    private void updatePreview() {
        if (currentTemplate.getBackgroundImagePath() != null && !currentTemplate.getBackgroundImagePath().isEmpty()) {
            File f = new File(currentTemplate.getBackgroundImagePath());
            if (f.exists()) {
                chequePreview.setImage(new Image(f.toURI().toString()));
            } else {
                showSampleCheque();
            }
        } else {
            showSampleCheque();
        }

        overlayPane.getChildren().clear();

        dateLabel = createDraggableLabel("1 2 0 3 2 0 2 6", "DATE");
        payeeLabel = createDraggableLabel("Payee Name Goes Here", "PAYEE");
        amountWordsLabel = createDraggableLabel("Ten Thousand Five Hundred Only", "WORDS");
        amountDigitsLabel = createDraggableLabel("10,500.00", "DIGITS");
        signatureLabel = createDraggableLabel("AUTHORIZED SIGNATORY", "SIGNATURE");

        overlayPane.getChildren().addAll(dateLabel, payeeLabel, amountWordsLabel, amountDigitsLabel, signatureLabel);

        fsSlider.setValue(currentTemplate.getFontSize());
        try {
            colorPicker.setValue(javafx.scene.paint.Color.web(currentTemplate.getFontColor()));
        } catch (Exception e) {
        }

        if (chequePreview.getImage() != null) {
            positionLabels();
        }
    }

    private void showSampleCheque() {
        // Create a gray rectangle placeholder if no image
        chequePreview.setImage(null);
        designerStack.setStyle(
                "-fx-background-color: #f8fafc; -fx-border-color: #cbd5e1; -fx-border-style: dashed; -fx-border-width: 2; -fx-border-radius: 12;");
        Label placeholder = new Label(
                "S A M P L E   C H E Q U E   O U T L I N E\n(Upload your bank's cheque image for exact positioning)");
        placeholder.setStyle("-fx-text-fill: #94a3b8; -fx-font-weight: bold; -fx-text-alignment: center;");
        designerStack.getChildren().add(placeholder);

        // Ensure overlay is still on top
        designerStack.getChildren().remove(overlayPane);
        designerStack.getChildren().add(overlayPane);
    }

    private void positionLabels() {
        double w = designerStack.getWidth();
        double h = designerStack.getHeight();
        if (w == 0)
            w = 850;
        if (h == 0)
            h = 400;

        updateLabelPos(dateLabel, currentTemplate.getDateX(), currentTemplate.getDateY(), w, h);
        updateLabelPos(payeeLabel, currentTemplate.getPayeeX(), currentTemplate.getPayeeY(), w, h);
        updateLabelPos(amountWordsLabel, currentTemplate.getAmountWordsX(), currentTemplate.getAmountWordsY(), w, h);
        updateLabelPos(amountDigitsLabel, currentTemplate.getAmountDigitsX(), currentTemplate.getAmountDigitsY(), w, h);
        updateLabelPos(signatureLabel, currentTemplate.getSignatureX(), currentTemplate.getSignatureY(), w, h);
    }

    private void updateLabelPos(Label lbl, double xPct, double yPct, double parentW, double parentH) {
        lbl.setLayoutX(xPct * parentW);
        lbl.setLayoutY(yPct * parentH);
        lbl.setStyle("-fx-font-family: 'Inter'; -fx-font-size: " + currentTemplate.getFontSize() +
                "; -fx-text-fill: " + currentTemplate.getFontColor() +
                "; -fx-font-weight: bold; -fx-padding: 5; -fx-background-color: rgba(255,255,255,0.7); -fx-border-color: #3b82f6; -fx-border-radius: 4;");
    }

    private Label createDraggableLabel(String text, String type) {
        Label lbl = new Label(text);

        lbl.setOnMouseDragged(e -> {
            double w = designerStack.getWidth();
            double h = designerStack.getHeight();

            // Proper relative positioning
            double newX = e.getX() + lbl.getLayoutX() - 10; // Simple drag delta
            double newY = e.getY() + lbl.getLayoutY() - 10;

            if (newX < 0)
                newX = 0;
            if (newY < 0)
                newY = 0;
            if (newX > w - 100)
                newX = w - 100;
            if (newY > h - 30)
                newY = h - 30;

            lbl.setLayoutX(newX);
            lbl.setLayoutY(newY);

            // Calculate percentage for backward compatibility
            double pctX = newX / w;
            double pctY = newY / h;

            // Convert to MM coordinates if mapper exists
            Double mmX = null, mmY = null;
            if (mapper != null) {
                javafx.geometry.Point2D physical = mapper.screenToPhysical(newX, newY);
                mmX = physical.getX();
                mmY = physical.getY();

                // Update coordinate display
                coordLabel.setText(String.format("Position: %.1fmm, %.1fmm (%.0fpx, %.0fpx)",
                        mmX, mmY, newX, newY));
            }

            switch (type) {
                case "DATE" -> {
                    currentTemplate.setDateX(pctX);
                    currentTemplate.setDateY(pctY);
                    if (mmX != null) {
                        currentTemplate.setDateXMm(mmX);
                        currentTemplate.setDateYMm(mmY);
                    }
                }
                case "PAYEE" -> {
                    currentTemplate.setPayeeX(pctX);
                    currentTemplate.setPayeeY(pctY);
                    if (mmX != null) {
                        currentTemplate.setPayeeXMm(mmX);
                        currentTemplate.setPayeeYMm(mmY);
                    }
                }
                case "WORDS" -> {
                    currentTemplate.setAmountWordsX(pctX);
                    currentTemplate.setAmountWordsY(pctY);
                    if (mmX != null) {
                        currentTemplate.setAmountWordsXMm(mmX);
                        currentTemplate.setAmountWordsYMm(mmY);
                    }
                }
                case "DIGITS" -> {
                    currentTemplate.setAmountDigitsX(pctX);
                    currentTemplate.setAmountDigitsY(pctY);
                    if (mmX != null) {
                        currentTemplate.setAmountDigitsXMm(mmX);
                        currentTemplate.setAmountDigitsYMm(mmY);
                    }
                }
                case "SIGNATURE" -> {
                    currentTemplate.setSignatureX(pctX);
                    currentTemplate.setSignatureY(pctY);
                    if (mmX != null) {
                        currentTemplate.setSignatureXMm(mmX);
                        currentTemplate.setSignatureYMm(mmY);
                    }
                }
            }
        });

        return lbl;
    }

    private void handleAddBank() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Bank");
        dialog.setHeaderText("Enter Bank Name");
        dialog.showAndWait().ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                bankCombo.getItems().add(name);
                bankCombo.setValue(name);
                templates.clear();
            }
        });
    }

    private void handleAddFormat() {
        if (bankCombo.getValue() == null) {
            new Alert(Alert.AlertType.WARNING, "Please select/add a bank first.").show();
            return;
        }
        TextInputDialog dialog = new TextInputDialog("Standard Format");
        dialog.setTitle("New Format");
        dialog.setHeaderText("Enter Format Name for " + bankCombo.getValue());
        dialog.showAndWait().ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                ChequeTemplate nt = ChequeTemplate.createDefault();
                nt.setBankName(bankCombo.getValue());
                nt.setTemplateName(name);
                nt.setId(null); // Force new
                currentTemplate = nt;
                updatePreview();
                handleSave();
            }
        });
    }

    private void handleFileUpload() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png"));
        File file = chooser.showOpenDialog(getScene().getWindow());
        if (file != null) {
            try {
                File dir = com.lax.sme_manager.util.DatabaseManager.getAppDataDir().resolve("banks").toFile();
                if (!dir.exists())
                    dir.mkdirs();

                String fileName = bankCombo.getValue() + "_" + currentTemplate.getTemplateName() + ".jpg";
                File dest = new File(dir, fileName.replaceAll("\\s+", "_"));

                Files.copy(file.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                currentTemplate.setBackgroundImagePath(dest.getAbsolutePath());

                // Initialize CoordinateMapper with uploaded image dimensions
                Image uploadedImage = new Image(dest.toURI().toString());
                mapper = new CoordinateMapper(uploadedImage.getWidth(), uploadedImage.getHeight());

                // Update status
                coordLabel.setText(String.format("Image: %.0f×%.0f px | Scale: %.4f mm/px",
                        uploadedImage.getWidth(), uploadedImage.getHeight(), mapper.getScaleFactor()));

                updatePreview();
            } catch (Exception e) {
                LOGGER.error("Image upload failed", e);
                coordLabel.setText("Image upload failed");
            }
        }
    }

    private void handleSave() {
        if (bankCombo.getValue() == null)
            return;
        repository.save(currentTemplate);
        loadTemplatesForBank(bankCombo.getValue());
        new Alert(Alert.AlertType.INFORMATION, "Format saved successfully!").show();
    }
}
