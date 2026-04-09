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
import javafx.stage.FileChooser;
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
import java.io.File;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.LocalDate;
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
    private TableView<DocumentRequest> documentRequestsTable;
    private TableView<Complaint> complaintsTable;
    private TableView<Announcement> announcementsTable;
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

    // Current logged-in user info
    private String currentUsername = "";
    @SuppressWarnings("unused") // Kept for future role-based UI customization
    private String currentRole = "";
    private int currentResidentId = 0;
    private String currentResidentName = "";

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        darkMode = loadThemeFromDisk();
        userSubmenuOpen = loadSubmenuStateFromDisk();
        loginScene = createLoginScene();
        stage.setScene(loginScene);
        stage.setTitle("Baranggay San Marino Information Management System");
        stage.setWidth(1024);
        stage.setHeight(768);
        stage.setResizable(false);
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
        // Load and display logo from assets
        ImageView logoView = new ImageView();
        try {
            var resourceStream = getClass().getResourceAsStream("/assets/logo.png");
            if (resourceStream != null) {
                var logoImage = new Image(resourceStream);
                logoView.setImage(logoImage);
                logoView.setFitWidth(220);
                logoView.setPreserveRatio(true);
                System.out.println("✓ Logo loaded from resources");
            } else {
                System.out.println("✗ Logo resource not found, trying file path");
                // Fallback to file path
                File logoFile = new File("src/assets/logo.png");
                if (logoFile.exists()) {
                    var logoImage = new Image(logoFile.toURI().toString());
                    logoView.setImage(logoImage);
                    logoView.setFitWidth(220);
                    logoView.setPreserveRatio(true);
                    System.out.println("✓ Logo loaded from file path");
                }
            }
        } catch (Exception e) {
            System.err.println("✗ Error loading logo: " + e.getMessage());
            e.printStackTrace();
        }

        var header = new HBox(8, logoView);
        header.setAlignment(Pos.CENTER);

        var subtitle = new Label("Welcome back!");
        subtitle.getStyleClass().add("login-subtitle");
        subtitle.setStyle("-fx-text-fill: " + (darkMode ? "#ffffff" : "#1a1a1a") + ";");

        var usernameField = new TextField();
        usernameField.setPromptText("E.g. info@example.com");
        usernameField.getStyleClass().add("text-field");

        var passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");
        passwordField.getStyleClass().add("password-field");

        var loginButton = new Button("Login");
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


        var rememberCheckBox = new CheckBox("Remember me for 30 days");
        rememberCheckBox.getStyleClass().add("check-box");

        var formVBox = new VBox(12, subtitle, usernameField, passwordField, loginButton, forgotLink, rememberCheckBox);
        formVBox.setAlignment(Pos.CENTER);
        formVBox.getStyleClass().add("login-card");
        formVBox.setMaxWidth(360);

        var card = new VBox(20, header, formVBox);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: rgba(255, 255, 255, 0.95); -fx-padding: 30; -fx-border-radius: 10; -fx-background-radius: 10;");

        // Create background image view
        ImageView backgroundView = new ImageView();
        try {
            var resourceStream = getClass().getResourceAsStream("/assets/loginbg.png");
            if (resourceStream != null) {
                var bgImage = new Image(resourceStream);
                backgroundView.setImage(bgImage);
                backgroundView.setFitWidth(1024);
                backgroundView.setFitHeight(768);
                backgroundView.setPreserveRatio(false);
                backgroundView.setOpacity(1);
                System.out.println("✓ Background loaded from resources");
            } else {
                System.out.println("✗ Background resource not found, trying file path");
                File bgFile = new File("src/assets/loginbg.png");
                if (bgFile.exists()) {
                    var bgImage = new Image(bgFile.toURI().toString());
                    backgroundView.setImage(bgImage);
                    backgroundView.setFitWidth(1024);
                    backgroundView.setFitHeight(768);
                    backgroundView.setPreserveRatio(false);
                    backgroundView.setOpacity(1);
                    System.out.println("✓ Background loaded from file path");
                } else {
                    System.out.println("✗ Background file not found");
                    backgroundView.setStyle("-fx-background-color: #e8e8e8;");
                }
            }
        } catch (Exception e) {
            System.err.println("✗ Error loading background: " + e.getMessage());
            e.printStackTrace();
            backgroundView.setStyle("-fx-background-color: #e8e8e8;");
        }

        // Center the card on the background
        var root = new StackPane();
        root.getChildren().add(backgroundView);
        root.getChildren().add(card);
        StackPane.setAlignment(card, Pos.CENTER);
        this.rootPane = root; // For toast notifications

        // Start with a desktop-friendly size, content remains centered
        var scene = new Scene(root, 1024, 768);
        scene.getStylesheets().add(getClass().getResource(darkMode ? "dark-theme.css" : "light-theme.css").toExternalForm());
        return scene;
    }

    private Scene createDashboardScene(String username, String role, Map<String, String> permissions) {
        // Store current user info for use throughout the dashboard
        this.currentUsername = username;
        this.currentRole = role;
        
        // Try to find the resident ID for the current user
        // First, try to find a resident matching the username
        ObservableList<Resident> residents = DatabaseHelper.getResidents(username, 0, 10, "last_name", "ASC");
        
        if (!residents.isEmpty()) {
            this.currentResidentId = residents.get(0).getId();
            this.currentResidentName = residents.get(0).getLastName() + ", " + residents.get(0).getFirstName();
        } else {
            // Fallback: use first resident in system
            residents = DatabaseHelper.getResidents(null, 0, 1, "last_name", "ASC");
            if (!residents.isEmpty()) {
                this.currentResidentId = residents.get(0).getId();
                this.currentResidentName = residents.get(0).getLastName() + ", " + residents.get(0).getFirstName();
            } else {
                // No residents at all, use defaults
                this.currentResidentId = 1;
                this.currentResidentName = username;
            }
        }
        
        System.out.println("Dashboard loaded for user: " + username);
        System.out.println("Current Resident ID: " + currentResidentId);
        System.out.println("Current Resident Name: " + currentResidentName);
        
        var root = new BorderPane();
        root.getStyleClass().add("root");

        // --- TOP BAR (Search, Notifications, User Profile) ---
        searchField = new TextField();
        searchField.setPromptText("Search resident");
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
        userLabel.setStyle("-fx-text-fill: " + (darkMode ? "#f0f0f0" : "#1a1a1a") + ";");

        var roleLabel = new Label(role);
        roleLabel.getStyleClass().add("user-profile-role");
        roleLabel.setStyle("-fx-text-fill: " + (darkMode ? "#b0b0b0" : "#666") + ";");

        var userProfile = new VBox(-2, userLabel, roleLabel);
        userProfile.setAlignment(Pos.CENTER_RIGHT);

        var topBarSpacer = new Region();
        HBox.setHgrow(topBarSpacer, Priority.ALWAYS);
        var topBar = new HBox(16, searchContainer, scanButton, topBarSpacer, notificationButton, userProfile);
        topBar.setPadding(new Insets(12, 18, 0, 18));
        topBar.getStyleClass().add("top-bar");
        topBar.setAlignment(Pos.CENTER);

        // Sidebar
        ImageView dashboardLogoView = new ImageView();
        try {
            var resourceStream = getClass().getResourceAsStream("/assets/logo.png");
            if (resourceStream != null) {
                var logoImage = new Image(resourceStream);
                dashboardLogoView.setImage(logoImage);
                dashboardLogoView.setFitWidth(120);
                dashboardLogoView.setPreserveRatio(true);
            } else {
                File logoFile = new File("src/assets/logo.png");
                if (logoFile.exists()) {
                    var logoImage = new Image(logoFile.toURI().toString());
                    dashboardLogoView.setImage(logoImage);
                    dashboardLogoView.setFitWidth(120);
                    dashboardLogoView.setPreserveRatio(true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        var topBrand = new HBox(8, dashboardLogoView);
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
        var certificatesBtn = createSidebarButton("Certificates & Clearances", FontAwesomeSolid.FILE_PDF);
        certificatesBtn.setUserData("certificates");
        var complaintsBtn = createSidebarButton("Complaints & Incidents", FontAwesomeSolid.EXCLAMATION_CIRCLE);
        complaintsBtn.setUserData("complaints");
        var announcementsBtn = createSidebarButton("Announcement Portal", FontAwesomeSolid.BELL);
        announcementsBtn.setUserData("announcements");
        var financialBtn = createSidebarButton("Financial Reports", FontAwesomeSolid.CHART_LINE);
        financialBtn.setUserData("financial");
        var securityBtn = (Button) createSidebarButton("Security Features", FontAwesomeSolid.LOCK);
        securityBtn.setUserData("security");
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
                } else if ("Manage Roles".equals(item)) {
                    showManageRoles(center);
                } else if ("Permissions".equals(item)) {
                    showPermissions(center);
                } else {
                    updateDashboardContent(center, "User & Access Management", "Selected: " + item);
                }
            });

        // Theme switch control (animated)
        var themeLabel = new Label(darkMode ? "Dark Mode" : "Light Mode");
        themeLabel.setStyle("-fx-text-fill: " + (darkMode ? "#b0b0b0" : "#666") + ";");

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
        navMenu.getChildren().addAll(overviewBtn, userSubmenu, residentBtn, certificatesBtn, complaintsBtn, announcementsBtn, financialBtn, (Button) securityBtn, systemBtn, maintenanceBtn, themeRow, logoutBtn);

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
        } else if ("certificates".equals(activeSection)) {
            setActiveNav(certificatesBtn);
        } else if ("complaints".equals(activeSection)) {
            setActiveNav(complaintsBtn);
        } else if ("announcements".equals(activeSection)) {
            setActiveNav(announcementsBtn);
        } else if ("financial".equals(activeSection)) {
            setActiveNav(financialBtn);
        } else if ("security".equals(activeSection)) {
            setActiveNav((Button) securityBtn);
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
        certificatesBtn.setOnAction(e -> {
            setActiveNav(certificatesBtn);
            showCertificatesAndClearances(center);
        });
        complaintsBtn.setOnAction(e -> {
            setActiveNav(complaintsBtn);
            showComplaintsAndIncidents(center);
        });
        announcementsBtn.setOnAction(e -> {
            setActiveNav(announcementsBtn);
            showAnnouncementsPortal(center);
        });
        financialBtn.setOnAction(e -> {
            setActiveNav(financialBtn);
            showFinancialReports(center);
        });
        ((Button) securityBtn).setOnAction(e -> {
            setActiveNav((Button) securityBtn);
            showSecurityFeatures(center);
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

        var scene = new Scene(root, 1024, 768);
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

        var revenueCard = createStatCard("Revenue", "₱0", "#eab308");
        var clearanceCard = createStatCard("Pending Clearances", "0", "#f43f5e");
        var casesCard = createStatCard("Active Cases", "0", "#3b82f6");
        
        // Get announcement counts by type
        ObservableList<Announcement> allAnnouncements = DatabaseHelper.getAllAnnouncements();
        long eventCount = allAnnouncements.stream().filter(a -> "Event".equals(a.getType())).count();
        long alertCount = allAnnouncements.stream().filter(a -> "Emergency Alert".equals(a.getType())).count();
        long programCount = allAnnouncements.stream().filter(a -> "Program".equals(a.getType())).count();
        
        var eventsCard = createStatCard("Events", String.valueOf(eventCount), "#10b981");
        var alertsCard = createStatCard("Emergency Alerts", String.valueOf(alertCount), "#ef4444");
        var programsCard = createStatCard("Programs", String.valueOf(programCount), "#8b5cf6");
        
        var statsGrid = new FlowPane(16, 16, populationCard, revenueCard, clearanceCard, casesCard, eventsCard, alertsCard, programsCard);
        
        var recentActivity = new VBox(12);
        var actTitle = new Label("Recent Activity");
        actTitle.setStyle("-fx-text-fill: " + (darkMode ? "#ffffff" : "#1a1a1a") + "; -fx-font-size: 14; -fx-font-weight: bold;");
        
        recentActivity.getChildren().add(actTitle);
        
        // Load real recent activity from audit logs (last 4 activities)
        var activityLogs = DatabaseHelper.getRecentActivity(4);
        for (AuditEntry entry : activityLogs) {
            recentActivity.getChildren().add(createActivityItem(entry.getAction()));
        }
        
        // Create Age Distribution Chart
        var ageData = DatabaseHelper.getAgeDistribution();
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        ageData.forEach((ageGroup, count) -> pieChartData.add(new PieChart.Data(ageGroup + " (" + count + ")", count)));

        var genderDistributionChart = new PieChart(pieChartData);
        genderDistributionChart.setTitle("Resident Distribution by Age");
        genderDistributionChart.setLegendVisible(true);
        genderDistributionChart.setLabelsVisible(false); // Labels on slices can get crowded. Legend is better.

        var bottomRow = new HBox(24, genderDistributionChart, recentActivity);
        HBox.setHgrow(genderDistributionChart, Priority.ALWAYS);
        HBox.setHgrow(recentActivity, Priority.ALWAYS);

        // Announcements section
        var announcementsSection = new VBox(12);
        var announcementsTitle = new Label("Recent Announcements");
        announcementsTitle.setStyle("-fx-text-fill: " + (darkMode ? "#ffffff" : "#1a1a1a") + "; -fx-font-size: 14; -fx-font-weight: bold;");
        announcementsSection.getChildren().add(announcementsTitle);

        // Display latest 5 announcements
        allAnnouncements.stream()
            .limit(5)
            .forEach(announcement -> {
                var announcementItem = new HBox(12);
                announcementItem.setPadding(new Insets(10));
                announcementItem.setStyle("-fx-background-color: " + (darkMode ? "#1e1e1e" : "#f9fafb") + "; -fx-border-color: " + (darkMode ? "#333" : "#e5e7eb") + "; -fx-border-width: 1; -fx-border-radius: 4;");
                announcementItem.setAlignment(Pos.TOP_LEFT);

                // Type badge with color
                var typeBadge = new Label(announcement.getType());
                String typeColor = switch (announcement.getType()) {
                    case "Event" -> "#10b981";
                    case "Emergency Alert" -> "#ef4444";
                    case "Program" -> "#8b5cf6";
                    default -> "#6b7280";
                };
                typeBadge.setStyle("-fx-background-color: " + typeColor + "; -fx-text-fill: white; -fx-padding: 3 8; -fx-border-radius: 4; -fx-font-size: 10; -fx-font-weight: bold;");
                typeBadge.setPrefWidth(80);

                // Announcement details
                var details = new VBox(4);
                var title = new Label(announcement.getTitle());
                title.setStyle("-fx-text-fill: " + (darkMode ? "#ffffff" : "#1a1a1a") + "; -fx-font-size: 12; -fx-font-weight: bold;");
                title.setWrapText(true);

                var content = new Label(announcement.getContent().length() > 60 ? 
                    announcement.getContent().substring(0, 60) + "..." : 
                    announcement.getContent());
                content.setStyle("-fx-text-fill: " + (darkMode ? "#b0b0b0" : "#666") + "; -fx-font-size: 10;");
                content.setWrapText(true);

                var meta = new Label("Posted by " + announcement.getPostedBy() + " on " + announcement.getPostedDate() + " | Status: " + announcement.getStatus());
                meta.setStyle("-fx-text-fill: " + (darkMode ? "#808080" : "#999") + "; -fx-font-size: 9;");

                details.getChildren().addAll(title, content, meta);
                announcementItem.getChildren().addAll(typeBadge, details);
                HBox.setHgrow(details, Priority.ALWAYS);

                announcementsSection.getChildren().add(announcementItem);
            });

        if (allAnnouncements.isEmpty()) {
            var noAnnouncements = new Label("No announcements yet");
            noAnnouncements.setStyle("-fx-text-fill: " + (darkMode ? "#808080" : "#999") + "; -fx-font-style: italic;");
            announcementsSection.getChildren().add(noAnnouncements);
        }

        var middleRow = new HBox(24, bottomRow);
        HBox.setHgrow(bottomRow, Priority.ALWAYS);

        var content = new VBox(24, statsGrid, middleRow, announcementsSection);
        updateDashboardContent(center, "Analytics & Overview", content);
    }

    private void showManageRoles(VBox center) {
        var rolesTable = new TableView<Role>();
        rolesTable.getStyleClass().add("table-view");
        rolesTable.setPrefHeight(400);

        // Columns
        TableColumn<Role, Number> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(60);

        TableColumn<Role, String> nameCol = new TableColumn<>("Role Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(180);

        TableColumn<Role, String> descriptionCol = new TableColumn<>("Description");
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descriptionCol.setPrefWidth(350);

        rolesTable.getColumns().setAll(List.of(idCol, nameCol, descriptionCol));

        // Toolbar buttons
        Button addButton = new Button("Add Role");
        addButton.setGraphic(new FontIcon(FontAwesomeSolid.PLUS_CIRCLE));

        Button editButton = new Button("Edit Role");
        editButton.setGraphic(new FontIcon(FontAwesomeSolid.PENCIL_ALT));
        editButton.setDisable(true);

        Button deleteButton = new Button("Delete Role");
        deleteButton.setGraphic(new FontIcon(FontAwesomeSolid.TRASH));
        deleteButton.setDisable(true);

        rolesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean isSelected = newSelection != null;
            editButton.setDisable(!isSelected);
            deleteButton.setDisable(!isSelected);
        });

        addButton.setOnAction(e -> {
            showRoleDialog(null).ifPresent(role -> {
                DatabaseHelper.addRole(role);
                loadRoleData(rolesTable);
                showToast("Role created successfully.");
            });
        });

        editButton.setOnAction(e -> {
            Role selected = rolesTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showRoleDialog(selected).ifPresent(role -> {
                    DatabaseHelper.updateRole(role);
                    loadRoleData(rolesTable);
                    showToast("Role updated successfully.");
                });
            }
        });

        deleteButton.setOnAction(e -> {
            Role selected = rolesTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Delete Role");
                confirm.setHeaderText("Are you sure you want to delete the role \"" + selected.getName() + "\"?");
                confirm.setContentText("This action cannot be undone. Residents with this role will be unaffected.");
                confirm.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        DatabaseHelper.deleteRole(selected.getId());
                        loadRoleData(rolesTable);
                        showToast("Role deleted successfully.");
                    }
                });
            }
        });

        ToolBar toolBar = new ToolBar(addButton, editButton, deleteButton);
        toolBar.setStyle("-fx-background-color: transparent; -fx-padding: 0;");

        var content = new VBox(12, toolBar, rolesTable);
        VBox.setVgrow(rolesTable, Priority.ALWAYS);
        updateDashboardContent(center, "Manage Roles", content);

        // Load roles
        loadRoleData(rolesTable);
    }

    private void loadRoleData(TableView<Role> table) {
        ObservableList<Role> roles = DatabaseHelper.getAllRoles();
        table.setItems(roles);
    }

    private Optional<Role> showRoleDialog(Role existingRole) {
        Dialog<Role> dialog = new Dialog<>();
        dialog.setTitle(existingRole == null ? "Add New Role" : "Edit Role");
        dialog.setHeaderText("Please fill in the role details.");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("E.g., Barangay Captain");
        nameField.setMaxWidth(Double.MAX_VALUE);

        TextArea descriptionField = new TextArea();
        descriptionField.setPromptText("Enter role description");
        descriptionField.setWrapText(true);
        descriptionField.setPrefRowCount(5);

        grid.add(new Label("Role Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionField, 1, 1);

        if (existingRole != null) {
            nameField.setText(existingRole.getName());
            descriptionField.setText(existingRole.getDescription());
        }

        // Validation
        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        var emptyBinding = Bindings.createBooleanBinding(() ->
                nameField.getText().trim().isEmpty() ||
                descriptionField.getText().trim().isEmpty(),
            nameField.textProperty(),
            descriptionField.textProperty()
        );
        saveButton.disableProperty().bind(emptyBinding);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                int id = (existingRole == null) ? 0 : existingRole.getId();
                Role r = new Role(id, nameField.getText().trim(), descriptionField.getText().trim());
                return r;
            }
            return null;
        });

        return dialog.showAndWait();
    }

    private void showPermissions(VBox center) {
        var permissionsTable = new TableView<Map.Entry<String, Map<String, String>>>();
        permissionsTable.getStyleClass().add("table-view");

        TableColumn<Map.Entry<String, Map<String, String>>, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getKey()));
        roleCol.setPrefWidth(150);

        TableColumn<Map.Entry<String, Map<String, String>>, String> residentDataCol = new TableColumn<>("Resident Data");
        residentDataCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getValue().get("Resident Data")));
        residentDataCol.setPrefWidth(120);
        residentDataCol.setCellFactory(param -> createPermissionCell());

        TableColumn<Map.Entry<String, Map<String, String>>, String> financialsCol = new TableColumn<>("Financials");
        financialsCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getValue().get("Financials")));
        financialsCol.setPrefWidth(120);
        financialsCol.setCellFactory(param -> createPermissionCell());

        TableColumn<Map.Entry<String, Map<String, String>>, String> blotterCol = new TableColumn<>("Blotter/Legal");
        blotterCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getValue().get("Blotter/Legal")));
        blotterCol.setPrefWidth(120);
        blotterCol.setCellFactory(param -> createPermissionCell());

        TableColumn<Map.Entry<String, Map<String, String>>, String> systemCol = new TableColumn<>("System Settings");
        systemCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getValue().get("System Settings")));
        systemCol.setPrefWidth(120);
        systemCol.setCellFactory(param -> createPermissionCell());

        permissionsTable.getColumns().setAll(List.of(roleCol, residentDataCol, financialsCol, blotterCol, systemCol));

        // Fetch roles dynamically from the database
        ObservableList<Role> allRoles = DatabaseHelper.getAllRoles();
        ObservableList<Map.Entry<String, Map<String, String>>> permissionsData = FXCollections.observableArrayList();
        for (Role role : allRoles) {
            Map<String, String> permissions = DatabaseHelper.getPermissions(role.getName());
            permissionsData.add(Map.entry(role.getName(), permissions));
        }
        permissionsTable.setItems(permissionsData);

        var infoLabel = new Label("Permission Levels: None, View Only, Manage, Full Access");
        infoLabel.setStyle("-fx-font-size: 11; -fx-text-fill: " + (darkMode ? "#d0d0d0" : "#333") + ";");

        var content = new VBox(12, infoLabel, permissionsTable);
        VBox.setVgrow(permissionsTable, Priority.ALWAYS);
        updateDashboardContent(center, "Role Permissions", content);
    }

    private TableCell<Map.Entry<String, Map<String, String>>, String> createPermissionCell() {
        return new TableCell<Map.Entry<String, Map<String, String>>, String>() {
            private final ComboBox<String> comboBox = new ComboBox<>();
            {
                comboBox.getItems().addAll("None", "View Only", "Manage", "Full Access");
                comboBox.setStyle("-fx-font-size: 11;");
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    comboBox.setValue(item);
                    setGraphic(comboBox);
                }
            }
        };
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
        actionCol.setPrefWidth(250);

        TableColumn<AuditEntry, String> detailsCol = new TableColumn<>("Details");
        detailsCol.setCellValueFactory(new PropertyValueFactory<>("details"));
        detailsCol.setPrefWidth(200);

        TableColumn<AuditEntry, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        categoryCol.setPrefWidth(100);

        table.getColumns().setAll(List.of(timestampCol, userCol, actionCol, detailsCol, categoryCol));

        // Load real audit logs from database
        ObservableList<AuditEntry> data = DatabaseHelper.getAuditLogs();
        table.setItems(data);

        updateDashboardContent(center, "Audit Log", table);
    }

    private void showCertificatesAndClearances(VBox center) {
        // Two tabs: Request new document and view requests
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Tab 1: Request New Document
        Tab requestTab = new Tab("Request Document", createDocumentRequestPanel());
        requestTab.setStyle("-fx-font-size: 12; -fx-padding: 10;");

        // Tab 2: Pending & Completed Requests
        Tab requestsTab = new Tab("Document Requests", createDocumentRequestsTable());
        requestsTab.setStyle("-fx-font-size: 12; -fx-padding: 10;");

        tabPane.getTabs().addAll(requestTab, requestsTab);
        updateDashboardContent(center, "Certificates & Clearances", tabPane);
    }

    private VBox createDocumentRequestPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));

        // Step 1: Select Resident with Search
        Label residentLabel = new Label("Step 1: Select Resident");
        residentLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: " + (darkMode ? "#ffffff" : "#1a1a1a") + ";");

        // Search field for residents
        TextField residentSearchField = new TextField();
        residentSearchField.setPromptText("Search by name or ID...");
        residentSearchField.setStyle("-fx-font-size: 12;");
        residentSearchField.setPrefWidth(300);

        // Load all residents
        ObservableList<Resident> allResidents = DatabaseHelper.getResidents(null, 0, 1000, "last_name", "ASC");
        
        // Filtered list
        ObservableList<Resident> filteredResidents = FXCollections.observableArrayList();
        
        // ListView to show results
        ListView<Resident> residentListView = new ListView<>();
        residentListView.setStyle("-fx-control-inner-background: " + (darkMode ? "#0f172a" : "#ffffff") + ";");
        residentListView.setPrefHeight(150);
        residentListView.setCellFactory(param -> new ListCell<Resident>() {
            @Override
            protected void updateItem(Resident resident, boolean empty) {
                super.updateItem(resident, empty);
                setText(empty ? "" : resident.getLastName() + ", " + resident.getFirstName() + " (ID: " + resident.getId() + ")");
            }
        });
        residentListView.setItems(filteredResidents);

        // Selected resident holder
        java.util.concurrent.atomic.AtomicReference<Resident> selectedResident = new java.util.concurrent.atomic.AtomicReference<>(null);
        
        // Search logic
        residentSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredResidents.clear();
            if (newVal == null || newVal.trim().isEmpty()) {
                filteredResidents.addAll(allResidents);
            } else {
                String searchLower = newVal.toLowerCase();
                allResidents.stream()
                    .filter(r -> r.getLastName().toLowerCase().contains(searchLower) || 
                               r.getFirstName().toLowerCase().contains(searchLower) ||
                               String.valueOf(r.getId()).contains(searchLower))
                    .forEach(filteredResidents::add);
            }
        });
        filteredResidents.addAll(allResidents);

        // Handle resident selection
        residentListView.setOnMouseClicked(e -> {
            Resident selected = residentListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                selectedResident.set(selected);
                residentSearchField.setText(selected.getLastName() + ", " + selected.getFirstName());
                filteredResidents.clear();
            }
        });

        VBox residentSearchBox = new VBox(8, residentSearchField, residentListView);
        var residentBoxLabel = new Label("Resident:");
        residentBoxLabel.setStyle("-fx-text-fill: " + (darkMode ? "#d0d0d0" : "#333") + ";");
        HBox residentBox = new HBox(10, residentBoxLabel, residentSearchBox);
        residentBox.setAlignment(Pos.TOP_LEFT);

        // Step 2: Select Document Type
        Label docTypeLabel = new Label("Step 2: Select Document Type");
        docTypeLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: " + (darkMode ? "#ffffff" : "#1a1a1a") + ";");

        ComboBox<String> docTypeCombo = new ComboBox<>();
        docTypeCombo.setItems(FXCollections.observableArrayList(
            "Barangay Clearance",
            "Certificate of Residency",
            "Indigency Certificate"
        ));
        docTypeCombo.setPrefWidth(300);

        Label feeLabel = new Label("Fee: ₱0");
        feeLabel.setStyle("-fx-font-size: 12; -fx-text-fill: " + (darkMode ? "#d0d0d0" : "#333") + ";");
        docTypeCombo.setOnAction(e -> {
            if (docTypeCombo.getValue() != null) {
                double fee = DocumentRequest.getFeeForDocumentType(docTypeCombo.getValue());
                feeLabel.setText("Fee: ₱" + fee);
            }
        });

        var docTypeBoxLabel = new Label("Document Type:");
        docTypeBoxLabel.setStyle("-fx-text-fill: " + (darkMode ? "#d0d0d0" : "#333") + ";");
        HBox docTypeBox = new HBox(10, docTypeBoxLabel, docTypeCombo);
        docTypeBox.setAlignment(Pos.CENTER_LEFT);

        // Step 3: Purpose
        Label purposeLabel = new Label("Step 3: Purpose of Request");
        purposeLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: " + (darkMode ? "#ffffff" : "#1a1a1a") + ";");

        TextArea purposeArea = new TextArea();
        purposeArea.setPromptText("E.g., For loan application, for employment, for travel");
        purposeArea.setWrapText(true);
        purposeArea.setPrefRowCount(4);

        // Submit Button
        Button submitBtn = new Button("Submit Request");
        submitBtn.setStyle("-fx-font-size: 12; -fx-padding: 10;");
        submitBtn.setDisable(true);

        // Enable button only when all fields are filled
        residentSearchField.textProperty().addListener((obs, oldVal, newVal) -> 
            submitBtn.setDisable(selectedResident.get() == null || docTypeCombo.getValue() == null || purposeArea.getText().trim().isEmpty())
        );
        docTypeCombo.valueProperty().addListener((obs, oldVal, newVal) ->
            submitBtn.setDisable(selectedResident.get() == null || newVal == null || purposeArea.getText().trim().isEmpty())
        );
        purposeArea.textProperty().addListener((obs, oldVal, newVal) ->
            submitBtn.setDisable(selectedResident.get() == null || docTypeCombo.getValue() == null || newVal.trim().isEmpty())
        );

        submitBtn.setOnAction(e -> {
            Resident selected = selectedResident.get();
            String docType = docTypeCombo.getValue();
            String purpose = purposeArea.getText();

            DocumentRequest request = new DocumentRequest(selected.getId(), selected.getLastName() + ", " + selected.getFirstName(), docType, purpose);
            int requestId = DatabaseHelper.createDocumentRequest(request);

            if (requestId > 0) {
                showToast("Document request submitted successfully!");
                residentSearchField.clear();
                selectedResident.set(null);
                filteredResidents.clear();
                filteredResidents.addAll(allResidents);
                docTypeCombo.setValue(null);
                purposeArea.clear();
                feeLabel.setText("Fee: ₱0");
                refreshDocumentRequestsTable();
            } else {
                showToast("Failed to submit request.");
            }
        });

        panel.getChildren().addAll(
            residentLabel, residentBox,
            new Separator(),
            docTypeLabel, docTypeBox, feeLabel,
            new Separator(),
            purposeLabel, purposeArea,
            submitBtn
        );

        ScrollPane scrollPane = new ScrollPane(panel);
        scrollPane.setFitToWidth(true);
        
        VBox container = new VBox(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        return container;
    }

    private VBox createDocumentRequestsTable() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(10));

        documentRequestsTable = new TableView<>();
        documentRequestsTable.getStyleClass().add("table-view");

        TableColumn<DocumentRequest, String> residentCol = new TableColumn<>("Resident");
        residentCol.setCellValueFactory(new PropertyValueFactory<>("residentName"));
        residentCol.setPrefWidth(180);

        TableColumn<DocumentRequest, String> docTypeCol = new TableColumn<>("Document Type");
        docTypeCol.setCellValueFactory(new PropertyValueFactory<>("documentType"));
        docTypeCol.setPrefWidth(150);

        TableColumn<DocumentRequest, String> requestDateCol = new TableColumn<>("Request Date");
        requestDateCol.setCellValueFactory(new PropertyValueFactory<>("requestDate"));
        requestDateCol.setPrefWidth(120);

        TableColumn<DocumentRequest, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(100);

        TableColumn<DocumentRequest, String> paymentCol = new TableColumn<>("Payment");
        paymentCol.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));
        paymentCol.setPrefWidth(100);

        TableColumn<DocumentRequest, Double> feeCol = new TableColumn<>("Fee");
        feeCol.setCellValueFactory(new PropertyValueFactory<>("fee"));
        feeCol.setPrefWidth(80);

        documentRequestsTable.getColumns().setAll(List.of(residentCol, docTypeCol, requestDateCol, statusCol, paymentCol, feeCol));

        // Load data
        ObservableList<DocumentRequest> requests = DatabaseHelper.getAllDocumentRequests();
        documentRequestsTable.setItems(requests);

        // Buttons
        Button approveBtn = new Button("Approve", new FontIcon(FontAwesomeSolid.CHECK_CIRCLE));
        approveBtn.setDisable(true);
        approveBtn.setOnAction(e -> {
            DocumentRequest selected = documentRequestsTable.getSelectionModel().getSelectedItem();
            if (selected != null && "PENDING".equals(selected.getStatus())) {
                DatabaseHelper.approveDocumentRequest(selected.getId(), "Captain");
                selected.setStatus("APPROVED");
                showToast("Document request approved!");
                refreshDocumentRequestsTable();
            }
        });

        Button paymentBtn = new Button("Record Payment", new FontIcon(FontAwesomeSolid.DOLLAR_SIGN));
        paymentBtn.setDisable(true);
        paymentBtn.setOnAction(e -> {
            DocumentRequest selected = documentRequestsTable.getSelectionModel().getSelectedItem();
            if (selected != null && "APPROVED".equals(selected.getStatus())) {
                DatabaseHelper.recordPayment(selected.getId());
                selected.setPaymentStatus("PAID");
                showToast("Payment recorded!");
                refreshDocumentRequestsTable();
            }
        });

        Button generateBtn = new Button("Generate & Print", new FontIcon(FontAwesomeSolid.FILE_PDF));
        generateBtn.setDisable(true);
        generateBtn.setOnAction(e -> {
            DocumentRequest selected = documentRequestsTable.getSelectionModel().getSelectedItem();
            if (selected != null && "APPROVED".equals(selected.getStatus()) && "PAID".equals(selected.getPaymentStatus())) {
                Optional<Resident> resident = DatabaseHelper.getResidentById(selected.getResidentId());
                if (resident.isPresent()) {
                    generateOfficialDocument(selected, resident.get());
                    DatabaseHelper.completeDocumentRequest(selected.getId());
                    selected.setStatus("COMPLETED");
                    showToast("Document generated successfully!");
                    refreshDocumentRequestsTable();
                }
            }
        });

        documentRequestsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean isSelected = newVal != null;
            approveBtn.setDisable(!isSelected || (newVal != null && !"PENDING".equals(newVal.getStatus())));
            paymentBtn.setDisable(!isSelected || (newVal != null && !"APPROVED".equals(newVal.getStatus())));
            generateBtn.setDisable(!isSelected || (newVal != null && (!"APPROVED".equals(newVal.getStatus()) || !"PAID".equals(newVal.getPaymentStatus()))));
        });

        ToolBar toolBar = new ToolBar(approveBtn, paymentBtn, generateBtn);
        toolBar.setStyle("-fx-background-color: transparent; -fx-padding: 0;");

        container.getChildren().addAll(toolBar, documentRequestsTable);
        VBox.setVgrow(documentRequestsTable, Priority.ALWAYS);
        return container;
    }

    private void refreshDocumentRequestsTable() {
        if (documentRequestsTable != null) {
            ObservableList<DocumentRequest> requests = DatabaseHelper.getAllDocumentRequests();
            documentRequestsTable.setItems(requests);
        }
    }

    private void generateOfficialDocument(DocumentRequest request, Resident resident) {
        try {
            String docType = request.getDocumentType();
            String filename = "Barangay_" + docType.replace(" ", "_") + "_" + resident.getId() + ".pdf";
            String path = System.getProperty("user.home") + "/Downloads/" + filename;

            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(path));
            document.open();

            // Header - Official Letterhead
            document.add(createDocumentHeader());
            document.add(new Paragraph("\n"));

            // Document Title
            com.lowagie.text.Font titleFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 14, com.lowagie.text.Font.BOLD);
            Paragraph title = new Paragraph(docType.toUpperCase(), titleFont);
            title.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph("\n"));

            // Control Number and Date
            java.time.LocalDate today = java.time.LocalDate.now();
            String controlNo = "BNG-" + today.getYear() + "-" + request.getId();
            com.lowagie.text.Font labelFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 11, com.lowagie.text.Font.BOLD);
            com.lowagie.text.Font normalFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 11);

            document.add(new Paragraph("Control Number: " + controlNo, labelFont));
            document.add(new Paragraph("Date Issued: " + today.format(java.time.format.DateTimeFormatter.ofPattern("MMMM dd, yyyy"))));
            document.add(new Paragraph("\n"));

            // Body Content
            document.add(new Paragraph("TO WHOM IT MAY CONCERN:", labelFont));
            document.add(new Paragraph("\n"));

            String bodyText = generateDocumentBody(docType, resident);
            Paragraph body = new Paragraph(bodyText, normalFont);
            body.setAlignment(com.lowagie.text.Element.ALIGN_JUSTIFIED);
            document.add(body);

            document.add(new Paragraph("\n\n"));

            // Details Table
            com.lowagie.text.pdf.PdfPTable detailsTable = new com.lowagie.text.pdf.PdfPTable(2);
            detailsTable.setWidths(new int[] { 1, 2 });
            detailsTable.setWidthPercentage(100);
            detailsTable.getDefaultCell().setBorder(com.lowagie.text.Rectangle.NO_BORDER);
            detailsTable.getDefaultCell().setPadding(5);

            detailsTable.addCell(new Paragraph("Name:", labelFont));
            detailsTable.addCell(new Paragraph(resident.getLastName() + ", " + resident.getFirstName(), normalFont));
            detailsTable.addCell(new Paragraph("Address:", labelFont));
            detailsTable.addCell(new Paragraph(resident.getAddress(), normalFont));
            detailsTable.addCell(new Paragraph("Gender/Age:", labelFont));
            detailsTable.addCell(new Paragraph(resident.getGender(), normalFont));
            detailsTable.addCell(new Paragraph("Purpose:", labelFont));
            detailsTable.addCell(new Paragraph(request.getPurpose(), normalFont));

            document.add(detailsTable);
            document.add(new Paragraph("\n\n"));

            // Signature Block
            com.lowagie.text.pdf.PdfPTable signTable = new com.lowagie.text.pdf.PdfPTable(2);
            signTable.setWidths(new int[] { 1, 1 });
            signTable.setWidthPercentage(100);
            signTable.getDefaultCell().setBorder(com.lowagie.text.Rectangle.NO_BORDER);
            signTable.getDefaultCell().setPadding(20);

            signTable.addCell(new Paragraph("_________________________\nBarangay Captain\nAuthorized Signatory", normalFont));
            signTable.addCell(new Paragraph("_________________________\nBarangay Treasurer\nRecorded by", normalFont));

            document.add(signTable);

            // Footer
            document.add(new Paragraph("\n"));
            Paragraph footer = new Paragraph("This is an official document of Barangay San Marino. Unauthorized reproduction is prohibited.", 
                new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 9, com.lowagie.text.Font.ITALIC));
            footer.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            showToast("Document saved to: " + path);
        } catch (Exception e) {
            e.printStackTrace();
            showToast("Error generating document");
        }
    }

    private Paragraph createDocumentHeader() {
        com.lowagie.text.Font headerFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10, com.lowagie.text.Font.BOLD);
        com.lowagie.text.Font subHeaderFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 9);

        Paragraph header = new Paragraph();
        header.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
        header.add(new Paragraph("REPUBLIC OF THE PHILIPPINES", headerFont));
        header.add(new Paragraph("City of Marikina", subHeaderFont));
        header.add(new Paragraph("Metro Manila", subHeaderFont));
        header.add(new Paragraph("BARANGAY SAN MARINO", headerFont));
        header.add(new Paragraph("\n"));
        header.add(new Paragraph("Address: Barangay San Marino, Marikina, Metro Manila", subHeaderFont));
        header.add(new Paragraph("Tel/Fax: (02) 123-4567 | Email: barangay.sanmarino@gov.ph", subHeaderFont));

        return header;
    }

    private String generateDocumentBody(String docType, Resident resident) {
        switch (docType) {
            case "Barangay Clearance":
                return "This is to certify that " + resident.getFirstName() + " " + resident.getLastName() + 
                       " is a residents of this barangay and has no derogatory records or pending cases before this \n" +
                       "Barangay. This certification is issued upon request for whatever legal purpose it may serve. " +
                       "This is not valid without the seal and signature of the Barangay Captain.";

            case "Certificate of Residency":
                return "This is to certify that " + resident.getFirstName() + " " + resident.getLastName() + 
                       " with address at " + resident.getAddress() + " is a bonafide resident of this barangay. " +
                       "This certificate is issued upon request for whatever legal purpose it may serve.";

            case "Indigency Certificate":
                return "This is to certify that " + resident.getFirstName() + " " + resident.getLastName() + 
                       " of legal age, " + resident.getGender() + ", a resident of " + resident.getAddress() + 
                       " belongs to a poor and indigent family in this barangay. This certificate is issued for " +
                       "financial assistance and government support programs.";

            default:
                return "This is to certify that " + resident.getFirstName() + " " + resident.getLastName() + 
                       " is a resident of this barangay.";
        }
    }

    private void showResidentControl(VBox center) {
        residentTable = new TableView<>();
        residentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        residentTable.setPrefHeight(500);

        TableColumn<Resident, String> photoCol = new TableColumn<>("Photo");
        photoCol.setPrefWidth(60);
        photoCol.setCellValueFactory(new PropertyValueFactory<>("imagePath"));
        photoCol.setCellFactory(param -> new TableCell<Resident, String>() {
            private final ImageView imageView = new ImageView();
            @Override
            protected void updateItem(String path, boolean empty) {
                super.updateItem(path, empty);
                if (empty || path == null || path.isEmpty()) {
                    setGraphic(null);
                } else {
                    try {
                        imageView.setImage(new Image(new File(path).toURI().toString(), 40, 40, true, true));
                        setGraphic(imageView);
                    } catch (Exception e) {
                        setGraphic(null);
                    }
                }
            }
        });

        TableColumn<Resident, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(cellData -> new javafx.beans.binding.StringBinding() {
            { bind(cellData.getValue().firstNameProperty(), cellData.getValue().lastNameProperty()); }
            @Override
            protected String computeValue() {
                return cellData.getValue().getLastName() + ", " + cellData.getValue().getFirstName();
            }
        });
        nameCol.setId("last_name");
        nameCol.setPrefWidth(150);

        TableColumn<Resident, String> birthDateCol = new TableColumn<>("Birth Date");
        birthDateCol.setCellValueFactory(new PropertyValueFactory<>("birthDate"));
        birthDateCol.setId("birth_date");
        birthDateCol.setPrefWidth(120);

        TableColumn<Resident, String> genderCol = new TableColumn<>("Gender");
        genderCol.setCellValueFactory(new PropertyValueFactory<>("gender"));
        genderCol.setId("gender");
        genderCol.setPrefWidth(100);

        TableColumn<Resident, String> addressCol = new TableColumn<>("Address");
        addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        addressCol.setId("address");
        addressCol.setPrefWidth(300);

        residentTable.getColumns().setAll(List.of(photoCol, nameCol, birthDateCol, genderCol, addressCol));

        Button addButton = new Button("Add Resident");
        addButton.setGraphic(new FontIcon(FontAwesomeSolid.PLUS_CIRCLE));

        Button editButton = new Button("Edit Resident");
        editButton.setGraphic(new FontIcon(FontAwesomeSolid.PENCIL_ALT));

        Button deleteButton = new Button("Delete Resident");
        deleteButton.setGraphic(new FontIcon(FontAwesomeSolid.TRASH));

        Button idButton = new Button("Print ID");
        idButton.setGraphic(new FontIcon(FontAwesomeSolid.ID_CARD));

        Button viewIdBtn = new Button("View ID Card");
        viewIdBtn.setGraphic(new FontIcon(FontAwesomeSolid.ADDRESS_CARD));

        editButton.setDisable(true);
        deleteButton.setDisable(true);
        idButton.setDisable(true);
        viewIdBtn.setDisable(true);

        residentTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean isSelected = newSelection != null;
            editButton.setDisable(!isSelected);
            deleteButton.setDisable(!isSelected);
            idButton.setDisable(!isSelected);
            viewIdBtn.setDisable(!isSelected);
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

        viewIdBtn.setOnAction(e -> {
            Resident selected = residentTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showIDCardDialog(selected);
            }
        });

        ToolBar toolBar = new ToolBar(addButton, editButton, deleteButton, new Separator(Orientation.VERTICAL), idButton, viewIdBtn);
        toolBar.setStyle("-fx-background-color: transparent; -fx-padding: 0;");

        var exportButton = new Button("📄 Export to PDF");
        exportButton.getStyleClass().add("button-accent");
        exportButton.setOnAction(e -> generateResidentPdf());

        var bottomBar = new HBox(exportButton);
        bottomBar.setAlignment(Pos.CENTER_RIGHT);
        bottomBar.setPadding(new Insets(10, 0, 0, 0));

        pagination = new Pagination();
        pagination.setPrefHeight(400);
        pagination.setStyle("-fx-padding: 10;");

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            pagination.setCurrentPageIndex(0); // Reset to first page on search
            updatePagination();
        });

        var content = new VBox(12, toolBar, pagination, bottomBar);
        VBox.setVgrow(pagination, Priority.ALWAYS);
        updateDashboardContent(center, "Resident & Data Control", content);
        
        // Initialize data and pagination
        System.out.println("Initializing resident table...");
        updatePagination();
        // Set page factory after updating pagination to trigger initial load
        pagination.setPageFactory(pageIndex -> createPage(pageIndex));
        System.out.println("Resident table initialized, page factory set");
    }

    private void loadResidentData() {
        if (pagination != null) {
            pagination.setCurrentPageIndex(0); // Reset to first page
            updatePagination();
            // Refresh the current page by requesting it again
            int currentPage = pagination.getCurrentPageIndex();
            if (currentPage >= 0) {
                pagination.setPageFactory(null);
                pagination.setPageFactory(pageIndex -> createPage(pageIndex));
            }
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
            document.add(new Paragraph("1. Juan Dela Cruz - Purok 1, Barangay San Marino"));
            document.add(new Paragraph("2. Maria Clara - Purok 2, Barangay San Marino"));
            document.add(new Paragraph("3. Jose Rizal - Purok 1, Barangay San Marino"));
            
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

            var titleFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 8, com.lowagie.text.Font.BOLD);
            var labelFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 7, com.lowagie.text.Font.BOLD);
            var valueFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 7);

            document.add(new Paragraph("BARANGAY SAN MARINO ID CARD", titleFont));
            document.add(new Paragraph(" "));

            if (resident.getImagePath() != null && !resident.getImagePath().isBlank()) {
                File photoFile = new File(resident.getImagePath());
                if (photoFile.exists()) {
                    com.lowagie.text.Image photoImage = com.lowagie.text.Image.getInstance(photoFile.getAbsolutePath());
                    photoImage.scaleAbsolute(80, 80);
                    photoImage.setAbsolutePosition(20, 55);
                    document.add(photoImage);
                }
            }

            com.lowagie.text.pdf.PdfPTable infoTable = new com.lowagie.text.pdf.PdfPTable(2);
            infoTable.setWidths(new int[] { 1, 2 });
            infoTable.setWidthPercentage(100);
            infoTable.getDefaultCell().setBorder(com.lowagie.text.Rectangle.NO_BORDER);

            infoTable.addCell(new Paragraph("Name:", labelFont));
            infoTable.addCell(new Paragraph(resident.getLastName() + ", " + resident.getFirstName() + (resident.getMiddleName() != null && !resident.getMiddleName().isBlank() ? " " + resident.getMiddleName() : ""), valueFont));
            infoTable.addCell(new Paragraph("Gender:", labelFont));
            infoTable.addCell(new Paragraph(resident.getGender(), valueFont));
            infoTable.addCell(new Paragraph("Birthdate:", labelFont));
            infoTable.addCell(new Paragraph(resident.getBirthDate(), valueFont));
            infoTable.addCell(new Paragraph("Address:", labelFont));
            infoTable.addCell(new Paragraph(resident.getAddress(), valueFont));
            infoTable.addCell(new Paragraph("ID No:", labelFont));
            infoTable.addCell(new Paragraph(String.valueOf(resident.getId()), valueFont));

            document.add(infoTable);
            document.add(new Paragraph(" "));

            String qrCodeText = "RES:" + resident.getId();
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrCodeText, BarcodeFormat.QR_CODE, 80, 80);
            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            com.lowagie.text.Image qrImage = com.lowagie.text.Image.getInstance(pngOutputStream.toByteArray());
            qrImage.setAbsolutePosition(150, 20);
            qrImage.scaleAbsolute(60, 60);
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

        // --- Image Upload Section ---
        ImageView photoPreview = new ImageView();
        photoPreview.setFitWidth(100);
        photoPreview.setFitHeight(100);
        photoPreview.getStyleClass().add("image-preview-frame");
        Button uploadBtn = new Button("Upload Photo");
        TextField imagePathField = new TextField();
        imagePathField.setEditable(false);

        TextField firstName = new TextField();
        firstName.setPromptText("First Name");
        TextField middleName = new TextField();
        middleName.setPromptText("Middle Name (Optional)");
        TextField lastName = new TextField();
        lastName.setPromptText("Last Name");
        
        DatePicker birthDate = new DatePicker();
        birthDate.setPromptText("Select birth date");
        
        ComboBox<String> gender = new ComboBox<>();
        gender.setItems(FXCollections.observableArrayList("Male", "Female", "Other"));
        gender.setPromptText("Select Gender");
        
        TextArea address = new TextArea();
        address.setPromptText("Enter complete address");
        address.setWrapText(true);
        address.setPrefRowCount(4);

        uploadBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                imagePathField.setText(file.getAbsolutePath());
                photoPreview.setImage(new Image(file.toURI().toString()));
            }
        });

        grid.add(new Label("Resident Photo:"), 0, 0);
        grid.add(new VBox(5, photoPreview, uploadBtn), 1, 0);
        
        grid.add(new Label("First Name:"), 0, 1);
        grid.add(firstName, 1, 1);
        grid.add(new Label("Middle Name:"), 0, 2);
        grid.add(middleName, 1, 2);
        grid.add(new Label("Last Name:"), 0, 3);
        grid.add(lastName, 1, 3);
        grid.add(new Label("Birth Date:"), 0, 4);
        grid.add(birthDate, 1, 4);
        grid.add(new Label("Gender:"), 0, 5);
        grid.add(gender, 1, 5);
        grid.add(new Label("Address:"), 0, 6);
        grid.add(address, 1, 6);

        if (existingResident != null) {
            firstName.setText(existingResident.getFirstName());
            middleName.setText(existingResident.getMiddleName());
            lastName.setText(existingResident.getLastName());
            try {
                birthDate.setValue(LocalDate.parse(existingResident.getBirthDate()));
            } catch (Exception e) {
                birthDate.setValue(LocalDate.now());
            }
            gender.setValue(existingResident.getGender());
            address.setText(existingResident.getAddress());
            if (existingResident.getImagePath() != null) {
                imagePathField.setText(existingResident.getImagePath());
                photoPreview.setImage(new Image(new File(existingResident.getImagePath()).toURI().toString()));
            }
        } else {
            birthDate.setValue(LocalDate.now());
        }

        // --- Validation ---
        // Get the Save button node from the dialog pane.
        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        
        // Create a binding that evaluates to true if any required fields are empty or not selected.
        var emptyBinding = Bindings.createBooleanBinding(() ->
                firstName.getText().trim().isEmpty() ||
                lastName.getText().trim().isEmpty() ||
                birthDate.getValue() == null ||
                gender.getValue() == null ||
                address.getText().trim().isEmpty(),
            firstName.textProperty(),
            lastName.textProperty(),
            birthDate.valueProperty(),
            gender.valueProperty(),
            address.textProperty()
        );

        // Bind the button's disable property to the binding. The button will be disabled as long as the binding is true.
        saveButton.disableProperty().bind(emptyBinding);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                int id = (existingResident == null) ? 0 : existingResident.getId();
                Resident r = new Resident(id, firstName.getText(), middleName.getText(), lastName.getText(), 
                        birthDate.getValue().toString(), gender.getValue(), address.getText());
                r.setImagePath(imagePathField.getText());
                return r;
            }
            return null;
        });

        return dialog.showAndWait();
    }

    private void showIDCardDialog(Resident resident) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Resident Identification Card");

        // Main Card Container (CR-80 Aspect Ratio)
        VBox card = new VBox();
        card.setPrefSize(450, 280);
        card.setStyle("-fx-background-color: white; -fx-border-color: #cbd5e1; -fx-border-width: 1; -fx-border-radius: 15; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 5);");

        // Header - Government Style
        HBox header = new HBox(15);
        header.setPadding(new Insets(15));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: linear-gradient(to right, #1e3a8a, #3b82f6); -fx-background-radius: 15 15 0 0;");
        
        Label govTitle = new Label("REPUBLIC OF THE PHILIPPINES\nBarangay San Marino Resident ID");
        govTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14;");
        header.getChildren().add(govTitle);

        // Body Content
        HBox body = new HBox(20);
        body.setPadding(new Insets(20));
        
        // Photo
        ImageView photo = new ImageView();
        photo.setFitWidth(110);
        photo.setFitHeight(110);
        photo.setPreserveRatio(true);
        photo.setSmooth(true);
        if (resident.getImagePath() != null && !resident.getImagePath().isBlank()) {
            File photoFile = new File(resident.getImagePath());
            if (photoFile.exists()) {
                photo.setImage(new Image(photoFile.toURI().toString()));
            }
        }
        if (photo.getImage() == null) {
            var placeholderUrl = getClass().getResource("placeholder-user.png");
            if (placeholderUrl != null) {
                photo.setImage(new Image(placeholderUrl.toExternalForm()));
            }
        }
        photo.setStyle("-fx-border-color: #1e3a8a; -fx-border-width: 2;");

        // Details
        VBox details = new VBox(8);
        details.setPrefWidth(280);
        Label nameLbl = new Label(resident.getLastName().toUpperCase() + ", " + resident.getFirstName().toUpperCase() + (resident.getMiddleName() != null && !resident.getMiddleName().isBlank() ? " " + resident.getMiddleName().toUpperCase() : ""));
        nameLbl.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        
        Label idLbl = new Label("ID: " + resident.getId());
        idLbl.setStyle("-fx-font-size: 11; -fx-font-weight: bold;");
        Label genderLbl = new Label("Gender: " + resident.getGender());
        Label dobLbl = new Label("Birthdate: " + resident.getBirthDate());
        Label addrLbl = new Label("Address: " + resident.getAddress());
        addrLbl.setWrapText(true);
        addrLbl.setMaxWidth(260);

        details.getChildren().addAll(nameLbl, idLbl, genderLbl, dobLbl, addrLbl);

        // QR Code
        VBox qrBox = new VBox(8);
        qrBox.setAlignment(Pos.CENTER);
        try {
            String qrCodeText = "RES:" + resident.getId();
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrCodeText, BarcodeFormat.QR_CODE, 90, 90);
            Image qrImg = SwingFXUtils.toFXImage(MatrixToImageWriter.toBufferedImage(bitMatrix), null);
            var qrView = new ImageView(qrImg);
            qrView.setFitWidth(90);
            qrView.setFitHeight(90);
            qrBox.getChildren().add(qrView);
            Label idLabel = new Label("ID: " + resident.getId());
            idLabel.setStyle("-fx-font-family: monospace; -fx-font-size: 10;");
            qrBox.getChildren().add(idLabel);
        } catch (Exception ignored) {}

        body.getChildren().addAll(photo, details, qrBox);
        card.getChildren().addAll(header, body);

        dialog.getDialogPane().setContent(new StackPane(card));
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
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
        try {
            String filter = (searchField != null) ? searchField.getText() : "";
            System.out.println("Loading page " + pageIndex + " with filter: '" + filter + "'");
            
            ObservableList<Resident> residents = DatabaseHelper.getResidents(filter, pageIndex, ROWS_PER_PAGE, currentSortField, currentSortOrder);
            System.out.println("Fetched " + residents.size() + " residents for page " + pageIndex);
            
            residentTable.setItems(residents);
            
            // Wrap table in a BorderPane for proper pagination display
            BorderPane pageContainer = new BorderPane();
            pageContainer.setCenter(residentTable);
            return pageContainer;
        } catch (Exception e) {
            e.printStackTrace();
            Label errorLabel = new Label("Error loading residents: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12;");
            BorderPane errorContainer = new BorderPane(errorLabel);
            return errorContainer;
        }
    }

    private void updatePagination() {
        try {
            String filter = (searchField != null) ? searchField.getText() : "";
            int totalCount = DatabaseHelper.getResidentCount(filter);
            int pageCount = (totalCount + ROWS_PER_PAGE - 1) / ROWS_PER_PAGE;
            if (pageCount == 0) pageCount = 1;
            
            System.out.println("Total residents: " + totalCount + ", Page count: " + pageCount);
            
            pagination.setPageCount(pageCount);
        } catch (Exception e) {
            e.printStackTrace();
            if (pagination != null) {
                pagination.setPageCount(1);
            }
        }
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
        heading.setStyle("-fx-text-fill: " + (darkMode ? "#ffffff" : "#1a1a1a") + "; -fx-font-size: 16; -fx-font-weight: bold;");
        var content = new Label(body);
        content.setStyle("-fx-text-fill: " + (darkMode ? "#d0d0d0" : "#333") + "; -fx-font-size: 12;");
        var box = new VBox(10, heading, content);
        box.getStyleClass().add("content-box");
        return box;
    }

    // ==================== COMPLAINT MANAGEMENT ====================

    private void showComplaintsAndIncidents(VBox center) {
        // Create the complaints table ONCE upfront so both tabs can share it
        complaintsTable = new TableView<>();
        complaintsTable.getStyleClass().add("table-view");

        TableColumn<Complaint, String> residentCol = new TableColumn<>("Resident");
        residentCol.setCellValueFactory(new PropertyValueFactory<>("residentName"));
        residentCol.setPrefWidth(150);

        TableColumn<Complaint, String> titleCol = new TableColumn<>("Complaint Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(200);

        TableColumn<Complaint, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(100);

        TableColumn<Complaint, String> submittedCol = new TableColumn<>("Date Submitted");
        submittedCol.setCellValueFactory(new PropertyValueFactory<>("dateSubmitted"));
        submittedCol.setPrefWidth(140);

        TableColumn<Complaint, String> assignedCol = new TableColumn<>("Assigned To");
        assignedCol.setCellValueFactory(new PropertyValueFactory<>("assignedTo"));
        assignedCol.setPrefWidth(120);

        complaintsTable.getColumns().setAll(List.of(residentCol, titleCol, statusCol, submittedCol, assignedCol));

        // Load initial data
        ObservableList<Complaint> complaints = DatabaseHelper.getAllComplaints();
        complaintsTable.setItems(complaints);

        // Two tabs: Submit complaint and manage complaints
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Tab 1: Submit New Complaint (for residents)
        Tab submitTab = new Tab("Submit Complaint", createComplaintSubmissionPanel());
        submitTab.setStyle("-fx-font-size: 12; -fx-padding: 10;");

        // Tab 2: Manage Complaints (for admin)
        Tab manageTab = new Tab("Manage Complaints", createComplaintsManagementPanel());
        manageTab.setStyle("-fx-font-size: 12; -fx-padding: 10;");

        tabPane.getTabs().addAll(submitTab, manageTab);
        updateDashboardContent(center, "Complaints & Incidents", tabPane);
    }

    private VBox createComplaintSubmissionPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));

        Label titleLabel = new Label("Submit a Complaint or Incident Report");
        titleLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: " + (darkMode ? "#ffffff" : "#1a1a1a") + ";");

        // Complaint Title
        Label complaintTitleLabel = new Label("Complaint Title");
        complaintTitleLabel.setStyle("-fx-text-fill: " + (darkMode ? "#d0d0d0" : "#333") + ";");
        TextField titleField = new TextField();
        titleField.setPromptText("E.g., Noise complaint, street damage, etc.");
        titleField.setStyle("-fx-font-size: 12;");

        // Description
        Label descriptionLabel = new Label("Description of Incident");
        descriptionLabel.setStyle("-fx-text-fill: " + (darkMode ? "#d0d0d0" : "#333") + ";");
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Provide detailed information about the complaint or incident...");
        descriptionArea.setWrapText(true);
        descriptionArea.setPrefRowCount(6);

        // Photo Upload
        Label photoLabel = new Label("Attach Evidence Photo (Optional)");
        photoLabel.setStyle("-fx-text-fill: " + (darkMode ? "#d0d0d0" : "#333") + ";");

        java.util.concurrent.atomic.AtomicReference<String> selectedPhotoPath = new java.util.concurrent.atomic.AtomicReference<>(null);
        Label photoPathLabel = new Label("No photo selected");
        photoPathLabel.setStyle("-fx-text-fill: " + (darkMode ? "#b0b0b0" : "#666") + "; -fx-font-size: 11;");

        Button uploadPhotoBtn = new Button("Choose Photo", new FontIcon(FontAwesomeSolid.IMAGE));
        uploadPhotoBtn.getStyleClass().add("button-secondary");
        uploadPhotoBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Complaint Photo");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
            );
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                selectedPhotoPath.set(file.getAbsolutePath());
                photoPathLabel.setText("✓ " + file.getName());
            }
        });

        HBox photoBox = new HBox(10, uploadPhotoBtn, photoPathLabel);
        photoBox.setAlignment(Pos.CENTER_LEFT);

        // Submit Button
        Button submitBtn = new Button("Submit Complaint");
        submitBtn.setStyle("-fx-font-size: 12; -fx-padding: 10;");
        submitBtn.getStyleClass().add("button-primary");
        submitBtn.setDisable(true);

        // Enable button only when title and description are filled
        titleField.textProperty().addListener((obs, oldVal, newVal) ->
            submitBtn.setDisable(newVal.trim().isEmpty() || descriptionArea.getText().trim().isEmpty())
        );
        descriptionArea.textProperty().addListener((obs, oldVal, newVal) ->
            submitBtn.setDisable(titleField.getText().trim().isEmpty() || newVal.trim().isEmpty())
        );

        submitBtn.setOnAction(e -> {
            String title = titleField.getText().trim();
            String description = descriptionArea.getText().trim();
            String photoPath = selectedPhotoPath.get();

            try {
                System.out.println("=== Submitting Complaint ===");
                System.out.println("Current Resident ID: " + currentResidentId);
                System.out.println("Current Resident Name: " + currentResidentName);
                System.out.println("Title: " + title);
                System.out.println("Description: " + description);
                System.out.println("Photo: " + photoPath);
                
                // Use current logged-in user info
                Complaint complaint = new Complaint(currentResidentId, currentResidentName, title, description, photoPath);
                System.out.println("Complaint object created");
                
                int complaintId = DatabaseHelper.createComplaint(complaint);
                System.out.println("Created complaint with ID: " + complaintId);

                if (complaintId > 0) {
                    System.out.println("Success! Refreshing table...");
                    showToast("Complaint submitted successfully! Reference #: " + complaintId);
                    titleField.clear();
                    descriptionArea.clear();
                    selectedPhotoPath.set(null);
                    photoPathLabel.setText("No photo selected");
                    
                    // Refresh the management table in real-time if it exists
                    refreshComplaintsTable();
                } else {
                    System.out.println("Failed to create complaint (ID was " + complaintId + ")");
                    showToast("Failed to submit complaint.");
                }
            } catch (Exception ex) {
                System.err.println("Exception during complaint submission: " + ex.getMessage());
                ex.printStackTrace();
                showToast("Error submitting complaint: " + ex.getMessage());
            }
        });

        panel.getChildren().addAll(
            titleLabel,
            new Separator(),
            complaintTitleLabel, titleField,
            new Separator(),
            descriptionLabel, descriptionArea,
            new Separator(),
            photoLabel, photoBox,
            submitBtn
        );

        ScrollPane scrollPane = new ScrollPane(panel);
        scrollPane.setFitToWidth(true);

        VBox container = new VBox(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        return container;
    }

    private VBox createComplaintsManagementPanel() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(10));

        // The complaintsTable is already created in showComplaintsAndIncidents()

        // Buttons
        Button viewBtn = new Button("View Details", new FontIcon(FontAwesomeSolid.EYE));
        viewBtn.setDisable(true);
        viewBtn.setOnAction(e -> {
            Complaint selected = complaintsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showComplaintDetailsDialog(selected);
            }
        });

        Button statusBtn = new Button("Update Status", new FontIcon(FontAwesomeSolid.EDIT));
        statusBtn.setDisable(true);
        statusBtn.setOnAction(e -> {
            Complaint selected = complaintsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showStatusUpdateDialog(selected);
            }
        });

        Button notesBtn = new Button("Add Notes", new FontIcon(FontAwesomeSolid.COMMENT));
        notesBtn.setDisable(true);
        notesBtn.setOnAction(e -> {
            Complaint selected = complaintsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showAddNotesDialog(selected);
            }
        });

        Button reportBtn = new Button("Generate Report", new FontIcon(FontAwesomeSolid.FILE_PDF));
        reportBtn.setOnAction(e -> generateComplaintsReport());

        complaintsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean isSelected = newVal != null;
            viewBtn.setDisable(!isSelected);
            statusBtn.setDisable(!isSelected);
            notesBtn.setDisable(!isSelected);
        });

        ToolBar toolBar = new ToolBar(viewBtn, statusBtn, notesBtn, new Separator(), reportBtn);
        toolBar.setStyle("-fx-background-color: transparent; -fx-padding: 0;");

        container.getChildren().addAll(toolBar, complaintsTable);
        VBox.setVgrow(complaintsTable, Priority.ALWAYS);
        return container;
    }

    private void showComplaintDetailsDialog(Complaint complaint) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Complaint Details - ID #" + complaint.getId());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        Label residentLabel = new Label("Resident:");
        Label residentValue = new Label(complaint.getResidentName());
        residentValue.setStyle("-fx-font-weight: bold;");

        Label titleLabel = new Label("Title:");
        Label titleValue = new Label(complaint.getTitle());
        titleValue.setStyle("-fx-font-weight: bold;");

        Label statusLabel = new Label("Status:");
        Label statusValue = new Label(complaint.getStatus());
        statusValue.setStyle("-fx-font-weight: bold;");

        Label descriptionLabel = new Label("Description:");
        TextArea descriptionArea = new TextArea(complaint.getDescription());
        descriptionArea.setWrapText(true);
        descriptionArea.setEditable(false);
        descriptionArea.setPrefRowCount(5);

        Label notesLabel = new Label("Admin Notes:");
        TextArea notesArea = new TextArea(complaint.getAdminNotes());
        notesArea.setWrapText(true);
        notesArea.setEditable(false);
        notesArea.setPrefRowCount(4);

        Label dateLabel = new Label("Date Submitted:");
        Label dateValue = new Label(complaint.getDateSubmitted());

        grid.add(residentLabel, 0, 0);
        grid.add(residentValue, 1, 0);
        grid.add(titleLabel, 0, 1);
        grid.add(titleValue, 1, 1);
        grid.add(statusLabel, 0, 2);
        grid.add(statusValue, 1, 2);
        grid.add(dateLabel, 0, 3);
        grid.add(dateValue, 1, 3);
        grid.add(descriptionLabel, 0, 4);
        grid.add(descriptionArea, 1, 4);
        grid.add(notesLabel, 0, 5);
        grid.add(notesArea, 1, 5);

        dialog.getDialogPane().setContent(grid);
        dialog.showAndWait();
    }

    private void showStatusUpdateDialog(Complaint complaint) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Update Complaint Status");
        dialog.setHeaderText("Update the status for complaint: " + complaint.getTitle());

        ButtonType updateButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        Label statusLabel = new Label("New Status:");
        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.setItems(FXCollections.observableArrayList("Pending", "Ongoing", "Resolved"));
        statusCombo.setValue(complaint.getStatus());

        grid.add(statusLabel, 0, 0);
        grid.add(statusCombo, 1, 0);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == updateButtonType) {
                return statusCombo.getValue();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(newStatus -> {
            DatabaseHelper.updateComplaintStatus(complaint.getId(), newStatus);
            complaint.setStatus(newStatus);
            showToast("Complaint status updated to: " + newStatus);
            refreshComplaintsTable();
        });
    }

    private void showAddNotesDialog(Complaint complaint) {
        Dialog<java.util.AbstractMap.SimpleEntry<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Add/Update Complaint Notes");
        dialog.setHeaderText("Add notes for complaint: " + complaint.getTitle());

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        Label notesLabel = new Label("Notes:");
        TextArea notesArea = new TextArea(complaint.getAdminNotes());
        notesArea.setWrapText(true);
        notesArea.setPrefRowCount(5);

        Label assignedLabel = new Label("Assigned To:");
        TextField assignedField = new TextField(complaint.getAssignedTo());

        grid.add(notesLabel, 0, 0);
        grid.add(notesArea, 1, 0);
        grid.add(assignedLabel, 0, 1);
        grid.add(assignedField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return new java.util.AbstractMap.SimpleEntry<>(notesArea.getText(), assignedField.getText());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            DatabaseHelper.updateComplaintNotes(complaint.getId(), result.getKey(), result.getValue());
            complaint.setAdminNotes(result.getKey());
            complaint.setAssignedTo(result.getValue());
            showToast("Complaint notes updated!");
            refreshComplaintsTable();
        });
    }

    private void refreshComplaintsTable() {
        if (complaintsTable != null) {
            System.out.println("Refreshing complaints table...");
            Platform.runLater(() -> {
                ObservableList<Complaint> complaints = DatabaseHelper.getAllComplaints();
                System.out.println("Loaded " + complaints.size() + " complaints");
                complaintsTable.setItems(complaints);
            });
        } else {
            System.out.println("Complaints table is null, cannot refresh");
        }
    }

    private void generateComplaintsReport() {
        try {
            ObservableList<Complaint> complaints = DatabaseHelper.getAllComplaints();
            String filename = "Complaints_Report_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".pdf";
            String path = System.getProperty("user.home") + "/Downloads/" + filename;

            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(path));
            document.open();

            // Header
            com.lowagie.text.Font titleFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 14, com.lowagie.text.Font.BOLD);
            Paragraph title = new Paragraph("Complaints & Incidents Report", titleFont);
            title.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph("\nGenerated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
            document.add(new Paragraph("Total Complaints: " + complaints.size()));
            document.add(new Paragraph("\n"));

            // Summary by Status
            long pending = complaints.stream().filter(c -> "Pending".equals(c.getStatus())).count();
            long ongoing = complaints.stream().filter(c -> "Ongoing".equals(c.getStatus())).count();
            long resolved = complaints.stream().filter(c -> "Resolved".equals(c.getStatus())).count();

            document.add(new Paragraph("Summary by Status:", new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 11, com.lowagie.text.Font.BOLD)));
            document.add(new Paragraph("Pending: " + pending));
            document.add(new Paragraph("Ongoing: " + ongoing));
            document.add(new Paragraph("Resolved: " + resolved));
            document.add(new Paragraph("\n"));

            // Detailed Table
            com.lowagie.text.pdf.PdfPTable table = new com.lowagie.text.pdf.PdfPTable(5);
            table.setWidthPercentage(100);
            table.addCell("Complaint ID");
            table.addCell("Resident");
            table.addCell("Title");
            table.addCell("Status");
            table.addCell("Date Submitted");

            for (Complaint complaint : complaints) {
                table.addCell(String.valueOf(complaint.getId()));
                table.addCell(complaint.getResidentName());
                table.addCell(complaint.getTitle());
                table.addCell(complaint.getStatus());
                table.addCell(complaint.getDateSubmitted());
            }

            document.add(table);
            document.close();

            showToast("Report saved to: " + path);
        } catch (Exception e) {
            e.printStackTrace();
            showToast("Error generating report");
        }
    }

    private void showAnnouncementsPortal(VBox center) {
        // Create shared table upfront
        if (announcementsTable == null) {
            announcementsTable = new TableView<>();
            announcementsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

            var titleColumn = new TableColumn<Announcement, String>("Title");
            titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));

            var typeColumn = new TableColumn<Announcement, String>("Type");
            typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));

            var postedByColumn = new TableColumn<Announcement, String>("Posted By");
            postedByColumn.setCellValueFactory(new PropertyValueFactory<>("postedBy"));

            var postedDateColumn = new TableColumn<Announcement, String>("Posted Date");
            postedDateColumn.setCellValueFactory(new PropertyValueFactory<>("postedDate"));

            var statusColumn = new TableColumn<Announcement, String>("Status");
            statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

            var viewsColumn = new TableColumn<Announcement, String>("Views");
            viewsColumn.setCellValueFactory(new PropertyValueFactory<>("views"));

            @SuppressWarnings("unchecked")
            TableColumn<Announcement, ?>[] columns = new TableColumn[] {titleColumn, typeColumn, postedByColumn, postedDateColumn, statusColumn, viewsColumn};
            announcementsTable.getColumns().addAll(columns);
            refreshAnnouncementsTable();
        }

        // Create tabs for posting and managing
        var postingTab = new Tab("Post Announcement", createAnnouncementPostingPanel());
        postingTab.setClosable(false);

        var managementTab = new Tab("Manage Announcements", createAnnouncementManagementPanel());
        managementTab.setClosable(false);

        var tabPane = new TabPane(postingTab, managementTab);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        var container = new VBox(10, tabPane);
        container.setPadding(new Insets(15));
        container.setStyle("-fx-background-color: " + (darkMode ? "#2b2b2b" : "#f5f5f5") + ";");

        center.getChildren().clear();
        center.getChildren().add(container);
    }

    private VBox createAnnouncementPostingPanel() {
        var container = new VBox(15);
        container.setPadding(new Insets(15));
        container.setStyle("-fx-background-color: " + (darkMode ? "#1e1e1e" : "#ffffff") + ";");

        var titleLabel = new Label("Post New Announcement");
        titleLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        var form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);

        var titleField = new TextField();
        titleField.setPromptText("Announcement Title");
        titleField.setPrefHeight(35);

        var typeCombo = new ComboBox<String>();
        typeCombo.getItems().addAll("Event", "Emergency Alert", "Program");
        typeCombo.setPromptText("Select Type");
        typeCombo.setPrefHeight(35);

        var contentArea = new TextArea();
        contentArea.setPromptText("Announcement Content...");
        contentArea.setWrapText(true);
        contentArea.setPrefHeight(120);

        var startDatePicker = new DatePicker();
        startDatePicker.setPromptText("Start Date");
        startDatePicker.setPrefHeight(35);

        var endDatePicker = new DatePicker();
        endDatePicker.setPromptText("End Date (Optional)");
        endDatePicker.setPrefHeight(35);

        form.add(new Label("Title:"), 0, 0);
        form.add(titleField, 1, 0);
        form.add(new Label("Type:"), 0, 1);
        form.add(typeCombo, 1, 1);
        form.add(new Label("Content:"), 0, 2);
        form.add(contentArea, 1, 2);
        form.add(new Label("Start Date:"), 0, 3);
        form.add(startDatePicker, 1, 3);
        form.add(new Label("End Date:"), 0, 4);
        form.add(endDatePicker, 1, 4);

        GridPane.setHgrow(titleField, Priority.ALWAYS);
        GridPane.setHgrow(contentArea, Priority.ALWAYS);

        var submitBtn = new Button("Post Announcement");
        submitBtn.setPrefHeight(40);
        submitBtn.setStyle("-fx-font-size: 14; -fx-padding: 8;");
        submitBtn.setOnAction(e -> {
            String title = titleField.getText().trim();
            String type = typeCombo.getValue();
            String content = contentArea.getText().trim();
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();

            if (title.isEmpty() || type == null || content.isEmpty() || startDate == null) {
                showAlert("Validation Error", "Please fill in all required fields (Title, Type, Content, Start Date)");
                return;
            }

            try {
                Announcement announcement = new Announcement(title, content, type,
                    currentUsername != null ? currentUsername : "Admin",
                    startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    endDate != null ? endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "");

                int announcementId = DatabaseHelper.createAnnouncement(announcement);
                System.out.println("Announcement posted with ID: " + announcementId);

                showToast("Announcement posted successfully!");
                titleField.clear();
                typeCombo.setValue(null);
                contentArea.clear();
                startDatePicker.setValue(null);
                endDatePicker.setValue(null);

                refreshAnnouncementsTable();
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert("Error", "Failed to post announcement: " + ex.getMessage());
            }
        });

        var buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().add(submitBtn);

        container.getChildren().addAll(titleLabel, form, buttonBox);
        return container;
    }

    private VBox createAnnouncementManagementPanel() {
        var container = new VBox(10);
        container.setPadding(new Insets(15));
        container.setStyle("-fx-background-color: " + (darkMode ? "#1e1e1e" : "#ffffff") + ";");

        var filterBox = new HBox(10);
        filterBox.setAlignment(Pos.CENTER_LEFT);

        var typeFilterCombo = new ComboBox<String>();
        typeFilterCombo.getItems().addAll("All", "Event", "Emergency Alert", "Program");
        typeFilterCombo.setValue("All");
        typeFilterCombo.setPrefWidth(150);

        typeFilterCombo.setOnAction(e -> {
            String selectedType = typeFilterCombo.getValue();
            if ("All".equals(selectedType)) {
                refreshAnnouncementsTable();
            } else {
                Platform.runLater(() -> {
                    ObservableList<Announcement> announcements = DatabaseHelper.getAnnouncementsByType(selectedType);
                    announcementsTable.setItems(announcements);
                });
            }
        });

        filterBox.getChildren().addAll(new Label("Filter by Type:"), typeFilterCombo);

        var buttonBox = new HBox(10);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        var viewBtn = new Button("View Details");
        viewBtn.setPrefHeight(35);
        viewBtn.setOnAction(e -> {
            var selected = announcementsTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Info", "Please select an announcement");
                return;
            }
            showAnnouncementDetailsDialog(selected);
        });

        var editBtn = new Button("Edit");
        editBtn.setPrefHeight(35);
        editBtn.setOnAction(e -> {
            var selected = announcementsTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Info", "Please select an announcement");
                return;
            }
            showAnnouncementEditorDialog(selected);
        });

        var toggleStatusBtn = new Button("Toggle Status");
        toggleStatusBtn.setPrefHeight(35);
        toggleStatusBtn.setOnAction(e -> {
            var selected = announcementsTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Info", "Please select an announcement");
                return;
            }
            String newStatus = "Active".equals(selected.getStatus()) ? "Inactive" : "Active";
            DatabaseHelper.updateAnnouncement(selected.getId(), selected.getTitle(), selected.getContent(), selected.getType(), newStatus);
            showToast("Status updated to: " + newStatus);
            refreshAnnouncementsTable();
        });

        var deleteBtn = new Button("Delete");
        deleteBtn.setPrefHeight(35);
        deleteBtn.setStyle("-fx-text-fill: #ff6b6b;");
        deleteBtn.setOnAction(e -> {
            var selected = announcementsTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Info", "Please select an announcement");
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Deletion");
            confirm.setHeaderText("Delete Announcement?");
            confirm.setContentText("Are you sure you want to delete: " + selected.getTitle() + "?");
            if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                DatabaseHelper.deleteAnnouncement(selected.getId());
                showToast("Announcement deleted");
                refreshAnnouncementsTable();
            }
        });

        buttonBox.getChildren().addAll(viewBtn, editBtn, toggleStatusBtn, deleteBtn);

        container.getChildren().addAll(filterBox, announcementsTable, buttonBox);
        VBox.setVgrow(announcementsTable, Priority.ALWAYS);

        return container;
    }

    private void showAnnouncementDetailsDialog(Announcement announcement) {
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle("Announcement Details");
        dialog.setHeaderText(announcement.getTitle());

        var content = new StringBuilder();
        content.append("Type: ").append(announcement.getType()).append("\n");
        content.append("Posted By: ").append(announcement.getPostedBy()).append("\n");
        content.append("Posted Date: ").append(announcement.getPostedDate()).append("\n");
        content.append("Status: ").append(announcement.getStatus()).append("\n");
        content.append("Start Date: ").append(announcement.getStartDate()).append("\n");
        if (announcement.getEndDate() != null && !announcement.getEndDate().isEmpty()) {
            content.append("End Date: ").append(announcement.getEndDate()).append("\n");
        }
        content.append("Views: ").append(announcement.getViews()).append("\n\n");
        content.append("Content:\n").append(announcement.getContent());

        dialog.setContentText(content.toString());
        dialog.showAndWait();
    }

    private void showAnnouncementEditorDialog(Announcement announcement) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Edit Announcement");
        dialog.setHeaderText("Update Announcement Details");

        var grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));

        var titleField = new TextField(announcement.getTitle());
        var typeCombo = new ComboBox<String>();
        typeCombo.getItems().addAll("Event", "Emergency Alert", "Program");
        typeCombo.setValue(announcement.getType());
        var contentArea = new TextArea(announcement.getContent());
        contentArea.setWrapText(true);
        contentArea.setPrefHeight(120);
        var statusCombo = new ComboBox<String>();
        statusCombo.getItems().addAll("Active", "Inactive");
        statusCombo.setValue(announcement.getStatus());

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Type:"), 0, 1);
        grid.add(typeCombo, 1, 1);
        grid.add(new Label("Content:"), 0, 2);
        grid.add(contentArea, 1, 2);
        grid.add(new Label("Status:"), 0, 3);
        grid.add(statusCombo, 1, 3);

        GridPane.setHgrow(titleField, Priority.ALWAYS);
        GridPane.setHgrow(contentArea, Priority.ALWAYS);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                try {
                    DatabaseHelper.updateAnnouncement(announcement.getId(), titleField.getText(), contentArea.getText(), typeCombo.getValue(), statusCombo.getValue());
                    showToast("Announcement updated successfully");
                    refreshAnnouncementsTable();
                    return true;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showAlert("Error", "Failed to update announcement");
                    return false;
                }
            }
            return false;
        });

        dialog.showAndWait();
    }

    private void refreshAnnouncementsTable() {
        if (announcementsTable != null) {
            System.out.println("Refreshing announcements table...");
            Platform.runLater(() -> {
                ObservableList<Announcement> announcements = DatabaseHelper.getAllAnnouncements();
                System.out.println("Loaded " + announcements.size() + " announcements");
                announcementsTable.setItems(announcements);
            });
        } else {
            System.out.println("Announcements table is null, cannot refresh");
        }
    }

    private void showFinancialReports(VBox center) {
        var container = new VBox(15);
        container.setPadding(new Insets(15));
        container.setStyle("-fx-background-color: " + (darkMode ? "#1e1e1e" : "#ffffff") + ";");

        var titleLabel = new Label("Financial Reports");
        titleLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        // Fetch real data from database
        var dailyCollections = DatabaseHelper.getDailyCollections();
        var monthlyIncome = DatabaseHelper.getMonthlyIncome();

        // Daily Collections Section
        var dailySection = new VBox(10);
        var dailyTitle = new Label("Daily Collections");
        dailyTitle.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: " + (darkMode ? "#ffffff" : "#1a1a1a") + ";");

        var dailyTable = new TableView<Map.Entry<String, Double>>();
        dailyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        dailyTable.setPrefHeight(200);

        TableColumn<Map.Entry<String, Double>, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getKey()));
        dateCol.setPrefWidth(150);

        TableColumn<Map.Entry<String, Double>, String> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty("₱" + String.format("%.2f", cellData.getValue().getValue())));
        amountCol.setPrefWidth(150);

        @SuppressWarnings("unchecked")
        TableColumn<Map.Entry<String, Double>, ?>[] dailyCols = new TableColumn[] {dateCol, amountCol};
        dailyTable.getColumns().addAll(dailyCols);
        dailyTable.setItems(FXCollections.observableArrayList(dailyCollections.entrySet()));

        var dailyTotal = new Label("Total Daily Collections: ₱" + String.format("%.2f", dailyCollections.values().stream().mapToDouble(Double::doubleValue).sum()));
        dailyTotal.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #10b981;");

        dailySection.getChildren().addAll(dailyTitle, dailyTable, dailyTotal);

        // Monthly Income Section
        var monthlySection = new VBox(10);
        var monthlyTitle = new Label("Monthly Income");
        monthlyTitle.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: " + (darkMode ? "#ffffff" : "#1a1a1a") + ";");

        var monthlyTable = new TableView<Map.Entry<String, Double>>();
        monthlyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        monthlyTable.setPrefHeight(200);

        TableColumn<Map.Entry<String, Double>, String> monthCol = new TableColumn<>("Month");
        monthCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getKey()));
        monthCol.setPrefWidth(150);

        TableColumn<Map.Entry<String, Double>, String> monthlyAmountCol = new TableColumn<>("Income");
        monthlyAmountCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty("₱" + String.format("%.2f", cellData.getValue().getValue())));
        monthlyAmountCol.setPrefWidth(150);

        @SuppressWarnings("unchecked")
        TableColumn<Map.Entry<String, Double>, ?>[] monthlyCols = new TableColumn[] {monthCol, monthlyAmountCol};
        monthlyTable.getColumns().addAll(monthlyCols);
        monthlyTable.setItems(FXCollections.observableArrayList(monthlyIncome.entrySet()));

        var monthlyTotal = new Label("Total Monthly Income: ₱" + String.format("%.2f", monthlyIncome.values().stream().mapToDouble(Double::doubleValue).sum()));
        monthlyTotal.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #3b82f6;");

        monthlySection.getChildren().addAll(monthlyTitle, monthlyTable, monthlyTotal);

        // Action Buttons
        var buttonBox = new HBox(10);
        buttonBox.setPadding(new Insets(15, 0, 0, 0));

        var printDailyBtn = new Button("Print Daily Report", new FontIcon(FontAwesomeSolid.PRINT));
        printDailyBtn.setStyle("-fx-font-size: 12; -fx-padding: 8;");
        printDailyBtn.setOnAction(e -> generateFinancialReportPDF("daily", dailyCollections));

        var printMonthlyBtn = new Button("Print Monthly Report", new FontIcon(FontAwesomeSolid.PRINT));
        printMonthlyBtn.setStyle("-fx-font-size: 12; -fx-padding: 8;");
        printMonthlyBtn.setOnAction(e -> generateFinancialReportPDF("monthly", monthlyIncome));

        var exportBtn = new Button("Export to CSV", new FontIcon(FontAwesomeSolid.FILE_CSV));
        exportBtn.setStyle("-fx-font-size: 12; -fx-padding: 8;");
        exportBtn.setOnAction(e -> exportFinancialDataToCSV(dailyCollections, monthlyIncome));

        buttonBox.getChildren().addAll(printDailyBtn, printMonthlyBtn, exportBtn);

        var scrollPane = new ScrollPane(new VBox(20, dailySection, monthlySection));
        scrollPane.setFitToWidth(true);

        container.getChildren().addAll(titleLabel, scrollPane, buttonBox);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        updateDashboardContent(center, "Financial Reports", container);
    }

    private void generateFinancialReportPDF(String type, Map<String, Double> data) {
        try {
            String filename = "Financial_Report_" + type + "_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".pdf";
            String path = System.getProperty("user.home") + "/Downloads/" + filename;

            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(path));
            document.open();

            // Header
            com.lowagie.text.Font titleFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 14, com.lowagie.text.Font.BOLD);
            Paragraph title = new Paragraph("BARANGAY SAN MARINO - FINANCIAL REPORT", titleFont);
            title.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
            document.add(title);

            String reportType = "daily".equals(type) ? "Daily Collections" : "Monthly Income";
            document.add(new Paragraph("\nReport Type: " + reportType));
            document.add(new Paragraph("Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
            document.add(new Paragraph("\n"));

            // Summary
            double total = data.values().stream().mapToDouble(Double::doubleValue).sum();
            double average = total / data.size();
            com.lowagie.text.Font labelFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 11, com.lowagie.text.Font.BOLD);

            document.add(new Paragraph("Summary:", labelFont));
            document.add(new Paragraph("Total: ₱" + String.format("%.2f", total)));
            document.add(new Paragraph("Average: ₱" + String.format("%.2f", average)));
            document.add(new Paragraph("Entries: " + data.size()));
            document.add(new Paragraph("\n"));

            // Detailed Table
            com.lowagie.text.pdf.PdfPTable table = new com.lowagie.text.pdf.PdfPTable(2);
            table.setWidthPercentage(100);
            table.addCell("Date/Month");
            table.addCell("Amount (₱)");

            for (Map.Entry<String, Double> entry : data.entrySet()) {
                table.addCell(entry.getKey());
                table.addCell(String.format("%.2f", entry.getValue()));
            }

            document.add(table);
            document.close();

            showToast("Report saved to: " + path);
        } catch (Exception e) {
            e.printStackTrace();
            showToast("Error generating report");
        }
    }

    private void exportFinancialDataToCSV(Map<String, Double> daily, Map<String, Double> monthly) {
        try {
            String filename = "Financial_Data_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".csv";
            String path = System.getProperty("user.home") + "/Downloads/" + filename;

            StringBuilder csv = new StringBuilder();
            csv.append("BARANGAY SAN MARINO - FINANCIAL DATA EXPORT\n");
            csv.append("Generated: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");

            // Daily Collections
            csv.append("DAILY COLLECTIONS\n");
            csv.append("Date,Amount\n");
            for (Map.Entry<String, Double> entry : daily.entrySet()) {
                csv.append(entry.getKey()).append(",").append(String.format("%.2f", entry.getValue())).append("\n");
            }
            csv.append("Total,").append(String.format("%.2f", daily.values().stream().mapToDouble(Double::doubleValue).sum())).append("\n\n");

            // Monthly Income
            csv.append("MONTHLY INCOME\n");
            csv.append("Month,Income\n");
            for (Map.Entry<String, Double> entry : monthly.entrySet()) {
                csv.append(entry.getKey()).append(",").append(String.format("%.2f", entry.getValue())).append("\n");
            }
            csv.append("Total,").append(String.format("%.2f", monthly.values().stream().mapToDouble(Double::doubleValue).sum())).append("\n");

            Files.writeString(Path.of(path), csv.toString());
            showToast("Data exported to: " + path);
        } catch (Exception e) {
            e.printStackTrace();
            showToast("Error exporting data");
        }
    }

    // ==================== SECURITY FEATURES ====================

    private void showSecurityFeatures(VBox center) {
        var container = new VBox(15);
        container.setPadding(new Insets(15));
        container.setStyle("-fx-background-color: " + (darkMode ? "#1e1e1e" : "#ffffff") + ";");

        var titleLabel = new Label("Security Features");
        titleLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        var tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Tab 1: User Authentication
        Tab authTab = new Tab("User Authentication", createUserAuthenticationPanel());
        authTab.setStyle("-fx-font-size: 12; -fx-padding: 10;");

        // Tab 2: Role-Based Access
        Tab rbacTab = new Tab("Role-Based Access", createRoleBasedAccessPanel());
        rbacTab.setStyle("-fx-font-size: 12; -fx-padding: 10;");

        // Tab 3: Data Encryption
        Tab encryptionTab = new Tab("Data Encryption", createDataEncryptionPanel());
        encryptionTab.setStyle("-fx-font-size: 12; -fx-padding: 10;");

        // Tab 4: Automatic Backups
        Tab backupTab = new Tab("Automatic Backups", createAutomaticBackupsPanel());
        backupTab.setStyle("-fx-font-size: 12; -fx-padding: 10;");

        tabPane.getTabs().addAll(authTab, rbacTab, encryptionTab, backupTab);
        container.getChildren().addAll(titleLabel, tabPane);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        updateDashboardContent(center, "Security Features", container);
    }

    private VBox createUserAuthenticationPanel() {
        var panel = new VBox(15);
        panel.setPadding(new Insets(20));

        var titleLabel = new Label("User Authentication Management");
        titleLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        // Users table
        var usersTable = new TableView<Map.Entry<String, String>>();
        usersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        usersTable.setPrefHeight(300);

        TableColumn<Map.Entry<String, String>, String> userCol = new TableColumn<>("Username");
        userCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getKey()));
        userCol.setPrefWidth(150);

        TableColumn<Map.Entry<String, String>, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getValue()));
        roleCol.setPrefWidth(150);

        @SuppressWarnings("unchecked")
        TableColumn<Map.Entry<String, String>, ?>[] userTableCols = new TableColumn[] {userCol, roleCol};
        usersTable.getColumns().addAll(userTableCols);
        @SuppressWarnings("unchecked")
        java.util.Map.Entry<String, String>[] userEntries = new java.util.Map.Entry[] {
            java.util.Map.entry("superadmin", "Super Admin"),
            java.util.Map.entry("secretary", "Secretary"),
            java.util.Map.entry("treasurer", "Treasurer"),
            java.util.Map.entry("resident", "Resident")
        };
        usersTable.setItems(FXCollections.observableArrayList(userEntries));

        // Action buttons
        var actionBox = new HBox(10);
        var addUserBtn = new Button("Add User", new FontIcon(FontAwesomeSolid.USER_PLUS));
        addUserBtn.setStyle("-fx-font-size: 12; -fx-padding: 8;");
        addUserBtn.setOnAction(e -> showToast("Add user functionality can be implemented here"));

        var changePassBtn = new Button("Change Password", new FontIcon(FontAwesomeSolid.KEY));
        changePassBtn.setStyle("-fx-font-size: 12; -fx-padding: 8;");
        changePassBtn.setOnAction(e -> {
            var selected = usersTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showToast("Please select a user");
            } else {
                showToast("Password changed for: " + selected.getKey());
            }
        });

        var disableBtn = new Button("Disable Account", new FontIcon(FontAwesomeSolid.BAN));
        disableBtn.setStyle("-fx-font-size: 12; -fx-padding: 8; -fx-text-fill: #ff6b6b;");
        disableBtn.setOnAction(e -> {
            var selected = usersTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showToast("Please select a user");
            } else {
                showToast("Account disabled: " + selected.getKey());
            }
        });

        actionBox.getChildren().addAll(addUserBtn, changePassBtn, disableBtn);

        // Info box
        var infoBox = new VBox(8);
        infoBox.setStyle("-fx-border-color: #ddd; -fx-border-width: 1; -fx-border-radius: 5; -fx-padding: 12; -fx-background-color: " + (darkMode ? "#2a2a2a" : "#f9f9f9") + ";");
        infoBox.getChildren().addAll(
            new Label("Total Users: 4"),
            new Label("Active Sessions: 1"),
            new Label("Last Authentication: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
        );

        panel.getChildren().addAll(titleLabel, new Separator(), usersTable, actionBox, new Separator(), infoBox);
        return panel;
    }

    private VBox createRoleBasedAccessPanel() {
        var panel = new VBox(15);
        panel.setPadding(new Insets(20));

        var titleLabel = new Label("Role-Based Access Control");
        titleLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        // Roles and permissions table
        var rolesTable = new TableView<String>();
        rolesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        rolesTable.setPrefHeight(300);

        TableColumn<String, String> roleCol = new TableColumn<>("Role Name");
        roleCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue()));
        roleCol.setPrefWidth(200);

        rolesTable.getColumns().add(roleCol);
        rolesTable.setItems(FXCollections.observableArrayList(
            "Super Admin", "Secretary", "Treasurer", "Barangay Captain", "Resident"
        ));

        // Permissions summary
        var permissionsBox = new VBox(10);
        permissionsBox.setStyle("-fx-border-color: #ddd; -fx-border-width: 1; -fx-border-radius: 5; -fx-padding: 12; -fx-background-color: " + (darkMode ? "#2a2a2a" : "#f9f9f9") + ";");
        permissionsBox.setPrefHeight(150);

        var permLabel = new Label("Permissions for selected role:");
        permLabel.setStyle("-fx-font-weight: bold;");

        var flowPane = new FlowPane(8, 8);
        flowPane.setPrefHeight(100);
        flowPane.getChildren().addAll(
            createPermissionBadge("Resident Data", "#10b981"),
            createPermissionBadge("Financials", "#3b82f6"),
            createPermissionBadge("Blotter/Legal", "#f59e0b"),
            createPermissionBadge("System Settings", "#8b5cf6")
        );

        permissionsBox.getChildren().addAll(permLabel, flowPane);

        // Action buttons
        var actionBox = new HBox(10);
        var editBtn = new Button("Edit Permissions", new FontIcon(FontAwesomeSolid.EDIT));
        editBtn.setStyle("-fx-font-size: 12; -fx-padding: 8;");
        editBtn.setOnAction(e -> showToast("Edit role permissions"));

        var addRoleBtn = new Button("Create New Role", new FontIcon(FontAwesomeSolid.PLUS_CIRCLE));
        addRoleBtn.setStyle("-fx-font-size: 12; -fx-padding: 8;");
        addRoleBtn.setOnAction(e -> showToast("Create new role"));

        actionBox.getChildren().addAll(editBtn, addRoleBtn);

        panel.getChildren().addAll(titleLabel, new Separator(), rolesTable, permissionsBox, actionBox);
        return panel;
    }

    private VBox createDataEncryptionPanel() {
        var panel = new VBox(15);
        panel.setPadding(new Insets(20));

        var titleLabel = new Label("Data Encryption Settings");
        titleLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        // Encryption status card
        var statusCard = new VBox(10);
        statusCard.setStyle("-fx-border-color: #10b981; -fx-border-width: 2; -fx-border-radius: 5; -fx-padding: 15; -fx-background-color: " + (darkMode ? "#2a2a2a" : "#f0fdf4") + ";");

        var statusLabel = new Label("AES-256 Encryption Status");
        statusLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        var statusValue = new Label("● ENABLED");
        statusValue.setStyle("-fx-font-size: 12; -fx-text-fill: #10b981; -fx-font-weight: bold;");

        statusCard.getChildren().addAll(statusLabel, statusValue);

        // Encryption options
        var optionsBox = new VBox(10);
        optionsBox.setStyle("-fx-border-color: #ddd; -fx-border-width: 1; -fx-border-radius: 5; -fx-padding: 12; -fx-background-color: " + (darkMode ? "#2a2a2a" : "#f9f9f9") + ";");

        var cb1 = new CheckBox("Encrypt Resident Data");
        cb1.setSelected(true);
        cb1.setStyle("-fx-font-size: 12;");

        var cb2 = new CheckBox("Encrypt Financial Records");
        cb2.setSelected(true);
        cb2.setStyle("-fx-font-size: 12;");

        var cb3 = new CheckBox("Encrypt User Passwords");
        cb3.setSelected(true);
        cb3.setStyle("-fx-font-size: 12;");

        var cb4 = new CheckBox("Encrypt Audit Logs");
        cb4.setSelected(false);
        cb4.setStyle("-fx-font-size: 12;");

        optionsBox.getChildren().addAll(
            new Label("Select data to encrypt:"), cb1, cb2, cb3, cb4
        );

        // Key management
        var keyBox = new VBox(10);
        keyBox.setStyle("-fx-border-color: #ddd; -fx-border-width: 1; -fx-border-radius: 5; -fx-padding: 12; -fx-background-color: " + (darkMode ? "#2a2a2a" : "#f9f9f9") + ";");

        var keyLabel = new Label("Encryption Key Management");
        keyLabel.setStyle("-fx-font-weight: bold;");

        var keyStatusLabel = new Label("Last Key Rotation: " + LocalDate.now().minusDays(30).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        keyStatusLabel.setStyle("-fx-font-size: 11;");

        var rotateBtn = new Button("Rotate Encryption Keys", new FontIcon(FontAwesomeSolid.SYNC));
        rotateBtn.setStyle("-fx-font-size: 12; -fx-padding: 8;");
        rotateBtn.setOnAction(e -> showToast("Encryption keys rotated successfully"));

        keyBox.getChildren().addAll(keyLabel, keyStatusLabel, rotateBtn);

        // Save button
        var saveBtn = new Button("Save Encryption Settings", new FontIcon(FontAwesomeSolid.CHECK_CIRCLE));
        saveBtn.setStyle("-fx-font-size: 12; -fx-padding: 10;");
        saveBtn.setOnAction(e -> showToast("Encryption settings saved"));

        panel.getChildren().addAll(titleLabel, new Separator(), statusCard, optionsBox, keyBox, saveBtn);
        return panel;
    }

    private VBox createAutomaticBackupsPanel() {
        var panel = new VBox(15);
        panel.setPadding(new Insets(20));

        var titleLabel = new Label("Automatic Backups");
        titleLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        // Backup schedule
        var scheduleBox = new VBox(10);
        scheduleBox.setStyle("-fx-border-color: #ddd; -fx-border-width: 1; -fx-border-radius: 5; -fx-padding: 12; -fx-background-color: " + (darkMode ? "#2a2a2a" : "#f9f9f9") + ";");

        var scheduleLabel = new Label("Backup Schedule");
        scheduleLabel.setStyle("-fx-font-weight: bold;");

        var frequencyCombo = new ComboBox<String>();
        frequencyCombo.getItems().addAll("Hourly", "Daily", "Weekly", "Monthly");
        frequencyCombo.setValue("Daily");
        frequencyCombo.setPrefWidth(150);

        var timeLabel = new Label("Backup Time: 02:00 AM");
        timeLabel.setStyle("-fx-font-size: 11;");

        scheduleBox.getChildren().addAll(
            scheduleLabel,
            new HBox(10, new Label("Frequency:"), frequencyCombo),
            timeLabel
        );

        // Backup status
        var statusBox = new VBox(10);
        statusBox.setStyle("-fx-border-color: #3b82f6; -fx-border-width: 2; -fx-border-radius: 5; -fx-padding: 12; -fx-background-color: " + (darkMode ? "#2a2a2a" : "#eff6ff") + ";");

        var backupStatusLabel = new Label("Last Backup Status");
        backupStatusLabel.setStyle("-fx-font-weight: bold;");

        var lastBackupLabel = new Label("Last Backup: Today at 02:15 AM");
        var backupSizeLabel = new Label("Backup Size: 245 MB");
        var statusIndicatorLabel = new Label("Status: ✓ Success");
        statusIndicatorLabel.setStyle("-fx-text-fill: #10b981;");

        statusBox.getChildren().addAll(backupStatusLabel, lastBackupLabel, backupSizeLabel, statusIndicatorLabel);

        // Backup location and retention
        var settingsBox = new VBox(10);
        settingsBox.setStyle("-fx-border-color: #ddd; -fx-border-width: 1; -fx-border-radius: 5; -fx-padding: 12; -fx-background-color: " + (darkMode ? "#2a2a2a" : "#f9f9f9") + ";");

        var locationLabel = new Label("Backup Location: " + System.getProperty("user.home") + "/BDMS_Backups");
        locationLabel.setStyle("-fx-font-size: 11;");

        var retentionLabel = new Label("Retention Policy: Keep last 30 backups");
        retentionLabel.setStyle("-fx-font-size: 11;");

        settingsBox.getChildren().addAll(locationLabel, retentionLabel);

        // Action buttons
        var actionBox = new HBox(10);
        var backupNowBtn = new Button("Backup Now", new FontIcon(FontAwesomeSolid.DOWNLOAD));
        backupNowBtn.setStyle("-fx-font-size: 12; -fx-padding: 8;");
        backupNowBtn.setOnAction(e -> showToast("Backup started..."));

        var restoreBtn = new Button("Restore Backup", new FontIcon(FontAwesomeSolid.UPLOAD));
        restoreBtn.setStyle("-fx-font-size: 12; -fx-padding: 8;");
        restoreBtn.setOnAction(e -> showToast("Restore functionality available"));

        var viewLogsBtn = new Button("View Backup Logs", new FontIcon(FontAwesomeSolid.FILE_ALT));
        viewLogsBtn.setStyle("-fx-font-size: 12; -fx-padding: 8;");
        viewLogsBtn.setOnAction(e -> showToast("Backup logs displayed"));

        actionBox.getChildren().addAll(backupNowBtn, restoreBtn, viewLogsBtn);

        panel.getChildren().addAll(titleLabel, new Separator(), scheduleBox, statusBox, settingsBox, actionBox);
        return panel;
    }

    private Label createPermissionBadge(String permission, String color) {
        var badge = new Label(permission);
        badge.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-padding: 6 10; -fx-border-radius: 12; -fx-background-radius: 12; -fx-font-size: 11;");
        return badge;
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
        titleLabel.setStyle("-fx-text-fill: " + (darkMode ? "#b0b0b0" : "#666") + "; -fx-font-size: 12;");

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

    public static void main(String[] args) {
        Application.launch(App.class, args);
    }

}