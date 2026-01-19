package com.lax.sme_manager.ui;

import com.lax.sme_manager.repository.VendorRepository;
import com.lax.sme_manager.util.VendorCache;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import com.lax.sme_manager.util.DatabaseManager;
import com.lax.sme_manager.util.BackupService;
import com.lax.sme_manager.repository.PurchaseRepository;
import com.lax.sme_manager.service.MetricsService;
import com.lax.sme_manager.service.PurchaseHistoryService;
import com.lax.sme_manager.ui.theme.LaxTheme;
import com.lax.sme_manager.ui.view.DashboardView;
import com.lax.sme_manager.ui.view.PurchaseEntryView;
import com.lax.sme_manager.ui.view.PurchaseHistoryView;
import com.lax.sme_manager.viewmodel.PurchaseHistoryViewModel;
import com.lax.sme_manager.ui.view.PurchaseDetailView;
import com.lax.sme_manager.domain.Vendor;
import com.lax.sme_manager.repository.model.PurchaseEntity;
import com.lax.sme_manager.ui.component.UIStyles;
import com.lax.sme_manager.util.i18n.AppLabel;
import com.lax.sme_manager.ui.view.PurchaseEditView;
import com.lax.sme_manager.ui.view.SettingsView;
import com.lax.sme_manager.ui.view.ChequeWizardView;
import com.lax.sme_manager.ui.view.ChequeDesignerView;
import com.lax.sme_manager.ui.view.VendorManagementView;

/**
 * LaxSmeManagerApp - Main application window
 * Features:
 * - Dark sidebar navigation with themed colors
 * - Dynamic content area (changes based on selection)
 * - All styling uses LaxTheme constants
 */
public class LaxSmeManagerApp {
    private final VendorCache vendorCache;
    private final PurchaseHistoryService historyService;
    private final MetricsService metricsService;
    private final VendorRepository vendorRepository;

    private Stage stage;
    private BorderPane root;
    private StackPane contentArea;
    private Button currentActiveButton;

    // Cached view instances for lazy loading
    private DashboardView dashboardView;
    private PurchaseEntryView purchaseEntryView;
    private PurchaseHistoryView purchaseHistoryView;
    private VBox reportsView;
    private ChequeWizardView chequeWizardView;
    private ChequeDesignerView chequeDesignerView;
    private SettingsView settingsView;
    private VendorManagementView vendorManagementView;

    public LaxSmeManagerApp(Stage stage) {
        this.stage = stage;

        // Initialize Core Services
        this.vendorRepository = new VendorRepository();
        this.vendorCache = new VendorCache(vendorRepository);
        this.vendorCache.initialize();

        PurchaseRepository purchaseRepository = new PurchaseRepository();
        this.historyService = new PurchaseHistoryService(purchaseRepository, vendorRepository);
        this.metricsService = new MetricsService(purchaseRepository);

        initialize();
    }

    private void initialize() {
        root = new BorderPane();
        root.setStyle(getBackgroundStyle());

        // LEFT: Sidebar Navigation
        root.setLeft(createSidebar());

        // CENTER: Content area (dynamic - changes based on sidebar selection)
        contentArea = new StackPane();
        contentArea.setStyle(getContentAreaStyle());
        root.setCenter(contentArea);

        // BOTTOM: Status Bar
        root.setBottom(createStatusBar());

        // Show Purchase Entry by default
        showPurchaseEntry();

        // Scene setup
        Scene scene = new Scene(root, 1400, 800);
        stage.setScene(scene);
        stage.setTitle("Lax SME Manager - Purchase System");

        // Backup on close
        stage.setOnCloseRequest(e -> {
            new BackupService().performBackup();
            System.exit(0);
        });
        stage.show();
    }

    // ============ SIDEBAR NAVIGATION ============
    private VBox createSidebar() {
        VBox sidebar = new VBox();
        sidebar.setStyle(getSidebarStyle());
        sidebar.setPrefWidth(220);
        sidebar.setSpacing(0);

        // Header
        Label header = new Label("LAX MANAGER");
        header.setStyle(getSidebarHeaderStyle());
        header.setMaxWidth(Double.MAX_VALUE);
        header.setPadding(new Insets(
                LaxTheme.Spacing.SPACE_20,
                LaxTheme.Spacing.SPACE_16,
                LaxTheme.Spacing.SPACE_20,
                LaxTheme.Spacing.SPACE_16));
        VBox.setVgrow(header, Priority.NEVER);

        // Navigation items
        VBox navItems = new VBox();
        navItems.setStyle("-fx-spacing: " + LaxTheme.Spacing.SPACE_8 + ";");
        navItems.setPadding(new Insets(LaxTheme.Spacing.SPACE_12, 0, 0, 0));
        VBox.setVgrow(navItems, Priority.ALWAYS);

        // Navigation buttons
        Button dashboardBtn = createNavButton(AppLabel.TITLE_DASHBOARD.get(), false);
        dashboardBtn.setOnAction(e -> {
            updateActiveButton(dashboardBtn);
            showDashboard();
        });

        Button entryBtn = createNavButton(AppLabel.TITLE_PURCHASE_ENTRY.get(), true);
        entryBtn.setOnAction(e -> {
            updateActiveButton(entryBtn);
            showPurchaseEntry();
        });
        this.currentActiveButton = entryBtn;

        Button historyBtn = createNavButton(AppLabel.TITLE_PURCHASE_HISTORY.get(), false);
        historyBtn.setOnAction(e -> {
            updateActiveButton(historyBtn);
            showPurchaseHistory();
        });

        Button reportsBtn = createNavButton("📈 Reports", false);
        reportsBtn.setOnAction(e -> {
            updateActiveButton(reportsBtn);
            showReports();
        });

        Button chequeBtn = createNavButton(AppLabel.TITLE_CHEQUE_WIZARD.get(), false);
        chequeBtn.setOnAction(e -> {
            updateActiveButton(chequeBtn);
            showChequeWizard();
        });

        Button designerBtn = createNavButton(AppLabel.TITLE_CHEQUE_DESIGNER.get(), false);
        designerBtn.setOnAction(e -> {
            updateActiveButton(designerBtn);
            showChequeDesigner();
        });

        Button settingsBtn = createNavButton(AppLabel.TITLE_SETTINGS.get(), false);
        settingsBtn.setOnAction(e -> {
            updateActiveButton(settingsBtn);
            showSettings();
        });

        Button vendorsBtn = createNavButton("👥 Vendors", false);
        vendorsBtn.setOnAction(e -> {
            updateActiveButton(vendorsBtn);
            showVendorManagement();
        });

        navItems.getChildren().addAll(dashboardBtn, entryBtn, historyBtn, chequeBtn, designerBtn, reportsBtn,
                vendorsBtn, settingsBtn);
        sidebar.getChildren().addAll(header, navItems);

        return sidebar;
    }

    private Button createNavButton(String text, boolean active) {
        Button btn = new Button(text);
        btn.setPrefWidth(220);
        btn.setMinHeight(LaxTheme.Sidebar.BUTTON_MIN_HEIGHT);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle(getSidebarButtonStyle(active));
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(
                LaxTheme.Sidebar.BUTTON_PADDING_V,
                LaxTheme.Sidebar.BUTTON_PADDING_H,
                LaxTheme.Sidebar.BUTTON_PADDING_V,
                LaxTheme.Sidebar.BUTTON_PADDING_H));
        btn.setCursor(javafx.scene.Cursor.HAND);

        btn.setOnMouseEntered(e -> {
            if (btn != currentActiveButton) {
                btn.setStyle(getSidebarButtonHoverStyle());
            }
        });

        btn.setOnMouseExited(e -> {
            if (btn != currentActiveButton) {
                btn.setStyle(getSidebarButtonStyle(false));
            }
        });

        return btn;
    }

    private void updateActiveButton(Button newActiveButton) {
        if (currentActiveButton != null) {
            currentActiveButton.setStyle(getSidebarButtonStyle(false));
        }
        newActiveButton.setStyle(getSidebarButtonStyle(true));
        currentActiveButton = newActiveButton;
    }

    // ============ SCREEN METHODS ============
    private void showDashboard() {
        if (dashboardView == null) {
            dashboardView = new DashboardView(metricsService);
        }
        contentArea.getChildren().clear();
        contentArea.getChildren().add(dashboardView);
    }

    private void showPurchaseEntry() {
        if (purchaseEntryView == null) {
            purchaseEntryView = new PurchaseEntryView(vendorCache);
        }
        contentArea.getChildren().clear();
        contentArea.getChildren().add(purchaseEntryView);
    }

    private void showPurchaseHistory() {
        if (purchaseHistoryView == null) {
            purchaseHistoryView = new PurchaseHistoryView(
                    new PurchaseHistoryViewModel(historyService), vendorRepository, vendorCache);
            purchaseHistoryView.setOnPurchaseSelected(this::showPurchaseDetailsDialog);
            purchaseHistoryView.setOnPurchaseEdit(this::showPurchaseEditDialog);
        }
        contentArea.getChildren().clear();
        contentArea.getChildren().add(purchaseHistoryView);
    }

    private void showPurchaseDetailsDialog(PurchaseEntity purchase) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Purchase Details - #" + purchase.getId());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.setResizable(true);
        dialog.getDialogPane().setPrefWidth(800);
        dialog.getDialogPane().setPrefHeight(600);

        // Fetch vendor name directly from cache
        Vendor v = vendorCache.findById(purchase.getVendorId());
        String vName = (v != null) ? v.getName() : "Unknown (" + purchase.getVendorId() + ")";

        PurchaseDetailView detailView = new PurchaseDetailView(purchase, vName);
        dialog.getDialogPane().setContent(detailView);

        dialog.showAndWait();
    }

    private void showPurchaseEditDialog(PurchaseEntity purchase) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Edit Purchase - #" + purchase.getId());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.setResizable(true);
        dialog.getDialogPane().setPrefWidth(800);
        dialog.getDialogPane().setPrefHeight(750);

        PurchaseEditView editView = new PurchaseEditView(purchase, vendorCache);
        editView.setOnSave(() -> {
            dialog.close();
            showPurchaseHistory(); // Refresh list after save
        });
        editView.setOnCancel(dialog::close);

        dialog.getDialogPane().setContent(editView);
        dialog.showAndWait();
    }

    private void showChequeWizard() {
        if (chequeWizardView == null) {
            chequeWizardView = new ChequeWizardView(new PurchaseRepository(), vendorRepository);
        }
        contentArea.getChildren().clear();
        contentArea.getChildren().add(chequeWizardView);
    }

    private void showChequeDesigner() {
        if (chequeDesignerView == null) {
            chequeDesignerView = new ChequeDesignerView();
        }
        contentArea.getChildren().clear();
        contentArea.getChildren().add(chequeDesignerView);
    }

    private void showReports() {
        if (reportsView == null) {
            reportsView = new VBox();
            reportsView.setStyle(getScreenContentStyle());
            reportsView.setPadding(new Insets(LaxTheme.Spacing.SPACE_24));
            reportsView.setSpacing(LaxTheme.Spacing.SPACE_16);

            Label title = new Label("📈 Reports");
            title.setStyle(getScreenTitleStyle());

            Label placeholder = new Label("Reports coming soon...");
            placeholder.setStyle(getPlaceholderStyle());

            reportsView.getChildren().addAll(title, placeholder);
        }
        contentArea.getChildren().clear();
        contentArea.getChildren().add(reportsView);
    }

    private void showSettings() {
        if (settingsView == null) {
            settingsView = new SettingsView();
        }
        contentArea.getChildren().clear();

        // CRITICAL: Must wrap in ScrollPane for accessibility
        ScrollPane scrollSettings = new ScrollPane(settingsView);
        scrollSettings.setFitToWidth(true);
        scrollSettings.setStyle("-fx-background-color: transparent;");
        contentArea.getChildren().add(scrollSettings);
    }

    private void showVendorManagement() {
        if (vendorManagementView == null) {
            vendorManagementView = new VendorManagementView();
        }
        contentArea.getChildren().clear();
        contentArea.getChildren().add(vendorManagementView);
    }

    private HBox createStatusBar() {
        HBox statusBar = new HBox(12);
        statusBar.setPadding(new Insets(6, 16, 6, 16));
        statusBar.setAlignment(Pos.CENTER_RIGHT);
        statusBar.setStyle(
                "-fx-background-color: #F1F5F9; -fx-border-color: #E2E8F0 transparent transparent transparent;");

        // DB Health Indicator
        HBox healthBox = new HBox(8);
        healthBox.setAlignment(Pos.CENTER_LEFT);

        Circle dot = new Circle(4);
        dot.setFill(Color.web("#22C55E")); // Green

        Label healthLbl = new Label("Database Connected");
        healthLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");

        healthBox.getChildren().addAll(dot, healthLbl);

        statusBar.getChildren().add(healthBox);
        return statusBar;
    }

    // ============ STYLING (ALL USING LaxTheme) ============

    private String getBackgroundStyle() {
        return String.format(
                "-fx-background-color: %s;",
                LaxTheme.Colors.LIGHT_GRAY);
    }

    private String getContentAreaStyle() {
        return String.format(
                "-fx-background-color: %s;",
                LaxTheme.Colors.LIGHT_GRAY);
    }

    private String getSidebarStyle() {
        return String.format(
                "-fx-background-color: %s; -fx-text-fill: %s;",
                LaxTheme.Colors.DARK_NAVY,
                LaxTheme.Colors.WHITE);
    }

    private String getSidebarHeaderStyle() {
        return String.format(
                "-fx-font-size: %d; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: %s; " +
                        "-fx-border-color: rgba(255,255,255,0.1) transparent transparent transparent; " +
                        "-fx-border-width: 0 0 1 0;",
                LaxTheme.Typography.FONT_SIZE_LG,
                LaxTheme.Colors.WHITE);
    }

    private String getSidebarButtonStyle(boolean active) {
        if (active) {
            return String.format(
                    "-fx-background-color: rgba(13, 148, 136, 0.1); " +
                            "-fx-text-fill: white; " +
                            "-fx-border-color: %s transparent transparent transparent; " +
                            "-fx-border-width: 0 0 0 4; " +
                            "-fx-font-weight: %d; " +
                            "-fx-font-size: %d; " +
                            "-fx-min-height: %d;",
                    LaxTheme.Colors.PRIMARY_TEAL,
                    LaxTheme.Typography.WEIGHT_SEMIBOLD,
                    LaxTheme.Sidebar.FONT_SIZE,
                    LaxTheme.Sidebar.BUTTON_MIN_HEIGHT);
        }
        return String.format(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: rgba(255,255,255,0.8); " +
                        "-fx-border-color: transparent; " +
                        "-fx-font-size: %d; " +
                        "-fx-min-height: %d;",
                LaxTheme.Sidebar.FONT_SIZE,
                LaxTheme.Sidebar.BUTTON_MIN_HEIGHT);
    }

    private String getSidebarButtonHoverStyle() {
        return String.format(
                "-fx-background-color: rgba(255,255,255,0.05); " +
                        "-fx-text-fill: rgba(255,255,255,0.9); " +
                        "-fx-font-size: %d; " +
                        "-fx-min-height: %d;",
                LaxTheme.Sidebar.FONT_SIZE,
                LaxTheme.Sidebar.BUTTON_MIN_HEIGHT);
    }

    private String getScreenContentStyle() {
        return String.format(
                "-fx-background-color: %s;",
                LaxTheme.Colors.LIGHT_GRAY);
    }

    private String getScreenTitleStyle() {
        return String.format(
                "-fx-font-size: %d; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: %s;",
                LaxTheme.Typography.FONT_SIZE_2XL,
                LaxTheme.Colors.TEXT_PRIMARY);
    }

    private String getPlaceholderStyle() {
        return String.format(
                "-fx-font-size: %d; " +
                        "-fx-text-fill: %s;",
                LaxTheme.Typography.FONT_SIZE_MD,
                LaxTheme.Colors.TEXT_SECONDARY);
    }
}