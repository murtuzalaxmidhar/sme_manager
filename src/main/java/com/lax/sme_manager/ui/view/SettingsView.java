package com.lax.sme_manager.ui.view;

import com.lax.sme_manager.repository.SignatureRepository;
import com.lax.sme_manager.repository.ChequeConfigRepository;
import com.lax.sme_manager.repository.model.SignatureConfig;
import com.lax.sme_manager.ui.component.AlertUtils;
import com.lax.sme_manager.repository.model.ChequeConfig;
import com.lax.sme_manager.ui.component.UIStyles;
import com.lax.sme_manager.ui.theme.LaxTheme;
import com.lax.sme_manager.domain.User;
import com.lax.sme_manager.repository.UserRepository;
import com.lax.sme_manager.util.ImageUtils;
import com.lax.sme_manager.util.BackupService;
import com.lax.sme_manager.util.PasswordManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;

public class SettingsView extends VBox {

    private final SignatureRepository sigRepo = new SignatureRepository();
    private final ChequeConfigRepository configRepo = new ChequeConfigRepository();
    private final VBox signatureList = new VBox(10);
    private final ImageView previewView = new ImageView();
    private SignatureConfig activeConfig;
    private ChequeConfig chequeConfig;
    private final User currentUser;
    private final UserRepository userRepo = new UserRepository();

    public SettingsView(User currentUser) {
        this.currentUser = currentUser;
        this.chequeConfig = configRepo.getConfig();
        if (this.chequeConfig == null)
            this.chequeConfig = new ChequeConfig();
        initializeUI();
        loadSignatures();
    }

    private void initializeUI() {
        setPadding(new Insets(25));
        setSpacing(32);
        setStyle("-fx-background-color: transparent;");

        Label titleLabel = new Label("⚙️ Settings");
        titleLabel.setStyle(UIStyles.getTitleStyle());
        // getChildren().add(titleLabel); // Removed duplicate add, combined at the end

        // Create all sections
        VBox profileSection = createUserProfileSection();
        VBox userManagementSection = (currentUser.isAdmin()) ? createUserManagementSection() : null;
        VBox maintenanceSection = (currentUser.isAdmin()) ? createMaintenanceSection() : null;

        // --- 1. GENERAL SETTINGS (Restored Language) ---
        VBox generalSection = createSection("🔧 General & Localization");
        GridPane generalGrid = new GridPane();
        generalGrid.setHgap(20);
        generalGrid.setVgap(15);

        generalGrid.add(new Label("Application Language:"), 0, 0);
        ComboBox<com.lax.sme_manager.util.i18n.LanguageMode> langCombo = new ComboBox<>();
        langCombo.getItems().addAll(com.lax.sme_manager.util.i18n.LanguageMode.values());
        langCombo.setValue(com.lax.sme_manager.util.i18n.LanguageManager.getInstance().getMode());
        langCombo.getSelectionModel().selectedItemProperty().addListener((obs, old, n) -> {
            if (n != null)
                com.lax.sme_manager.util.i18n.LanguageManager.getInstance().setMode(n);
        });
        generalGrid.add(langCombo, 1, 0);

        generalGrid.add(new Label("Database Path:"), 0, 1);
        TextField dbPath = new TextField("data/lax_manager.db");
        dbPath.setEditable(false);
        dbPath.setPrefWidth(300);
        generalGrid.add(dbPath, 1, 1);

        Button btnBackup = new Button("📦 Create Backup Now");
        btnBackup.setStyle(LaxTheme.getButtonStyle(LaxTheme.ButtonType.SECONDARY));
        btnBackup.setOnAction(e -> {
            new BackupService().performBackup();
            AlertUtils.showInfo("Information", "Manual Backup Successful");
        });
        generalGrid.add(btnBackup, 1, 2);
        generalSection.getChildren().add(generalGrid);

        // --- 2. CHEQUE CONFIGURATION ---
        VBox chequeSection = createSection("🏦 Cheque Print Defaults");
        GridPane chequeGrid = new GridPane();
        chequeGrid.setHgap(20);
        chequeGrid.setVgap(15);

        chequeGrid.add(new Label("Default Bank Name:"), 0, 0);
        TextField bankName = new TextField(chequeConfig.getBankName());
        bankName.setPrefWidth(250);
        chequeGrid.add(bankName, 1, 0);

        chequeGrid.add(new Label("Default Font Size:"), 0, 1);
        Slider fontSlider = new Slider(8, 24, chequeConfig.getFontSize() > 0 ? chequeConfig.getFontSize() : 12);
        fontSlider.setShowTickLabels(true);
        fontSlider.setShowTickMarks(true);
        chequeGrid.add(fontSlider, 1, 1);

        Button btnSaveCheque = new Button("Save Cheque Defaults");
        btnSaveCheque.setStyle(LaxTheme.getButtonStyle(LaxTheme.ButtonType.PRIMARY));
        btnSaveCheque.setOnAction(e -> {
            chequeConfig.setBankName(bankName.getText());
            chequeConfig.setFontSize((int) fontSlider.getValue());
            configRepo.saveConfig(chequeConfig);
            AlertUtils.showInfo("Settings Saved", "Cheque defaults saved.");
        });
        chequeGrid.add(btnSaveCheque, 1, 2);
        chequeSection.getChildren().add(chequeGrid);

        // --- 3. SIGNATURE SECTION ---
        VBox sigSection = createSection("🖋️ Digital Signature Management");
        Label rbiNote = new Label(
                "RBI Compliance: Ensure scanned signatures look 'pen-authentic'. Check opacity and transparency.");
        rbiNote.setStyle(
                "-fx-text-fill: #b45309; -fx-background-color: #fffbeb; -fx-padding: 10; -fx-border-color: #f59e0b; -fx-font-size: 11px;");

        HBox sigLayout = new HBox(30);
        VBox left = new VBox(15);
        Button btnAdd = new Button("+ Add New Signature");
        btnAdd.setStyle(LaxTheme.getButtonStyle(LaxTheme.ButtonType.PRIMARY));
        btnAdd.setOnAction(e -> handleAdd());

        signatureList.setPrefWidth(380);
        left.getChildren().addAll(btnAdd, new Label("Saved Signatures:"), signatureList);

        VBox right = new VBox(15);
        right.setStyle(
                "-fx-background-color: #f8fafc; -fx-padding: 15; -fx-border-color: #e2e8f0; -fx-background-radius: 8;");
        Label tuningLabel = new Label("Signature Tuning");
        tuningLabel.setStyle("-fx-font-weight: bold;");

        Slider opacitySlider = new Slider(0.1, 1.0, 1.0);
        CheckBox cbTransparent = new CheckBox("Remove Background (Make Transparent)");
        cbTransparent.setSelected(true);

        opacitySlider.valueProperty().addListener((obs, old, n) -> {
            if (activeConfig != null) {
                activeConfig.setOpacity(n.doubleValue());
                updatePreview();
            }
        });
        cbTransparent.selectedProperty().addListener((obs, old, n) -> {
            if (activeConfig != null) {
                activeConfig.setTransparent(n);
                updatePreview();
            }
        });

        StackPane previewBox = new StackPane(previewView);
        previewBox.setPrefSize(200, 70);
        previewBox.setStyle(
                "-fx-border-color: #64748b; -fx-border-style: dashed; -fx-background-color: white; -fx-padding: 5;");

        Button btnSaveSig = new Button("Apply Tuning & Save");
        btnSaveSig.setStyle(LaxTheme.getButtonStyle(LaxTheme.ButtonType.SECONDARY));
        btnSaveSig.setOnAction(e -> {
            if (activeConfig != null) {
                sigRepo.saveSignature(activeConfig);
                AlertUtils.showInfo("Signature Saved", "Signature tuning saved.");
            }
        });

        right.getChildren().addAll(tuningLabel, new Label("Opacity:"), opacitySlider, cbTransparent,
                new Label("Actual Size Visualizer (Scale 1:1):"), previewBox, btnSaveSig);

        sigLayout.getChildren().addAll(left, right);
        sigSection.getChildren().addAll(rbiNote, sigLayout);

        // --- 4. SECURITY (Password Management) ---
        VBox securitySection = createSection("🔒 Security Settings");

        // Container for side-by-side password change forms
        HBox passwordsContainer = new HBox(30);
        passwordsContainer.setAlignment(Pos.TOP_LEFT);

        // -- Left: Login Password --
        VBox loginPwBox = new VBox(10);
        Label loginPwTitle = new Label("Change Login Password");
        loginPwTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #0f172a;");

        GridPane loginPwGrid = new GridPane();
        loginPwGrid.setHgap(12);
        loginPwGrid.setVgap(10);
        String labelStyle = "-fx-font-size: 12px; -fx-text-fill: #475569;";

        Label lOld = new Label("Current Password:");
        lOld.setStyle(labelStyle);
        PasswordField loginOldPw = new PasswordField();
        loginOldPw.setPromptText("Current");
        loginOldPw.setStyle(LaxTheme.getInputStyle());
        Label lNew = new Label("New Password:");
        lNew.setStyle(labelStyle);
        PasswordField loginNewPw = new PasswordField();
        loginNewPw.setPromptText("New");
        loginNewPw.setStyle(LaxTheme.getInputStyle());
        Label lConf = new Label("Confirm Password:");
        lConf.setStyle(labelStyle);
        PasswordField loginConfPw = new PasswordField();
        loginConfPw.setPromptText("Confirm");
        loginConfPw.setStyle(LaxTheme.getInputStyle());

        loginPwGrid.add(lOld, 0, 0);
        loginPwGrid.add(loginOldPw, 1, 0);
        loginPwGrid.add(lNew, 0, 1);
        loginPwGrid.add(loginNewPw, 1, 1);
        loginPwGrid.add(lConf, 0, 2);
        loginPwGrid.add(loginConfPw, 1, 2);

        Button btnChangeLogin = new Button("🔑 Change Login Password");
        btnChangeLogin.setStyle(LaxTheme.getButtonStyle(LaxTheme.ButtonType.PRIMARY));
        btnChangeLogin.setOnAction(e -> {
            String oldPw = loginOldPw.getText();
            String newPw = loginNewPw.getText();
            String confPw = loginConfPw.getText();
            if (oldPw.isEmpty() || newPw.isEmpty() || confPw.isEmpty()) {
                AlertUtils.showWarning("Missing Fields", "Please fill all fields.");
                return;
            }
            if (!newPw.equals(confPw)) {
                AlertUtils.showError("Mismatch", "New password and confirmation do not match.");
                return;
            }
            if (newPw.length() < 4) {
                AlertUtils.showError("Too Short", "Password must be at least 4 characters.");
                return;
            }
            if (PasswordManager.changeLoginPassword(oldPw, newPw)) {
                AlertUtils.showInfo("Password Changed", "Login password updated successfully!");
                loginOldPw.clear();
                loginNewPw.clear();
                loginConfPw.clear();
            } else {
                AlertUtils.showError("Wrong Password", "Current password is incorrect.");
            }
        });
        loginPwBox.getChildren().addAll(loginPwTitle, loginPwGrid, btnChangeLogin);

        // -- Right: Recycle Bin Password --
        VBox recyclePwBox = new VBox(10);
        Label recyclePwTitle = new Label("Change Recycle Bin Password");
        recyclePwTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #0f172a;");

        GridPane recyclePwGrid = new GridPane();
        recyclePwGrid.setHgap(12);
        recyclePwGrid.setVgap(10);

        Label rOld = new Label("Current Password:");
        rOld.setStyle(labelStyle);
        PasswordField recycleOldPw = new PasswordField();
        recycleOldPw.setPromptText("Current");
        recycleOldPw.setStyle(LaxTheme.getInputStyle());
        Label rNew = new Label("New Password:");
        rNew.setStyle(labelStyle);
        PasswordField recycleNewPw = new PasswordField();
        recycleNewPw.setPromptText("New");
        recycleNewPw.setStyle(LaxTheme.getInputStyle());
        Label rConf = new Label("Confirm Password:");
        rConf.setStyle(labelStyle);
        PasswordField recycleConfPw = new PasswordField();
        recycleConfPw.setPromptText("Confirm");
        recycleConfPw.setStyle(LaxTheme.getInputStyle());

        recyclePwGrid.add(rOld, 0, 0);
        recyclePwGrid.add(recycleOldPw, 1, 0);
        recyclePwGrid.add(rNew, 0, 1);
        recyclePwGrid.add(recycleNewPw, 1, 1);
        recyclePwGrid.add(rConf, 0, 2);
        recyclePwGrid.add(recycleConfPw, 1, 2);

        Button btnChangeRecycle = new Button("🔑 Change Recycle Bin Password");
        btnChangeRecycle.setStyle(LaxTheme.getButtonStyle(LaxTheme.ButtonType.PRIMARY));
        btnChangeRecycle.setOnAction(e -> {
            String oldPw = recycleOldPw.getText();
            String newPw = recycleNewPw.getText();
            String confPw = recycleConfPw.getText();
            if (oldPw.isEmpty() || newPw.isEmpty() || confPw.isEmpty()) {
                AlertUtils.showWarning("Missing Fields", "Please fill all fields.");
                return;
            }
            if (!newPw.equals(confPw)) {
                AlertUtils.showError("Mismatch", "New password and confirmation do not match.");
                return;
            }
            if (newPw.length() < 4) {
                AlertUtils.showError("Too Short", "Password must be at least 4 characters.");
                return;
            }
            if (PasswordManager.changeRecyclePassword(oldPw, newPw)) {
                AlertUtils.showInfo("Password Changed", "Recycle Bin password updated successfully!");
                recycleOldPw.clear();
                recycleNewPw.clear();
                recycleConfPw.clear();
            } else {
                AlertUtils.showError("Wrong Password", "Current password is incorrect.");
            }
        });
        recyclePwBox.getChildren().addAll(recyclePwTitle, recyclePwGrid, btnChangeRecycle);

        passwordsContainer.getChildren().addAll(loginPwBox, recyclePwBox);

        // Separator
        Separator sep = new Separator();
        sep.setPadding(new Insets(15, 0, 10, 0));

        // -- Security Question Setup --
        VBox secQuestionBox = new VBox(10);
        Label secQuestionTitle = new Label("Password Recovery (Security Question)");
        secQuestionTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #0f172a;");
        Label secQuestionSub = new Label("Set up a security question to recover forgotten passwords.");
        secQuestionSub.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");

        GridPane sqGrid = new GridPane();
        sqGrid.setHgap(12);
        sqGrid.setVgap(10);

        Label q1Label = new Label("Security Question 1:");
        q1Label.setStyle(labelStyle);
        TextField sq1Field = new TextField();
        sq1Field.setPromptText("e.g. What is your mother's maiden name?");
        sq1Field.setPrefWidth(300);
        sq1Field.setStyle(LaxTheme.getInputStyle());
        sq1Field.setText(PasswordManager.getSecurityQuestion1());

        Label a1Label = new Label("Answer 1:");
        a1Label.setStyle(labelStyle);
        PasswordField ans1Field = new PasswordField();
        ans1Field.setPromptText("Answer to question 1");
        ans1Field.setPrefWidth(300);
        ans1Field.setStyle(LaxTheme.getInputStyle());

        Label q2Label = new Label("Security Question 2:");
        q2Label.setStyle(labelStyle);
        TextField sq2Field = new TextField();
        sq2Field.setPromptText("e.g. What was your first pet's name?");
        sq2Field.setPrefWidth(300);
        sq2Field.setStyle(LaxTheme.getInputStyle());
        sq2Field.setText(PasswordManager.getSecurityQuestion2());

        Label a2Label = new Label("Answer 2:");
        a2Label.setStyle(labelStyle);
        PasswordField ans2Field = new PasswordField();
        ans2Field.setPromptText("Answer to question 2");
        ans2Field.setPrefWidth(300);
        ans2Field.setStyle(LaxTheme.getInputStyle());

        sqGrid.add(q1Label, 0, 0);
        sqGrid.add(sq1Field, 1, 0);
        sqGrid.add(a1Label, 0, 1);
        sqGrid.add(ans1Field, 1, 1);

        sqGrid.add(q2Label, 0, 2);
        sqGrid.add(sq2Field, 1, 2);
        sqGrid.add(a2Label, 0, 3);
        sqGrid.add(ans2Field, 1, 3);

        boolean alreadySet = PasswordManager.hasSecurityQuestions();
        sqGrid.setDisable(alreadySet);

        // Verification field if questions already exist
        VBox actionBox = new VBox(10);
        Button btnSaveSq = new Button("🛡️ Save Recovery Setup");
        btnSaveSq.setStyle(LaxTheme.getButtonStyle(LaxTheme.ButtonType.SECONDARY));
        btnSaveSq.setDisable(alreadySet);

        if (alreadySet) {
            Button btnUnlock = new Button("🔓 Change Recovery Questions");
            btnUnlock.setStyle(
                    "-fx-background-color: #f8fafc; -fx-text-fill: #475569; -fx-border-color: #e2e8f0; -fx-border-radius: 6;");

            btnUnlock.setOnAction(e -> {
                Dialog<String> authDialog = new Dialog<>();
                authDialog.setTitle("Authorize Change");
                authDialog.setHeaderText("Enter Admin Password to modify recovery settings.");
                AlertUtils.styleDialog(authDialog);

                ButtonType okBtn = new ButtonType("Authorize", ButtonBar.ButtonData.OK_DONE);
                authDialog.getDialogPane().getButtonTypes().addAll(okBtn, ButtonType.CANCEL);

                PasswordField pf = new PasswordField();
                pf.setPromptText("Current Login Password");
                pf.setStyle(LaxTheme.getInputStyle());
                authDialog.getDialogPane().setContent(new VBox(10, new Label("Admin Password:"), pf));

                authDialog.setResultConverter(b -> b == okBtn ? pf.getText() : null);

                java.util.Optional<String> result = authDialog.showAndWait();
                if (result.isPresent() && PasswordManager.validateLogin(result.get())) {
                    sqGrid.setDisable(false);
                    btnSaveSq.setDisable(false);
                    btnUnlock.setDisable(true);
                    btnUnlock.setText("🔓 Unlocked for Editing");
                } else if (result.isPresent()) {
                    AlertUtils.showError("Unauthorized", "Incorrect password.");
                }
            });
            actionBox.getChildren().add(btnUnlock);
        }

        btnSaveSq.setOnAction(e -> {
            String q1 = sq1Field.getText().trim();
            String a1 = ans1Field.getText().trim();
            String q2 = sq2Field.getText().trim();
            String a2 = ans2Field.getText().trim();

            if (q1.isEmpty() || a1.isEmpty() || q2.isEmpty() || a2.isEmpty()) {
                AlertUtils.showWarning("Missing Fields", "Please enter both questions and answers.");
                return;
            }

            PasswordManager.setSecurityQuestions(q1, a1, q2, a2);
            AlertUtils.showInfo("Protected",
                    "Security questions updated successfully!\nThey are now locked again for safety.");

            // Clean up and Lock Again
            ans1Field.clear();
            ans2Field.clear();
            sqGrid.setDisable(true);
            btnSaveSq.setDisable(true);

            // Re-show the settings to refresh the "Unlock" button logic
            // In a real app we'd trigger a reload, but for now just locking UI is enough.
        });

        secQuestionBox.getChildren().addAll(secQuestionTitle, secQuestionSub, sqGrid, actionBox, btnSaveSq);

        securitySection.getChildren().addAll(passwordsContainer, sep, secQuestionBox);

        // Add everything in order
        getChildren().add(titleLabel);
        getChildren().add(profileSection);
        if (userManagementSection != null) {
            getChildren().add(userManagementSection);
        }
        if (maintenanceSection != null) {
            getChildren().add(maintenanceSection);
        }
        getChildren().addAll(generalSection, chequeSection, sigSection, securitySection);
    }

    private void loadSignatures() {
        signatureList.getChildren().clear();
        List<SignatureConfig> list = sigRepo.getAllSignatures();
        for (SignatureConfig sig : list) {
            HBox item = new HBox(10);
            item.setAlignment(Pos.CENTER_LEFT);
            item.setStyle(
                    "-fx-padding: 8; -fx-border-color: #e2e8f0; -fx-background-color: white; -fx-background-radius: 4;");

            Label name = new Label(sig.getName());
            name.setPrefWidth(140);
            name.setWrapText(true);
            name.setStyle("-fx-font-size: 12px;");

            Button btnActive = new Button(
                    chequeConfig.getActiveSignatureId() == sig.getId() ? "✅ Active" : "Set Active");
            btnActive.setStyle(chequeConfig.getActiveSignatureId() == sig.getId()
                    ? "-fx-background-color: #0d9488; -fx-text-fill: white;"
                    : LaxTheme.getButtonStyle(LaxTheme.ButtonType.SECONDARY));
            btnActive.setOnAction(e -> {
                chequeConfig.setActiveSignatureId(sig.getId());
                configRepo.saveConfig(chequeConfig);
                loadSignatures();
                AlertUtils.showInfo("Signature Updated", "Signature set as active for printing.");
            });

            Button btnSelect = new Button("⚙️");
            btnSelect.setTooltip(new Tooltip("Edit Tuning"));
            btnSelect.setOnAction(e -> selectSignature(sig));

            Button btnDel = new Button("🗑️");
            btnDel.setStyle("-fx-text-fill: #ef4444; -fx-background-color: transparent;");
            btnDel.setOnAction(e -> {
                sigRepo.deleteSignature(sig.getId());
                if (chequeConfig.getActiveSignatureId() == sig.getId()) {
                    chequeConfig.setActiveSignatureId(0);
                    configRepo.saveConfig(chequeConfig);
                }
                loadSignatures();
            });

            item.getChildren().addAll(name, btnActive, btnSelect, btnDel);
            signatureList.getChildren().add(item);
        }
    }

    private void selectSignature(SignatureConfig sig) {
        this.activeConfig = sig;
        updatePreview();
    }

    private void handleAdd() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose Signature Image");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File file = chooser.showOpenDialog(getScene().getWindow());
        if (file != null) {
            try {
                SignatureConfig sig = SignatureConfig.builder()
                        .name(file.getName())
                        .path(file.getAbsolutePath())
                        .opacity(1.0)
                        .isTransparent(true)
                        .scale(1.0)
                        .thickness(1.0)
                        .build();
                sigRepo.saveSignature(sig);

                // Immediately reload and select the new one
                loadSignatures();
                List<SignatureConfig> updatedList = sigRepo.getAllSignatures();
                SignatureConfig added = updatedList.stream()
                        .filter(s -> s.getPath().equals(file.getAbsolutePath()))
                        .findFirst().orElse(null);
                if (added != null)
                    selectSignature(added);

                AlertUtils.showInfo("Information", "Signature uploaded and selected for tuning.");
            } catch (Exception e) {
                AlertUtils.showError("Error", "Failed to upload: " + e.getMessage());
            }
        }
    }

    private void updatePreview() {
        if (activeConfig == null)
            return;
        var img = ImageUtils.processSignature(activeConfig.getPath(), activeConfig.getOpacity(),
                activeConfig.isTransparent());
        if (img != null) {
            previewView.setImage(img);
            previewView.setFitWidth(180);
            previewView.setFitHeight(60);
            previewView.setPreserveRatio(true);
        } else {
            previewView.setImage(null);
            Label error = new Label("Image load fail");
            error.setStyle("-fx-text-fill: red;");
            // stackpane will show it
        }
    }

    private VBox createUserProfileSection() {
        VBox profileSection = createSection("👤 My Profile (" + currentUser.getUsername() + ")");
        GridPane profileGrid = new GridPane();
        profileGrid.setHgap(20);
        profileGrid.setVgap(15);

        Label lblCurrent = new Label("Username:");
        Label valCurrent = new Label(currentUser.getUsername());
        valCurrent.setStyle("-fx-font-weight: bold;");

        Label lblRole = new Label("My Role:");
        Label valRole = new Label(currentUser.getRole().name());
        valRole.setStyle(
                "-fx-font-weight: bold; -fx-text-fill: " + (currentUser.isAdmin() ? "#ef4444" : "#0d9488") + ";");

        PasswordField newPass = new PasswordField();
        newPass.setPromptText("New Password");
        newPass.setStyle(LaxTheme.getInputStyle());

        Button btnUpdate = new Button("Update Password");
        btnUpdate.setStyle(LaxTheme.getButtonStyle(LaxTheme.ButtonType.SECONDARY));
        btnUpdate.setOnAction(e -> {
            String pw = newPass.getText().trim();
            if (pw.isEmpty()) {
                AlertUtils.showWarning("Error", "Password cannot be empty.");
                return;
            }
            if (userRepo.updatePassword(currentUser.getId(), pw)) {
                AlertUtils.showInfo("Success", "Password updated successfully.");
                newPass.clear();
            } else {
                AlertUtils.showError("Error", "Failed to update password.");
            }
        });

        profileGrid.add(lblCurrent, 0, 0);
        profileGrid.add(valCurrent, 1, 0);
        profileGrid.add(lblRole, 0, 1);
        profileGrid.add(valRole, 1, 1);
        profileGrid.add(new Label("Change Password:"), 0, 2);
        profileGrid.add(newPass, 1, 2);
        profileGrid.add(btnUpdate, 1, 3);

        profileSection.getChildren().add(profileGrid);
        return profileSection;
    }

    private VBox createUserManagementSection() {
        VBox userSection = createSection("👥 User Management (Admin Only)");
        userSection.setSpacing(15);

        // -- Create User Form --
        HBox createForm = new HBox(10);
        createForm.setAlignment(Pos.CENTER_LEFT);

        TextField uName = new TextField();
        uName.setPromptText("Operator Username");
        uName.setStyle(LaxTheme.getInputStyle());

        PasswordField uPass = new PasswordField();
        uPass.setPromptText("Initial Password");
        uPass.setStyle(LaxTheme.getInputStyle());

        Button btnAdd = new Button("➕ Add Operator");
        btnAdd.setStyle(LaxTheme.getButtonStyle(LaxTheme.ButtonType.PRIMARY));

        VBox listContainer = new VBox(5);
        listContainer.setStyle(
                "-fx-background-color: #f8fafc; -fx-padding: 10; -fx-background-radius: 8; -fx-border-color: #e2e8f0;");

        Runnable refreshList = () -> {
            listContainer.getChildren().clear();
            List<User> users = userRepo.getAllUsers();
            for (User u : users) {
                HBox row = new HBox(15);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(5, 0, 5, 0));

                Label name = new Label(u.getUsername());
                name.setPrefWidth(150);
                name.setStyle("-fx-font-weight: bold;");

                Label role = new Label(u.getRole().name());
                role.setPrefWidth(80);
                role.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Button btnDel = new Button("🗑️");
                btnDel.setStyle("-fx-background-color: transparent; -fx-text-fill: #ef4444; -fx-cursor: hand;");
                if (u.getUsername().equalsIgnoreCase("admin") || u.getId().equals(currentUser.getId())) {
                    btnDel.setDisable(true);
                }

                btnDel.setOnAction(e -> {
                    if (AlertUtils.showConfirmation("Delete User",
                            "Are you sure you want to delete user: " + u.getUsername() + "?")) {
                        if (userRepo.deleteUser(u.getId())) {
                            AlertUtils.showInfo("Success", "User deleted.");
                            // Need to refresh locally
                            listContainer.getChildren().remove(row);
                        }
                    }
                });

                row.getChildren().addAll(name, role, spacer, btnDel);
                listContainer.getChildren().add(row);
            }
        };

        btnAdd.setOnAction(e -> {
            String name = uName.getText().trim();
            String pass = uPass.getText().trim();
            if (name.isEmpty() || pass.isEmpty()) {
                AlertUtils.showWarning("Error", "Username and Password are required.");
                return;
            }
            if (userRepo.createUser(name, pass, User.Role.OPERATOR)) {
                AlertUtils.showInfo("Success", "Operator account created.");
                uName.clear();
                uPass.clear();
                refreshList.run();
            } else {
                AlertUtils.showError("Error", "Failed to create user (Username might already exist).");
            }
        });

        createForm.getChildren().addAll(uName, uPass, btnAdd);
        userSection.getChildren().addAll(new Label("Add New Operator Account:"), createForm, new Separator(),
                new Label("Existing User Accounts:"), listContainer);

        refreshList.run();
        return userSection;
    }

    private VBox createSection(String title) {
        VBox box = new VBox(15);
        box.setStyle(UIStyles.getCardStyle());
        box.setPadding(new Insets(20));
        Label lbl = new Label(title);
        lbl.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #334155;");
        box.getChildren().add(lbl);
        return box;
    }

    private VBox createMaintenanceSection() {
        VBox maintenanceSection = createSection("🧹 Database Health & Maintenance (Admin Only)");
        maintenanceSection.setSpacing(15);

        Label desc = new Label("Optimizing your database keeps the application running at peak performance.");
        desc.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        HBox statsBox = new HBox(30);
        statsBox.setPadding(new Insets(10));
        statsBox.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 8;");

        VBox dbSizeBox = new VBox(5);
        Label dbSizeTitle = new Label("Current DB Size");
        dbSizeTitle.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");
        Label dbSizeVal = new Label("Calculating...");
        dbSizeVal.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        VBox archiveBox = new VBox(5);
        Label archiveTitle = new Label("Archived Records");
        archiveTitle.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");
        Label archiveVal = new Label("Calculating...");
        archiveVal.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        statsBox.getChildren().addAll(dbSizeBox, archiveBox);
        dbSizeBox.getChildren().addAll(dbSizeTitle, dbSizeVal);
        archiveBox.getChildren().addAll(archiveTitle, archiveVal);

        // Update stats
        File dbFile = com.lax.sme_manager.util.DatabaseManager.getDatabaseFile();
        if (dbFile.exists()) {
            double sizeMb = dbFile.length() / (1024.0 * 1024.0);
            dbSizeVal.setText(String.format("%.2f MB", sizeMb));
        }

        try (java.sql.Connection conn = com.lax.sme_manager.util.DatabaseManager.getConnection();
                java.sql.Statement stmt = conn.createStatement();
                java.sql.ResultSet rs = stmt.executeQuery("SELECT count(*) FROM purchase_entries_archive")) {
            if (rs.next()) {
                archiveVal.setText(String.valueOf(rs.getInt(1)));
            }
        } catch (java.sql.SQLException e) {
            archiveVal.setText("0");
        }

        // --- BACKUP CLEANUP ---
        VBox backupBox = new VBox(10);
        Label backupSub = new Label("Backup Cleanup: Automatically retain only the last 30 days.");
        backupSub.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        Button btnCleanup = new Button("🧹 Clean Up Now");
        btnCleanup.setStyle(LaxTheme.getButtonStyle(LaxTheme.ButtonType.SECONDARY));
        btnCleanup.setOnAction(e -> {
            new BackupService().performBackup(); // performBackup calls cleanup
            AlertUtils.showInfo("Maintenance", "Old backups (older than 30 days) have been removed.");
        });
        backupBox.getChildren().addAll(backupSub, btnCleanup);

        // --- DATA ARCHIVING ---
        VBox archivingBox = new VBox(10);
        Label archiveSub = new Label("Data Archiving: Move records older than a specific date to archive.");
        archiveSub.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        HBox archiveControls = new HBox(15);
        archiveControls.setAlignment(Pos.CENTER_LEFT);
        DatePicker archiveBefore = new DatePicker(
                java.time.LocalDate.now().minusYears(1).withMonth(1).withDayOfMonth(1));
        Button btnArchive = new Button("📦 Archive Old Data");
        btnArchive.setStyle(LaxTheme.getButtonStyle(LaxTheme.ButtonType.PRIMARY));

        btnArchive.setOnAction(e -> {
            java.time.LocalDate date = archiveBefore.getValue();
            if (date == null)
                return;

            if (AlertUtils.showConfirmation("Confirm Archiving",
                    "Are you sure you want to archive all data older than " + date + "?\n" +
                            "Archived data will be moved to a separate table for performance.")) {

                com.lax.sme_manager.repository.PurchaseRepository repo = new com.lax.sme_manager.repository.PurchaseRepository();
                int moved = repo.archiveOldData(date);
                if (moved >= 0) {
                    AlertUtils.showInfo("Archiving Complete", moved + " records moved to archive.");
                    // Refresh stats
                    try (java.sql.Connection conn = com.lax.sme_manager.util.DatabaseManager.getConnection();
                            java.sql.Statement stmt = conn.createStatement();
                            java.sql.ResultSet rs = stmt
                                    .executeQuery("SELECT count(*) FROM purchase_entries_archive")) {
                        if (rs.next())
                            archiveVal.setText(String.valueOf(rs.getInt(1)));
                    } catch (Exception ignored) {
                    }
                } else {
                    AlertUtils.showError("Archiving Failed", "An error occurred during archiving. Check logs.");
                }
            }
        });

        archiveControls.getChildren().addAll(new Label("Archive data before:"), archiveBefore, btnArchive);
        archivingBox.getChildren().addAll(archiveSub, archiveControls);

        // --- BROWSE ARCHIVE ---
        VBox browseBox = new VBox(10);
        Label browseSub = new Label("Archive Explorer: Browse and search all your cold storage data.");
        browseSub.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        Button btnBrowse = new Button("🔎 Browse Archive");
        btnBrowse.setStyle(LaxTheme.getButtonStyle(LaxTheme.ButtonType.SECONDARY));
        btnBrowse.setOnAction(e -> {
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Archive Explorer");
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialog.getDialogPane().setContent(new ArchiveExplorerView());
            dialog.getDialogPane().setPrefSize(900, 600);
            dialog.setResizable(true);
            AlertUtils.styleDialog(dialog);
            dialog.showAndWait();

            // Refresh stats after potential restoration
            updateMaintenanceStats(dbSizeVal, archiveVal);
        });
        browseBox.getChildren().addAll(browseSub, btnBrowse);

        maintenanceSection.getChildren().addAll(desc, statsBox, new Separator(), backupBox, new Separator(),
                archivingBox, new Separator(), browseBox);
        return maintenanceSection;
    }

    private void updateMaintenanceStats(Label dbSize, Label archive) {
        File dbFile = com.lax.sme_manager.util.DatabaseManager.getDatabaseFile();
        if (dbFile.exists()) {
            double sizeMb = dbFile.length() / (1024.0 * 1024.0);
            dbSize.setText(String.format("%.2f MB", sizeMb));
        }

        try (java.sql.Connection conn = com.lax.sme_manager.util.DatabaseManager.getConnection();
                java.sql.Statement stmt = conn.createStatement();
                java.sql.ResultSet rs = stmt.executeQuery("SELECT count(*) FROM purchase_entries_archive")) {
            if (rs.next()) {
                archive.setText(String.valueOf(rs.getInt(1)));
            }
        } catch (java.sql.SQLException e) {
            archive.setText("0");
        }
    }
}
