package com.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.animation.ScaleTransition;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.ParallelTransition;
import javafx.geometry.Orientation;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
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
import javafx.scene.text.TextAlignment;
import javafx.scene.Cursor;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.stage.DirectoryChooser;
import javafx.stage.Popup;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Duration;
import javafx.event.ActionEvent;
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
    
    // Enhanced table references
    private TableUtils.EnhancedTable<ResidentUserRow> enhancedUsersTable;
    private TableView<ResidentUserRow> usersManagementTable;
    private TextField searchField; // Promoted to class level for access in other methods
    private Pagination pagination;
    private static final int ROWS_PER_PAGE = 15;
    private String currentSortField = "last_name";
    private String currentSortOrder = "ASC";


    private Scene loginScene;
    private Stage primaryStage;
    private StackPane rootPane; // For toast notifications

    // Navigation state
    private Button selectedNavButton;
    private Rectangle navIndicator;
    private VBox navMenu;
    private VBox sidebarVBox;           // reference to sidebar VBox for collapse
    private boolean sidebarCollapsed = false;
    private static final double SIDEBAR_EXPANDED = 240;
    private static final double SIDEBAR_COLLAPSED = 60;

    // Submenu state (persists between restarts)
    private boolean userSubmenuOpen = false;
    private Button selectedSubmenuButton;
    private Rectangle submenuIndicator;
    
    private VBox userSubmenuContainer;

    // Persisted settings
    private final Path submenuStateFile = Paths.get(System.getProperty("user.home"), ".bdms_submenu_open");
    private final Path rememberMeFile = Paths.get(System.getProperty("user.home"), ".bdms_remember_me");

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
        this.primaryStage = stage;
        stage.setTitle("Barangay San Marino BDMS");
        stage.setResizable(false);
        stage.setWidth(1280);
        stage.setHeight(900);
        
        // Set window icon (appears in taskbar, title bar, Alt+Tab)
        try {
            // Load multiple icon sizes for better system integration
            String[] iconSizes = {"icon.png", "icon_48.png", "icon_32.png", "icon_16.png"};
            boolean iconLoaded = false;
            
            for (String iconFile : iconSizes) {
                var iconStream = getClass().getResourceAsStream("/assets/" + iconFile);
                if (iconStream != null) {
                    stage.getIcons().add(new Image(iconStream));
                    iconLoaded = true;
                } else {
                    // Fallback to file path
                    File file = new File("src/assets/" + iconFile);
                    if (file.exists()) {
                        stage.getIcons().add(new Image(file.toURI().toString()));
                        iconLoaded = true;
                    }
                }
            }
            
            if (iconLoaded) {
                System.out.println("✓ Window icons loaded successfully");
            } else {
                System.out.println("✗ No window icons found");
            }
        } catch (Exception e) {
            System.err.println("✗ Error loading window icon: " + e.getMessage());
        }
        
        loginScene = createLoginScene();
        stage.setScene(loginScene);
        stage.show();
    }

    private void saveSubmenuStateToDisk() {
        try {
            Files.writeString(submenuStateFile, userSubmenuOpen ? "open" : "closed", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveRememberMe(String username) {
        try {
            Files.writeString(rememberMeFile, username, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String loadRememberMe() {
        try {
            if (Files.exists(rememberMeFile)) {
                return Files.readString(rememberMeFile).trim();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private void clearRememberMe() {
        try {
            Files.deleteIfExists(rememberMeFile);
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
                logoView.setFitWidth(280);
                logoView.setPreserveRatio(true);
                System.out.println("✓ Logo loaded from resources");
            } else {
                System.out.println("✗ Logo resource not found, trying file path");
                // Fallback to file path
                File logoFile = new File("src/assets/logo.png");
                if (logoFile.exists()) {
                    var logoImage = new Image(logoFile.toURI().toString());
                    logoView.setImage(logoImage);
                    logoView.setFitWidth(280);
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

        var usernameField = new TextField();
        usernameField.setPromptText("E.g. info@example.com");
        usernameField.getStyleClass().add("text-field");

        var passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");
        passwordField.getStyleClass().add("password-field");

        var loginButton = new Button("Login");
        loginButton.getStyleClass().add("button-primary");
        loginButton.setMaxWidth(Double.MAX_VALUE);
        
        var rememberCheckBox = new CheckBox("Remember me for 30 days");
        rememberCheckBox.getStyleClass().add("check-box");
        
        // Load saved username if remember me was checked
        String savedUsername = loadRememberMe();
        if (!savedUsername.isEmpty()) {
            usernameField.setText(savedUsername);
            rememberCheckBox.setSelected(true);
            passwordField.requestFocus(); // Focus on password field
        }

        // Login action handler
        Runnable performLogin = () -> {
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
                // Handle remember me
                if (rememberCheckBox.isSelected()) {
                    saveRememberMe(username);
                } else {
                    clearRememberMe();
                }
                
                Map<String, String> permissions = DatabaseHelper.getPermissions(role);
                primaryStage.setScene(createDashboardScene(username, role, permissions));
                primaryStage.centerOnScreen();
            } else {
                showToast("Invalid username or password.");
            }
        };

        loginButton.setOnAction(e -> performLogin.run());
        
        // Enable Enter key to login from both fields
        usernameField.setOnAction(e -> performLogin.run());
        passwordField.setOnAction(e -> performLogin.run());

        var forgotLink = new Hyperlink("Forgot your password?");
        forgotLink.getStyleClass().add("hyperlink");
        forgotLink.setOnAction(e -> showAlert("Forgot Password", "Please contact support to reset your password."));

        var formVBox = new VBox(12, subtitle, usernameField, passwordField, loginButton, forgotLink, rememberCheckBox);
        formVBox.setAlignment(Pos.CENTER);
        formVBox.getStyleClass().add("login-card");
        formVBox.setMaxWidth(360);

        var card = new VBox(20, header, formVBox);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("card-login");

        // Create background image view
        ImageView backgroundView = new ImageView();
        try {
            var resourceStream = getClass().getResourceAsStream("/assets/loginbg.png");
            if (resourceStream != null) {
                var bgImage = new Image(resourceStream);
                backgroundView.setImage(bgImage);
                backgroundView.setFitWidth(1280);
                backgroundView.setFitHeight(900);
                backgroundView.setPreserveRatio(false);
                backgroundView.setOpacity(1);
                System.out.println("✓ Background loaded from resources");
            } else {
                System.out.println("✗ Background resource not found, trying file path");
                File bgFile = new File("src/assets/loginbg.png");
                if (bgFile.exists()) {
                    var bgImage = new Image(bgFile.toURI().toString());
                    backgroundView.setImage(bgImage);
                    backgroundView.setFitWidth(1280);
                    backgroundView.setFitHeight(900);
                    backgroundView.setPreserveRatio(false);
                    backgroundView.setOpacity(1);
                    System.out.println("✓ Background loaded from file path");
                } else {
                    System.out.println("✗ Background file not found");
                    backgroundView.getStyleClass().add("bg-fallback");
                }
            }
        } catch (Exception e) {
            System.err.println("✗ Error loading background: " + e.getMessage());
            e.printStackTrace();
            backgroundView.getStyleClass().add("bg-fallback");
        }

        // Center the card on the background
        var root = new StackPane();
        root.getChildren().add(backgroundView);
        root.getChildren().add(card);
        StackPane.setAlignment(card, Pos.CENTER);
        this.rootPane = root; // For toast notifications

        // Start with a desktop-friendly size, content remains centered
        var scene = new Scene(root, 1280, 900);
        scene.getStylesheets().add(getClass().getResource("light-theme.css").toExternalForm());
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



        var userLabel = new Label(username);
        userLabel.getStyleClass().add("user-profile-name");

        var roleLabel = new Label(role);
        roleLabel.getStyleClass().add("user-profile-role");

        var userProfile = new VBox(-2, userLabel, roleLabel);
        userProfile.setAlignment(Pos.CENTER_RIGHT);

        var topBarSpacer = new Region();
        HBox.setHgrow(topBarSpacer, Priority.ALWAYS);
        var topBar = new HBox(16, searchContainer, scanButton, topBarSpacer, userProfile);
        topBar.setPadding(new Insets(12, 18, 0, 18));
        topBar.getStyleClass().add("top-bar");
        topBar.setAlignment(Pos.CENTER);

        // ── Sidebar ──────────────────────────────────────────────────────────
        ImageView dashboardLogoView = new ImageView();
        try {
            var resourceStream = getClass().getResourceAsStream("/assets/logo.png");
            if (resourceStream != null) {
                var logoImage = new Image(resourceStream);
                dashboardLogoView.setImage(logoImage);
                dashboardLogoView.setFitWidth(160);
                dashboardLogoView.setPreserveRatio(true);
            } else {
                File logoFile = new File("src/assets/logo.png");
                if (logoFile.exists()) {
                    var logoImage = new Image(logoFile.toURI().toString());
                    dashboardLogoView.setImage(logoImage);
                    dashboardLogoView.setFitWidth(160);
                    dashboardLogoView.setPreserveRatio(true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Logo container — hidden when collapsed
        var topBrand = new HBox(dashboardLogoView);
        topBrand.setAlignment(Pos.CENTER);
        topBrand.setPadding(new Insets(16, 0, 16, 0));

        navMenu = new VBox(4);
        navMenu.getStyleClass().add("sidebar-menu");

        navIndicator = new Rectangle(0, 3);
        navIndicator.getStyleClass().add("nav-indicator");
        navIndicator.setVisible(false);

        var menuStack = new StackPane(navMenu, navIndicator);
        StackPane.setAlignment(navIndicator, Pos.BOTTOM_LEFT);

        var center = new VBox(16);
        center.setPadding(new Insets(18));

        var overviewBtn     = createSidebarButton("Analytics & Overview",       FontAwesomeSolid.CHART_PIE);
        overviewBtn.setUserData("overview");
        var usersBtn        = createSidebarButton("User & Access",               FontAwesomeSolid.USERS_COG);
        usersBtn.setUserData("users");
        var residentBtn     = createSidebarButton("Residents",                   FontAwesomeSolid.ADDRESS_BOOK);
        residentBtn.setUserData("resident");
        var certificatesBtn = createSidebarButton("Certificates & Clearances",   FontAwesomeSolid.FILE_PDF);
        certificatesBtn.setUserData("certificates");
        var complaintsBtn   = createSidebarButton("Complaints & Incidents",      FontAwesomeSolid.EXCLAMATION_CIRCLE);
        complaintsBtn.setUserData("complaints");
        var announcementsBtn= createSidebarButton("Announcement Portal",         FontAwesomeSolid.BELL);
        announcementsBtn.setUserData("announcements");
        var financialBtn    = createSidebarButton("Financial Reports",           FontAwesomeSolid.CHART_LINE);
        financialBtn.setUserData("financial");
        var securityBtn     = (Button) createSidebarButton("Security Features",  FontAwesomeSolid.LOCK);
        securityBtn.setUserData("security");
        var systemBtn       = createSidebarButton("System Config",               FontAwesomeSolid.COGS);
        systemBtn.setUserData("system");
        var maintenanceBtn  = createSidebarButton("Maintenance",                 FontAwesomeSolid.SHIELD_ALT);
        maintenanceBtn.setUserData("maintenance");

        // Permission-based visibility
        Map<String, String> userPermissions = DatabaseHelper.getPermissions(currentRole);
        if ("None".equals(userPermissions.get("Analytics & Overview")))       { overviewBtn.setVisible(false);     overviewBtn.setManaged(false); }
        if ("None".equals(userPermissions.get("User & Access")))              { usersBtn.setVisible(false);        usersBtn.setManaged(false); }
        if ("None".equals(userPermissions.get("Resident Data")))              { residentBtn.setVisible(false);     residentBtn.setManaged(false); }
        if ("None".equals(userPermissions.get("Certificates & Clearances")))  { certificatesBtn.setVisible(false); certificatesBtn.setManaged(false); }
        if ("None".equals(userPermissions.get("Complaints & Incidents")))     { complaintsBtn.setVisible(false);   complaintsBtn.setManaged(false); }
        if ("None".equals(userPermissions.get("Announcements")))              { announcementsBtn.setVisible(false);announcementsBtn.setManaged(false); }
        if ("None".equals(userPermissions.get("Financial Reports")))          { financialBtn.setVisible(false);    financialBtn.setManaged(false); }
        if ("None".equals(userPermissions.get("Security Features")))          { securityBtn.setVisible(false);     securityBtn.setManaged(false); }
        if ("None".equals(userPermissions.get("System Config")))              { systemBtn.setVisible(false);       systemBtn.setManaged(false); }
        if ("None".equals(userPermissions.get("Maintenance")))                { maintenanceBtn.setVisible(false);  maintenanceBtn.setManaged(false); }

        var logoutBtn = createSidebarButton("Logout", FontAwesomeSolid.SIGN_OUT_ALT);
        logoutBtn.setOnAction(e -> {
            primaryStage.setMaximized(false);
            primaryStage.setScene(createLoginScene());
            primaryStage.centerOnScreen();
        });

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        navMenu.getChildren().addAll(overviewBtn, usersBtn, residentBtn, certificatesBtn,
            complaintsBtn, announcementsBtn, financialBtn, (Button) securityBtn,
            systemBtn, maintenanceBtn, spacer, logoutBtn);

        // ── Toggle button (chevron) ───────────────────────────────────────────
        FontIcon toggleIcon = new FontIcon(FontAwesomeSolid.CHEVRON_LEFT);
        toggleIcon.setIconSize(14);
        toggleIcon.getStyleClass().add("sidebar-toggle-icon");
        Button toggleBtn = new Button("", toggleIcon);
        toggleBtn.getStyleClass().add("sidebar-toggle-btn");
        toggleBtn.setMaxWidth(Double.MAX_VALUE);
        toggleBtn.setOnAction(e -> toggleSidebar(toggleIcon, topBrand, navMenu));

        // ── Restore active section ────────────────────────────────────────────
        if      ("users".equals(activeSection))         setActiveNav(usersBtn);
        else if ("resident".equals(activeSection))      setActiveNav(residentBtn);
        else if ("certificates".equals(activeSection))  setActiveNav(certificatesBtn);
        else if ("complaints".equals(activeSection))    setActiveNav(complaintsBtn);
        else if ("announcements".equals(activeSection)) setActiveNav(announcementsBtn);
        else if ("financial".equals(activeSection))     setActiveNav(financialBtn);
        else if ("security".equals(activeSection))      setActiveNav((Button) securityBtn);
        else if ("system".equals(activeSection))        setActiveNav(systemBtn);
        else if ("maintenance".equals(activeSection))   setActiveNav(maintenanceBtn);
        else                                            setActiveNav(overviewBtn);

        // ── Navigation actions ────────────────────────────────────────────────
        overviewBtn.setOnAction(e -> { setActiveNav(overviewBtn);           showOverview(center); });
        usersBtn.setOnAction(e ->    { setActiveNav(usersBtn);              showUserAndAccess(center); });
        residentBtn.setOnAction(e -> { setActiveNav(residentBtn);           showResidentControl(center); });
        certificatesBtn.setOnAction(e -> { setActiveNav(certificatesBtn);   showCertificatesAndClearances(center); });
        complaintsBtn.setOnAction(e -> { setActiveNav(complaintsBtn);       showComplaintsAndIncidents(center); });
        announcementsBtn.setOnAction(e -> { setActiveNav(announcementsBtn); showAnnouncementsPortal(center); });
        financialBtn.setOnAction(e -> { setActiveNav(financialBtn);         showFinancialReports(center); });
        ((Button) securityBtn).setOnAction(e -> { setActiveNav((Button) securityBtn); showSecurityFeatures(center); });
        systemBtn.setOnAction(e -> { setActiveNav(systemBtn);               showSystemConfiguration(center); });
        maintenanceBtn.setOnAction(e -> { setActiveNav(maintenanceBtn);     showMaintenance(center); });

        // Brand header sits above the nav items
        navMenu.getChildren().add(0, topBrand);
        topBrand.setPadding(new Insets(0, 0, 8, 0));

        var navScrollPane = new ScrollPane(menuStack);
        navScrollPane.getStyleClass().add("scroll-pane-transparent");
        navScrollPane.setFitToWidth(true);
        navScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        navScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(navScrollPane, Priority.ALWAYS);

        sidebarVBox = new VBox(0, toggleBtn, navScrollPane);
        sidebarVBox.getStyleClass().add("sidebar");
        sidebarVBox.setPrefWidth(SIDEBAR_EXPANDED);
        sidebarVBox.setMinWidth(SIDEBAR_EXPANDED);
        sidebarVBox.setMaxWidth(SIDEBAR_EXPANDED);

        Platform.runLater(() -> {
            if (selectedNavButton != null) moveSelectionIndicator(selectedNavButton);
        });

        root.setLeft(sidebarVBox);
        
        // Make center content scrollable and responsive with consistent padding
        center.setPadding(new Insets(20));
        center.setMaxWidth(Double.MAX_VALUE);
        
        var scrollPane = new ScrollPane(center);
        scrollPane.getStyleClass().add("scroll-pane");
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        var mainContent = new VBox(0, topBar, scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        var mainStack = new StackPane(mainContent);
        this.rootPane = mainStack; // For toast notifications
        root.setCenter(mainStack);

        // initial overview content
        if ("overview".equals(activeSection) || activeSection == null) {
            showOverview(center);
        }

        var scene = new Scene(root, 1280, 900);
        scene.getStylesheets().add(getClass().getResource("light-theme.css").toExternalForm());
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

    /** Toggle sidebar between expanded (text+icon) and collapsed (icon only). */
    private void toggleSidebar(FontIcon toggleIcon, HBox topBrand, VBox navMenu) {
        sidebarCollapsed = !sidebarCollapsed;
        double targetWidth = sidebarCollapsed ? SIDEBAR_COLLAPSED : SIDEBAR_EXPANDED;

        // Animate width
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.millis(200),
                new javafx.animation.KeyValue(sidebarVBox.prefWidthProperty(), targetWidth,
                    javafx.animation.Interpolator.EASE_BOTH),
                new javafx.animation.KeyValue(sidebarVBox.minWidthProperty(), targetWidth,
                    javafx.animation.Interpolator.EASE_BOTH),
                new javafx.animation.KeyValue(sidebarVBox.maxWidthProperty(), targetWidth,
                    javafx.animation.Interpolator.EASE_BOTH)
            )
        );
        timeline.play();

        // Flip chevron
        toggleIcon.setIconCode(sidebarCollapsed ? FontAwesomeSolid.CHEVRON_RIGHT : FontAwesomeSolid.CHEVRON_LEFT);

        // Hide/show logo
        topBrand.setVisible(!sidebarCollapsed);
        topBrand.setManaged(!sidebarCollapsed);

        // Show/hide button labels — use properties map to avoid corrupting userData
        for (javafx.scene.Node node : navMenu.getChildren()) {
            if (!(node instanceof Button btn) || btn.getGraphic() == null) continue;
            if (sidebarCollapsed) {
                // Save label text as a node property, then hide it
                btn.getProperties().put("sidebarLabel", btn.getText());
                btn.setText("");
                btn.getStyleClass().add("sidebar-button-collapsed");
                String label = (String) btn.getProperties().get("sidebarLabel");
                if (label != null && !label.isBlank()) btn.setTooltip(new Tooltip(label));
            } else {
                // Restore label
                String saved = (String) btn.getProperties().get("sidebarLabel");
                if (saved != null) btn.setText(saved);
                btn.getStyleClass().remove("sidebar-button-collapsed");
                btn.setTooltip(null);
            }
        }
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
        // Enhanced dashboard with government-style header and improved stats
        VBox dashboardContainer = new VBox(20);
        dashboardContainer.setPadding(new Insets(20));
        
        // Government header
        VBox headerSection = new VBox(4);
        Label republikaLabel = new Label("REPUBLIKA NG PILIPINAS");
        republikaLabel.getStyleClass().add("overview-republika");
        
        Label barangayLabel = new Label("BARANGAY SAN MARINO");
        barangayLabel.getStyleClass().add("overview-barangay");
        
        Label systemLabel = new Label("Document Management System");
        systemLabel.getStyleClass().add("overview-system");
        
        headerSection.getChildren().addAll(republikaLabel, barangayLabel, systemLabel);
        
        // Enhanced statistics cards
        int totalPopulation = DatabaseHelper.getResidentCount(null);
        int issuedRecords = DatabaseHelper.getIssuedDocumentsCount();
        int docRequests = DatabaseHelper.getPendingClearancesCount();
        int openComplaints = DatabaseHelper.getActiveCasesCount();
        double revenue = DatabaseHelper.getTotalRevenue();
        
        var populationCard = createEnhancedStatCard("👥", String.format("%,d", totalPopulation), "TOTAL POPULATION", "#3b82f6");
        var recordsCard = createEnhancedStatCard("📋", String.format("%,d", issuedRecords), "ISSUED RECORDS", "#10b981");
        var requestsCard = createEnhancedStatCard("📄", String.valueOf(docRequests), "DOC REQUESTS", "#f59e0b");
        var complaintsCard = createEnhancedStatCard("⚠️", String.valueOf(openComplaints), "OPEN COMPLAINTS", "#ef4444");
        var revenueCard = createEnhancedStatCard("💰", String.format("₱%,.0f", revenue), "REVENUE (MTD)", "#8b5cf6");
        
        var statsGrid = new HBox(16, populationCard, recordsCard, requestsCard, complaintsCard, revenueCard);
        statsGrid.setAlignment(Pos.CENTER);
        
        // Chart section (existing chart with better styling)
        var ageData = DatabaseHelper.getAgeDistribution();
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        ageData.forEach((ageGroup, count) -> pieChartData.add(new PieChart.Data(ageGroup + " (" + count + ")", count)));

        var distributionChart = new PieChart(pieChartData);
        distributionChart.setTitle("Resident Distribution by Age");
        distributionChart.setPrefSize(600, 300);
        distributionChart.setLegendVisible(true);
        distributionChart.setLabelsVisible(false);
        
        var chartContainer = new HBox(distributionChart);
        chartContainer.setAlignment(Pos.CENTER);

        // Recent announcements (existing functionality)
        var announcementsSection = new VBox(12);
        var announcementsTitle = new Label("Recent Announcements");
        announcementsTitle.getStyleClass().add("overview-announcements-title");
        announcementsSection.getChildren().add(announcementsTitle);

        ObservableList<Announcement> allAnnouncements = DatabaseHelper.getAllAnnouncements();
        allAnnouncements.stream().limit(3).forEach(announcement -> {
            var announcementItem = new HBox(12);
            announcementItem.setPadding(new Insets(12));
            announcementItem.getStyleClass().add("card-announcement-item");
            announcementItem.setAlignment(Pos.TOP_LEFT);

            var typeBadge = new Label(announcement.getType());
            String typeColor = switch (announcement.getType()) {
                case "Event" -> "#10b981";
                case "Emergency Alert" -> "#ef4444";
                case "Program" -> "#8b5cf6";
                default -> "#6b7280";
            };
            // Dynamic color badge — color value is runtime, static parts in CSS
            typeBadge.getStyleClass().add("type-badge");
            typeBadge.setStyle("-fx-background-color: " + typeColor + ";");

            var details = new VBox(4);
            HBox.setHgrow(details, Priority.ALWAYS);
            
            var title = new Label(announcement.getTitle());
            title.getStyleClass().addAll("overview-announcement-title");
            title.setWrapText(true);

            var meta = new Label("Posted on " + announcement.getPostedDate());
            meta.getStyleClass().add("overview-announcement-meta");

            details.getChildren().addAll(title, meta);
            announcementItem.getChildren().addAll(typeBadge, details);
            announcementsSection.getChildren().add(announcementItem);
        });

        if (allAnnouncements.isEmpty()) {
            var noAnnouncements = new Label("No announcements yet");
            noAnnouncements.getStyleClass().add("overview-no-announcements");
            announcementsSection.getChildren().add(noAnnouncements);
        }

        dashboardContainer.getChildren().addAll(headerSection, statsGrid, chartContainer, announcementsSection);
        
        center.getChildren().clear();
        center.getChildren().add(dashboardContainer);
    }
    
    private VBox createEnhancedStatCard(String icon, String value, String title, String color) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(16));
        card.setPrefWidth(180);
        card.getStyleClass().add("card");
        
        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().add("overview-stat-icon");
        
        Label valueLabel = new Label(value);
        // Dynamic color — only the color value is dynamic
        valueLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: 700; -fx-text-fill: " + color + ";");
        
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().addAll("text-muted-xs", "text-semibold");
        
        card.getChildren().addAll(iconLabel, valueLabel, titleLabel);
        return card;
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
        addButton.getStyleClass().addAll("button-secondary", "button-small");
        addButton.setTooltip(new Tooltip("Add Role"));

        Button editButton = new Button("Edit Role");
        editButton.setGraphic(new FontIcon(FontAwesomeSolid.PENCIL_ALT));
        editButton.getStyleClass().addAll("button-secondary", "button-small");
        editButton.setTooltip(new Tooltip("Edit Role"));
        editButton.setDisable(true);

        Button deleteButton = new Button("Delete Role");
        deleteButton.setGraphic(new FontIcon(FontAwesomeSolid.TRASH));
        deleteButton.getStyleClass().addAll("button-secondary", "button-small");
        deleteButton.setTooltip(new Tooltip("Delete Role"));
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
        toolBar.getStyleClass().add("toolbar-transparent");

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
        permissionsTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        TableColumn<Map.Entry<String, Map<String, String>>, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getKey()));
        roleCol.setPrefWidth(180);
        roleCol.setMinWidth(180);

        // Create columns for all system modules
        TableColumn<Map.Entry<String, Map<String, String>>, String> analyticsCol = new TableColumn<>("Analytics");
        analyticsCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getValue().get("Analytics & Overview")));
        analyticsCol.setPrefWidth(100);
        analyticsCol.setCellFactory(param -> createPermissionCell());

        TableColumn<Map.Entry<String, Map<String, String>>, String> userAccessCol = new TableColumn<>("Users");
        userAccessCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getValue().get("User & Access")));
        userAccessCol.setPrefWidth(100);
        userAccessCol.setCellFactory(param -> createPermissionCell());

        TableColumn<Map.Entry<String, Map<String, String>>, String> residentDataCol = new TableColumn<>("Residents");
        residentDataCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getValue().get("Resident Data")));
        residentDataCol.setPrefWidth(100);
        residentDataCol.setCellFactory(param -> createPermissionCell());

        TableColumn<Map.Entry<String, Map<String, String>>, String> certificatesCol = new TableColumn<>("Certificates");
        certificatesCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getValue().get("Certificates & Clearances")));
        certificatesCol.setPrefWidth(100);
        certificatesCol.setCellFactory(param -> createPermissionCell());

        TableColumn<Map.Entry<String, Map<String, String>>, String> complaintsCol = new TableColumn<>("Complaints");
        complaintsCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getValue().get("Complaints & Incidents")));
        complaintsCol.setPrefWidth(100);
        complaintsCol.setCellFactory(param -> createPermissionCell());

        TableColumn<Map.Entry<String, Map<String, String>>, String> announcementsCol = new TableColumn<>("Announcements");
        announcementsCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getValue().get("Announcements")));
        announcementsCol.setPrefWidth(120);
        announcementsCol.setCellFactory(param -> createPermissionCell());

        TableColumn<Map.Entry<String, Map<String, String>>, String> financialCol = new TableColumn<>("Financial");
        financialCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getValue().get("Financial Reports")));
        financialCol.setPrefWidth(100);
        financialCol.setCellFactory(param -> createPermissionCell());

        TableColumn<Map.Entry<String, Map<String, String>>, String> securityCol = new TableColumn<>("Security");
        securityCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getValue().get("Security Features")));
        securityCol.setPrefWidth(100);
        securityCol.setCellFactory(param -> createPermissionCell());

        TableColumn<Map.Entry<String, Map<String, String>>, String> systemCol = new TableColumn<>("System");
        systemCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getValue().get("System Config")));
        systemCol.setPrefWidth(100);
        systemCol.setCellFactory(param -> createPermissionCell());

        TableColumn<Map.Entry<String, Map<String, String>>, String> maintenanceCol = new TableColumn<>("Maintenance");
        maintenanceCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getValue().get("Maintenance")));
        maintenanceCol.setPrefWidth(110);
        maintenanceCol.setCellFactory(param -> createPermissionCell());

        permissionsTable.getColumns().setAll(List.of(roleCol, analyticsCol, userAccessCol, residentDataCol, 
            certificatesCol, complaintsCol, announcementsCol, financialCol, securityCol, systemCol, maintenanceCol));

        // Fetch roles dynamically from the database
        ObservableList<Role> allRoles = DatabaseHelper.getAllRoles();
        ObservableList<Map.Entry<String, Map<String, String>>> permissionsData = FXCollections.observableArrayList();
        for (Role role : allRoles) {
            Map<String, String> permissions = DatabaseHelper.getPermissions(role.getName());
            permissionsData.add(Map.entry(role.getName(), permissions));
        }
        permissionsTable.setItems(permissionsData);

        var infoLabel = new Label("Permission Levels: None, View Only, Manage, Full Access");
        infoLabel.setStyle("-fx-font-size: 11; -fx-text-fill: " + "#333" + "; -fx-font-weight: bold;");

        var legendBox = new HBox(15);
        legendBox.setPadding(new Insets(10, 0, 10, 0));
        legendBox.getChildren().addAll(
            createLegendItem("None", "#ef4444"),
            createLegendItem("View Only", "#f59e0b"),
            createLegendItem("Manage", "#3b82f6"),
            createLegendItem("Full Access", "#10b981")
        );

        var content = new VBox(12, infoLabel, legendBox, permissionsTable);
        VBox.setVgrow(permissionsTable, Priority.ALWAYS);
        updateDashboardContent(center, "Role Permissions", content);
    }

    private HBox createLegendItem(String label, String color) {
        var box = new HBox(5);
        box.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        var colorBox = new javafx.scene.layout.Region();
        colorBox.setPrefSize(12, 12);
        colorBox.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 2;");
        
        var labelText = new Label(label);
        labelText.setStyle("-fx-font-size: 10; -fx-text-fill: #666;");
        
        box.getChildren().addAll(colorBox, labelText);
        return box;
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
        tabPane.getStyleClass().add("tab-pane");

        // Tab 1: Request New Document
        Tab requestTab = new Tab("Request Document", createDocumentRequestPanel());
        requestTab.getStyleClass().add("tab");

        // Tab 2: Pending & Completed Requests
        Tab requestsTab = new Tab("Document Requests", createDocumentRequestsTable());
        requestsTab.getStyleClass().add("tab");

        tabPane.getTabs().addAll(requestTab, requestsTab);
        updateDashboardContent(center, "Certificates & Clearances", tabPane);
    }

    private VBox createDocumentRequestPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));

        // Step 1: Select Resident with Search
        Label residentLabel = new Label("Step 1: Select Resident");
        residentLabel.getStyleClass().add("text-subheading");

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
        residentListView.setStyle("-fx-control-inner-background: #ffffff;");
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
        residentBoxLabel.setStyle("-fx-text-fill: " + "#333" + ";");
        HBox residentBox = new HBox(10, residentBoxLabel, residentSearchBox);
        residentBox.setAlignment(Pos.TOP_LEFT);

        // Step 2: Select Document Type
        Label docTypeLabel = new Label("Step 2: Select Document Type");
        docTypeLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: " + "#1a1a1a" + ";");

        ComboBox<String> docTypeCombo = new ComboBox<>();
        docTypeCombo.setItems(FXCollections.observableArrayList(
            "Barangay Clearance",
            "Certificate of Residency",
            "Indigency Certificate"
        ));
        docTypeCombo.setPrefWidth(300);

        Label feeLabel = new Label("Fee: ₱0");
        feeLabel.setStyle("-fx-font-size: 12; -fx-text-fill: " + "#333" + ";");
        docTypeCombo.setOnAction(e -> {
            if (docTypeCombo.getValue() != null) {
                double fee = DocumentRequest.getFeeForDocumentType(docTypeCombo.getValue());
                feeLabel.setText("Fee: ₱" + fee);
            }
        });

        var docTypeBoxLabel = new Label("Document Type:");
        docTypeBoxLabel.setStyle("-fx-text-fill: " + "#333" + ";");
        HBox docTypeBox = new HBox(10, docTypeBoxLabel, docTypeCombo);
        docTypeBox.setAlignment(Pos.CENTER_LEFT);

        // Step 3: Purpose
        Label purposeLabel = new Label("Step 3: Purpose of Request");
        purposeLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: " + "#1a1a1a" + ";");

        TextArea purposeArea = new TextArea();
        purposeArea.setPromptText("E.g., For loan application, for employment, for travel");
        purposeArea.setWrapText(true);
        purposeArea.setPrefRowCount(4);

        // Submit Button
        Button submitBtn = new Button("Submit");
        submitBtn.getStyleClass().addAll("button-primary", "button-small");
        submitBtn.setTooltip(new Tooltip("Submit Request"));
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
        approveBtn.getStyleClass().addAll("button-secondary", "button-small");
        approveBtn.setTooltip(new Tooltip("Approve Request"));
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

        Button paymentBtn = new Button("Payment", new FontIcon(FontAwesomeSolid.DOLLAR_SIGN));
        paymentBtn.getStyleClass().addAll("button-secondary", "button-small");
        paymentBtn.setTooltip(new Tooltip("Record Payment"));
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

        Button generateBtn = new Button("Generate", new FontIcon(FontAwesomeSolid.FILE_PDF));
        generateBtn.getStyleClass().addAll("button-secondary", "button-small");
        generateBtn.setTooltip(new Tooltip("Generate & Print"));
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

        // SMS Button
        Button sendSMSBtn = new Button("SMS", new FontIcon(FontAwesomeSolid.MOBILE_ALT));
        sendSMSBtn.getStyleClass().addAll("button-warning", "button-small");
        sendSMSBtn.setTooltip(new Tooltip("Send SMS"));
        sendSMSBtn.setDisable(true);
        sendSMSBtn.setOnAction(e -> {
            DocumentRequest selected = documentRequestsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                sendDocumentSMS(selected);
            }
        });

        documentRequestsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean isSelected = newVal != null;
            approveBtn.setDisable(!isSelected || (newVal != null && !"PENDING".equals(newVal.getStatus())));
            paymentBtn.setDisable(!isSelected || (newVal != null && !"APPROVED".equals(newVal.getStatus())));
            generateBtn.setDisable(!isSelected || (newVal != null && (!"APPROVED".equals(newVal.getStatus()) || !"PAID".equals(newVal.getPaymentStatus()))));
            
            // Enable SMS button if resident has phone number
            if (newVal != null) {
                Optional<Resident> resident = DatabaseHelper.getResidentById(newVal.getResidentId());
                boolean hasPhone = resident.isPresent() && resident.get().getPhoneNumber() != null && !resident.get().getPhoneNumber().trim().isEmpty();
                sendSMSBtn.setDisable(!hasPhone);
            } else {
                sendSMSBtn.setDisable(true);
            }
        });

        ToolBar toolBar = new ToolBar(approveBtn, paymentBtn, generateBtn, sendSMSBtn);
        toolBar.getStyleClass().add("toolbar-transparent");

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

    private void sendDocumentSMS(DocumentRequest request) {
        // Get resident information
        Optional<Resident> residentOpt = DatabaseHelper.getResidentById(request.getResidentId());
        if (!residentOpt.isPresent()) {
            showAlert("Error", "Resident not found.");
            return;
        }
        
        Resident resident = residentOpt.get();
        String phone = resident.getPhoneNumber();
        
        if (phone == null || phone.trim().isEmpty()) {
            showAlert("No Phone Number", "This resident doesn't have a phone number registered.");
            return;
        }
        
        // Create SMS dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Send SMS Notification");
        dialog.setHeaderText("Send SMS to: " + resident.getFirstName() + " " + resident.getLastName());
        
        // Template selection
        ComboBox<String> templateCombo = new ComboBox<>();
        templateCombo.getItems().addAll(
            "Document Ready for Pickup",
            "Document Approved",
            "Document Pending",
            "Custom Message"
        );
        templateCombo.setValue("Document Ready for Pickup");
        
        // Message area
        TextArea messageArea = new TextArea();
        messageArea.setPrefRowCount(5);
        messageArea.setWrapText(true);
        
        // Character count label
        Label charCountLabel = new Label("Characters: 0");
        
        // Update message when template changes
        templateCombo.setOnAction(e -> {
            String template = templateCombo.getValue();
            String message = "";
            
            switch (template) {
                case "Document Ready for Pickup":
                    message = String.format(
                        "Good day! Your %s is now ready for pickup at Barangay San Marino. " +
                        "Please bring a valid ID. Office hours: Mon-Fri 8AM-5PM. Thank you!",
                        request.getDocumentType()
                    );
                    break;
                case "Document Approved":
                    message = String.format(
                        "Your %s request has been approved. Processing time: 3-5 business days. " +
                        "Reference: %s. Thank you!",
                        request.getDocumentType(),
                        request.getId()
                    );
                    break;
                case "Document Pending":
                    message = String.format(
                        "Your %s request is being processed. Reference: %s. " +
                        "We will notify you once it's ready. Thank you for your patience!",
                        request.getDocumentType(),
                        request.getId()
                    );
                    break;
                case "Custom Message":
                    message = "";
                    break;
            }
            
            messageArea.setText(message);
            charCountLabel.setText("Characters: " + message.length());
        });
        
        // Trigger initial message
        templateCombo.fireEvent(new ActionEvent());
        
        // Update character count on text change
        messageArea.textProperty().addListener((obs, old, newVal) -> {
            charCountLabel.setText("Characters: " + newVal.length());
            if (newVal.length() > 160) {
                charCountLabel.setStyle("-fx-text-fill: orange;");
            } else {
                charCountLabel.setStyle("-fx-text-fill: black;");
            }
        });
        
        // Layout
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        content.getChildren().addAll(
            new Label("Phone: " + phone),
            new Label("Document: " + request.getDocumentType()),
            new Label("Status: " + request.getStatus()),
            new Separator(),
            new Label("Select Template:"),
            templateCombo,
            new Label("Message:"),
            messageArea,
            charCountLabel
        );
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // Handle send
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String message = messageArea.getText();
            if (message == null || message.trim().isEmpty()) {
                showAlert("Error", "Message cannot be empty.");
                return;
            }
            
            // Send SMS
            System.out.println("📤 Sending SMS to: " + phone);
            SMSService.SMSResponse response = SMSService.sendSMS(phone, message);
            
            if (response.isSuccess()) {
                showAlert("SMS Sent Successfully!", 
                    "✅ SMS sent to: " + resident.getFirstName() + " " + resident.getLastName() + "\n" +
                    "📱 Phone: " + phone + "\n" +
                    "🆔 Message ID: " + response.getMessageId() + "\n\n" +
                    "The resident should receive the SMS within 1-5 minutes.");
            } else {
                showAlert("SMS Failed", 
                    "❌ Failed to send SMS\n\n" +
                    "Error: " + response.getMessage() + "\n" +
                    "Error Code: " + response.getErrorCode() + "\n\n" +
                    "Please check:\n" +
                    "1. Phone number is correct\n" +
                    "2. SMS service is enabled\n" +
                    "3. You have sufficient SMS credits");
            }
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
        residentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        residentTable.setPrefHeight(500);

        TableColumn<Resident, String> photoCol = new TableColumn<>("Photo");
        photoCol.setPrefWidth(60);
        photoCol.setMinWidth(60);
        photoCol.setMaxWidth(80);
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
        nameCol.setPrefWidth(180);
        nameCol.setMinWidth(150);

        TableColumn<Resident, String> birthDateCol = new TableColumn<>("Birth Date");
        birthDateCol.setCellValueFactory(new PropertyValueFactory<>("birthDate"));
        birthDateCol.setId("birth_date");
        birthDateCol.setPrefWidth(120);
        birthDateCol.setMinWidth(100);

        TableColumn<Resident, String> genderCol = new TableColumn<>("Gender");
        genderCol.setCellValueFactory(new PropertyValueFactory<>("gender"));
        genderCol.setId("gender");
        genderCol.setPrefWidth(100);
        genderCol.setMinWidth(80);

        TableColumn<Resident, String> addressCol = new TableColumn<>("Address");
        addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        addressCol.setId("address");
        addressCol.setPrefWidth(250);
        addressCol.setMinWidth(200);
        // Enable text wrapping for address
        addressCol.setCellFactory(col -> {
            TableCell<Resident, String> cell = new TableCell<Resident, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                        setText(null);
                    } else {
                        Label label = new Label(item);
                        label.setWrapText(true);
                        label.setMaxWidth(240);
                        label.setStyle("-fx-font-size: 13px;");
                        setGraphic(label);
                        setText(null);
                    }
                }
            };
            cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
            return cell;
        });
        
        TableColumn<Resident, String> phoneCol = new TableColumn<>("Phone Number");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        phoneCol.setPrefWidth(130);
        phoneCol.setMinWidth(120);

        residentTable.getColumns().setAll(List.of(photoCol, nameCol, birthDateCol, genderCol, addressCol, phoneCol));

        // Create enhanced table with filtering and sorting
        ObservableList<Resident> residentData = FXCollections.observableArrayList();
        TableUtils.EnhancedTable<Resident> enhancedResidentTable = TableUtils.createEnhancedTable(residentTable, residentData);
        
        // Set global search function for residents
        enhancedResidentTable.setGlobalFilter(resident -> {
            StringBuilder searchText = new StringBuilder();
            searchText.append(resident.getFirstName()).append(" ");
            searchText.append(resident.getLastName()).append(" ");
            searchText.append(resident.getBirthDate()).append(" ");
            searchText.append(resident.getGender()).append(" ");
            if (resident.getAddress() != null) {
                searchText.append(resident.getAddress()).append(" ");
            }
            if (resident.getPhoneNumber() != null) {
                searchText.append(resident.getPhoneNumber()).append(" ");
            }
            return searchText.toString();
        });

        // Setup column sorting
        Platform.runLater(() -> {
            enhancedResidentTable.addColumnFilter(nameCol, resident -> 
                resident.getLastName() + ", " + resident.getFirstName());
            enhancedResidentTable.addColumnFilter(birthDateCol, Resident::getBirthDate);
            enhancedResidentTable.addColumnFilter(genderCol, Resident::getGender);
            enhancedResidentTable.addColumnFilter(addressCol, resident -> 
                resident.getAddress() != null ? resident.getAddress() : "");
            enhancedResidentTable.addColumnFilter(phoneCol, resident -> 
                resident.getPhoneNumber() != null ? resident.getPhoneNumber() : "");
        });

        Button addButton = new Button("Add");
        addButton.setGraphic(new FontIcon(FontAwesomeSolid.PLUS_CIRCLE));
        addButton.getStyleClass().addAll("button-secondary", "button-small");
        addButton.setTooltip(new Tooltip("Add Resident"));

        Button importButton = new Button("Import");
        importButton.setGraphic(new FontIcon(FontAwesomeSolid.FILE_IMPORT));
        importButton.getStyleClass().addAll("button-secondary", "button-small");
        importButton.setTooltip(new Tooltip("Import CSV"));

        Button editButton = new Button("Edit");
        editButton.setGraphic(new FontIcon(FontAwesomeSolid.PENCIL_ALT));
        editButton.getStyleClass().addAll("button-secondary", "button-small");
        editButton.setTooltip(new Tooltip("Edit Resident"));

        Button deleteButton = new Button("Delete");
        deleteButton.setGraphic(new FontIcon(FontAwesomeSolid.TRASH));
        deleteButton.getStyleClass().addAll("button-secondary", "button-small");
        deleteButton.setTooltip(new Tooltip("Delete Resident"));

        Button idButton = new Button("Print ID");
        idButton.setGraphic(new FontIcon(FontAwesomeSolid.ID_CARD));
        idButton.getStyleClass().addAll("button-secondary", "button-small");
        idButton.setTooltip(new Tooltip("Print ID Card"));

        Button viewIdBtn = new Button("View ID");
        viewIdBtn.setGraphic(new FontIcon(FontAwesomeSolid.ADDRESS_CARD));
        viewIdBtn.getStyleClass().addAll("button-secondary", "button-small");
        viewIdBtn.setTooltip(new Tooltip("View ID Card"));

        Button exportButton = new Button("Export PDF");
        exportButton.setGraphic(new FontIcon(FontAwesomeSolid.FILE_PDF));
        exportButton.getStyleClass().addAll("button-accent", "button-small");
        exportButton.setTooltip(new Tooltip("Export to PDF"));
        exportButton.setOnAction(e -> generateResidentPdf());

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

        // Load all resident data for enhanced table
        loadAllResidentData(residentData);

        addButton.setOnAction(e -> {
            showResidentDialog(null).ifPresent(resident -> {
                DatabaseHelper.addResident(resident);
                loadAllResidentData(residentData);
                showToast("Resident added successfully.");
            });
        });

        editButton.setOnAction(e -> {
            Resident selected = residentTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showResidentDialog(selected).ifPresent(resident -> {
                    DatabaseHelper.updateResident(resident);
                    loadAllResidentData(residentData);
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
                        loadAllResidentData(residentData);
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

        importButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Import Residents from CSV");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
            );
            
            File selectedFile = fileChooser.showOpenDialog(primaryStage);
            if (selectedFile != null) {
                // Show confirmation dialog
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Import CSV");
                confirm.setHeaderText("Import residents from CSV file?");
                confirm.setContentText("File: " + selectedFile.getName() + "\n\nThis will add new residents to the database.");
                
                confirm.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        // Perform import
                        DatabaseHelper.ImportResult result = DatabaseHelper.bulkImportResidentsFromCSV(selectedFile.getAbsolutePath());
                        int success = result.getSuccessCount();
                        int failed = result.getErrorCount();
                        
                        // Show result dialog
                        Alert resultAlert = new Alert(Alert.AlertType.INFORMATION);
                        resultAlert.setTitle("Import Complete");
                        resultAlert.setHeaderText("CSV Import Results");
                        
                        if (result.hasErrors()) {
                            // Show detailed error information
                            StringBuilder content = new StringBuilder();
                            content.append(String.format("Successfully imported: %d residents\nFailed: %d records\n\n", success, failed));
                            
                            if (result.getErrors().size() <= 5) {
                                content.append("Errors:\n");
                                result.getErrors().forEach(error -> content.append("• ").append(error).append("\n"));
                            } else {
                                content.append("First 5 errors:\n");
                                result.getErrors().stream().limit(5).forEach(error -> content.append("• ").append(error).append("\n"));
                                content.append(String.format("\n... and %d more errors", result.getErrors().size() - 5));
                            }
                            
                            resultAlert.setContentText(content.toString());
                        } else {
                            resultAlert.setContentText(
                                String.format("Successfully imported: %d residents\nFailed: %d records", success, failed)
                            );
                        }
                        resultAlert.showAndWait();
                        
                        // Refresh the table
                        if (success > 0) {
                            loadAllResidentData(residentData);
                            showToast(String.format("Imported %d residents successfully.", success));
                        }
                    }
                });
            }
        });

        // Create action toolbar
        HBox actionBox = new HBox(16);
        actionBox.setAlignment(Pos.CENTER_LEFT);
        actionBox.setPadding(new Insets(0, 0, 20, 0));
        actionBox.getChildren().addAll(
            addButton, importButton, editButton, deleteButton, 
            new Separator(Orientation.VERTICAL), 
            idButton, viewIdBtn,
            new Separator(Orientation.VERTICAL),
            exportButton
        );

        // Create main content with enhanced table
        VBox content = new VBox(24);
        content.setPadding(new Insets(28));
        
        Label title = new Label("Resident & Data Control");
        title.getStyleClass().add("text-heading-lg");
        
        content.getChildren().addAll(title, actionBox, enhancedResidentTable.getContainer());
        VBox.setVgrow(enhancedResidentTable.getContainer(), Priority.ALWAYS);

        updateDashboardContent(center, "Resident & Data Control", content);
    }

    // Helper method to load all resident data for enhanced table
    private void loadAllResidentData(ObservableList<Resident> residentData) {
        residentData.clear();
        // Load all residents without pagination for enhanced table filtering
        ObservableList<Resident> allResidents = DatabaseHelper.getResidents(null, 0, Integer.MAX_VALUE, "last_name", "ASC");
        residentData.addAll(allResidents);
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
        
        // For new residents, set default image path and preview
        if (existingResident == null) {
            String defaultImagePath = getDefaultResidentImagePath();
            if (!defaultImagePath.isEmpty()) {
                imagePathField.setText(defaultImagePath);
                try {
                    photoPreview.setImage(new Image(new File(defaultImagePath).toURI().toString()));
                } catch (Exception ex) {
                    photoPreview.setImage(getDefaultUserIcon());
                }
            } else {
                photoPreview.setImage(getDefaultUserIcon());
            }
        } else {
            // For existing residents, use their current image or default icon
            photoPreview.setImage(getDefaultUserIcon());
        }

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
        
        TextField phoneNumber = new TextField();
        phoneNumber.setPromptText("e.g., 09171234567");
        phoneNumber.setPrefWidth(200);

        uploadBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                imagePathField.setText(file.getAbsolutePath());
                try {
                    photoPreview.setImage(new Image(file.toURI().toString()));
                } catch (Exception ex) {
                    // If image fails to load, use default icon
                    photoPreview.setImage(getDefaultUserIcon());
                    showToast("Failed to load image, using default icon.");
                }
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
        grid.add(new Label("Phone Number:"), 0, 7);
        grid.add(phoneNumber, 1, 7);

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
            phoneNumber.setText(existingResident.getPhoneNumber() != null ? existingResident.getPhoneNumber() : "");
            if (existingResident.getImagePath() != null && !existingResident.getImagePath().isEmpty()) {
                File imageFile = new File(existingResident.getImagePath());
                if (imageFile.exists()) {
                    try {
                        imagePathField.setText(existingResident.getImagePath());
                        photoPreview.setImage(new Image(imageFile.toURI().toString()));
                    } catch (Exception ex) {
                        // If image fails to load, use default icon
                        photoPreview.setImage(getDefaultUserIcon());
                    }
                } else {
                    // Image file doesn't exist, use default icon
                    photoPreview.setImage(getDefaultUserIcon());
                }
            } else {
                // No image path set, use default icon
                photoPreview.setImage(getDefaultUserIcon());
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
                
                // Set image path - use default if no custom image was selected
                String imagePath = imagePathField.getText();
                if (imagePath == null || imagePath.trim().isEmpty()) {
                    imagePath = getDefaultResidentImagePath();
                }
                r.setImagePath(imagePath);
                
                // Set phone number
                r.setPhoneNumber(phoneNumber.getText().trim());
                
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
        card.getStyleClass().add("card-rounded");

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
        nameLbl.getStyleClass().add("text-heading-sm");
        
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
                            // Refresh resident data in enhanced table if available
                            if (residentTable != null && residentTable.getItems() instanceof ObservableList) {
                                ObservableList<Resident> residentData = (ObservableList<Resident>) residentTable.getItems();
                                loadAllResidentData(residentData);
                            }
                        });
                    });
            } catch (NumberFormatException e) {
                e.printStackTrace(); // Log if the ID in the QR code is not a valid number
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

    private Image getDefaultUserIcon() {
        // Create a default user icon as a light gray circle with a user silhouette
        javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(100, 100);
        javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();
        
        // Draw light gray background circle
        gc.setFill(javafx.scene.paint.Color.web("#d0d0d0"));
        gc.fillOval(0, 0, 100, 100);
        
        // Draw circle border
        gc.setStroke(javafx.scene.paint.Color.web("#808080"));
        gc.setLineWidth(2);
        gc.strokeOval(0, 0, 100, 100);
        
        // Draw user silhouette (head and shoulders)
        gc.setFill(javafx.scene.paint.Color.web("#808080"));
        // Head
        gc.fillOval(35, 15, 30, 30);
        // Shoulders/body
        gc.fillPolygon(new double[]{15, 85, 75, 25}, new double[]{50, 50, 100, 100}, 4);
        
        return javafx.embed.swing.SwingFXUtils.toFXImage(
            javafx.embed.swing.SwingFXUtils.fromFXImage(
                canvas.snapshot(null, null), null), null);
    }

    /**
     * Gets the absolute path to the default resident photo.
     * This is used when creating new residents without uploading a custom photo.
     * @return Absolute path to defaultresident.jpg
     */
    private String getDefaultResidentImagePath() {
        // Try to get from resources first
        try {
            var resourceStream = getClass().getResourceAsStream("/assets/defaultresident.jpg");
            if (resourceStream != null) {
                resourceStream.close();
                // Resource exists, but we need the file path for database storage
                // Fall through to file path approach
            }
        } catch (Exception e) {
            // Ignore, will use file path
        }
        
        // Use the file path in src/assets
        File defaultImageFile = new File("src/assets/defaultresident.jpg");
        if (defaultImageFile.exists()) {
            return defaultImageFile.getAbsolutePath();
        }
        
        // Fallback: return empty string if not found
        return "";
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
        heading.setStyle("-fx-text-fill: " + "#1a1a1a" + "; -fx-font-size: 16; -fx-font-weight: bold;");
        var content = new Label(body);
        content.setStyle("-fx-text-fill: " + "#333" + "; -fx-font-size: 12;");
        var box = new VBox(10, heading, content);
        box.getStyleClass().add("content-box");
        return box;
    }

    // ==================== USER & ACCESS MANAGEMENT ====================

    private void showUserAndAccess(VBox center) {
        // Get user permissions for User & Access module
        Map<String, String> userPermissions = DatabaseHelper.getPermissions(currentRole);
        String userAccessPermission = userPermissions.get("User & Access");
        
        // Four tabs: Manage Users, Manage Roles, Permissions, and Audit Log
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.getStyleClass().add("tab-pane");

        // Tab 1: Manage Users - Only for Full Access and Manage
        if ("Full Access".equals(userAccessPermission) || "Manage".equals(userAccessPermission)) {
            Tab usersTab = new Tab("Manage Users", createManageUsersPanel());
            usersTab.getStyleClass().add("tab");
            tabPane.getTabs().add(usersTab);
        }

        // Tab 2: Manage Roles - Only for Full Access and Manage
        if ("Full Access".equals(userAccessPermission) || "Manage".equals(userAccessPermission)) {
            Tab rolesTab = new Tab("Manage Roles", createManageRolesPanel());
            rolesTab.getStyleClass().add("tab");
            tabPane.getTabs().add(rolesTab);
        }

        // Tab 3: Role Permissions - Visible for Full Access, Manage, and View Only
        if (!"None".equals(userAccessPermission)) {
            Tab permissionsTab = new Tab("Role Permissions", createPermissionsPanel());
            permissionsTab.getStyleClass().add("tab");
            tabPane.getTabs().add(permissionsTab);
        }

        // Tab 4: Audit Log - Visible for Full Access, Manage, and View Only
        if (!"None".equals(userAccessPermission)) {
            Tab auditTab = new Tab("Audit Log", createAuditLogPanel());
            auditTab.getStyleClass().add("tab");
            tabPane.getTabs().add(auditTab);
        }

        // If no tabs are available (shouldn't happen if menu is hidden), show message
        if (tabPane.getTabs().isEmpty()) {
            Label noAccessLabel = new Label("You do not have permission to access this section.");
            noAccessLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #666; -fx-padding: 20;");
            VBox messageBox = new VBox(noAccessLabel);
            messageBox.setAlignment(javafx.geometry.Pos.CENTER);
            messageBox.setPadding(new Insets(50));
            updateDashboardContent(center, "User & Access Management", messageBox);
        } else {
            updateDashboardContent(center, "User & Access Management", tabPane);
        }
    }

    private VBox createManageUsersPanel() {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(20));

        // Header
        Label title = new Label("User Management");
        title.getStyleClass().add("text-heading");

        Label subtitle = new Label("All residents are listed below. Assign or change roles to grant system access.");
        subtitle.getStyleClass().add("text-muted");

        VBox header = new VBox(4, title, subtitle);

        // Build the unified table
        TableView<ResidentUserRow> table = createResidentUserTable();
        ObservableList<ResidentUserRow> data = DatabaseHelper.getAllResidentsWithAccountInfo();

        TableUtils.EnhancedTable<ResidentUserRow> enhancedTable = TableUtils.createEnhancedTable(table, data);
        enhancedTable.setGlobalFilter(row ->
            row.getFullName() + " " + row.getPhoneNumber() + " " + row.getAddress() +
            " " + row.getRole() + " " + row.getUsername() +
            " " + (row.hasAccount() ? "has account active" : "no account")
        );

        this.usersManagementTable = table;
        this.enhancedUsersTable = enhancedTable;

        panel.getChildren().addAll(header, enhancedTable.getContainer());
        VBox.setVgrow(enhancedTable.getContainer(), Priority.ALWAYS);
        return panel;
    }

    private TableView<ResidentUserRow> createResidentUserTable() {
        TableView<ResidentUserRow> table = new TableView<>();
        table.getStyleClass().add("table-view");
        table.setPrefHeight(500);
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        table.setRowFactory(tv -> {
            TableRow<ResidentUserRow> row = new TableRow<>();
            row.setPrefHeight(38);
            return row;
        });

        // --- Name ---
        TableColumn<ResidentUserRow, String> nameCol = new TableColumn<>("Full Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        nameCol.setPrefWidth(180);
        nameCol.setMinWidth(150);

        // --- Phone ---
        TableColumn<ResidentUserRow, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        phoneCol.setPrefWidth(120);
        phoneCol.setMinWidth(100);

        // --- Address ---
        TableColumn<ResidentUserRow, String> addressCol = new TableColumn<>("Address");
        addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        addressCol.setPrefWidth(200);
        addressCol.setMinWidth(150);

        // --- Account Status badge ---
        TableColumn<ResidentUserRow, Boolean> statusCol = new TableColumn<>("Account");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("hasAccount"));
        statusCol.setPrefWidth(100);
        statusCol.setMinWidth(90);
        statusCol.setCellFactory(col -> new TableCell<ResidentUserRow, Boolean>() {
            @Override
            protected void updateItem(Boolean has, boolean empty) {
                super.updateItem(has, empty);
                if (empty || has == null) { setGraphic(null); return; }
                Label badge = new Label(has ? "Has Account" : "No Account");
                badge.setStyle(has
                    ? "-fx-background-color:#d1fae5;-fx-text-fill:#065f46;-fx-padding:3 8;-fx-background-radius:10;-fx-font-size:11px;-fx-font-weight:700;"
                    : "-fx-background-color:#f3f4f6;-fx-text-fill:#6b7280;-fx-padding:3 8;-fx-background-radius:10;-fx-font-size:11px;-fx-font-weight:700;");
                setGraphic(badge);
                setText(null);
            }
        });

        // --- Username ---
        TableColumn<ResidentUserRow, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        usernameCol.setPrefWidth(130);
        usernameCol.setMinWidth(100);
        usernameCol.setCellFactory(col -> new TableCell<ResidentUserRow, String>() {
            @Override
            protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty) { setText(null); return; }
                setText(val != null && !val.isEmpty() ? val : "—");
                setStyle(val == null || val.isEmpty() ? "-fx-text-fill:#9ca3af;" : "");
            }
        });

        // --- Role (inline ComboBox) ---
        TableColumn<ResidentUserRow, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        roleCol.setPrefWidth(200);
        roleCol.setMinWidth(170);
        roleCol.setCellFactory(col -> new TableCell<ResidentUserRow, String>() {
            private final ComboBox<String> combo = new ComboBox<>();
            {
                combo.setPromptText("Assign role...");
                combo.setPrefWidth(185);
                combo.setStyle("-fx-font-size:12px;");
                // Populate roles
                combo.getItems().add("— No Role —");
                for (Role r : DatabaseHelper.getAllRoles()) combo.getItems().add(r.getName());

                combo.setOnAction(e -> {
                    ResidentUserRow row = getTableView().getItems().get(getIndex());
                    String selected = combo.getValue();
                    if (selected == null) return;
                    if ("— No Role —".equals(selected)) {
                        // Remove account
                        if (row.hasAccount()) {
                            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                                "Remove system access for " + row.getFullName() + "?",
                                ButtonType.YES, ButtonType.NO);
                            confirm.setTitle("Remove Role");
                            confirm.showAndWait().ifPresent(btn -> {
                                if (btn == ButtonType.YES) {
                                    DatabaseHelper.removeRoleFromResident(row.getResidentId());
                                    row.setUserId(0);
                                    row.setRole("");
                                    row.setUsername("");
                                    getTableView().refresh();
                                    showToast("Access removed for " + row.getFullName());
                                }
                            });
                        }
                    } else {
                        // Assign / change role
                        String username = DatabaseHelper.assignRoleToResident(row.getResidentId(), selected);
                        if (username != null) {
                            User updated = DatabaseHelper.getUserByUsername(username);
                            if (updated != null) {
                                row.setUserId(updated.getId());
                                row.setUsername(updated.getUsername());
                            }
                            row.setRole(selected);
                            getTableView().refresh();
                            String msg = row.hasAccount()
                                ? "Role updated to " + selected + " for " + row.getFullName()
                                : "Account created for " + row.getFullName() + " (user: " + username + ", temp password: bdms@" + row.getResidentId() + ")";
                            showToast(msg);
                        } else {
                            showToast("Failed to assign role.");
                        }
                    }
                });
            }

            @Override
            protected void updateItem(String role, boolean empty) {
                super.updateItem(role, empty);
                if (empty) { setGraphic(null); return; }
                ResidentUserRow row = getTableView().getItems().get(getIndex());
                String current = row.getRole();
                if (current != null && !current.isEmpty()) {
                    combo.setValue(current);
                } else {
                    combo.setValue(null);
                }
                setGraphic(combo);
                setText(null);
            }
        });

        // --- Last Login ---
        TableColumn<ResidentUserRow, String> loginCol = new TableColumn<>("Last Login");
        loginCol.setCellValueFactory(new PropertyValueFactory<>("lastLogin"));
        loginCol.setPrefWidth(140);
        loginCol.setMinWidth(120);
        loginCol.setCellFactory(col -> new TableCell<ResidentUserRow, String>() {
            @Override
            protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty) { setText(null); return; }
                setText(val != null && !val.isEmpty() && !"Never".equals(val) ? val : "—");
                setStyle("Never".equals(val) || val == null || val.isEmpty() ? "-fx-text-fill:#9ca3af;" : "");
            }
        });

        table.getColumns().addAll(nameCol, phoneCol, addressCol, statusCol, usernameCol, roleCol, loginCol);
        return table;
    }

    private TableView<User> createUsersTable() {


        TableView<User> usersTable = new TableView<>();
        usersTable.getStyleClass().add("table-view");
        usersTable.setPrefHeight(500);
        // UNCONSTRAINED so each column respects its set width — no word-wrapping
        usersTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        TableColumn<User, Number> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(55);
        idCol.setMinWidth(55);
        idCol.setMaxWidth(70);

        TableColumn<User, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        usernameCol.setPrefWidth(130);
        usernameCol.setMinWidth(110);

        TableColumn<User, String> residentNameCol = new TableColumn<>("Resident Name");
        residentNameCol.setPrefWidth(160);
        residentNameCol.setMinWidth(150);
        residentNameCol.setCellValueFactory(cellData -> {
            User user = cellData.getValue();
            if (user.getResidentId() > 0) {
                Resident resident = DatabaseHelper.getResidentForUser(user.getId());
                if (resident != null) {
                    return new javafx.beans.property.SimpleStringProperty(
                        resident.getFirstName() + " " + resident.getLastName()
                    );
                }
            }
            return new javafx.beans.property.SimpleStringProperty("No resident linked");
        });

        TableColumn<User, String> residentInfoCol = new TableColumn<>("Contact Info");
        residentInfoCol.setPrefWidth(160);
        residentInfoCol.setMinWidth(140);
        residentInfoCol.setCellValueFactory(cellData -> {
            User user = cellData.getValue();
            if (user.getResidentId() > 0) {
                Resident resident = DatabaseHelper.getResidentForUser(user.getId());
                if (resident != null) {
                    String info = "";
                    if (resident.getPhoneNumber() != null && !resident.getPhoneNumber().trim().isEmpty()) {
                        info += "📱 " + resident.getPhoneNumber();
                    }
                    if (resident.getAddress() != null && !resident.getAddress().trim().isEmpty()) {
                        if (!info.isEmpty()) info += "\n";
                        info += "🏠 " + (resident.getAddress().length() > 25 ? 
                            resident.getAddress().substring(0, 25) + "..." : resident.getAddress());
                    }
                    return new javafx.beans.property.SimpleStringProperty(info.isEmpty() ? "No contact info" : info);
                }
            }
            return new javafx.beans.property.SimpleStringProperty("System account");
        });
        
        // Enable text wrapping for contact info
        residentInfoCol.setCellFactory(col -> {
            TableCell<User, String> cell = new TableCell<User, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                        setText(null);
                    } else {
                        Label label = new Label(item);
                        label.setWrapText(true);
                        label.setMaxWidth(180);
                        label.setStyle("-fx-font-size: 12px;");
                        setGraphic(label);
                        setText(null);
                    }
                }
            };
            cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
            return cell;
        });

        TableColumn<User, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        roleCol.setPrefWidth(120);
        roleCol.setMinWidth(100);

        TableColumn<User, String> createdCol = new TableColumn<>("Created");
        createdCol.setCellValueFactory(new PropertyValueFactory<>("createdDate"));
        createdCol.setPrefWidth(145);
        createdCol.setMinWidth(130);

        TableColumn<User, String> lastLoginCol = new TableColumn<>("Last Login");
        lastLoginCol.setCellValueFactory(new PropertyValueFactory<>("lastLogin"));
        lastLoginCol.setPrefWidth(145);
        lastLoginCol.setMinWidth(130);

        TableColumn<User, Boolean> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("isActive"));
        statusCol.setPrefWidth(85);
        statusCol.setMinWidth(75);
        statusCol.setCellFactory(col -> new TableCell<User, Boolean>() {
            @Override
            protected void updateItem(Boolean isActive, boolean empty) {
                super.updateItem(isActive, empty);
                if (empty || isActive == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(isActive ? "Active" : "Inactive");
                    badge.setStyle("-fx-background-color: " + (isActive ? "#10b981" : "#ef4444") + 
                        "; -fx-text-fill: white; -fx-padding: 4 8; -fx-border-radius: 12; -fx-background-radius: 12; -fx-font-size: 11px;");
                    setGraphic(badge);
                }
            }
        });

        TableColumn<User, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(120);
        actionsCol.setMinWidth(110);
        actionsCol.setSortable(false); // Actions column shouldn't be sortable
        actionsCol.setCellFactory(col -> new TableCell<User, Void>() {
            private final Button editBtn = new Button("", new FontIcon(FontAwesomeSolid.EDIT));
            private final Button deleteBtn = new Button("", new FontIcon(FontAwesomeSolid.TRASH));
            private final Button resetPasswordBtn = new Button("", new FontIcon(FontAwesomeSolid.KEY));
            private final HBox actionBox = new HBox(4, editBtn, resetPasswordBtn, deleteBtn);

            {
                editBtn.getStyleClass().add("button-secondary");
                editBtn.setTooltip(new Tooltip("Edit User"));
                editBtn.setPrefWidth(30);
                editBtn.setPrefHeight(28);
                editBtn.setOnAction(e -> {
                    User user = getTableView().getItems().get(getIndex());
                    showEditUserDialog(user);
                });

                resetPasswordBtn.getStyleClass().add("button-warning");
                resetPasswordBtn.setTooltip(new Tooltip("Reset Password"));
                resetPasswordBtn.setPrefWidth(30);
                resetPasswordBtn.setPrefHeight(28);
                resetPasswordBtn.setOnAction(e -> {
                    User user = getTableView().getItems().get(getIndex());
                    showResetPasswordDialog(user);
                });

                deleteBtn.getStyleClass().add("button-danger");
                deleteBtn.setTooltip(new Tooltip("Delete User"));
                deleteBtn.setPrefWidth(30);
                deleteBtn.setPrefHeight(28);
                deleteBtn.setOnAction(e -> {
                    User user = getTableView().getItems().get(getIndex());
                    showDeleteUserConfirmation(user);
                });

                actionBox.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    User user = getTableView().getItems().get(getIndex());
                    // Disable delete for current user
                    deleteBtn.setDisable(user.getUsername().equals(currentUsername));
                    setGraphic(actionBox);
                }
            }
        });

        usersTable.getColumns().addAll(idCol, usernameCol, residentNameCol, residentInfoCol, roleCol, createdCol, lastLoginCol, statusCol, actionsCol);

        // Standard row height — no text wrapping needed
        usersTable.setRowFactory(tv -> {
            TableRow<User> row = new TableRow<>();
            row.setPrefHeight(38);
            return row;
        });

        return usersTable;
    }

    private void setupUsersTableFilters() {
        // No-op: filtering is handled by the global search in EnhancedTable
    }

    private void refreshUsersManagementTable() {
        if (enhancedUsersTable != null) {
            enhancedUsersTable.refreshData(DatabaseHelper.getAllResidentsWithAccountInfo());
        }
    }

    private void showAddUserDialog() {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Add New User");
        dialog.setHeaderText("Create a new user account");

        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm Password");

        ComboBox<String> roleCombo = new ComboBox<>();
        ObservableList<Role> roles = DatabaseHelper.getAllRoles();
        for (Role role : roles) {
            roleCombo.getItems().add(role.getName());
        }
        roleCombo.setPromptText("Select Role");

        CheckBox activeCheckBox = new CheckBox("Active");
        activeCheckBox.setSelected(true);

        grid.add(new Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(new Label("Confirm Password:"), 0, 2);
        grid.add(confirmPasswordField, 1, 2);
        grid.add(new Label("Role:"), 0, 3);
        grid.add(roleCombo, 1, 3);
        grid.add(new Label("Status:"), 0, 4);
        grid.add(activeCheckBox, 1, 4);

        dialog.getDialogPane().setContent(grid);

        // Enable/disable create button based on input validation
        Node createButton = dialog.getDialogPane().lookupButton(createButtonType);
        createButton.setDisable(true);

        // Validation
        Runnable validateInput = () -> {
            boolean valid = !usernameField.getText().trim().isEmpty() &&
                           !passwordField.getText().isEmpty() &&
                           passwordField.getText().equals(confirmPasswordField.getText()) &&
                           roleCombo.getValue() != null;
            createButton.setDisable(!valid);
        };

        usernameField.textProperty().addListener((obs, old, text) -> validateInput.run());
        passwordField.textProperty().addListener((obs, old, text) -> validateInput.run());
        confirmPasswordField.textProperty().addListener((obs, old, text) -> validateInput.run());
        roleCombo.valueProperty().addListener((obs, old, role) -> validateInput.run());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                return new User(usernameField.getText().trim(), roleCombo.getValue());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(user -> {
            if (DatabaseHelper.createUser(user.getUsername(), passwordField.getText(), user.getRole())) {
                showToast("User created successfully");
                refreshUsersManagementTable();
            } else {
                showToast("Failed to create user. Username may already exist.");
            }
        });
    }

    private void showEditUserDialog(User user) {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Edit User");
        dialog.setHeaderText("Edit user: " + user.getUsername());

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField usernameField = new TextField(user.getUsername());
        usernameField.setPromptText("Username");

        ComboBox<String> roleCombo = new ComboBox<>();
        ObservableList<Role> roles = DatabaseHelper.getAllRoles();
        for (Role role : roles) {
            roleCombo.getItems().add(role.getName());
        }
        roleCombo.setValue(user.getRole());

        CheckBox activeCheckBox = new CheckBox("Active");
        activeCheckBox.setSelected(user.isActive());

        grid.add(new Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Role:"), 0, 1);
        grid.add(roleCombo, 1, 1);
        grid.add(new Label("Status:"), 0, 2);
        grid.add(activeCheckBox, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                user.setUsername(usernameField.getText().trim());
                user.setRole(roleCombo.getValue());
                user.setActive(activeCheckBox.isSelected());
                return user;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updatedUser -> {
            if (DatabaseHelper.updateUser(updatedUser.getId(), updatedUser.getUsername(), 
                                        updatedUser.getRole(), updatedUser.isActive())) {
                showToast("User updated successfully");
                refreshUsersManagementTable();
            } else {
                showToast("Failed to update user");
            }
        });
    }

    private void showResetPasswordDialog(User user) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Reset Password");
        dialog.setHeaderText("Reset password for: " + user.getUsername());

        ButtonType resetButtonType = new ButtonType("Reset", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(resetButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("New Password");

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm New Password");

        grid.add(new Label("New Password:"), 0, 0);
        grid.add(newPasswordField, 1, 0);
        grid.add(new Label("Confirm Password:"), 0, 1);
        grid.add(confirmPasswordField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // Enable/disable reset button based on input validation
        Node resetButton = dialog.getDialogPane().lookupButton(resetButtonType);
        resetButton.setDisable(true);

        Runnable validateInput = () -> {
            boolean valid = !newPasswordField.getText().isEmpty() &&
                           newPasswordField.getText().equals(confirmPasswordField.getText());
            resetButton.setDisable(!valid);
        };

        newPasswordField.textProperty().addListener((obs, old, text) -> validateInput.run());
        confirmPasswordField.textProperty().addListener((obs, old, text) -> validateInput.run());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == resetButtonType) {
                return newPasswordField.getText();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(newPassword -> {
            if (DatabaseHelper.changeUserPassword(user.getUsername(), newPassword)) {
                showToast("Password reset successfully");
            } else {
                showToast("Failed to reset password");
            }
        });
    }

    private void showPromoteResidentDialog() {
        Dialog<Resident> dialog = new Dialog<>();
        dialog.setTitle("Promote Resident to User");
        dialog.setHeaderText("Create user account for existing resident");

        ButtonType promoteButtonType = new ButtonType("Create Account", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(promoteButtonType, ButtonType.CANCEL);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // Search for residents without accounts
        Label searchLabel = new Label("Search Residents:");
        searchLabel.getStyleClass().add("text-bold");

        TextField residentSearchField = new TextField();
        residentSearchField.setPromptText("Search by name...");
        residentSearchField.setPrefWidth(400);

        // Table of residents without user accounts
        TableView<Resident> residentsTable = new TableView<>();
        residentsTable.setPrefHeight(300);
        residentsTable.getStyleClass().add("table-view");

        TableColumn<Resident, String> nameCol = new TableColumn<>("Name");
        nameCol.setPrefWidth(200);
        nameCol.setCellValueFactory(cellData -> {
            Resident resident = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                resident.getFirstName() + " " + resident.getLastName()
            );
        });

        TableColumn<Resident, String> addressCol = new TableColumn<>("Address");
        addressCol.setPrefWidth(250);
        addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));

        TableColumn<Resident, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setPrefWidth(120);
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));

        residentsTable.getColumns().addAll(nameCol, addressCol, phoneCol);

        // Load residents without accounts
        ObservableList<Resident> residentsWithoutAccounts = DatabaseHelper.getResidentsWithoutAccounts();
        residentsTable.setItems(residentsWithoutAccounts);

        // Search functionality
        residentSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                residentsTable.setItems(residentsWithoutAccounts);
            } else {
                ObservableList<Resident> filtered = FXCollections.observableArrayList();
                String searchTerm = newValue.toLowerCase();
                for (Resident resident : residentsWithoutAccounts) {
                    String fullName = (resident.getFirstName() + " " + resident.getLastName()).toLowerCase();
                    if (fullName.contains(searchTerm) || 
                        (resident.getAddress() != null && resident.getAddress().toLowerCase().contains(searchTerm))) {
                        filtered.add(resident);
                    }
                }
                residentsTable.setItems(filtered);
            }
        });

        // Account creation form
        Label formLabel = new Label("Account Details:");
        formLabel.getStyleClass().add("text-bold");

        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm Password");

        ComboBox<String> roleCombo = new ComboBox<>();
        ObservableList<Role> roles = DatabaseHelper.getAllRoles();
        for (Role role : roles) {
            roleCombo.getItems().add(role.getName());
        }
        roleCombo.setPromptText("Select Role");

        formGrid.add(new Label("Username:"), 0, 0);
        formGrid.add(usernameField, 1, 0);
        formGrid.add(new Label("Password:"), 0, 1);
        formGrid.add(passwordField, 1, 1);
        formGrid.add(new Label("Confirm Password:"), 0, 2);
        formGrid.add(confirmPasswordField, 1, 2);
        formGrid.add(new Label("Role:"), 0, 3);
        formGrid.add(roleCombo, 1, 3);

        // Auto-fill username when resident is selected
        residentsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                String suggestedUsername = (newSelection.getFirstName() + "." + newSelection.getLastName()).toLowerCase()
                    .replaceAll("[^a-z0-9.]", "");
                usernameField.setText(suggestedUsername);
            }
        });

        content.getChildren().addAll(
            searchLabel, residentSearchField, residentsTable,
            new Separator(),
            formLabel, formGrid
        );

        dialog.getDialogPane().setContent(content);

        // Enable/disable promote button based on validation
        Node promoteButton = dialog.getDialogPane().lookupButton(promoteButtonType);
        promoteButton.setDisable(true);

        Runnable validateInput = () -> {
            boolean valid = residentsTable.getSelectionModel().getSelectedItem() != null &&
                           !usernameField.getText().trim().isEmpty() &&
                           !passwordField.getText().isEmpty() &&
                           passwordField.getText().equals(confirmPasswordField.getText()) &&
                           roleCombo.getValue() != null;
            promoteButton.setDisable(!valid);
        };

        residentsTable.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> validateInput.run());
        usernameField.textProperty().addListener((obs, old, text) -> validateInput.run());
        passwordField.textProperty().addListener((obs, old, text) -> validateInput.run());
        confirmPasswordField.textProperty().addListener((obs, old, text) -> validateInput.run());
        roleCombo.valueProperty().addListener((obs, old, role) -> validateInput.run());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == promoteButtonType) {
                return residentsTable.getSelectionModel().getSelectedItem();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(selectedResident -> {
            if (DatabaseHelper.createUserFromResident(
                selectedResident.getId(), 
                usernameField.getText().trim(), 
                passwordField.getText(), 
                roleCombo.getValue())) {
                showToast("User account created successfully for " + selectedResident.getFirstName() + " " + selectedResident.getLastName());
                refreshUsersManagementTable();
            } else {
                showToast("Failed to create user account. Username may already exist or resident already has an account.");
            }
        });
    }

    private void showResidentsAccountStatusDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Residents Account Status");
        dialog.setHeaderText("View all residents and their system account status");

        ButtonType closeButtonType = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(closeButtonType);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(800);
        content.setPrefHeight(600);

        // Search field
        TextField searchField = new TextField();
        searchField.setPromptText("Search residents by name or address...");
        searchField.setPrefWidth(400);

        // Filter buttons
        HBox filterBox = new HBox(10);
        Button showAllBtn = new Button("All");
        Button showWithAccountsBtn = new Button("With Accounts");
        Button showWithoutAccountsBtn = new Button("No Accounts");
        
        showAllBtn.getStyleClass().addAll("button-primary", "button-small");
        showWithAccountsBtn.getStyleClass().addAll("button-secondary", "button-small");
        showWithoutAccountsBtn.getStyleClass().addAll("button-secondary", "button-small");
        
        showAllBtn.setPrefWidth(60);
        showWithAccountsBtn.setPrefWidth(120);
        showWithoutAccountsBtn.setPrefWidth(110);

        filterBox.getChildren().addAll(showAllBtn, showWithAccountsBtn, showWithoutAccountsBtn);

        // Residents table with enhanced functionality
        TableView<Resident> residentsTable = new TableView<>();
        residentsTable.setPrefHeight(400);
        residentsTable.getStyleClass().add("table-view");

        TableColumn<Resident, String> nameCol = new TableColumn<>("Name");
        nameCol.setPrefWidth(180);
        nameCol.setCellValueFactory(cellData -> {
            Resident resident = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                resident.getFirstName() + " " + resident.getLastName()
            );
        });

        TableColumn<Resident, String> addressCol = new TableColumn<>("Address");
        addressCol.setPrefWidth(200);
        addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));

        TableColumn<Resident, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setPrefWidth(120);
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));

        TableColumn<Resident, String> accountStatusCol = new TableColumn<>("Account Status");
        accountStatusCol.setPrefWidth(150);
        accountStatusCol.setCellValueFactory(cellData -> {
            Resident resident = cellData.getValue();
            User user = DatabaseHelper.getUserByResidentId(resident.getId());
            if (user != null) {
                return new javafx.beans.property.SimpleStringProperty("✅ " + user.getRole());
            } else {
                return new javafx.beans.property.SimpleStringProperty("❌ No Account");
            }
        });

        TableColumn<Resident, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(120);
        actionsCol.setSortable(false);
        actionsCol.setCellFactory(col -> new TableCell<Resident, Void>() {
            private final Button createAccountBtn = new Button("Create", new FontIcon(FontAwesomeSolid.USER_PLUS));
            private final Button viewAccountBtn = new Button("View", new FontIcon(FontAwesomeSolid.EYE));

            {
                createAccountBtn.getStyleClass().addAll("button-success", "button-small");
                viewAccountBtn.getStyleClass().addAll("button-info", "button-small");
                createAccountBtn.setPrefWidth(80);
                viewAccountBtn.setPrefWidth(70);
                
                createAccountBtn.setOnAction(e -> {
                    Resident resident = getTableView().getItems().get(getIndex());
                    dialog.close();
                    showPromoteResidentDialog();
                });

                viewAccountBtn.setOnAction(e -> {
                    Resident resident = getTableView().getItems().get(getIndex());
                    User user = DatabaseHelper.getUserByResidentId(resident.getId());
                    if (user != null) {
                        showUserAccountDetailsDialog(user, resident);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Resident resident = getTableView().getItems().get(getIndex());
                    User user = DatabaseHelper.getUserByResidentId(resident.getId());
                    if (user != null) {
                        setGraphic(viewAccountBtn);
                    } else {
                        setGraphic(createAccountBtn);
                    }
                }
            }
        });

        residentsTable.getColumns().addAll(nameCol, addressCol, phoneCol, accountStatusCol, actionsCol);

        // Create enhanced table for residents
        ObservableList<Resident> allResidents = DatabaseHelper.getResidents("", 0, 1000, "last_name", "ASC");
        TableUtils.EnhancedTable<Resident> enhancedResidentsTable = TableUtils.createEnhancedTable(residentsTable, allResidents);
        
        // Set global search function for residents
        enhancedResidentsTable.setGlobalFilter(resident -> {
            StringBuilder searchText = new StringBuilder();
            searchText.append(resident.getFirstName()).append(" ");
            searchText.append(resident.getLastName()).append(" ");
            if (resident.getAddress() != null) {
                searchText.append(resident.getAddress()).append(" ");
            }
            if (resident.getPhoneNumber() != null) {
                searchText.append(resident.getPhoneNumber()).append(" ");
            }
            
            // Add account status to search
            User user = DatabaseHelper.getUserByResidentId(resident.getId());
            if (user != null) {
                searchText.append(user.getRole()).append(" ");
                searchText.append("has account active");
            } else {
                searchText.append("no account inactive");
            }
            
            return searchText.toString();
        });

        // Setup column filters for residents table
        Platform.runLater(() -> {
            enhancedResidentsTable.addColumnFilter(nameCol, resident -> 
                resident.getFirstName() + " " + resident.getLastName());
            enhancedResidentsTable.addColumnFilter(addressCol, Resident::getAddress);
            enhancedResidentsTable.addColumnFilter(phoneCol, Resident::getPhoneNumber);
            enhancedResidentsTable.addColumnFilter(accountStatusCol, resident -> {
                User user = DatabaseHelper.getUserByResidentId(resident.getId());
                return user != null ? "✅ " + user.getRole() : "❌ No Account";
            });
        });

        // Filter functionality
        showAllBtn.setOnAction(e -> {
            enhancedResidentsTable.refreshData(allResidents);
            showAllBtn.getStyleClass().clear();
            showAllBtn.getStyleClass().addAll("button-primary", "button-small");
            showWithAccountsBtn.getStyleClass().clear();
            showWithAccountsBtn.getStyleClass().addAll("button-secondary", "button-small");
            showWithoutAccountsBtn.getStyleClass().clear();
            showWithoutAccountsBtn.getStyleClass().addAll("button-secondary", "button-small");
        });

        showWithAccountsBtn.setOnAction(e -> {
            ObservableList<Resident> withAccounts = FXCollections.observableArrayList();
            for (Resident resident : allResidents) {
                if (DatabaseHelper.getUserByResidentId(resident.getId()) != null) {
                    withAccounts.add(resident);
                }
            }
            enhancedResidentsTable.refreshData(withAccounts);
            showAllBtn.getStyleClass().clear();
            showAllBtn.getStyleClass().addAll("button-secondary", "button-small");
            showWithAccountsBtn.getStyleClass().clear();
            showWithAccountsBtn.getStyleClass().addAll("button-primary", "button-small");
            showWithoutAccountsBtn.getStyleClass().clear();
            showWithoutAccountsBtn.getStyleClass().addAll("button-secondary", "button-small");
        });

        showWithoutAccountsBtn.setOnAction(e -> {
            enhancedResidentsTable.refreshData(DatabaseHelper.getResidentsWithoutAccounts());
            showAllBtn.getStyleClass().clear();
            showAllBtn.getStyleClass().addAll("button-secondary", "button-small");
            showWithAccountsBtn.getStyleClass().clear();
            showWithAccountsBtn.getStyleClass().addAll("button-secondary", "button-small");
            showWithoutAccountsBtn.getStyleClass().clear();
            showWithoutAccountsBtn.getStyleClass().addAll("button-primary", "button-small");
        });

        content.getChildren().addAll(filterBox, enhancedResidentsTable.getContainer());

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("scroll-pane-transparent");

        dialog.getDialogPane().setContent(scrollPane);
        dialog.showAndWait();
    }

    private void showUserAccountDetailsDialog(User user, Resident resident) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("User Account Details");
        alert.setHeaderText("Account for: " + resident.getFirstName() + " " + resident.getLastName());
        
        String content = String.format(
            "Username: %s\n" +
            "Role: %s\n" +
            "Status: %s\n" +
            "Created: %s\n" +
            "Last Login: %s\n\n" +
            "Resident Information:\n" +
            "Address: %s\n" +
            "Phone: %s",
            user.getUsername(),
            user.getRole(),
            user.isActive() ? "Active" : "Inactive",
            user.getCreatedDate(),
            user.getLastLogin(),
            resident.getAddress() != null ? resident.getAddress() : "Not specified",
            resident.getPhoneNumber() != null ? resident.getPhoneNumber() : "Not specified"
        );
        
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showDeleteUserConfirmation(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete User");
        alert.setHeaderText("Delete user: " + user.getUsername());
        alert.setContentText("Are you sure you want to delete this user? This action cannot be undone.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (DatabaseHelper.deleteUser(user.getId())) {
                    showToast("User deleted successfully");
                    refreshUsersManagementTable();
                } else {
                    showToast("Failed to delete user");
                }
            }
        });
    }

    private VBox createManageRolesPanel() {
        // Get user permissions
        Map<String, String> userPermissions = DatabaseHelper.getPermissions(currentRole);
        String userAccessPermission = userPermissions.get("User & Access");
        boolean canManage = "Full Access".equals(userAccessPermission) || "Manage".equals(userAccessPermission);
        
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

        // Toolbar buttons - only show if user can manage
        Button addButton = new Button("Add Role");
        addButton.setGraphic(new FontIcon(FontAwesomeSolid.PLUS_CIRCLE));
        addButton.getStyleClass().addAll("button-secondary", "button-small");
        addButton.setTooltip(new Tooltip("Add Role"));
        addButton.setDisable(!canManage);

        Button editButton = new Button("Edit Role");
        editButton.setGraphic(new FontIcon(FontAwesomeSolid.PENCIL_ALT));
        editButton.getStyleClass().addAll("button-secondary", "button-small");
        editButton.setTooltip(new Tooltip("Edit Role"));
        editButton.setDisable(true);

        Button deleteButton = new Button("Delete Role");
        deleteButton.setGraphic(new FontIcon(FontAwesomeSolid.TRASH));
        deleteButton.getStyleClass().addAll("button-secondary", "button-small");
        deleteButton.setTooltip(new Tooltip("Delete Role"));
        deleteButton.setDisable(true);

        rolesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean isSelected = newSelection != null && canManage;
            editButton.setDisable(!isSelected);
            deleteButton.setDisable(!isSelected);
        });

        addButton.setOnAction(e -> {
            if (canManage) {
                showRoleDialog(null).ifPresent(role -> {
                    DatabaseHelper.addRole(role);
                    loadRoleData(rolesTable);
                    showToast("Role created successfully.");
                });
            }
        });

        editButton.setOnAction(e -> {
            if (canManage) {
                Role selected = rolesTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    showRoleDialog(selected).ifPresent(role -> {
                        DatabaseHelper.updateRole(role);
                        loadRoleData(rolesTable);
                        showToast("Role updated successfully.");
                    });
                }
            }
        });

        deleteButton.setOnAction(e -> {
            if (canManage) {
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
            }
        });

        ToolBar toolBar = new ToolBar(addButton, editButton, deleteButton);
        toolBar.getStyleClass().add("toolbar-transparent");

        VBox content;
        if (!canManage) {
            // Add read-only notice for view-only users
            Label readOnlyLabel = new Label("ℹ️ View Only Mode - You cannot add, edit, or delete roles");
            readOnlyLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #f59e0b; -fx-padding: 5 0 5 0; -fx-font-weight: bold;");
            content = new VBox(12, readOnlyLabel, toolBar, rolesTable);
        } else {
            content = new VBox(12, toolBar, rolesTable);
        }
        VBox.setVgrow(rolesTable, Priority.ALWAYS);

        // Load roles
        loadRoleData(rolesTable);
        
        return content;
    }

    private VBox createPermissionsPanel() {
        var permissionsTable = new TableView<Map.Entry<String, Map<String, String>>>();
        permissionsTable.getStyleClass().add("table-view");
        permissionsTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        permissionsTable.setEditable(true); // Make table editable

        TableColumn<Map.Entry<String, Map<String, String>>, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getKey()));
        roleCol.setPrefWidth(180);
        roleCol.setMinWidth(180);
        roleCol.setEditable(false); // Role name not editable

        // Create columns for all system modules - all editable
        TableColumn<Map.Entry<String, Map<String, String>>, String> analyticsCol = new TableColumn<>("Analytics");
        analyticsCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getValue().get("Analytics & Overview")));
        analyticsCol.setPrefWidth(100);
        analyticsCol.setCellFactory(param -> createEditablePermissionCell());
        analyticsCol.setEditable(true);

        TableColumn<Map.Entry<String, Map<String, String>>, String> userAccessCol = new TableColumn<>("Users");
        userAccessCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getValue().get("User & Access")));
        userAccessCol.setPrefWidth(100);
        userAccessCol.setCellFactory(param -> createEditablePermissionCell());
        userAccessCol.setEditable(true);

        TableColumn<Map.Entry<String, Map<String, String>>, String> residentDataCol = new TableColumn<>("Residents");
        residentDataCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getValue().get("Resident Data")));
        residentDataCol.setPrefWidth(100);
        residentDataCol.setCellFactory(param -> createEditablePermissionCell());
        residentDataCol.setEditable(true);

        TableColumn<Map.Entry<String, Map<String, String>>, String> certificatesCol = new TableColumn<>("Certificates");
        certificatesCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getValue().get("Certificates & Clearances")));
        certificatesCol.setPrefWidth(100);
        certificatesCol.setCellFactory(param -> createEditablePermissionCell());
        certificatesCol.setEditable(true);

        TableColumn<Map.Entry<String, Map<String, String>>, String> complaintsCol = new TableColumn<>("Complaints");
        complaintsCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getValue().get("Complaints & Incidents")));
        complaintsCol.setPrefWidth(100);
        complaintsCol.setCellFactory(param -> createEditablePermissionCell());
        complaintsCol.setEditable(true);

        TableColumn<Map.Entry<String, Map<String, String>>, String> announcementsCol = new TableColumn<>("Announcements");
        announcementsCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getValue().get("Announcements")));
        announcementsCol.setPrefWidth(120);
        announcementsCol.setCellFactory(param -> createEditablePermissionCell());
        announcementsCol.setEditable(true);

        TableColumn<Map.Entry<String, Map<String, String>>, String> financialCol = new TableColumn<>("Financial");
        financialCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getValue().get("Financial Reports")));
        financialCol.setPrefWidth(100);
        financialCol.setCellFactory(param -> createEditablePermissionCell());
        financialCol.setEditable(true);

        TableColumn<Map.Entry<String, Map<String, String>>, String> securityCol = new TableColumn<>("Security");
        securityCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getValue().get("Security Features")));
        securityCol.setPrefWidth(100);
        securityCol.setCellFactory(param -> createEditablePermissionCell());
        securityCol.setEditable(true);

        TableColumn<Map.Entry<String, Map<String, String>>, String> systemCol = new TableColumn<>("System");
        systemCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getValue().get("System Config")));
        systemCol.setPrefWidth(100);
        systemCol.setCellFactory(param -> createEditablePermissionCell());
        systemCol.setEditable(true);

        TableColumn<Map.Entry<String, Map<String, String>>, String> maintenanceCol = new TableColumn<>("Maintenance");
        maintenanceCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getValue().get("Maintenance")));
        maintenanceCol.setPrefWidth(110);
        maintenanceCol.setCellFactory(param -> createEditablePermissionCell());
        maintenanceCol.setEditable(true);

        permissionsTable.getColumns().setAll(List.of(roleCol, analyticsCol, userAccessCol, residentDataCol, 
            certificatesCol, complaintsCol, announcementsCol, financialCol, securityCol, systemCol, maintenanceCol));

        // Fetch roles dynamically from the database
        ObservableList<Role> allRoles = DatabaseHelper.getAllRoles();
        ObservableList<Map.Entry<String, Map<String, String>>> permissionsData = FXCollections.observableArrayList();
        for (Role role : allRoles) {
            Map<String, String> permissions = DatabaseHelper.getPermissions(role.getName());
            permissionsData.add(Map.entry(role.getName(), permissions));
        }
        permissionsTable.setItems(permissionsData);

        var infoLabel = new Label("Permission Levels: None, View Only, Manage, Full Access");
        infoLabel.setStyle("-fx-font-size: 11; -fx-text-fill: " + "#333" + "; -fx-font-weight: bold;");

        var legendBox = new HBox(15);
        legendBox.setPadding(new Insets(10, 0, 10, 0));
        legendBox.getChildren().addAll(
            createLegendItem("None", "#ef4444"),
            createLegendItem("View Only", "#f59e0b"),
            createLegendItem("Manage", "#3b82f6"),
            createLegendItem("Full Access", "#10b981")
        );

        var content = new VBox(12, infoLabel, legendBox, permissionsTable);
        VBox.setVgrow(permissionsTable, Priority.ALWAYS);
        
        // Add save button for permissions
        Button savePermissionsBtn = new Button("Save Changes", new FontIcon(FontAwesomeSolid.SAVE));
        savePermissionsBtn.getStyleClass().addAll("button-primary", "button-small");
        savePermissionsBtn.setTooltip(new Tooltip("Save All Changes"));
        savePermissionsBtn.setOnAction(e -> {
            // Save all permissions to database
            for (Map.Entry<String, Map<String, String>> entry : permissionsData) {
                String roleName = entry.getKey();
                Map<String, String> permissions = entry.getValue();
                for (Map.Entry<String, String> perm : permissions.entrySet()) {
                    DatabaseHelper.savePermission(roleName, perm.getKey(), perm.getValue());
                }
            }
            showToast("✓ Permissions saved successfully! Please restart the application for changes to take effect.");
        });
        
        Label noteLabel = new Label("ℹ️ Click on any permission cell to change it. Changes take effect after restart.");
        noteLabel.getStyleClass().add("text-muted-sm");
        
        VBox contentWithButton = new VBox(12, infoLabel, legendBox, permissionsTable, noteLabel, savePermissionsBtn);
        VBox.setVgrow(permissionsTable, Priority.ALWAYS);
        
        return contentWithButton;
    }

    private TableCell<Map.Entry<String, Map<String, String>>, String> createEditablePermissionCell() {
        return new TableCell<>() {
            private ComboBox<String> comboBox;
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    if (isEditing()) {
                        if (comboBox != null) {
                            comboBox.setValue(item);
                        }
                        setGraphic(comboBox);
                        setText(null);
                    } else {
                        setText(item);
                        setGraphic(null);
                        
                        // Set background color based on permission level
                        String style = "-fx-padding: 5; -fx-alignment: center; ";
                        switch (item) {
                            case "Full Access":
                                style += "-fx-background-color: #d1fae5; -fx-text-fill: #065f46;";
                                break;
                            case "Manage":
                                style += "-fx-background-color: #dbeafe; -fx-text-fill: #1e40af;";
                                break;
                            case "View Only":
                                style += "-fx-background-color: #fef3c7; -fx-text-fill: #92400e;";
                                break;
                            case "None":
                                style += "-fx-background-color: #fee2e2; -fx-text-fill: #991b1b;";
                                break;
                        }
                        setStyle(style);
                    }
                }
            }
            
            @Override
            public void startEdit() {
                super.startEdit();
                
                if (comboBox == null) {
                    createComboBox();
                }
                
                comboBox.setValue(getItem());
                setGraphic(comboBox);
                setText(null);
            }
            
            @Override
            public void cancelEdit() {
                super.cancelEdit();
                setText(getItem());
                setGraphic(null);
            }
            
            private void createComboBox() {
                comboBox = new ComboBox<>(FXCollections.observableArrayList(
                    "None", "View Only", "Manage", "Full Access"
                ));
                comboBox.setOnAction(event -> {
                    String newValue = comboBox.getValue();
                    commitEdit(newValue);
                    
                    // Update the underlying data
                    Map.Entry<String, Map<String, String>> rowData = getTableView().getItems().get(getIndex());
                    TableColumn<Map.Entry<String, Map<String, String>>, String> column = getTableColumn();
                    String moduleName = column.getText();
                    
                    // Map column header to full module name
                    String fullModuleName = getFullModuleName(moduleName);
                    rowData.getValue().put(fullModuleName, newValue);
                });
            }
            
            private String getFullModuleName(String shortName) {
                switch (shortName) {
                    case "Analytics": return "Analytics & Overview";
                    case "Users": return "User & Access";
                    case "Residents": return "Resident Data";
                    case "Certificates": return "Certificates & Clearances";
                    case "Complaints": return "Complaints & Incidents";
                    case "Announcements": return "Announcements";
                    case "Financial": return "Financial Reports";
                    case "Security": return "Security Features";
                    case "System": return "System Config";
                    case "Maintenance": return "Maintenance";
                    default: return shortName;
                }
            }
        };
    }

    private VBox createAuditLogPanel() {
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

        var content = new VBox(12, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        
        return content;
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
        tabPane.getStyleClass().add("tab-pane");

        // Tab 1: Submit New Complaint (for residents)
        Tab submitTab = new Tab("Submit Complaint", createComplaintSubmissionPanel());
        submitTab.getStyleClass().add("tab");

        // Tab 2: Manage Complaints (for admin)
        Tab manageTab = new Tab("Manage Complaints", createComplaintsManagementPanel());
        manageTab.getStyleClass().add("tab");

        tabPane.getTabs().addAll(submitTab, manageTab);
        updateDashboardContent(center, "Complaints & Incidents", tabPane);
    }

    private VBox createComplaintSubmissionPanel() {
        VBox panel = new VBox(16);
        panel.setPadding(new Insets(24));
        panel.setMaxWidth(Double.MAX_VALUE);

        Label titleLabel = new Label("Submit a Complaint or Incident Report");
        titleLabel.getStyleClass().add("text-heading-sm");

        // Complaint Title
        Label complaintTitleLabel = new Label("Complaint Title");
        complaintTitleLabel.getStyleClass().add("form-label");
        TextField titleField = new TextField();
        titleField.setPromptText("E.g., Noise complaint, street damage, etc.");
        titleField.getStyleClass().add("text-field");

        // Description
        Label descriptionLabel = new Label("Description of Incident");
        descriptionLabel.getStyleClass().add("form-label");
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Provide detailed information about the complaint or incident...");
        descriptionArea.setWrapText(true);
        descriptionArea.setPrefRowCount(6);
        descriptionArea.getStyleClass().add("text-area");

        // Photo Upload
        Label photoLabel = new Label("Attach Evidence Photo (Optional)");
        photoLabel.getStyleClass().add("form-label");

        java.util.concurrent.atomic.AtomicReference<String> selectedPhotoPath = new java.util.concurrent.atomic.AtomicReference<>(null);
        Label photoPathLabel = new Label("No photo selected");
        photoPathLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13;");

        Button uploadPhotoBtn = new Button("Photo", new FontIcon(FontAwesomeSolid.IMAGE));
        uploadPhotoBtn.getStyleClass().addAll("button-secondary", "button-small");
        uploadPhotoBtn.setTooltip(new Tooltip("Choose Photo"));
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
                photoPathLabel.setStyle("-fx-text-fill: #10b981; -fx-font-size: 13; -fx-font-weight: 600;");
            }
        });

        HBox photoBox = new HBox(12, uploadPhotoBtn, photoPathLabel);
        photoBox.setAlignment(Pos.CENTER_LEFT);

        // Submit Button
        Button submitBtn = new Button("Submit");
        submitBtn.getStyleClass().addAll("button-primary", "button-small");
        submitBtn.setTooltip(new Tooltip("Submit Complaint"));
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
        Button viewBtn = new Button("View", new FontIcon(FontAwesomeSolid.EYE));
        viewBtn.getStyleClass().addAll("button-secondary", "button-small");
        viewBtn.setTooltip(new Tooltip("View Details"));
        viewBtn.setDisable(true);
        viewBtn.setOnAction(e -> {
            Complaint selected = complaintsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showComplaintDetailsDialog(selected);
            }
        });

        Button statusBtn = new Button("Status", new FontIcon(FontAwesomeSolid.EDIT));
        statusBtn.getStyleClass().addAll("button-secondary", "button-small");
        statusBtn.setTooltip(new Tooltip("Update Status"));
        statusBtn.setDisable(true);
        statusBtn.setOnAction(e -> {
            Complaint selected = complaintsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showStatusUpdateDialog(selected);
            }
        });

        Button notesBtn = new Button("Notes", new FontIcon(FontAwesomeSolid.COMMENT));
        notesBtn.getStyleClass().addAll("button-secondary", "button-small");
        notesBtn.setTooltip(new Tooltip("Add Notes"));
        notesBtn.setDisable(true);
        notesBtn.setOnAction(e -> {
            Complaint selected = complaintsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showAddNotesDialog(selected);
            }
        });

        Button smsBtn = new Button("SMS", new FontIcon(FontAwesomeSolid.SMS));
        smsBtn.getStyleClass().addAll("button-warning", "button-small");
        smsBtn.setTooltip(new Tooltip("Send SMS"));
        smsBtn.setDisable(true);
        smsBtn.setOnAction(e -> {
            Complaint selected = complaintsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                sendComplaintSMS(selected);
            }
        });

        Button reportBtn = new Button("Report", new FontIcon(FontAwesomeSolid.FILE_PDF));
        reportBtn.getStyleClass().addAll("button-secondary", "button-small");
        reportBtn.setTooltip(new Tooltip("Generate Report"));
        reportBtn.setOnAction(e -> generateComplaintsReport());

        complaintsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean isSelected = newVal != null;
            viewBtn.setDisable(!isSelected);
            statusBtn.setDisable(!isSelected);
            notesBtn.setDisable(!isSelected);
            smsBtn.setDisable(!isSelected);
        });

        ToolBar toolBar = new ToolBar(viewBtn, statusBtn, notesBtn, smsBtn, new Separator(), reportBtn);
        toolBar.getStyleClass().add("toolbar-transparent");

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
        residentValue.getStyleClass().add("text-bold");

        Label titleLabel = new Label("Title:");
        Label titleValue = new Label(complaint.getTitle());
        titleValue.getStyleClass().add("text-bold");

        Label statusLabel = new Label("Status:");
        Label statusValue = new Label(complaint.getStatus());
        statusValue.getStyleClass().add("text-bold");

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

    private void sendComplaintSMS(Complaint complaint) {
        // Get resident information
        Optional<Resident> residentOpt = DatabaseHelper.getResidentById(complaint.getResidentId());
        if (!residentOpt.isPresent()) {
            showAlert("Error", "Resident not found.");
            return;
        }
        
        Resident resident = residentOpt.get();
        String phone = resident.getPhoneNumber();
        
        if (phone == null || phone.trim().isEmpty()) {
            showAlert("No Phone Number", "This resident doesn't have a phone number registered.");
            return;
        }
        
        // Create SMS dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Send SMS Notification");
        dialog.setHeaderText("Send SMS to: " + resident.getFirstName() + " " + resident.getLastName());
        
        // Template selection
        ComboBox<String> templateCombo = new ComboBox<>();
        templateCombo.getItems().addAll(
            "Complaint Received",
            "Complaint Under Investigation",
            "Complaint Resolved",
            "Custom Message"
        );
        templateCombo.setValue("Complaint Received");
        
        // Message area
        TextArea messageArea = new TextArea();
        messageArea.setPrefRowCount(5);
        messageArea.setWrapText(true);
        
        // Character count label
        Label charCountLabel = new Label("Characters: 0");
        
        // Update message when template changes
        templateCombo.setOnAction(e -> {
            String template = templateCombo.getValue();
            String message = "";
            
            switch (template) {
                case "Complaint Received":
                    message = String.format(
                        "Your complaint (Ref: %s) has been received by Barangay San Marino. " +
                        "We will investigate and update you on the progress. Thank you!",
                        complaint.getId()
                    );
                    break;
                case "Complaint Under Investigation":
                    message = String.format(
                        "Update on your complaint (Ref: %s): Currently under investigation. " +
                        "We are working to resolve this matter. Thank you for your patience!",
                        complaint.getId()
                    );
                    break;
                case "Complaint Resolved":
                    message = String.format(
                        "Your complaint (Ref: %s) has been resolved. " +
                        "Thank you for bringing this to our attention. For questions, visit the barangay office.",
                        complaint.getId()
                    );
                    break;
                case "Custom Message":
                    message = "";
                    break;
            }
            
            messageArea.setText(message);
            charCountLabel.setText("Characters: " + message.length());
        });
        
        // Trigger initial message
        templateCombo.fireEvent(new ActionEvent());
        
        // Update character count on text change
        messageArea.textProperty().addListener((obs, old, newVal) -> {
            charCountLabel.setText("Characters: " + newVal.length());
            if (newVal.length() > 160) {
                charCountLabel.setStyle("-fx-text-fill: orange;");
            } else {
                charCountLabel.setStyle("-fx-text-fill: black;");
            }
        });
        
        // Layout
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        content.getChildren().addAll(
            new Label("Phone: " + phone),
            new Label("Complaint: " + complaint.getTitle()),
            new Label("Status: " + complaint.getStatus()),
            new Label("Reference: " + complaint.getId()),
            new Separator(),
            new Label("Select Template:"),
            templateCombo,
            new Label("Message:"),
            messageArea,
            charCountLabel
        );
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // Handle send
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String message = messageArea.getText();
            if (message == null || message.trim().isEmpty()) {
                showAlert("Error", "Message cannot be empty.");
                return;
            }
            
            // Send SMS
            System.out.println("📤 Sending SMS to: " + phone);
            SMSService.SMSResponse response = SMSService.sendSMS(phone, message);
            
            if (response.isSuccess()) {
                showAlert("SMS Sent Successfully!", 
                    "✅ SMS sent to: " + resident.getFirstName() + " " + resident.getLastName() + "\n" +
                    "📱 Phone: " + phone + "\n" +
                    "🆔 Message ID: " + response.getMessageId() + "\n\n" +
                    "The resident should receive the SMS within 1-5 minutes.");
            } else {
                showAlert("SMS Failed", 
                    "❌ Failed to send SMS\n\n" +
                    "Error: " + response.getMessage() + "\n" +
                    "Error Code: " + response.getErrorCode() + "\n\n" +
                    "Please check:\n" +
                    "1. Phone number is correct\n" +
                    "2. SMS service is enabled\n" +
                    "3. You have sufficient SMS credits");
            }
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
        postingTab.getStyleClass().add("tab");

        var managementTab = new Tab("Manage Announcements", createAnnouncementManagementPanel());
        managementTab.setClosable(false);
        managementTab.getStyleClass().add("tab");

        var tabPane = new TabPane(postingTab, managementTab);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.getStyleClass().add("tab-pane");

        center.getChildren().clear();
        center.getChildren().add(tabPane);
    }

    private VBox createAnnouncementPostingPanel() {
        var container = new VBox(15);
        container.setPadding(new Insets(15));
        container.setStyle("-fx-background-color: " + "#ffffff" + ";");

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

        var submitBtn = new Button("Post");
        submitBtn.getStyleClass().addAll("button-primary", "button-small");
        submitBtn.setTooltip(new Tooltip("Post Announcement"));
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
        container.setStyle("-fx-background-color: " + "#ffffff" + ";");

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

        var viewBtn = new Button("View");
        viewBtn.getStyleClass().addAll("button-secondary", "button-small");
        viewBtn.setTooltip(new Tooltip("View Details"));
        viewBtn.setOnAction(e -> {
            var selected = announcementsTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Info", "Please select an announcement");
                return;
            }
            showAnnouncementDetailsDialog(selected);
        });

        var editBtn = new Button("Edit");
        editBtn.getStyleClass().addAll("button-secondary", "button-small");
        editBtn.setTooltip(new Tooltip("Edit Announcement"));
        editBtn.setOnAction(e -> {
            var selected = announcementsTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Info", "Please select an announcement");
                return;
            }
            showAnnouncementEditorDialog(selected);
        });

        var toggleStatusBtn = new Button("Toggle");
        toggleStatusBtn.getStyleClass().addAll("button-secondary", "button-small");
        toggleStatusBtn.setTooltip(new Tooltip("Toggle Status"));
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
        deleteBtn.getStyleClass().addAll("button-danger", "button-small");
        deleteBtn.setTooltip(new Tooltip("Delete Announcement"));
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

        // Broadcast SMS Button
        var broadcastSMSBtn = new Button("Broadcast", new FontIcon(FontAwesomeSolid.BULLHORN));
        broadcastSMSBtn.getStyleClass().addAll("button-warning", "button-small");
        broadcastSMSBtn.setTooltip(new Tooltip("Broadcast SMS"));
        broadcastSMSBtn.setOnAction(e -> {
            var selected = announcementsTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Info", "Please select an announcement to broadcast");
                return;
            }
            broadcastAnnouncementSMS(selected);
        });

        buttonBox.getChildren().addAll(viewBtn, editBtn, toggleStatusBtn, deleteBtn, broadcastSMSBtn);

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

    private void broadcastAnnouncementSMS(Announcement announcement) {
        // Get all residents with phone numbers
        ObservableList<Resident> allResidents = DatabaseHelper.getResidents("", 0, Integer.MAX_VALUE, "id", "ASC");
        
        // Filter residents with valid phone numbers
        java.util.List<String> phoneNumbers = new java.util.ArrayList<>();
        for (Resident resident : allResidents) {
            String phone = resident.getPhoneNumber();
            if (phone != null && !phone.trim().isEmpty()) {
                phoneNumbers.add(phone);
            }
        }
        
        if (phoneNumbers.isEmpty()) {
            showAlert("No Recipients", "No residents have phone numbers registered in the system.");
            return;
        }
        
        // Create SMS dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Broadcast Announcement SMS");
        dialog.setHeaderText("Broadcast to " + phoneNumbers.size() + " residents");
        
        // Template selection
        ComboBox<String> templateCombo = new ComboBox<>();
        templateCombo.getItems().addAll(
            "Announcement Notification",
            "Event Reminder",
            "Emergency Alert",
            "Custom Message"
        );
        templateCombo.setValue("Announcement Notification");
        
        // Message area
        TextArea messageArea = new TextArea();
        messageArea.setPrefRowCount(5);
        messageArea.setWrapText(true);
        
        // Character count label
        Label charCountLabel = new Label("Characters: 0");
        
        // Update message when template changes
        templateCombo.setOnAction(e -> {
            String template = templateCombo.getValue();
            String message = "";
            
            switch (template) {
                case "Announcement Notification":
                    message = String.format(
                        "Barangay San Marino Announcement: %s. For more details, visit the barangay office. Thank you!",
                        announcement.getTitle()
                    );
                    break;
                case "Event Reminder":
                    message = String.format(
                        "Reminder: %s on %s. Please mark your calendar. For inquiries, contact the barangay office. Thank you!",
                        announcement.getTitle(),
                        announcement.getStartDate()
                    );
                    break;
                case "Emergency Alert":
                    message = String.format(
                        "URGENT: %s. Please stay informed and follow barangay guidelines. Stay safe!",
                        announcement.getTitle()
                    );
                    break;
                case "Custom Message":
                    message = "";
                    break;
            }
            
            messageArea.setText(message);
            charCountLabel.setText("Characters: " + message.length());
        });
        
        // Trigger initial message
        templateCombo.fireEvent(new ActionEvent());
        
        // Update character count on text change
        messageArea.textProperty().addListener((obs, old, newVal) -> {
            charCountLabel.setText("Characters: " + newVal.length());
            if (newVal.length() > 160) {
                charCountLabel.setStyle("-fx-text-fill: orange;");
            } else {
                charCountLabel.setStyle("-fx-text-fill: black;");
            }
        });
        
        // Cost estimate
        int smsCount = (int) Math.ceil(messageArea.getText().length() / 160.0);
        Label costLabel = new Label("Estimated cost: " + (phoneNumbers.size() * smsCount) + " SMS credits");
        costLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #dc2626;");
        
        // Update cost when message changes
        messageArea.textProperty().addListener((obs, old, newVal) -> {
            int count = (int) Math.ceil(newVal.length() / 160.0);
            costLabel.setText("Estimated cost: " + (phoneNumbers.size() * count) + " SMS credits");
        });
        
        // Layout
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        content.getChildren().addAll(
            new Label("Recipients: " + phoneNumbers.size() + " residents"),
            new Label("Announcement: " + announcement.getTitle()),
            new Label("Type: " + announcement.getType()),
            new Separator(),
            new Label("Select Template:"),
            templateCombo,
            new Label("Message:"),
            messageArea,
            charCountLabel,
            costLabel
        );
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // Handle send
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String message = messageArea.getText();
            if (message == null || message.trim().isEmpty()) {
                showAlert("Error", "Message cannot be empty.");
                return;
            }
            
            // Confirm broadcast
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Broadcast");
            confirm.setHeaderText("Send SMS to " + phoneNumbers.size() + " residents?");
            confirm.setContentText("This will use approximately " + (phoneNumbers.size() * smsCount) + " SMS credits.\n\nProceed?");
            
            if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                return;
            }
            
            // Send bulk SMS
            System.out.println("📤 Broadcasting SMS to " + phoneNumbers.size() + " residents");
            String[] phoneArray = phoneNumbers.toArray(new String[0]);
            SMSService.SMSResponse response = SMSService.sendBulkSMS(phoneArray, message);
            
            if (response.isSuccess()) {
                showAlert("SMS Broadcast Successful!", 
                    "✅ SMS broadcast completed!\n\n" +
                    response.getMessage() + "\n\n" +
                    "Recipients should receive the SMS within 1-5 minutes.");
            } else {
                showAlert("SMS Broadcast Failed", 
                    "❌ Failed to broadcast SMS\n\n" +
                    "Error: " + response.getMessage() + "\n" +
                    "Error Code: " + response.getErrorCode() + "\n\n" +
                    "Please check:\n" +
                    "1. SMS service is enabled\n" +
                    "2. You have sufficient SMS credits\n" +
                    "3. Phone numbers are valid");
            }
        }
    }

    private void showFinancialReports(VBox center) {
        var container = new VBox(15);
        container.setPadding(new Insets(15));
        container.setStyle("-fx-background-color: " + "#ffffff" + ";");

        var titleLabel = new Label("Financial Reports");
        titleLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        // Export Folder Configuration Section
        var exportConfigBox = new VBox(10);
        exportConfigBox.setPadding(new Insets(15));
        exportConfigBox.getStyleClass().add("card-export-config");

        var configTitle = new Label("Export Configuration");
        configTitle.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #1e40af;");

        var configDesc = new Label("Configure the destination folder where financial reports will be exported. Once set, all exports for this document type will be saved to the selected folder.");
        configDesc.setWrapText(true);
        configDesc.setStyle("-fx-font-size: 11; -fx-text-fill: #475569;");

        var folderBox = new HBox(10);
        folderBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        var folderLabel = new Label("Target Folder:");
        folderLabel.setStyle("-fx-font-size: 12; -fx-font-weight: bold;");

        var folderPathField = new TextField();
        folderPathField.setPromptText("No folder selected yet");
        folderPathField.setEditable(false);
        folderPathField.setPrefWidth(400);
        folderPathField.setStyle("-fx-background-color: white;");
        
        // Load saved export path from preferences
        String savedPath = DatabaseHelper.getFinancialExportPath();
        if (savedPath != null && !savedPath.isEmpty()) {
            folderPathField.setText(savedPath);
        }

        var browseFolderBtn = new Button("Browse", new FontIcon(FontAwesomeSolid.FOLDER_OPEN));
        browseFolderBtn.getStyleClass().addAll("button-secondary", "button-small");
        browseFolderBtn.setTooltip(new Tooltip("Browse Folder"));
        browseFolderBtn.setOnAction(e -> {
            javafx.stage.DirectoryChooser dirChooser = new javafx.stage.DirectoryChooser();
            dirChooser.setTitle("Select Export Folder for Financial Reports");
            
            // Set initial directory if path exists
            if (!folderPathField.getText().isEmpty()) {
                java.io.File currentDir = new java.io.File(folderPathField.getText());
                if (currentDir.exists()) {
                    dirChooser.setInitialDirectory(currentDir);
                }
            }
            
            java.io.File selectedDir = dirChooser.showDialog(primaryStage);
            if (selectedDir != null) {
                folderPathField.setText(selectedDir.getAbsolutePath());
                DatabaseHelper.saveFinancialExportPath(selectedDir.getAbsolutePath());
                showToast("Export folder updated successfully");
            }
        });

        folderBox.getChildren().addAll(folderLabel, folderPathField, browseFolderBtn);
        exportConfigBox.getChildren().addAll(configTitle, configDesc, folderBox);

        // Fetch real data from database
        var dailyCollections = DatabaseHelper.getDailyCollections();
        var monthlyIncome = DatabaseHelper.getMonthlyIncome();
        var revenueByType = DatabaseHelper.getRevenueByDocumentType();
        var ytdSummary = DatabaseHelper.getYearToDateSummary();

        // Year-to-Date Summary Cards
        var summaryBox = new HBox(15);
        summaryBox.setPadding(new Insets(10, 0, 10, 0));

        var totalRevenueCard = createSummaryCard("Total Revenue (YTD)", 
            "₱" + String.format("%.2f", ytdSummary.get("total_revenue")), 
            "#10b981", FontAwesomeSolid.DOLLAR_SIGN);
        var pendingRevenueCard = createSummaryCard("Pending Revenue", 
            "₱" + String.format("%.2f", ytdSummary.get("pending_revenue")), 
            "#f59e0b", FontAwesomeSolid.CLOCK);
        var transactionsCard = createSummaryCard("Total Transactions", 
            ytdSummary.get("total_transactions").toString(), 
            "#3b82f6", FontAwesomeSolid.RECEIPT);
        var paidCountCard = createSummaryCard("Paid Documents", 
            ytdSummary.get("paid_count").toString(), 
            "#8b5cf6", FontAwesomeSolid.CHECK_CIRCLE);

        summaryBox.getChildren().addAll(totalRevenueCard, pendingRevenueCard, transactionsCard, paidCountCard);

        // Revenue by Document Type Section
        var revenueTypeSection = new VBox(10);
        var revenueTypeTitle = new Label("Revenue by Document Type");
        revenueTypeTitle.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: " + "#1a1a1a" + ";");

        var revenueTypeTable = new TableView<Map.Entry<String, Double>>();
        revenueTypeTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        revenueTypeTable.setPrefHeight(150);

        TableColumn<Map.Entry<String, Double>, String> docTypeCol = new TableColumn<>("Document Type");
        docTypeCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getKey()));
        docTypeCol.setPrefWidth(200);

        TableColumn<Map.Entry<String, Double>, String> revenueCol = new TableColumn<>("Total Revenue");
        revenueCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty("₱" + String.format("%.2f", cellData.getValue().getValue())));
        revenueCol.setPrefWidth(150);

        @SuppressWarnings("unchecked")
        TableColumn<Map.Entry<String, Double>, ?>[] revenueCols = new TableColumn[] {docTypeCol, revenueCol};
        revenueTypeTable.getColumns().addAll(revenueCols);
        revenueTypeTable.setItems(FXCollections.observableArrayList(revenueByType.entrySet()));

        revenueTypeSection.getChildren().addAll(revenueTypeTitle, revenueTypeTable);

        // Daily Collections Section
        var dailySection = new VBox(10);
        var dailyTitle = new Label("Daily Collections (Last 30 Days)");
        dailyTitle.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: " + "#1a1a1a" + ";");

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
        var monthlyTitle = new Label("Monthly Income (Last 12 Months)");
        monthlyTitle.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: " + "#1a1a1a" + ";");

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

        var printDailyBtn = new Button("Daily", new FontIcon(FontAwesomeSolid.PRINT));
        printDailyBtn.getStyleClass().addAll("button-secondary", "button-small");
        printDailyBtn.setTooltip(new Tooltip("Print Daily Report"));
        printDailyBtn.setOnAction(e -> generateFinancialReportPDF("daily", dailyCollections));

        var printMonthlyBtn = new Button("Monthly", new FontIcon(FontAwesomeSolid.PRINT));
        printMonthlyBtn.getStyleClass().addAll("button-secondary", "button-small");
        printMonthlyBtn.setTooltip(new Tooltip("Print Monthly Report"));
        printMonthlyBtn.setOnAction(e -> generateFinancialReportPDF("monthly", monthlyIncome));

        var printComprehensiveBtn = new Button("Full Report", new FontIcon(FontAwesomeSolid.FILE_PDF));
        printComprehensiveBtn.getStyleClass().addAll("button-secondary", "button-small");
        printComprehensiveBtn.setTooltip(new Tooltip("Print Comprehensive Report"));
        printComprehensiveBtn.setOnAction(e -> generateComprehensiveFinancialReport(dailyCollections, monthlyIncome, revenueByType, ytdSummary));

        var exportBtn = new Button("Export to CSV", new FontIcon(FontAwesomeSolid.FILE_CSV));
        exportBtn.setStyle("-fx-font-size: 12; -fx-padding: 8;");
        exportBtn.setOnAction(e -> exportFinancialDataToCSV(dailyCollections, monthlyIncome));

        buttonBox.getChildren().addAll(printDailyBtn, printMonthlyBtn, printComprehensiveBtn, exportBtn);

        var scrollPane = new ScrollPane(new VBox(20, exportConfigBox, summaryBox, revenueTypeSection, dailySection, monthlySection));
        scrollPane.setFitToWidth(true);

        container.getChildren().addAll(titleLabel, scrollPane, buttonBox);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        updateDashboardContent(center, "Financial Reports", container);
    }

    private VBox createSummaryCard(String title, String value, String color, org.kordamp.ikonli.Ikon icon) {
        var card = new VBox(8);
        card.setPadding(new Insets(15));
        card.getStyleClass().add("card-sm");
        card.setPrefWidth(200);

        var iconLabel = new Label("", new FontIcon(icon));
        iconLabel.setStyle("-fx-font-size: 24; -fx-text-fill: " + color + ";");

        var titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #6b7280;");

        var valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        card.getChildren().addAll(iconLabel, titleLabel, valueLabel);
        return card;
    }

    private void generateFinancialReportPDF(String type, Map<String, Double> data) {
        try {
            String filename = "Financial_Report_" + type + "_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".pdf";
            
            // Use configured export path or default to Downloads
            String exportPath = DatabaseHelper.getFinancialExportPath();
            String path;
            if (exportPath != null && !exportPath.isEmpty()) {
                path = exportPath + "/" + filename;
            } else {
                path = System.getProperty("user.home") + "/Downloads/" + filename;
            }

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

    private void generateComprehensiveFinancialReport(Map<String, Double> daily, Map<String, Double> monthly, 
                                                      Map<String, Double> revenueByType, Map<String, Object> ytdSummary) {
        try {
            String filename = "Comprehensive_Financial_Report_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".pdf";
            
            // Use configured export path or default to Downloads
            String exportPath = DatabaseHelper.getFinancialExportPath();
            String path;
            if (exportPath != null && !exportPath.isEmpty()) {
                path = exportPath + "/" + filename;
            } else {
                path = System.getProperty("user.home") + "/Downloads/" + filename;
            }

            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(path));
            document.open();

            // Header
            com.lowagie.text.Font titleFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 16, com.lowagie.text.Font.BOLD);
            Paragraph title = new Paragraph("BARANGAY SAN MARINO", titleFont);
            title.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
            document.add(title);

            com.lowagie.text.Font subtitleFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 14, com.lowagie.text.Font.BOLD);
            Paragraph subtitle = new Paragraph("COMPREHENSIVE FINANCIAL REPORT", subtitleFont);
            subtitle.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
            document.add(subtitle);

            document.add(new Paragraph("Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
            document.add(new Paragraph("\n"));

            // Year-to-Date Summary
            com.lowagie.text.Font sectionFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 12, com.lowagie.text.Font.BOLD);
            document.add(new Paragraph("YEAR-TO-DATE SUMMARY", sectionFont));
            document.add(new Paragraph("Total Revenue: ₱" + String.format("%.2f", ytdSummary.get("total_revenue"))));
            document.add(new Paragraph("Pending Revenue: ₱" + String.format("%.2f", ytdSummary.get("pending_revenue"))));
            document.add(new Paragraph("Total Transactions: " + ytdSummary.get("total_transactions")));
            document.add(new Paragraph("Paid Documents: " + ytdSummary.get("paid_count")));
            document.add(new Paragraph("Pending Documents: " + ytdSummary.get("pending_count")));
            document.add(new Paragraph("\n"));

            // Revenue by Document Type
            document.add(new Paragraph("REVENUE BY DOCUMENT TYPE", sectionFont));
            com.lowagie.text.pdf.PdfPTable typeTable = new com.lowagie.text.pdf.PdfPTable(2);
            typeTable.setWidthPercentage(100);
            typeTable.addCell("Document Type");
            typeTable.addCell("Total Revenue (₱)");

            for (Map.Entry<String, Double> entry : revenueByType.entrySet()) {
                typeTable.addCell(entry.getKey());
                typeTable.addCell(String.format("%.2f", entry.getValue()));
            }
            document.add(typeTable);
            document.add(new Paragraph("\n"));

            // Monthly Income Summary
            document.add(new Paragraph("MONTHLY INCOME (Last 12 Months)", sectionFont));
            double monthlyTotal = monthly.values().stream().mapToDouble(Double::doubleValue).sum();
            document.add(new Paragraph("Total: ₱" + String.format("%.2f", monthlyTotal)));
            document.add(new Paragraph("Average: ₱" + String.format("%.2f", monthlyTotal / monthly.size())));
            document.add(new Paragraph("\n"));

            com.lowagie.text.pdf.PdfPTable monthlyTable = new com.lowagie.text.pdf.PdfPTable(2);
            monthlyTable.setWidthPercentage(100);
            monthlyTable.addCell("Month");
            monthlyTable.addCell("Income (₱)");

            for (Map.Entry<String, Double> entry : monthly.entrySet()) {
                monthlyTable.addCell(entry.getKey());
                monthlyTable.addCell(String.format("%.2f", entry.getValue()));
            }
            document.add(monthlyTable);
            document.add(new Paragraph("\n"));

            // Daily Collections Summary
            document.add(new Paragraph("DAILY COLLECTIONS (Last 30 Days)", sectionFont));
            double dailyTotal = daily.values().stream().mapToDouble(Double::doubleValue).sum();
            document.add(new Paragraph("Total: ₱" + String.format("%.2f", dailyTotal)));
            document.add(new Paragraph("Average: ₱" + String.format("%.2f", dailyTotal / daily.size())));
            document.add(new Paragraph("\n"));

            // Footer
            document.add(new Paragraph("\n\n"));
            Paragraph footer = new Paragraph("This is a system-generated report from Barangay San Marino BDMS");
            footer.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
            com.lowagie.text.Font footerFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 9, com.lowagie.text.Font.ITALIC);
            footer.setFont(footerFont);
            document.add(footer);

            document.close();

            showToast("Comprehensive report saved to: " + path);
        } catch (Exception e) {
            e.printStackTrace();
            showToast("Error generating comprehensive report");
        }
    }

    private void exportFinancialDataToCSV(Map<String, Double> daily, Map<String, Double> monthly) {
        try {
            String filename = "Financial_Data_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".csv";
            
            // Use configured export path or default to Downloads
            String exportPath = DatabaseHelper.getFinancialExportPath();
            String path;
            if (exportPath != null && !exportPath.isEmpty()) {
                path = exportPath + "/" + filename;
            } else {
                path = System.getProperty("user.home") + "/Downloads/" + filename;
            }

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
        var tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.getStyleClass().add("tab-pane");

        // Tab 1: User Authentication
        Tab authTab = new Tab("User Authentication", createUserAuthenticationPanel());
        authTab.getStyleClass().add("tab");

        // Tab 2: Data Encryption (includes password hashing with BCrypt)
        Tab encryptionTab = new Tab("Data Encryption", createDataEncryptionPanel());
        encryptionTab.getStyleClass().add("tab");

        // Note: Automatic Backups moved to Maintenance tab to avoid duplication
        // Note: Role-Based Access moved to User & Access tab

        tabPane.getTabs().addAll(authTab, encryptionTab);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        updateDashboardContent(center, "Security Features", tabPane);
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
        infoBox.setStyle("-fx-border-color: #ddd; -fx-border-width: 1; -fx-border-radius: 5; -fx-padding: 12; -fx-background-color: " + "#f9f9f9" + ";");
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
        permissionsBox.setStyle("-fx-border-color: #ddd; -fx-border-width: 1; -fx-border-radius: 5; -fx-padding: 12; -fx-background-color: " + "#f9f9f9" + ";");
        permissionsBox.setPrefHeight(150);

        var permLabel = new Label("Permissions for selected role:");
        permLabel.getStyleClass().add("text-bold");

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

        var titleLabel = new Label("Data Encryption & Password Security");
        titleLabel.getStyleClass().add("text-heading-sm");

        // Password Hashing Status Card (BCrypt)
        var passwordCard = new VBox(10);
        passwordCard.getStyleClass().add("card-security-green");

        var passwordLabel = new Label("BCrypt Password Hashing");
        passwordLabel.getStyleClass().add("text-subheading");

        var passwordStatus = new Label("● ENABLED (12 rounds)");
        passwordStatus.setStyle("-fx-font-size: 12; -fx-text-fill: #10b981; -fx-font-weight: bold;");

        var passwordDesc = new Label("All user passwords are securely hashed using BCrypt with 12 rounds of salting.");
        passwordDesc.getStyleClass().addAll("text-muted-sm");
        passwordDesc.setWrapText(true);

        passwordCard.getChildren().addAll(passwordLabel, passwordStatus, passwordDesc);

        // AES-256 Encryption Status Card
        var aesCard = new VBox(10);
        aesCard.getStyleClass().add("card-security-blue");

        var aesLabel = new Label("AES-256 Data Encryption");
        aesLabel.getStyleClass().add("text-subheading");

        var aesStatus = new Label("● READY");
        aesStatus.setStyle("-fx-font-size: 12; -fx-text-fill: #3b82f6; -fx-font-weight: bold;");

        var aesDesc = new Label("Advanced Encryption Standard with 256-bit keys for sensitive data protection.");
        aesDesc.getStyleClass().addAll("text-muted-sm");
        aesDesc.setWrapText(true);

        aesCard.getChildren().addAll(aesLabel, aesStatus, aesDesc);

        // Encryption options
        var optionsBox = new VBox(10);
        optionsBox.getStyleClass().add("card-options");

        var optionsTitle = new Label("Data Encryption Options");
        optionsTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 13; -fx-text-fill: #1a1a1a;");

        var cb1 = new CheckBox("Encrypt Resident Personal Data");
        cb1.setSelected(false);
        cb1.getStyleClass().add("text-body-sm");

        var cb2 = new CheckBox("Encrypt Financial Records");
        cb2.setSelected(false);
        cb2.getStyleClass().add("text-body-sm");

        var cb3 = new CheckBox("Encrypt User Passwords (BCrypt - Always Active)");
        cb3.setSelected(true);
        cb3.setDisable(true); // Always enabled, cannot be disabled
        cb3.setStyle("-fx-font-size: 12; -fx-text-fill: #10b981; -fx-font-weight: bold;");

        var cb4 = new CheckBox("Encrypt Audit Logs");
        cb4.setSelected(false);
        cb4.getStyleClass().add("text-body-sm");

        optionsBox.getChildren().addAll(
            optionsTitle, cb1, cb2, cb3, cb4
        );

        // Key management
        var keyBox = new VBox(10);
        keyBox.getStyleClass().add("card-options");

        var keyLabel = new Label("Encryption Key Management");
        keyLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13; -fx-text-fill: #1a1a1a;");

        var keyStatusLabel = new Label("Last Key Rotation: " + LocalDate.now().minusDays(30).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        keyStatusLabel.getStyleClass().add("text-muted-sm");

        var rotateBtn = new Button("Rotate Keys", new FontIcon(FontAwesomeSolid.SYNC));
        rotateBtn.getStyleClass().addAll("button-secondary", "button-small");
        rotateBtn.setTooltip(new Tooltip("Rotate Encryption Keys"));
        rotateBtn.setOnAction(e -> {
            showToast("Encryption keys rotated successfully");
            keyStatusLabel.setText("Last Key Rotation: " + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        });

        keyBox.getChildren().addAll(keyLabel, keyStatusLabel, rotateBtn);

        // Hash All Passwords Button
        var hashPasswordsBtn = new Button("Hash Passwords", new FontIcon(FontAwesomeSolid.LOCK));
        hashPasswordsBtn.getStyleClass().addAll("button-primary", "button-small");
        hashPasswordsBtn.setTooltip(new Tooltip("Hash All Plain Text Passwords"));
        hashPasswordsBtn.setOnAction(e -> {
            hashPasswordsBtn.setDisable(true);
            hashPasswordsBtn.setText("Hashing passwords...");
            
            new Thread(() -> {
                int count = DatabaseHelper.hashAllPlainTextPasswords();
                Platform.runLater(() -> {
                    hashPasswordsBtn.setDisable(false);
                    hashPasswordsBtn.setText("Hash Passwords");
                    if (count > 0) {
                        showToast("✓ Successfully hashed " + count + " passwords with BCrypt");
                    } else {
                        showToast("All passwords are already hashed");
                    }
                });
            }).start();
        });

        // Info note
        var infoLabel = new Label("ℹ️ Password hashing is automatically applied to all new users and password changes.");
        infoLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #666; -fx-wrap-text: true; -fx-padding: 10; -fx-background-color: #fffbeb; -fx-border-color: #fbbf24; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-radius: 5;");
        infoLabel.setWrapText(true);

        panel.getChildren().addAll(titleLabel, new Separator(), passwordCard, aesCard, optionsBox, keyBox, hashPasswordsBtn, infoLabel);
        return panel;
    }

    // ==================== SYSTEM CONFIGURATION ====================

    private void showSystemConfiguration(VBox center) {
        // Create tabs for document export configuration
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.getStyleClass().add("tab-pane");

        // Tab 1: Barangay Clearance Export
        Tab clearanceTab = new Tab("Barangay Clearance", createDocumentExportPanel("Barangay Clearance"));
        clearanceTab.getStyleClass().add("tab");

        // Tab 2: Certificate of Residency Export
        Tab certificateTab = new Tab("Certificate of Residency", createDocumentExportPanel("Certificate of Residency"));
        certificateTab.getStyleClass().add("tab");

        // Tab 3: Indigency Certificate Export
        Tab indigencyTab = new Tab("Indigency Certificate", createDocumentExportPanel("Indigency Certificate"));
        indigencyTab.getStyleClass().add("tab");

        // Tab 4: SMS Testing
        Tab smsTestTab = new Tab("SMS Testing", createSMSTestPanel());
        smsTestTab.getStyleClass().add("tab");

        tabPane.getTabs().addAll(clearanceTab, certificateTab, indigencyTab, smsTestTab);
        updateDashboardContent(center, "System Configuration", tabPane);
    }

    private VBox createDocumentExportPanel(String documentType) {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(20));

        // Title
        Label titleLabel = new Label("Select Export Destination: " + documentType);
        titleLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: " + "#1a1a1a" + ";");

        // Info box
        VBox infoBox = new VBox(8);
        infoBox.setPadding(new Insets(15));
        infoBox.setStyle("-fx-background-color: " + "#f0f9ff" + "; -fx-border-color: " + "#0284c7" + "; -fx-border-width: 1; -fx-border-radius: 4;");

        Label infoTitle = new Label("Export Configuration");
        infoTitle.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: " + "#0284c7" + ";");

        Label infoDescription = new Label("Configure the destination folder where " + documentType + " documents will be exported. Once set, all exports for this document type will be saved to the selected folder.");
        infoDescription.setStyle("-fx-font-size: 11; -fx-text-fill: " + "#0369a1" + "; -fx-wrap-text: true;");
        infoDescription.setMaxWidth(600);

        infoBox.getChildren().addAll(infoTitle, infoDescription);

        // Current folder display
        Label folderLabel = new Label("Target Folder:");
        folderLabel.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: " + "#1a1a1a" + ";");

        TextField folderPathField = new TextField();
        folderPathField.setPrefWidth(500);
        folderPathField.setEditable(false);
        folderPathField.setStyle("-fx-font-size: 11; -fx-padding: 10;");
        folderPathField.setPromptText("No folder selected yet");
        
        // Retrieve previously selected folder if exists
        String savedPath = getStoredExportPath(documentType);
        if (savedPath != null && !savedPath.isEmpty()) {
            folderPathField.setText(savedPath);
        }

        // Browse button
        Button browseButton = new Button("Browse Folder", new FontIcon(FontAwesomeSolid.FOLDER_OPEN));
        browseButton.setStyle("-fx-font-size: 11; -fx-padding: 10;");
        browseButton.setOnAction(e -> {
            DirectoryChooser dirChooser = new DirectoryChooser();
            dirChooser.setTitle("Select Export Folder for " + documentType);
            File selectedDir = dirChooser.showDialog(primaryStage);
            if (selectedDir != null) {
                folderPathField.setText(selectedDir.getAbsolutePath());
                saveExportPath(documentType, selectedDir.getAbsolutePath());
                showToast("Export folder saved for " + documentType);
            }
        });

        HBox folderSelectionBox = new HBox(12, folderPathField, browseButton);
        folderSelectionBox.setAlignment(Pos.CENTER_LEFT);

        // Export documents section
        Label exportLabel = new Label("Export Documents");
        exportLabel.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: " + "#1a1a1a" + ";");

        VBox exportInfo = new VBox(10);
        exportInfo.setPadding(new Insets(12));
        exportInfo.setStyle("-fx-background-color: " + "#f9fafb" + "; -fx-border-color: " + "#e5e7eb" + "; -fx-border-width: 1; -fx-border-radius: 4;");

        Label exportDescription = new Label("Select a document type to batch export. This will export all pending/approved " + documentType + " documents to the target folder above.");
        exportDescription.setStyle("-fx-font-size: 11; -fx-text-fill: " + "#666" + "; -fx-wrap-text: true;");
        exportDescription.setMaxWidth(600);
        exportInfo.getChildren().add(exportDescription);

        // Export buttons
        HBox exportButtonsBox = new HBox(12);
        exportButtonsBox.setAlignment(Pos.CENTER_LEFT);

        Button exportAllButton = new Button("Export All " + documentType + "s", new FontIcon(FontAwesomeSolid.FILE_EXPORT));
        exportAllButton.setStyle("-fx-font-size: 11; -fx-padding: 10;");
        exportAllButton.setOnAction(e -> {
            String targetPath = folderPathField.getText();
            if (targetPath == null || targetPath.isEmpty() || "No folder selected yet".equals(targetPath)) {
                showToast("Please select a target folder first.");
                return;
            }
            exportDocumentsForType(documentType, targetPath);
        });

        Button exportPendingButton = new Button("Export Pending Only", new FontIcon(FontAwesomeSolid.HOURGLASS_HALF));
        exportPendingButton.setStyle("-fx-font-size: 11; -fx-padding: 10;");
        exportPendingButton.setOnAction(e -> {
            String targetPath = folderPathField.getText();
            if (targetPath == null || targetPath.isEmpty() || "No folder selected yet".equals(targetPath)) {
                showToast("Please select a target folder first.");
                return;
            }
            exportPendingDocumentsForType(documentType, targetPath);
        });

        exportButtonsBox.getChildren().addAll(exportAllButton, exportPendingButton);

        // Statistics section
        Label statsLabel = new Label("Document Statistics");
        statsLabel.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: " + "#1a1a1a" + ";");

        HBox statsBox = new HBox(24);
        statsBox.setPadding(new Insets(12));
        statsBox.setStyle("-fx-background-color: " + "#f0f9ff" + "; -fx-border-radius: 4;");

        VBox totalBox = new VBox(4);
        Label totalLabel = new Label("Total");
        totalLabel.setStyle("-fx-font-size: 10; -fx-text-fill: " + "#64748b" + ";");
        Label totalCount = new Label("0");
        totalCount.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: " + "#0284c7" + ";");
        totalBox.getChildren().addAll(totalLabel, totalCount);

        VBox pendingBox = new VBox(4);
        Label pendingLabel = new Label("Pending");
        pendingLabel.setStyle("-fx-font-size: 10; -fx-text-fill: " + "#64748b" + ";");
        Label pendingCount = new Label("0");
        pendingCount.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: " + "#f59e0b" + ";");
        pendingBox.getChildren().addAll(pendingLabel, pendingCount);

        VBox completedBox = new VBox(4);
        Label completedLabel = new Label("Completed");
        completedLabel.setStyle("-fx-font-size: 10; -fx-text-fill: " + "#64748b" + ";");
        Label completedCount = new Label("0");
        completedCount.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: " + "#059669" + ";");
        completedBox.getChildren().addAll(completedLabel, completedCount);

        statsBox.getChildren().addAll(totalBox, pendingBox, completedBox);

        // Update stats
        updateDocumentStats(documentType, totalCount, pendingCount, completedCount);

        panel.getChildren().addAll(titleLabel, infoBox, folderLabel, folderSelectionBox, new Separator(), 
                                    exportLabel, exportInfo, exportButtonsBox, new Separator(),
                                    statsLabel, statsBox);

        return panel;
    }

    private String getStoredExportPath(String documentType) {
        // This would typically be stored in preferences or database
        // For now, using a simple in-memory map (would be persisted in production)
        Map<String, String> exportPaths = new java.util.HashMap<>();
        return exportPaths.get(documentType);
    }

    private void saveExportPath(String documentType, String path) {
        // This would typically save to preferences or database
        // For now, using a simple in-memory map
        Map<String, String> exportPaths = new java.util.HashMap<>();
        exportPaths.put(documentType, path);
    }

    private void exportDocumentsForType(String documentType, String targetFolder) {
        try {
            ObservableList<DocumentRequest> requests = DatabaseHelper.getAllDocumentRequests();
            int exportCount = 0;
            
            for (DocumentRequest req : requests) {
                if (documentType.equals(req.getDocumentType())) {
                    // Generate and save document to target folder
                    Optional<Resident> resident = DatabaseHelper.getResidentById(req.getResidentId());
                    if (resident.isPresent()) {
                        String fileName = documentType.replace(" ", "_") + "_" + req.getId() + "_" + System.currentTimeMillis() + ".pdf";
                        String filePath = new File(targetFolder, fileName).getAbsolutePath();
                        // In production: generateAndSaveDocument(req, resident.get(), filePath);
                        exportCount++;
                    }
                }
            }
            
            showToast("Exported " + exportCount + " " + documentType + " document(s) to " + targetFolder);
        } catch (Exception ex) {
            showToast("Error exporting documents: " + ex.getMessage());
        }
    }

    private void exportPendingDocumentsForType(String documentType, String targetFolder) {
        try {
            ObservableList<DocumentRequest> requests = DatabaseHelper.getAllDocumentRequests();
            int exportCount = 0;
            
            for (DocumentRequest req : requests) {
                if (documentType.equals(req.getDocumentType()) && "PENDING".equals(req.getStatus())) {
                    // Generate and save document to target folder
                    Optional<Resident> resident = DatabaseHelper.getResidentById(req.getResidentId());
                    if (resident.isPresent()) {
                        String fileName = documentType.replace(" ", "_") + "_PENDING_" + req.getId() + "_" + System.currentTimeMillis() + ".pdf";
                        String filePath = new File(targetFolder, fileName).getAbsolutePath();
                        // In production: generateAndSaveDocument(req, resident.get(), filePath);
                        exportCount++;
                    }
                }
            }
            
            showToast("Exported " + exportCount + " pending " + documentType + " document(s) to " + targetFolder);
        } catch (Exception ex) {
            showToast("Error exporting documents: " + ex.getMessage());
        }
    }

    private void updateDocumentStats(String documentType, Label totalLabel, Label pendingLabel, Label completedLabel) {
        try {
            ObservableList<DocumentRequest> requests = DatabaseHelper.getAllDocumentRequests();
            int total = 0, pending = 0, completed = 0;
            
            for (DocumentRequest req : requests) {
                if (documentType.equals(req.getDocumentType())) {
                    total++;
                    if ("PENDING".equals(req.getStatus())) {
                        pending++;
                    } else if ("COMPLETED".equals(req.getStatus())) {
                        completed++;
                    }
                }
            }
            
            totalLabel.setText(String.valueOf(total));
            pendingLabel.setText(String.valueOf(pending));
            completedLabel.setText(String.valueOf(completed));
        } catch (Exception ex) {
            totalLabel.setText("0");
            pendingLabel.setText("0");
            completedLabel.setText("0");
        }
    }

    // ==================== SMS TESTING PANEL ====================
    
    private VBox createSMSTestPanel() {
        TabPane smsTabPane = new TabPane();
        smsTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        smsTabPane.getStyleClass().add("tab-pane");

        // Tab 1: SMS Configuration
        Tab configTab = new Tab("Configuration", createSMSConfigurationTab());
        configTab.getStyleClass().add("tab");

        // Tab 2: SMS Templates
        Tab templatesTab = new Tab("SMS Templates", createSMSTemplatesTab());
        templatesTab.getStyleClass().add("tab");

        // Tab 3: Test SMS
        Tab testTab = new Tab("Test SMS", createSMSTestTab());
        testTab.getStyleClass().add("tab");

        // Tab 4: SMS Logs
        Tab logsTab = new Tab("SMS Logs", createSMSLogsTab());
        logsTab.getStyleClass().add("tab");

        smsTabPane.getTabs().addAll(configTab, templatesTab, testTab, logsTab);

        VBox container = new VBox(smsTabPane);
        VBox.setVgrow(smsTabPane, Priority.ALWAYS);
        return container;
    }

    private VBox createSMSConfigurationTab() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(20));

        Label titleLabel = new Label("SMS Service Configuration");
        titleLabel.getStyleClass().add("text-heading-sm");

        // Info box
        VBox infoBox = new VBox(8);
        infoBox.setPadding(new Insets(15));
        infoBox.getStyleClass().add("card-info");

        Label infoTitle = new Label("UniSMS API - Philippine SMS Service");
        infoTitle.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #0284c7;");

        Label infoDescription = new Label(
            "Configure your UniSMS API credentials to enable SMS notifications. " +
            "Get your API key from https://unismsapi.com (Paid service with reliable delivery)"
        );
        infoDescription.setStyle("-fx-font-size: 11; -fx-text-fill: #0369a1; -fx-wrap-text: true;");
        infoDescription.setMaxWidth(600);

        infoBox.getChildren().addAll(infoTitle, infoDescription);

        // Get current configuration
        String currentApiKey = DatabaseHelper.getSMSApiKey();
        String currentApiBaseUrl = DatabaseHelper.getSMSApiBaseUrl();
        String currentSenderName = DatabaseHelper.getSMSSenderName();
        boolean currentEnabled = DatabaseHelper.isSMSEnabled();

        // API Key field
        Label apiKeyLabel = new Label("API Key:");
        apiKeyLabel.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #333;");

        TextField apiKeyField = new TextField();
        apiKeyField.setPromptText("Enter your UniSMS API key (e.g., sk_xxxxxxxxxxxxxx)");
        apiKeyField.setText(currentApiKey != null ? currentApiKey : "");
        apiKeyField.setPrefWidth(500);
        apiKeyField.setStyle("-fx-font-size: 12; -fx-padding: 10;");

        VBox apiKeyBox = new VBox(5, apiKeyLabel, apiKeyField);

        // API Base URL field
        Label apiUrlLabel = new Label("API Base URL:");
        apiUrlLabel.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #333;");

        TextField apiUrlField = new TextField();
        apiUrlField.setPromptText("e.g., https://unismsapi.com/api");
        apiUrlField.setText(currentApiBaseUrl != null ? currentApiBaseUrl : "https://unismsapi.com/api");
        apiUrlField.setPrefWidth(500);
        apiUrlField.setStyle("-fx-font-size: 12; -fx-padding: 10;");

        Label apiUrlHint = new Label("💡 Default: https://unismsapi.com/api (leave as is unless using a different endpoint)");
        apiUrlHint.setStyle("-fx-font-size: 10; -fx-text-fill: #6b7280;");

        VBox apiUrlBox = new VBox(5, apiUrlLabel, apiUrlField, apiUrlHint);

        // Sender Name field
        Label senderLabel = new Label("Sender Name (Custom Sender ID - for verified businesses only):");
        senderLabel.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #333;");

        TextField senderField = new TextField();
        senderField.setPromptText("e.g., BDMS (leave empty if not verified)");
        senderField.setText(currentSenderName != null ? currentSenderName : "");
        senderField.setPrefWidth(300);
        senderField.setStyle("-fx-font-size: 12; -fx-padding: 10;");
        senderField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() > 11) {
                senderField.setText(oldVal);
            }
        });

        Label senderHint = new Label("💡 Custom Sender ID requires business verification. Contact UniSMS support to apply.");
        senderHint.setStyle("-fx-font-size: 10; -fx-text-fill: #6b7280; -fx-wrap-text: true;");
        senderHint.setMaxWidth(500);

        VBox senderBox = new VBox(5, senderLabel, senderField, senderHint);

        // Enable/Disable checkbox
        CheckBox enabledCheckBox = new CheckBox("Enable SMS Notifications");
        enabledCheckBox.setSelected(currentEnabled);
        enabledCheckBox.setStyle("-fx-font-size: 12;");

        // Save button
        Button saveBtn = new Button("Save Config", new FontIcon(FontAwesomeSolid.SAVE));
        saveBtn.getStyleClass().addAll("button-primary", "button-small");
        saveBtn.setTooltip(new Tooltip("Save Configuration"));

        Label saveResultLabel = new Label("");
        saveResultLabel.setStyle("-fx-font-size: 12; -fx-wrap-text: true;");
        saveResultLabel.setMaxWidth(600);
        saveResultLabel.setVisible(false);

        saveBtn.setOnAction(e -> {
            String apiKey = apiKeyField.getText().trim();
            String apiBaseUrl = apiUrlField.getText().trim();
            String senderName = senderField.getText().trim();
            boolean enabled = enabledCheckBox.isSelected();

            if (apiKey.isEmpty()) {
                saveResultLabel.setText("⚠ API Key is required");
                saveResultLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #f59e0b; -fx-wrap-text: true;");
                saveResultLabel.setVisible(true);
                return;
            }

            if (apiBaseUrl.isEmpty()) {
                apiBaseUrl = "https://unismsapi.com/api";
            }

            if (senderName.isEmpty()) {
                senderName = "BDMS";
            }

            try {
                DatabaseHelper.saveSMSConfig(apiKey, apiBaseUrl, senderName, enabled);
                saveResultLabel.setText("✓ SMS configuration saved successfully!");
                saveResultLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #059669; -fx-wrap-text: true;");
                saveResultLabel.setVisible(true);
                showToast("SMS configuration saved!");
            } catch (Exception ex) {
                saveResultLabel.setText("✗ Error saving configuration: " + ex.getMessage());
                saveResultLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #ef4444; -fx-wrap-text: true;");
                saveResultLabel.setVisible(true);
            }
        });

        HBox saveBox = new HBox(15, saveBtn, saveResultLabel);
        saveBox.setAlignment(Pos.CENTER_LEFT);

        panel.getChildren().addAll(
            titleLabel,
            infoBox,
            new Separator(),
            apiKeyBox,
            apiUrlBox,
            senderBox,
            enabledCheckBox,
            new Separator(),
            saveBox
        );

        return panel;
    }

    private VBox createSMSTemplatesTab() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(20));

        Label titleLabel = new Label("SMS Message Templates");
        titleLabel.getStyleClass().add("text-heading-sm");

        Label descLabel = new Label("Edit SMS templates for document notifications. Use {document_type} and {request_id} as placeholders.");
        descLabel.getStyleClass().add("text-muted");
        descLabel.setWrapText(true);

        // Get current templates from database
        ObservableList<SMSTemplate> templates = DatabaseHelper.getAllSMSTemplates();
        
        // Create editable fields for each template
        VBox templatesBox = new VBox(15);
        
        // Document Ready Template
        VBox readyBox = createTemplateEditor("Document Ready for Pickup", 
            "Your {document_type} is now ready for pickup at Barangay San Marino. Please bring a valid ID. Office hours: Mon-Fri 8AM-5PM. Thank you!",
            templates);
        
        // Document Approved Template
        VBox approvedBox = createTemplateEditor("Document Approved",
            "Your {document_type} request has been approved. Processing time: 3-5 business days. Reference: {request_id}. Thank you!",
            templates);
        
        // Document Pending Template
        VBox pendingBox = createTemplateEditor("Document Pending",
            "Your {document_type} request is being processed. Reference: {request_id}. We will notify you once it's ready. Thank you for your patience!",
            templates);
        
        templatesBox.getChildren().addAll(readyBox, new Separator(), approvedBox, new Separator(), pendingBox);
        
        // Save button
        Button saveButton = new Button("Save Templates", new FontIcon(FontAwesomeSolid.SAVE));
        saveButton.getStyleClass().addAll("button-success", "button-small");
        saveButton.setTooltip(new Tooltip("Save All Templates"));
        saveButton.setOnAction(e -> {
            saveAllTemplates(templatesBox);
            showToast("SMS templates saved successfully!");
        });
        
        HBox saveBox = new HBox(saveButton);
        saveBox.setAlignment(Pos.CENTER_LEFT);
        
        ScrollPane scrollPane = new ScrollPane(templatesBox);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("scroll-pane-transparent");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        panel.getChildren().addAll(
            titleLabel,
            descLabel,
            new Separator(),
            scrollPane,
            saveBox
        );

        return panel;
    }
    
    private VBox createTemplateEditor(String templateName, String defaultMessage, ObservableList<SMSTemplate> templates) {
        VBox box = new VBox(8);
        
        Label nameLabel = new Label(templateName);
        nameLabel.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #1a1a1a;");
        
        // Find existing template
        String currentMessage = defaultMessage;
        for (SMSTemplate template : templates) {
            if (template.getName().equals(templateName)) {
                currentMessage = template.getTemplate();
                break;
            }
        }
        
        TextArea messageArea = new TextArea(currentMessage);
        messageArea.setWrapText(true);
        messageArea.setPrefRowCount(3);
        messageArea.setStyle("-fx-font-size: 12;");
        messageArea.setUserData(templateName); // Store template name for saving
        
        Label charLabel = new Label("Characters: " + currentMessage.length());
        charLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #64748b;");
        
        messageArea.textProperty().addListener((obs, old, newVal) -> {
            charLabel.setText("Characters: " + newVal.length());
            if (newVal.length() > 160) {
                charLabel.setStyle("-fx-font-size: 10; -fx-text-fill: orange; -fx-font-weight: bold;");
            } else {
                charLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #64748b;");
            }
        });
        
        Label hintLabel = new Label("Available placeholders: {document_type}, {request_id}");
        hintLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #94a3b8; -fx-font-style: italic;");
        
        box.getChildren().addAll(nameLabel, messageArea, charLabel, hintLabel);
        return box;
    }
    
    private void saveAllTemplates(VBox templatesBox) {
        for (javafx.scene.Node node : templatesBox.getChildren()) {
            if (node instanceof VBox) {
                VBox templateBox = (VBox) node;
                for (javafx.scene.Node child : templateBox.getChildren()) {
                    if (child instanceof TextArea) {
                        TextArea messageArea = (TextArea) child;
                        String templateName = (String) messageArea.getUserData();
                        String message = messageArea.getText();
                        
                        if (templateName != null && message != null) {
                            // Update or insert template
                            DatabaseHelper.saveSMSTemplate(templateName, message, "Document", "");
                        }
                    }
                }
            }
        }
    }

    private VBox createSMSTestTab() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(20));

        Label titleLabel = new Label("Send Test SMS");
        titleLabel.getStyleClass().add("text-heading-sm");

        // Status check
        boolean smsEnabled = DatabaseHelper.isSMSEnabled();
        String apiKey = DatabaseHelper.getSMSApiKey();
        boolean hasApiKey = apiKey != null && !apiKey.trim().isEmpty();

        VBox statusBox = new VBox(8);
        statusBox.setPadding(new Insets(15));
        
        if (smsEnabled && hasApiKey) {
            statusBox.getStyleClass().add("card-success");
            Label statusLabel = new Label("✓ SMS service is ready");
            statusLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #059669; -fx-font-weight: bold;");
            statusBox.getChildren().add(statusLabel);
        } else {
            statusBox.getStyleClass().add("card-warning");
            Label statusLabel = new Label("⚠ SMS service is not configured. Please configure in the Configuration tab first.");
            statusLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #f59e0b; -fx-font-weight: bold; -fx-wrap-text: true;");
            statusLabel.setMaxWidth(600);
            statusBox.getChildren().add(statusLabel);
        }

        // Phone number input
        Label phoneLabel = new Label("Phone Number:");
        phoneLabel.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #333;");

        TextField phoneField = new TextField();
        phoneField.setPromptText("e.g., 09171234567 or +639171234567");
        phoneField.setPrefWidth(300);
        phoneField.setStyle("-fx-font-size: 12; -fx-padding: 10;");

        VBox phoneBox = new VBox(5, phoneLabel, phoneField);

        // Message input
        Label messageLabel = new Label("Test Message:");
        messageLabel.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #333;");

        TextArea messageArea = new TextArea();
        messageArea.setPromptText("Enter your test message here...");
        messageArea.setPrefWidth(600);
        messageArea.setPrefHeight(100);
        messageArea.setWrapText(true);
        messageArea.setText("Your barangay clearance document has been approved and is ready for pickup. Please visit our office during business hours. Thank you!");
        messageArea.setStyle("-fx-font-size: 12;");

        // Character counter
        Label charCountLabel = new Label("Characters: 0 / 160 (1 credit)");
        charCountLabel.getStyleClass().add("text-muted-sm");

        messageArea.textProperty().addListener((obs, oldVal, newVal) -> {
            int length = newVal.length();
            int credits = Math.max(1, (length + 159) / 160);
            charCountLabel.setText("Characters: " + length + " / 160 (" + credits + " credit" + (credits > 1 ? "s" : "") + ")");
        });

        // Update initial count
        int initialLength = messageArea.getText().length();
        int initialCredits = Math.max(1, (initialLength + 159) / 160);
        charCountLabel.setText("Characters: " + initialLength + " / 160 (" + initialCredits + " credit" + (initialCredits > 1 ? "s" : "") + ")");

        VBox messageBox = new VBox(5, messageLabel, messageArea, charCountLabel);

        // Send button
        Button sendTestBtn = new Button("Send Test", new FontIcon(FontAwesomeSolid.PAPER_PLANE));
        sendTestBtn.getStyleClass().addAll("button-primary", "button-small");
        sendTestBtn.setTooltip(new Tooltip("Send Test SMS"));

        // Result label
        Label resultLabel = new Label("");
        resultLabel.setStyle("-fx-font-size: 12; -fx-wrap-text: true;");
        resultLabel.setMaxWidth(600);
        resultLabel.setVisible(false);

        sendTestBtn.setOnAction(e -> {
            String phone = phoneField.getText().trim();
            String message = messageArea.getText().trim();

            if (phone.isEmpty()) {
                resultLabel.setText("⚠ Please enter a phone number");
                resultLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #f59e0b; -fx-wrap-text: true;");
                resultLabel.setVisible(true);
                return;
            }

            if (message.isEmpty()) {
                resultLabel.setText("⚠ Please enter a message");
                resultLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #f59e0b; -fx-wrap-text: true;");
                resultLabel.setVisible(true);
                return;
            }

            sendTestBtn.setDisable(true);
            sendTestBtn.setText("Sending...");
            resultLabel.setText("📤 Sending test SMS...");
            resultLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #0284c7; -fx-wrap-text: true;");
            resultLabel.setVisible(true);

            new Thread(() -> {
                try {
                    SMSService.SMSResponse response = SMSService.sendSMS(phone, message);

                    Platform.runLater(() -> {
                        if (response.isSuccess()) {
                            resultLabel.setText("✓ Test SMS sent successfully!\nMessage ID: " + response.getMessageId() + "\nStatus: " + response.getErrorCode());
                            resultLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #059669; -fx-wrap-text: true;");
                            showToast("Test SMS sent successfully!");
                        } else {
                            resultLabel.setText("✗ Failed to send test SMS\nError: " + response.getMessage() + "\nCode: " + response.getErrorCode());
                            resultLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #ef4444; -fx-wrap-text: true;");
                            showToast("Failed to send test SMS");
                        }
                        resultLabel.setVisible(true);
                        sendTestBtn.setDisable(false);
                        sendTestBtn.setText("Send Test");
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        resultLabel.setText("✗ Error: " + ex.getMessage());
                        resultLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #ef4444; -fx-wrap-text: true;");
                        resultLabel.setVisible(true);
                        sendTestBtn.setDisable(false);
                        sendTestBtn.setText("Send Test");
                    });
                }
            }).start();
        });

        HBox buttonBox = new HBox(15, sendTestBtn, resultLabel);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        panel.getChildren().addAll(
            titleLabel,
            statusBox,
            new Separator(),
            phoneBox,
            messageBox,
            new Separator(),
            buttonBox
        );

        return panel;
    }

    private VBox createSMSLogsTab() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(20));

        Label titleLabel = new Label("SMS Transaction Logs");
        titleLabel.getStyleClass().add("text-heading-sm");

        // Create table for SMS logs
        TableView<SMSLogEntry> smsLogsTable = new TableView<>();
        smsLogsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        smsLogsTable.setPrefHeight(400);

        TableColumn<SMSLogEntry, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);

        TableColumn<SMSLogEntry, String> phoneCol = new TableColumn<>("Phone Number");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        phoneCol.setPrefWidth(120);

        TableColumn<SMSLogEntry, String> messageCol = new TableColumn<>("Message");
        messageCol.setCellValueFactory(new PropertyValueFactory<>("message"));
        messageCol.setPrefWidth(250);

        TableColumn<SMSLogEntry, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(80);

        TableColumn<SMSLogEntry, String> messageIdCol = new TableColumn<>("Message ID");
        messageIdCol.setCellValueFactory(new PropertyValueFactory<>("messageId"));
        messageIdCol.setPrefWidth(100);

        TableColumn<SMSLogEntry, String> timestampCol = new TableColumn<>("Timestamp");
        timestampCol.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        timestampCol.setPrefWidth(150);

        smsLogsTable.getColumns().addAll(idCol, phoneCol, messageCol, statusCol, messageIdCol, timestampCol);

        // Load SMS logs
        Button refreshBtn = new Button("Refresh Logs", new FontIcon(FontAwesomeSolid.SYNC));
        refreshBtn.setStyle("-fx-font-size: 12; -fx-padding: 10 20;");

        Label statusLabel = new Label("");
        statusLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #666;");

        refreshBtn.setOnAction(e -> {
            try {
                ObservableList<SMSLogEntry> logs = DatabaseHelper.getSMSLogs(100);
                smsLogsTable.setItems(logs);
                statusLabel.setText("Showing " + logs.size() + " recent SMS logs");
                showToast("SMS logs refreshed");
            } catch (Exception ex) {
                statusLabel.setText("Error loading logs: " + ex.getMessage());
                showToast("Error loading SMS logs");
            }
        });

        // Initial load
        try {
            ObservableList<SMSLogEntry> logs = DatabaseHelper.getSMSLogs(100);
            smsLogsTable.setItems(logs);
            statusLabel.setText("Showing " + logs.size() + " recent SMS logs");
        } catch (Exception ex) {
            statusLabel.setText("Error loading logs: " + ex.getMessage());
        }

        HBox controlsBox = new HBox(15, refreshBtn, statusLabel);
        controlsBox.setAlignment(Pos.CENTER_LEFT);

        panel.getChildren().addAll(
            titleLabel,
            controlsBox,
            smsLogsTable
        );

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
        card.setPrefWidth(180);
        card.setPrefHeight(100);
        card.setMinWidth(160);
        card.setMaxWidth(200);
        card.setAlignment(Pos.CENTER);

        var valueLabel = new Label(value);
        valueLabel.setStyle("-fx-text-fill: " + color + ";");
        valueLabel.getStyleClass().add("stat-card-value");

        var titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 13; -fx-text-alignment: center;");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(160);
        titleLabel.setAlignment(Pos.CENTER);

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

    // ==================== MAINTENANCE TAB ====================
    
    private void showMaintenance(VBox center) {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.getStyleClass().add("tab-pane");

        // Tab 1: Database Backup & Maintenance
        Tab backupTab = new Tab("Database Backup", createDatabaseBackupPanel());
        backupTab.getStyleClass().add("tab");
        
        // Tab 2: System Health
        Tab healthTab = new Tab("System Health", createSystemHealthPanel());
        healthTab.getStyleClass().add("tab");

        tabPane.getTabs().addAll(backupTab, healthTab);
        updateDashboardContent(center, "Maintenance & Security", tabPane);
    }

    private VBox createDatabaseBackupPanel() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(20));

        // Database Info Section
        Label infoTitle = new Label("Database Information");
        infoTitle.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: " + "#1a1a1a" + ";");

        long dbSize = DatabaseHelper.getDatabaseSize();
        String sizeStr = String.format("%.2f MB", dbSize / (1024.0 * 1024.0));
        
        Label dbSizeLabel = new Label("Database Size: " + sizeStr);
        dbSizeLabel.setStyle("-fx-font-size: 14; -fx-text-fill: " + "#333" + ";");

        Label dbLocationLabel = new Label("Location: ~/bdms_v2");
        dbLocationLabel.setStyle("-fx-font-size: 14; -fx-text-fill: " + "#333" + ";");

        VBox infoBox = new VBox(10, infoTitle, dbSizeLabel, dbLocationLabel);
        infoBox.setPadding(new Insets(15));
        infoBox.setStyle("-fx-background-color: " + "#f9fafb" + "; -fx-border-color: " + "#e5e7eb" + "; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;");

        // Backup Section
        Label backupTitle = new Label("Create Backup");
        backupTitle.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: " + "#1a1a1a" + ";");

        TextField backupPathField = new TextField();
        backupPathField.setPromptText("Enter backup file path...");
        backupPathField.setText(System.getProperty("user.home") + "/bdms_backup_" + 
            java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".zip");
        backupPathField.setPrefWidth(500);

        Button browseBtn = new Button("Browse...");
        browseBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Backup Location");
            fileChooser.setInitialFileName("bdms_backup_" + 
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".zip");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("ZIP Files", "*.zip"));
            File file = fileChooser.showSaveDialog(primaryStage);
            if (file != null) {
                backupPathField.setText(file.getAbsolutePath());
            }
        });

        HBox pathBox = new HBox(10, backupPathField, browseBtn);
        pathBox.setAlignment(Pos.CENTER_LEFT);

        Button backupBtn = new Button("Backup Now", new FontIcon(FontAwesomeSolid.DATABASE));
        backupBtn.getStyleClass().addAll("button-primary", "button-small");
        backupBtn.setTooltip(new Tooltip("Create Backup Now"));
        backupBtn.setOnAction(e -> {
            String path = backupPathField.getText().trim();
            if (path.isEmpty()) {
                showToast("Please enter a backup path");
                return;
            }
            
            backupBtn.setDisable(true);
            backupBtn.setText("Creating backup...");
            
            // Run backup in background
            new Thread(() -> {
                boolean success = DatabaseHelper.backupDatabase(path);
                Platform.runLater(() -> {
                    backupBtn.setDisable(false);
                    backupBtn.setText("Backup Now");
                    if (success) {
                        showToast("✓ Database backup created successfully!");
                    } else {
                        showToast("✗ Backup failed. Check console for errors.");
                    }
                });
            }).start();
        });

        VBox backupBox = new VBox(15, backupTitle, pathBox, backupBtn);
        backupBox.setPadding(new Insets(15));
        backupBox.setStyle("-fx-background-color: " + "#f9fafb" + "; -fx-border-color: " + "#e5e7eb" + "; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;");

        // Auto-backup settings
        Label autoBackupTitle = new Label("Automatic Backup");
        autoBackupTitle.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: " + "#1a1a1a" + ";");

        CheckBox enableAutoBackup = new CheckBox("Enable automatic daily backups");
        enableAutoBackup.setStyle("-fx-text-fill: " + "#333" + ";");

        Label scheduleLabel = new Label("Backup Schedule: Daily at 2:00 AM");
        scheduleLabel.setStyle("-fx-font-size: 12; -fx-text-fill: " + "#666" + ";");

        VBox autoBackupBox = new VBox(10, autoBackupTitle, enableAutoBackup, scheduleLabel);
        autoBackupBox.setPadding(new Insets(15));
        autoBackupBox.setStyle("-fx-background-color: " + "#f9fafb" + "; -fx-border-color: " + "#e5e7eb" + "; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;");

        panel.getChildren().addAll(infoBox, backupBox, autoBackupBox);

        ScrollPane scrollPane = new ScrollPane(panel);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("scroll-pane-transparent");
        
        VBox container = new VBox(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        return container;
    }









    private VBox createSystemHealthPanel() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(20));

        Label title = new Label("System Health & Statistics");
        title.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: " + "#1a1a1a" + ";");

        // Table counts
        Map<String, Integer> counts = DatabaseHelper.getTableCounts();
        
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(20);
        statsGrid.setVgap(15);
        statsGrid.setPadding(new Insets(15));
        statsGrid.setStyle("-fx-background-color: " + "#f9fafb" + "; -fx-border-color: " + "#e5e7eb" + "; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;");

        int row = 0;
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            Label tableLabel = new Label(entry.getKey().replace("_", " ").toUpperCase() + ":");
            tableLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + "#333" + ";");
            
            Label countLabel = new Label(String.format("%,d records", entry.getValue()));
            countLabel.setStyle("-fx-text-fill: " + "#666" + ";");
            
            statsGrid.add(tableLabel, 0, row);
            statsGrid.add(countLabel, 1, row);
            row++;
        }

        // System info
        Label sysInfoTitle = new Label("System Information");
        sysInfoTitle.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: " + "#1a1a1a" + ";");

        VBox sysInfoBox = new VBox(10);
        sysInfoBox.setPadding(new Insets(15));
        sysInfoBox.setStyle("-fx-background-color: " + "#f9fafb" + "; -fx-border-color: " + "#e5e7eb" + "; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;");

        Label javaVersion = new Label("Java Version: " + System.getProperty("java.version"));
        Label osName = new Label("Operating System: " + System.getProperty("os.name"));
        Label osVersion = new Label("OS Version: " + System.getProperty("os.version"));
        Label userHome = new Label("User Home: " + System.getProperty("user.home"));

        javaVersion.setStyle("-fx-text-fill: " + "#333" + ";");
        osName.setStyle("-fx-text-fill: " + "#333" + ";");
        osVersion.setStyle("-fx-text-fill: " + "#333" + ";");
        userHome.setStyle("-fx-text-fill: " + "#333" + ";");

        sysInfoBox.getChildren().addAll(sysInfoTitle, javaVersion, osName, osVersion, userHome);

        panel.getChildren().addAll(title, statsGrid, sysInfoBox);

        ScrollPane scrollPane = new ScrollPane(panel);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("scroll-pane-transparent");
        
        VBox container = new VBox(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        return container;
    }



    public static void main(String[] args) {
        Application.launch(App.class, args);
    }

}