package com.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.animation.ScaleTransition;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Orientation;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;

import com.github.sarxos.webcam.Webcam;
import javafx.embed.swing.SwingFXUtils;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

import java.io.IOException;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Optional;
import java.util.Map;
import java.util.function.Consumer;
import java.awt.image.BufferedImage;
import javafx.beans.binding.Bindings;

public class App extends Application {
    private TableView<Resident> residentTable;
    private TextField searchField; // Promoted to class level for access in other methods
    private Pagination pagination;
    private static final int ROWS_PER_PAGE = 15;
    private String currentSortField = "last_name";
    private String currentSortOrder = "ASC";


    private Scene loginScene;
    private Stage primaryStage;
    private StackPane rootPane; // For toast notifications
    private boolean darkMode = true;

    // Navigation state
    private Button selectedNavButton;
    private Rectangle navIndicator;
    private VBox navMenu;

    // Submenu state (persists between restarts)
    private boolean userSubmenuOpen = false;
    private Button selectedSubmenuButton;
    private Rectangle submenuIndicator;
    
    private VBox userSubmenuContainer;

    // Persisted settings
    private final Path themeFile = Paths.get(System.getProperty("user.home"), ".bdms_theme");
    private final Path submenuStateFile = Paths.get(System.getProperty("user.home"), ".bdms_submenu_open");

    // Last selected view (used when rebuilding UI on theme toggle)
    private String activeSection = "overview";
    private String activeSubmenuItem = null;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        darkMode = loadThemeFromDisk();
        userSubmenuOpen = loadSubmenuStateFromDisk();
        loginScene = createLoginScene();
        stage.setScene(loginScene);
        stage.setTitle("BDMS");
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.show();
    }

    private boolean loadSubmenuStateFromDisk() {
        try {
            if (Files.exists(submenuStateFile)) {
                var value = Files.readString(submenuStateFile).trim();
                return "open".equalsIgnoreCase(value);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void saveSubmenuStateToDisk() {
        try {
            Files.writeString(submenuStateFile, userSubmenuOpen ? "open" : "closed", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean loadThemeFromDisk() {
        try {
            if (Files.exists(themeFile)) {
                var value = Files.readString(themeFile).trim();
                return "light".equalsIgnoreCase(value) ? false : true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    private void saveThemeToDisk() {
        try {
            Files.writeString(themeFile, darkMode ? "dark" : "light", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Scene createLoginScene() {
        var logoDot = new Label(" ");
        logoDot.getStyleClass().add("activity-item-dot"); // Re-using a similar style

        var titleLabel = new Label("Baranggay San Marino Information Management System");
        titleLabel.getStyleClass().add("brand-logo-label");
        titleLabel.getStyleClass().add("text-color-primary");

        var header = new HBox(8, logoDot, titleLabel);
        header.setAlignment(Pos.CENTER);

        var subtitle = new Label("Welcome back!");
        subtitle.getStyleClass().add("login-subtitle");
        subtitle.getStyleClass().add("text-color-primary");

        var usernameField = new TextField();
        usernameField.setPromptText("E.g. info@example.com");
        usernameField.getStyleClass().add("text-field");

        var passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");
        passwordField.getStyleClass().add("password-field");

        var loginButton = new Button("Log in with email");
        loginButton.getStyleClass().add("button-primary");
        loginButton.setMaxWidth(Double.MAX_VALUE);

        loginButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            if (username.isEmpty() || password.isEmpty()) {
                // Use toast for non-blocking feedback
                if (rootPane != null) {
                    showToast("Please enter both username and password.");
                } else {
                    showAlert("Error", "Please enter both username and password.");
                }
                return;
            }
            String role = DatabaseHelper.authenticate(username, password);
            if (role != null) {
                Map<String, String> permissions = DatabaseHelper.getPermissions(role);
                primaryStage.setScene(createDashboardScene(username, role, permissions));
                primaryStage.centerOnScreen();
            } else {
                showToast("Invalid username or password.");
            }
        });

        var forgotLink = new Hyperlink("Forgot your password?");
        forgotLink.getStyleClass().add("hyperlink");
        forgotLink.setOnAction(e -> showAlert("Forgot Password", "Please contact support to reset your password."));

        var orLabel = new Label("or");
        orLabel.getStyleClass().add("text-color-secondary");
        var separatorHBox = new HBox(10, new Separator(), orLabel, new Separator());
        separatorHBox.setAlignment(Pos.CENTER);
        VBox.setVgrow(separatorHBox, javafx.scene.layout.Priority.NEVER);

        var googleBtn = new Button("Login with Google");
        googleBtn.getStyleClass().add("button-secondary");
        googleBtn.setMaxWidth(Double.MAX_VALUE);

        var githubBtn = new Button("Login with GitHub");
        githubBtn.getStyleClass().add("button-secondary");
        githubBtn.setMaxWidth(Double.MAX_VALUE);

        var rememberCheckBox = new CheckBox("Remember me for 30 days");
        rememberCheckBox.getStyleClass().add("check-box");

        var formVBox = new VBox(12, subtitle, usernameField, passwordField, loginButton, forgotLink, separatorHBox, googleBtn, githubBtn, rememberCheckBox);
        formVBox.setAlignment(Pos.CENTER);
        formVBox.getStyleClass().add("login-card");
        formVBox.setMaxWidth(360);

        var card = new VBox(20, header, formVBox);
        card.setAlignment(Pos.CENTER);

        var root = new StackPane(card);
        root.getStyleClass().add("root");
        root.setPadding(new Insets(60));
        this.rootPane = root; // For toast notifications

        // Start with a desktop-friendly size, content remains centered
        var scene = new Scene(root, 1024, 768);
        scene.getStylesheets().add(getClass().getResource(darkMode ? "dark-theme.css" : "light-theme.css").toExternalForm());
        return scene;
    }

    private Scene createDashboardScene(String username, String role, Map<String, String> permissions) {
        var root = new BorderPane();
        root.getStyleClass().add("root");

        // --- TOP BAR (Search, Notifications, User Profile) ---
        searchField = new TextField();
        searchField.setPromptText("Search resident, case ID...");
        searchField.getStyleClass().add("search-field");
        
        // Unique Functionality: Scan-to-Edit
        // Barcode scanners usually terminate with an ENTER key, triggering onAction.
        searchField.setOnAction(e -> {
            String query = searchField.getText().trim();
            if (query.startsWith("RES:")) {
                handleScanResult(query);
            }
        });

        var searchIcon = new FontIcon(FontAwesomeSolid.SEARCH);
        searchIcon.getStyleClass().add("search-field-icon");

        var searchContainer = new StackPane(searchField, searchIcon);
        searchContainer.setAlignment(Pos.CENTER_LEFT);

        var scanButton = new Button("", new FontIcon(FontAwesomeSolid.CAMERA));
        scanButton.setTooltip(new Tooltip("Scan QR Code with Camera"));
        scanButton.getStyleClass().add("button-secondary");
        scanButton.setOnAction(e -> startCameraScan());

        var notificationIcon = new FontIcon(FontAwesomeSolid.BELL);
        var notificationDot = new Circle(4, Color.web("#f43f5e"));
        StackPane.setAlignment(notificationDot, Pos.TOP_RIGHT);
        notificationDot.setTranslateX(-2);
        notificationDot.setTranslateY(2);

        var notificationButton = new StackPane(notificationIcon, notificationDot);
        notificationButton.setPadding(new Insets(8));
        notificationButton.getStyleClass().add("notification-button");
        notificationButton.setOnMouseClicked(e -> showAlert("Notifications", "- New clearance request from Maria Clara.\n- Blotter case #2023-005 requires attention."));

        var userLabel = new Label(username);
        userLabel.getStyleClass().add("user-profile-name");
        userLabel.getStyleClass().add("text-color-primary");

        var roleLabel = new Label(role);
        roleLabel.getStyleClass().add("user-profile-role");
        roleLabel.getStyleClass().add("text-color-secondary");

        var userProfile = new VBox(-2, userLabel, roleLabel);
        userProfile.setAlignment(Pos.CENTER_RIGHT);

        var topBarSpacer = new Region();
        HBox.setHgrow(topBarSpacer, Priority.ALWAYS);
        var topBar = new HBox(16, searchContainer, scanButton, topBarSpacer, notificationButton, userProfile);
        topBar.setPadding(new Insets(12, 18, 0, 18));
        topBar.getStyleClass().add("top-bar");
        topBar.setAlignment(Pos.CENTER);

        // Sidebar
        var logoDot = new Label(" ");
        logoDot.getStyleClass().add("brand-logo-dot");
        var logoLabel = new Label("BDMS");
        logoLabel.getStyleClass().add("brand-logo-label");
        logoLabel.getStyleClass().add("text-color-primary");
        var topBrand = new HBox(8, logoDot, logoLabel);
        topBrand.setAlignment(Pos.CENTER_LEFT);
        topBrand.setPadding(new Insets(20, 0, 20, 20));

        navMenu = new VBox(8);
        navMenu.getStyleClass().add("sidebar-menu");

        // Selection underline that slides beneath the active nav item
        navIndicator = new Rectangle(0, 3);
        navIndicator.getStyleClass().add("nav-indicator");
        navIndicator.setVisible(false);

        var menuStack = new StackPane(navMenu, navIndicator);
        StackPane.setAlignment(navIndicator, Pos.BOTTOM_LEFT);


        var center = new VBox(16);
        center.setPadding(new Insets(18));

        var overviewBtn = createSidebarButton("Analytics & Overview", FontAwesomeSolid.CHART_PIE);
        overviewBtn.setUserData("overview");
        var usersBtn = createSidebarButton("User & Access", FontAwesomeSolid.USERS_COG);
        usersBtn.setUserData("users");
        var residentBtn = createSidebarButton("Residents", FontAwesomeSolid.ADDRESS_BOOK);
        residentBtn.setUserData("resident");
        var systemBtn = createSidebarButton("System Config", FontAwesomeSolid.COGS);
        systemBtn.setUserData("system");
        var maintenanceBtn = createSidebarButton("Maintenance", FontAwesomeSolid.SHIELD_ALT);
        maintenanceBtn.setUserData("maintenance");


        // Collapsible submenu for User & Access Management
        var userSubmenu = createCollapsibleSubmenu("User & Access", java.util.List.of(
            "Manage Roles", "Permissions", "Audit Log"), item -> {
                setActiveNav(usersBtn);
                if ("Audit Log".equals(item)) {
                    showAuditLog(center);
                } else {
                    updateDashboardContent(center, "User & Access Management", "Selected: " + item);
                }
            });

        // Theme switch control (animated)
        var themeLabel = new Label(darkMode ? "Dark Mode" : "Light Mode");
        themeLabel.getStyleClass().add("text-color-secondary");
        themeLabel.getStyleClass().add("theme-label");

        var themeSwitch = createThemeSwitch(() -> {
            // Hot-swap CSS without recreating the scene to preserve state
            Scene scene = primaryStage.getScene();
            scene.getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource(darkMode ? "dark-theme.css" : "light-theme.css").toExternalForm());
            themeLabel.setText(darkMode ? "Dark Mode" : "Light Mode");
        });
        
        var themeRow = new HBox(10, themeSwitch, themeLabel);
        themeRow.setAlignment(Pos.CENTER_LEFT);
        themeRow.setPadding(new Insets(8, 0, 0, 0));

        var logoutBtn = createSidebarButton("Logout", FontAwesomeSolid.SIGN_OUT_ALT);
        logoutBtn.setOnAction(e -> {
            primaryStage.setMaximized(false);
            primaryStage.setScene(createLoginScene());
            primaryStage.centerOnScreen();
        });

        // Add navigation items to sidebar
        navMenu.getChildren().addAll(overviewBtn, userSubmenu, residentBtn, systemBtn, maintenanceBtn, themeRow, logoutBtn);

        // Restore last active section (if any)
        if ("users".equals(activeSection)) {
            setActiveNav(usersBtn);
            if (userSubmenuOpen) {
                animateSubmenuHeight(userSubmenuContainer, true);
            }
            if (activeSubmenuItem != null) {
                // Attempt to restore previously selected submenu item
                for (var node : userSubmenuContainer.getChildren()) {
                    if (node instanceof Button btn && activeSubmenuItem.equals(btn.getUserData())) {
                        setActiveSubmenuItem(btn);
                        Platform.runLater(() -> moveSubmenuIndicator(btn));
                        break;
                    }
                }
            }
        } else if ("resident".equals(activeSection)) {
            setActiveNav(residentBtn);
        } else if ("system".equals(activeSection)) {
            setActiveNav(systemBtn);
        } else if ("maintenance".equals(activeSection)) {
            setActiveNav(maintenanceBtn);
        } else {
            setActiveNav(overviewBtn);
        }

        // Navigation actions
        overviewBtn.setOnAction(e -> {
            setActiveNav(overviewBtn);
            showOverview(center);
        });
        usersBtn.setOnAction(e -> {
            setActiveNav(usersBtn);
            var content = createContentBox("User & Access Management", "Select an option from the submenu to manage roles, permissions, or view audit logs.");
            updateDashboardContent(center, "User & Access Management", content);
        });
        residentBtn.setOnAction(e -> {
            setActiveNav(residentBtn);
            showResidentControl(center);
        });
        systemBtn.setOnAction(e -> {
            setActiveNav(systemBtn);
            updateDashboardContent(center, "System Configuration", "Branding, document templates, and fee settings.");
        });
        maintenanceBtn.setOnAction(e -> {
            setActiveNav(maintenanceBtn);
            updateDashboardContent(center, "Maintenance & Security", "Database backup, notifications, and announcement blast.");
        });

        // Move the brand header inside the menu so it aligns with nav items
        navMenu.getChildren().add(0, topBrand);
        topBrand.setPadding(new Insets(0, 0, 16, 0));

        var sidebar = new VBox(menuStack);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(260);

        // Ensure the indicator is in the correct position after layout
        Platform.runLater(() -> {
            if (selectedNavButton != null) moveSelectionIndicator(selectedNavButton);
        });

        root.setLeft(sidebar);
        
        // Make center content scrollable and responsive
        var scrollPane = new ScrollPane(center);
        scrollPane.getStyleClass().add("scroll-pane");
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        var mainContent = new VBox(0, topBar, scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        var mainStack = new StackPane(mainContent);
        this.rootPane = mainStack; // For toast notifications
        root.setCenter(mainStack);

        // initial overview content
        if ("overview".equals(activeSection) || activeSection == null) {
            showOverview(center);
        }

        var scene = new Scene(root, 900, 600);
        scene.getStylesheets().add(getClass().getResource(darkMode ? "dark-theme.css" : "light-theme.css").toExternalForm());
        return scene;
    }

    private Button createSidebarButton(String text, FontAwesomeSolid iconCode) {
        var icon = new FontIcon(iconCode);
        var button = new Button(text, icon);
        button.setMaxWidth(Double.MAX_VALUE);
        button.getStyleClass().add("sidebar-button");
        button.setGraphicTextGap(12);
        return button;
    }
    private Button createSidebarButton(String text) {
        var button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.getStyleClass().add("sidebar-button");
        return button;
    }

    private void setActiveNav(Button button) {
        if (selectedNavButton != null) {
            selectedNavButton.getStyleClass().remove("selected");
        }
        selectedNavButton = button;
        if (selectedNavButton != null) {
            selectedNavButton.getStyleClass().add("selected");
            navIndicator.setVisible(true);
            moveSelectionIndicator(button);

            // subtle selection animation
            var scale = new ScaleTransition(Duration.millis(150), selectedNavButton);
            scale.setFromX(1);
            scale.setFromY(1);
            scale.setToX(1.02);
            scale.setToY(1.02);
            scale.setAutoReverse(true);
            scale.setCycleCount(2);
            scale.play();
        }

        activeSection = (button.getUserData() instanceof String ? (String) button.getUserData() : null);
    }

    private void moveSelectionIndicator(Button button) {
        if (navIndicator == null || navMenu == null || button == null) return;

        var bounds = button.getBoundsInParent();
        var targetX = bounds.getMinX() + 16; // Adjust for padding
        var targetY = bounds.getMaxY() - navIndicator.getHeight() - 4;
        var targetWidth = Math.max(40, bounds.getWidth() - 32);

        var translate = new TranslateTransition(Duration.millis(250), navIndicator);
        translate.setToX(targetX);
        translate.setToY(targetY);

        var widthAnim = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(Duration.millis(250), new javafx.animation.KeyValue(navIndicator.widthProperty(), targetWidth))
        );

        new javafx.animation.ParallelTransition(translate, widthAnim).play();
    }

    private StackPane createThemeSwitch(Runnable onToggle) {
        var track = new Rectangle(44, 22);
        track.setArcWidth(22);
        track.setArcHeight(22);
        track.getStyleClass().add("theme-switch-track");

        var knob = new Circle(10);
        knob.getStyleClass().add("theme-switch-knob");
        knob.setTranslateX(darkMode ? 10 : -10);

        var switchPane = new StackPane(track, knob);
        switchPane.setPadding(new Insets(6));
        switchPane.setOnMouseClicked(e -> {
            darkMode = !darkMode;
            animateToggle(knob);
            saveThemeToDisk();
            // The onToggle runnable is now responsible for updating the UI.
            // No need to recreate the scene here.
            if (onToggle != null) {
                onToggle.run();
            }
        });

        return switchPane;
    }

    private void animateToggle(Circle knob) {
        var translate = new TranslateTransition(Duration.millis(180), knob);
        translate.setToX(darkMode ? 10 : -10);
        translate.play();
    }

    private void updateDashboardContent(VBox center, String title, String body) {
        updateDashboardContent(center, title, createContentBox(title, body));
    }

    private void updateDashboardContent(VBox center, String title, Node content) {
        if (center.getChildren().isEmpty()) {
            center.getChildren().add(content);
            return;
        }

        // Smooth transition between pages
        var fadeOut = new javafx.animation.FadeTransition(Duration.millis(180), center);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            center.getChildren().setAll(content);
            var fadeIn = new javafx.animation.FadeTransition(Duration.millis(200), center);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();
        });
        fadeOut.play();
    }

    private void showOverview(VBox center) {
        int totalPopulation = DatabaseHelper.getResidentCount(null);
        var populationCard = createStatCard("Total Population", String.format("%,d", totalPopulation), "#30c88e");

        var revenueCard = createStatCard("Revenue", "₱12,345", "#eab308");
        var clearanceCard = createStatCard("Pending Clearances", "42", "#f43f5e");
        var casesCard = createStatCard("Active Cases", "5", "#3b82f6");
        
        var statsGrid = new FlowPane(16, 16, populationCard, revenueCard, clearanceCard, casesCard);
        
        var recentActivity = new VBox(12);
        var actTitle = new Label("Recent Activity");
        actTitle.getStyleClass().add("activity-title");
        
        recentActivity.getChildren().add(actTitle);
        recentActivity.getChildren().addAll(
            createActivityItem("New resident registered: Juan Dela Cruz"),
            createActivityItem("Barangay Clearance issued to Maria Clara"),
            createActivityItem("Blotter case #2023-005 filed"),
            createActivityItem("System backup completed")
        );
        
        // Create Purok Distribution Chart
        var purokData = DatabaseHelper.getResidentDistributionByPurok();
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        purokData.forEach((purok, count) -> pieChartData.add(new PieChart.Data(purok + " (" + count + ")", count)));

        var purokDistributionChart = new PieChart(pieChartData);
        purokDistributionChart.setTitle("Resident Distribution by Purok");
        purokDistributionChart.setLegendVisible(true);
        purokDistributionChart.setLabelsVisible(false); // Labels on slices can get crowded. Legend is better.

        var bottomRow = new HBox(24, purokDistributionChart, recentActivity);
        HBox.setHgrow(purokDistributionChart, Priority.ALWAYS);
        HBox.setHgrow(recentActivity, Priority.ALWAYS);

        var content = new VBox(24, statsGrid, bottomRow);
        updateDashboardContent(center, "Analytics & Overview", content);
    }

    private void showAuditLog(VBox center) {
        var table = new TableView<AuditEntry>();
        table.getStyleClass().add("table-view");

        TableColumn<AuditEntry, String> timestampCol = new TableColumn<>("Timestamp");
        timestampCol.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        timestampCol.setPrefWidth(180);

        TableColumn<AuditEntry, String> userCol = new TableColumn<>("User");
        userCol.setCellValueFactory(new PropertyValueFactory<>("user"));
        userCol.setPrefWidth(120);

        TableColumn<AuditEntry, String> actionCol = new TableColumn<>("Action");
        actionCol.setCellValueFactory(new PropertyValueFactory<>("action"));
        actionCol.setPrefWidth(400);

        table.getColumns().setAll(List.of(timestampCol, userCol, actionCol));

        // Placeholder data
        ObservableList<AuditEntry> data = FXCollections.observableArrayList(
            new AuditEntry("2024-08-01 10:05:12", "secretary", "Issued Barangay Clearance to Maria Clara."),
            new AuditEntry("2024-08-01 09:45:30", "superadmin", "Updated system setting: 'fee_clearance' to ₱50.00."),
            new AuditEntry("2024-07-31 15:20:01", "captain", "Filed new blotter case #2023-005."),
            new AuditEntry("2024-07-31 11:00:45", "secretary", "Registered new resident: Juan Dela Cruz.")
        );
        table.setItems(data);

        updateDashboardContent(center, "Audit Log", table);
    }

    private void showResidentControl(VBox center) {
        residentTable = new TableView<>();
        residentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<Resident, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(cellData -> new javafx.beans.binding.StringBinding() {
            { bind(cellData.getValue().firstNameProperty(), cellData.getValue().lastNameProperty()); }
            @Override
            protected String computeValue() {
                return cellData.getValue().getLastName() + ", " + cellData.getValue().getFirstName();
            }
        });
        nameCol.setId("last_name");

        TableColumn<Resident, String> birthDateCol = new TableColumn<>("Birth Date");
        birthDateCol.setCellValueFactory(new PropertyValueFactory<>("birthDate"));
        birthDateCol.setId("birth_date");

        TableColumn<Resident, String> genderCol = new TableColumn<>("Gender");
        genderCol.setCellValueFactory(new PropertyValueFactory<>("gender"));
        genderCol.setId("gender");

        TableColumn<Resident, String> purokCol = new TableColumn<>("Purok");
        purokCol.setCellValueFactory(new PropertyValueFactory<>("purok"));
        purokCol.setId("purok");

        residentTable.getColumns().setAll(List.of(nameCol, birthDateCol, genderCol, purokCol));

        Button addButton = new Button("Add Resident");
        addButton.setGraphic(new FontIcon(FontAwesomeSolid.PLUS_CIRCLE));

        Button editButton = new Button("Edit Resident");
        editButton.setGraphic(new FontIcon(FontAwesomeSolid.PENCIL_ALT));

        Button deleteButton = new Button("Delete Resident");
        deleteButton.setGraphic(new FontIcon(FontAwesomeSolid.TRASH));

        Button idButton = new Button("Print ID");
        idButton.setGraphic(new FontIcon(FontAwesomeSolid.ID_CARD));

        Button viewQrButton = new Button("View QR");
        viewQrButton.setGraphic(new FontIcon(FontAwesomeSolid.QRCODE));

        editButton.setDisable(true);
        deleteButton.setDisable(true);
        idButton.setDisable(true);
        viewQrButton.setDisable(true);

        residentTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean isSelected = newSelection != null;
            editButton.setDisable(!isSelected);
            deleteButton.setDisable(!isSelected);
            idButton.setDisable(!isSelected);
            viewQrButton.setDisable(!isSelected);
        });

        // Custom sort policy for server-side sorting with pagination
        residentTable.setSortPolicy(table -> {
            if (table.getSortOrder().isEmpty()) {
                currentSortField = "last_name";
                currentSortOrder = "ASC";
            } else {
                TableColumn<Resident, ?> col = table.getSortOrder().get(0);
                if (col.getId() != null) {
                    currentSortField = col.getId();
                    currentSortOrder = col.getSortType() == TableColumn.SortType.ASCENDING ? "ASC" : "DESC";
                }
            }
            loadResidentData();
            return true;
        });

        addButton.setOnAction(e -> {
            showResidentDialog(null).ifPresent(resident -> {
                DatabaseHelper.addResident(resident);
                loadResidentData();
                showToast("Resident added successfully.");
            });
        });

        editButton.setOnAction(e -> {
            Resident selected = residentTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showResidentDialog(selected).ifPresent(resident -> {
                    DatabaseHelper.updateResident(resident);
                    loadResidentData();
                    showToast("Resident updated successfully.");
                });
            }
        });

        deleteButton.setOnAction(e -> {
            Resident selected = residentTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Delete Resident");
                confirm.setHeaderText("Are you sure you want to delete " + selected.getFirstName() + " " + selected.getLastName() + "?");
                confirm.setContentText("This action cannot be undone.");
                confirm.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        DatabaseHelper.deleteResident(selected.getId());
                        loadResidentData();
                        showToast("Resident deleted successfully.");
                    }
                });
            }
        });

        idButton.setOnAction(e -> {
            Resident selected = residentTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                generateResidentIDCard(selected);
            }
        });

        viewQrButton.setOnAction(e -> {
            Resident selected = residentTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showQRCodeDialog(selected);
            }
        });

        ToolBar toolBar = new ToolBar(addButton, editButton, deleteButton, new Separator(Orientation.VERTICAL), idButton, viewQrButton);
        toolBar.setStyle("-fx-background-color: transparent; -fx-padding: 0;");

        var exportButton = new Button("📄 Export to PDF");
        exportButton.getStyleClass().add("button-accent");
        exportButton.setOnAction(e -> generateResidentPdf());

        var bottomBar = new HBox(exportButton);
        bottomBar.setAlignment(Pos.CENTER_RIGHT);
        bottomBar.setPadding(new Insets(10, 0, 0, 0));

        pagination = new Pagination();
        pagination.setPageFactory(this::createPage);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> updatePagination());

        var content = new VBox(12, toolBar, pagination, bottomBar);
        VBox.setVgrow(pagination, Priority.ALWAYS);
        updateDashboardContent(center, "Resident & Data Control", content);
        updatePagination();
    }

    private void loadResidentData() {
        if (pagination != null) {
            updatePagination();
            // Force the page factory to be called for the current page
            pagination.setPageFactory(null);
            pagination.setPageFactory(this::createPage);
        }
    }

    private void generateResidentPdf() {
        Document document = new Document();
        try {
            String path = System.getProperty("user.home") + "/Downloads/Resident_List.pdf";
            PdfWriter.getInstance(document, new FileOutputStream(path));
            document.open();
            document.add(new Paragraph("Barangay Resident List"));
            document.add(new Paragraph("Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
            document.add(new Paragraph(" ")); // Spacer
            
            // In a real app, you would loop through resident data from the database
            document.add(new Paragraph("1. Juan Dela Cruz - Purok 1"));
            document.add(new Paragraph("2. Maria Clara - Purok 2"));
            document.add(new Paragraph("3. Jose Rizal - Purok 1"));
            
            document.close();
            showToast("PDF generated successfully at: " + path);
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to generate PDF.");
        }
    }

    private void generateResidentIDCard(Resident resident) {
        // ID-1 Card Size (approx 242x153 points)
        Document document = new Document(new com.lowagie.text.Rectangle(242, 153));
        try {
            String path = System.getProperty("user.home") + "/Downloads/ID_" + resident.getId() + ".pdf";
            PdfWriter.getInstance(document, new FileOutputStream(path));
            document.open();

            // Add Resident Info
            var font = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10, com.lowagie.text.Font.BOLD);
            document.add(new Paragraph("BARANGAY ID SYSTEM", new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 8)));
            document.add(new Paragraph(resident.getLastName() + ", " + resident.getFirstName(), font));
            document.add(new Paragraph("Purok: " + resident.getPurok(), new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 8)));

            // Generate QR Code: "RES:" + ID
            String qrCodeText = "RES:" + resident.getId();
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrCodeText, BarcodeFormat.QR_CODE, 100, 100);
            
            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            
            com.lowagie.text.Image qrImage = com.lowagie.text.Image.getInstance(pngOutputStream.toByteArray());
            qrImage.setAbsolutePosition(150, 40); // Position on the right side of the card
            document.add(qrImage);

            document.close();
            showToast("ID Card generated: " + path);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to generate ID Card.");
        }
    }

    private Optional<Resident> showResidentDialog(Resident existingResident) {
        Dialog<Resident> dialog = new Dialog<>();
        dialog.setTitle(existingResident == null ? "Add New Resident" : "Edit Resident");
        dialog.setHeaderText("Please fill in the resident's details.");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField firstName = new TextField();
        firstName.setPromptText("First Name");
        TextField lastName = new TextField();
        lastName.setPromptText("Last Name");
        TextField birthDate = new TextField();
        birthDate.setPromptText("YYYY-MM-DD");
        TextField gender = new TextField();
        gender.setPromptText("Gender");
        TextField purok = new TextField();
        purok.setPromptText("Purok / Zone");

        grid.add(new Label("First Name:"), 0, 0);
        grid.add(firstName, 1, 0);
        grid.add(new Label("Last Name:"), 0, 1);
        grid.add(lastName, 1, 1);
        grid.add(new Label("Birth Date:"), 0, 2);
        grid.add(birthDate, 1, 2);
        grid.add(new Label("Gender:"), 0, 3);
        grid.add(gender, 1, 3);
        grid.add(new Label("Purok:"), 0, 4);
        grid.add(purok, 1, 4);

        if (existingResident != null) {
            firstName.setText(existingResident.getFirstName());
            lastName.setText(existingResident.getLastName());
            birthDate.setText(existingResident.getBirthDate());
            gender.setText(existingResident.getGender());
            purok.setText(existingResident.getPurok());
        }

        // --- Validation ---
        // Get the Save button node from the dialog pane.
        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        
        // Create a binding that evaluates to true if any of the text fields are empty.
        // This is a declarative way to express the validation rule.
        var emptyBinding = Bindings.createBooleanBinding(() ->
                firstName.getText().trim().isEmpty() ||
                lastName.getText().trim().isEmpty() ||
                birthDate.getText().trim().isEmpty() ||
                gender.getText().trim().isEmpty() ||
                purok.getText().trim().isEmpty(),
            firstName.textProperty(),
            lastName.textProperty(),
            birthDate.textProperty(),
            gender.textProperty(),
            purok.textProperty()
        );

        // Bind the button's disable property to the binding. The button will be disabled as long as the binding is true.
        saveButton.disableProperty().bind(emptyBinding);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                int id = (existingResident == null) ? 0 : existingResident.getId();
                return new Resident(id, firstName.getText(), lastName.getText(), birthDate.getText(), gender.getText(), purok.getText());
            }
            return null;
        });

        return dialog.showAndWait();
    }

    private void showQRCodeDialog(Resident resident) {
        try {
            String qrCodeText = "RES:" + resident.getId();
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrCodeText, BarcodeFormat.QR_CODE, 300, 300);
            
            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            Image fxImage = SwingFXUtils.toFXImage(bufferedImage, null);
            
            ImageView imageView = new ImageView(fxImage);
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Resident QR Code");
            alert.setHeaderText(resident.getFirstName() + " " + resident.getLastName());
            alert.getDialogPane().setContent(new StackPane(imageView));
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Could not generate QR code.");
        }
    }

    private void startCameraScan() {
        Webcam webcam = Webcam.getDefault();
        if (webcam == null) {
            showAlert("Error", "No webcam detected.");
            return;
        }

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Scan QR Code");
        dialog.setHeaderText("Point your camera at a Resident QR Code");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        ImageView preview = new ImageView();
        preview.setFitWidth(320);
        preview.setFitHeight(240);
        dialog.getDialogPane().setContent(new StackPane(preview));

        AtomicBoolean scanning = new AtomicBoolean(true);

        // Scanning thread
        Thread scanThread = new Thread(() -> {
            try {
                webcam.open();
                while (scanning.get()) {
                    BufferedImage image = webcam.getImage();
                    if (image != null) {
                        // Update UI with camera frame
                        Image fxImage = SwingFXUtils.toFXImage(image, null);
                        Platform.runLater(() -> preview.setImage(fxImage));

                        // Attempt to decode QR
                        try {
                            BufferedImageLuminanceSource source = new BufferedImageLuminanceSource(image);
                            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                            Result result = new MultiFormatReader().decode(bitmap);

                            if (result != null) {
                                String text = result.getText();
                                scanning.set(false);
                                Platform.runLater(() -> {
                                    dialog.setResult(text);
                                    dialog.close();
                                });
                            }
                        } catch (Exception ignored) {
                            // No QR found in this frame, continue scanning
                        }
                    }
                }
            } finally {
                webcam.close();
            }
        });
        scanThread.setDaemon(true);
        scanThread.start();

        dialog.setOnHidden(e -> scanning.set(false));
        dialog.showAndWait().ifPresent(this::handleScanResult);
    }

    private void handleScanResult(String code) {
        if (code != null && code.startsWith("RES:")) {
            try {
                int id = Integer.parseInt(code.substring(4));
                DatabaseHelper.getResidentById(id)
                    .ifPresent(r -> {
                        showResidentDialog(r).ifPresent(updated -> {
                            DatabaseHelper.updateResident(updated);
                            if (pagination != null) loadResidentData();
                        });
                    });
            } catch (NumberFormatException e) {
                e.printStackTrace(); // Log if the ID in the QR code is not a valid number
            }
        }
    }

    private Node createPage(int pageIndex) {
        String filter = searchField.getText();
        ObservableList<Resident> residents = DatabaseHelper.getResidents(filter, pageIndex, ROWS_PER_PAGE, currentSortField, currentSortOrder);
        residentTable.setItems(residents);
        return residentTable;
    }

    private void updatePagination() {
        String filter = searchField.getText();
        int totalCount = DatabaseHelper.getResidentCount(filter);
        int pageCount = (totalCount + ROWS_PER_PAGE - 1) / ROWS_PER_PAGE;
        if (pageCount == 0) pageCount = 1;
        pagination.setPageCount(pageCount);
    }

    private VBox createCollapsibleSubmenu(String title, List<String> items, Consumer<String> onSelect) {
        var mainButton = createSidebarButton(title, FontAwesomeSolid.USERS);
        var submenu = new VBox(6);
        userSubmenuContainer = submenu;
        submenu.setPadding(new Insets(0, 0, 0, 20));
        submenu.setMaxHeight(0);
        submenu.setManaged(false);

        for (var label : items) {
            var itemBtn = createSidebarButton("- " + label);
            itemBtn.setUserData(label);
            itemBtn.setOnAction(e -> {
                setActiveSubmenuItem(itemBtn);
                onSelect.accept(label);
            });
            itemBtn.getStyleClass().add("submenu-button");
            submenu.getChildren().add(itemBtn);
        }

        mainButton.setOnAction(e -> {
            userSubmenuOpen = !userSubmenuOpen;
            saveSubmenuStateToDisk();
            animateSubmenuHeight(submenu, userSubmenuOpen);
        });

        var container = new VBox(4, mainButton, submenu);
        return container;
    }

    private void setActiveSubmenuItem(Button button) {
        if (selectedSubmenuButton != null) {
            selectedSubmenuButton.getStyleClass().remove("selected");
        }
        selectedSubmenuButton = button;
        selectedSubmenuButton.getStyleClass().add("selected");
        activeSubmenuItem = (button.getUserData() instanceof String ? (String) button.getUserData() : null);
    }
    private void moveSubmenuIndicator(Button button) {
        if (submenuIndicator == null || button == null) return;

        var bounds = button.getBoundsInParent();
        var targetY = bounds.getMinY() + (bounds.getHeight() - submenuIndicator.getHeight()) / 2;

        new TranslateTransition(Duration.millis(250), submenuIndicator).setToY(targetY);
    }

    private void showToast(String message) {
        if (rootPane == null) return;

        Label toastLabel = new Label(message);
        toastLabel.getStyleClass().add("toast-notification");
        
        StackPane.setAlignment(toastLabel, Pos.BOTTOM_CENTER);
        StackPane.setMargin(toastLabel, new Insets(0, 0, 30, 0));
        
        toastLabel.setOpacity(0);
        rootPane.getChildren().add(toastLabel);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), toastLabel);
        fadeIn.setToValue(1);

        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(Duration.seconds(2.5));
        
        FadeTransition fadeOut = new FadeTransition(Duration.millis(400), toastLabel);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> rootPane.getChildren().remove(toastLabel));

        pause.setOnFinished(e -> fadeOut.play());
        fadeIn.setOnFinished(e -> pause.play());
        fadeIn.play();
    }

    private void animateSubmenuHeight(VBox submenu, boolean expand) {
        double targetHeight = expand ? submenu.getChildren().size() * 34 + 8 : 0;
        submenu.setManaged(true);
        submenu.setVisible(true);

        var heightAnim = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(Duration.ZERO, new javafx.animation.KeyValue(submenu.maxHeightProperty(), submenu.getHeight())),
            new javafx.animation.KeyFrame(Duration.millis(220), new javafx.animation.KeyValue(submenu.maxHeightProperty(), targetHeight))
        );
        heightAnim.setOnFinished(e -> {
            if (!expand) {
                submenu.setManaged(false);
                submenu.setVisible(false);
            }
        });
        heightAnim.play();
    }

    private VBox createContentBox(String title, String body) {
        var heading = new Label(title);
        heading.getStyleClass().add("content-box-title");
        var content = new Label(body);
        content.getStyleClass().add("content-box-body");
        var box = new VBox(10, heading, content);
        box.getStyleClass().add("content-box");
        return box;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private VBox createStatCard(String title, String value, String color) {
        var card = new VBox(8);
        card.getStyleClass().add("stat-card");

        var valueLabel = new Label(value);
        // Dynamic color is one of the few good uses for inline style
        valueLabel.setStyle("-fx-text-fill: " + color + ";");
        valueLabel.getStyleClass().add("stat-card-value");

        var titleLabel = new Label(title);
        titleLabel.getStyleClass().add("stat-card-title");

        card.getChildren().addAll(valueLabel, titleLabel);
        return card;
    }

    private HBox createActivityItem(String text) {
        var item = new HBox(12);
        item.setPadding(new Insets(8, 0, 8, 0));

        var dot = new Label(" ");
        dot.getStyleClass().add("activity-item-dot");

        var textLabel = new Label(text);
        textLabel.getStyleClass().add("activity-item-text");

        item.getChildren().addAll(dot, textLabel);
        return item;
    }

    public static class AuditEntry {
        private final String timestamp;
        private final String user;
        private final String action;

        public AuditEntry(String timestamp, String user, String action) {
            this.timestamp = timestamp;
            this.user = user;
            this.action = action;
        }

        public String getTimestamp() { return timestamp; }
        public String getUser() { return user; }
        public String getAction() { return action; }
    }


    public static void main(String[] args) {
        launch();
    }

}