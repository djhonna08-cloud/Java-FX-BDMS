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
        stage.setTitle("Baranggay San Marino Information Management System");
        stage.setMinWidth(1000);
        stage.setMinHeight(700);
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

        var rememberCheckBox = new CheckBox("Remember me for 30 days");
        rememberCheckBox.getStyleClass().add("check-box");

        var formVBox = new VBox(12, subtitle, usernameField, passwordField, loginButton, forgotLink, separatorHBox, rememberCheckBox);
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

        var revenueCard = createStatCard("Revenue", "₱0", "#eab308");
        var clearanceCard = createStatCard("Pending Clearances", "0", "#f43f5e");
        var casesCard = createStatCard("Active Cases", "0", "#3b82f6");
        
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

        var content = new VBox(24, statsGrid, bottomRow);
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
        infoLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #666;");

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
        Application.launch(App.class, args);
    }

}