# SMS Integration Code Examples

This document provides ready-to-use code examples for integrating SMS notifications into your BDMS workflows.

---

## 📋 Table of Contents
1. [Basic SMS Sending](#basic-sms-sending)
2. [Document Request Notifications](#document-request-notifications)
3. [Complaint Notifications](#complaint-notifications)
4. [Announcement Broadcasts](#announcement-broadcasts)
5. [OTP Verification](#otp-verification)
6. [SMS Configuration UI](#sms-configuration-ui)
7. [SMS Log Viewer](#sms-log-viewer)

---

## 1. Basic SMS Sending

### Send a Simple SMS
```java
// Send a basic SMS
String phoneNumber = "09171234567";
String message = "Hello from Barangay San Isidro BDMS!";

SMSService.SMSResponse response = SMSService.sendSMS(phoneNumber, message);

if (response.isSuccess()) {
    System.out.println("✅ SMS sent successfully!");
    System.out.println("Message ID: " + response.getMessageId());
} else {
    System.err.println("❌ Failed to send SMS: " + response.getMessage());
    System.err.println("Error Code: " + response.getErrorCode());
}
```

### Send SMS with Custom Sender Name
```java
String phoneNumber = "09171234567";
String message = "Your document is ready for pickup.";
String senderName = "BrgySanIsi"; // Max 11 characters

SMSService.SMSResponse response = SMSService.sendSMS(phoneNumber, message, senderName);
```

### Send Priority SMS (Faster Delivery)
```java
String phoneNumber = "09171234567";
String message = "URGENT: Emergency meeting at 3 PM today.";

SMSService.SMSResponse response = SMSService.sendPrioritySMS(phoneNumber, message);
```

---

## 2. Document Request Notifications

### Send "Document Approved" SMS
```java
/**
 * Call this method when a document request is approved
 */
public void sendDocumentApprovedSMS(int residentId, String documentType) {
    // Get resident phone number
    Optional<Resident> residentOpt = DatabaseHelper.getResidentById(residentId);
    if (!residentOpt.isPresent()) {
        System.err.println("Resident not found: " + residentId);
        return;
    }
    
    Resident resident = residentOpt.get();
    String phoneNumber = resident.getPhoneNumber(); // Requires phone_number column
    
    if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
        System.err.println("No phone number for resident: " + residentId);
        return;
    }
    
    // Get SMS template
    String template = DatabaseHelper.getSMSTemplate("Document Approved");
    if (template == null) {
        template = "Your {document_type} request has been approved. You may claim it at the Barangay Hall. - Barangay San Isidro";
    }
    
    // Replace placeholders
    String message = template.replace("{document_type}", documentType);
    
    // Send SMS
    SMSService.SMSResponse response = SMSService.sendSMS(phoneNumber, message);
    
    if (response.isSuccess()) {
        System.out.println("✅ Document approval SMS sent to " + resident.getFullName());
        
        // Optional: Add notification to system
        DatabaseHelper.addNotification(new Notification(
            "SMS Sent",
            "Document approval SMS sent to " + resident.getFullName(),
            "SUCCESS",
            "check-circle"
        ));
    } else {
        System.err.println("❌ Failed to send SMS: " + response.getMessage());
    }
}
```

### Send "Document Ready for Pickup" SMS
```java
/**
 * Call this method when a document is ready for pickup
 */
public void sendDocumentReadySMS(int residentId, String documentType) {
    Optional<Resident> residentOpt = DatabaseHelper.getResidentById(residentId);
    if (!residentOpt.isPresent()) return;
    
    Resident resident = residentOpt.get();
    String phoneNumber = resident.getPhoneNumber();
    
    if (phoneNumber == null || phoneNumber.trim().isEmpty()) return;
    
    String template = DatabaseHelper.getSMSTemplate("Document Ready");
    if (template == null) {
        template = "Your {document_type} is now ready for pickup. Please bring a valid ID. - Barangay San Isidro";
    }
    
    String message = template.replace("{document_type}", documentType);
    
    SMSService.sendSMS(phoneNumber, message);
}
```

### Integration Example: Document Approval Button
```java
// In your document request approval handler
approveButton.setOnAction(e -> {
    int requestId = selectedRequest.getId();
    int residentId = selectedRequest.getResidentId();
    String documentType = selectedRequest.getDocumentType();
    
    // Update database
    DatabaseHelper.updateDocumentRequestStatus(requestId, "APPROVED");
    
    // Send SMS notification
    sendDocumentApprovedSMS(residentId, documentType);
    
    // Refresh UI
    refreshDocumentRequestsTable();
    
    showToast("Document approved and SMS sent!");
});
```

---

## 3. Complaint Notifications

### Send "Complaint Received" SMS
```java
/**
 * Call this method when a complaint is submitted
 */
public void sendComplaintReceivedSMS(int complaintId, int residentId, String complaintTitle) {
    Optional<Resident> residentOpt = DatabaseHelper.getResidentById(residentId);
    if (!residentOpt.isPresent()) return;
    
    Resident resident = residentOpt.get();
    String phoneNumber = resident.getPhoneNumber();
    
    if (phoneNumber == null || phoneNumber.trim().isEmpty()) return;
    
    String template = DatabaseHelper.getSMSTemplate("Complaint Received");
    if (template == null) {
        template = "Your complaint \"{title}\" has been received. Reference: {complaint_id}. We will update you soon. - Barangay San Isidro";
    }
    
    String message = template
        .replace("{title}", complaintTitle)
        .replace("{complaint_id}", "CMPL-" + complaintId);
    
    SMSService.sendSMS(phoneNumber, message);
}
```

### Send "Complaint Resolved" SMS
```java
/**
 * Call this method when a complaint is resolved
 */
public void sendComplaintResolvedSMS(int residentId, String complaintTitle) {
    Optional<Resident> residentOpt = DatabaseHelper.getResidentById(residentId);
    if (!residentOpt.isPresent()) return;
    
    Resident resident = residentOpt.get();
    String phoneNumber = resident.getPhoneNumber();
    
    if (phoneNumber == null || phoneNumber.trim().isEmpty()) return;
    
    String template = DatabaseHelper.getSMSTemplate("Complaint Resolved");
    if (template == null) {
        template = "Your complaint \"{title}\" has been resolved. Thank you for your patience. - Barangay San Isidro";
    }
    
    String message = template.replace("{title}", complaintTitle);
    
    SMSService.sendSMS(phoneNumber, message);
}
```

### Integration Example: Complaint Resolution
```java
// In your complaint resolution handler
resolveButton.setOnAction(e -> {
    int complaintId = selectedComplaint.getId();
    int residentId = selectedComplaint.getResidentId();
    String title = selectedComplaint.getTitle();
    
    // Update database
    DatabaseHelper.updateComplaintStatus(complaintId, "Resolved");
    
    // Send SMS notification
    sendComplaintResolvedSMS(residentId, title);
    
    // Refresh UI
    refreshComplaintsTable();
    
    showToast("Complaint resolved and SMS sent!");
});
```

---

## 4. Announcement Broadcasts

### Send Announcement to All Residents
```java
/**
 * Send announcement SMS to all residents with phone numbers
 */
public void broadcastAnnouncementSMS(String title, String content) {
    // Get all resident phone numbers
    List<String> phoneNumbers = getAllResidentPhoneNumbers();
    
    if (phoneNumbers.isEmpty()) {
        showAlert("No Recipients", "No residents have phone numbers registered.");
        return;
    }
    
    // Get template
    String template = DatabaseHelper.getSMSTemplate("Announcement");
    if (template == null) {
        template = "{title}: {content} - Barangay San Isidro";
    }
    
    // Prepare message (truncate content if too long)
    String truncatedContent = content.length() > 100 ? content.substring(0, 97) + "..." : content;
    String message = template
        .replace("{title}", title)
        .replace("{content}", truncatedContent);
    
    // Convert to array
    String[] phoneNumberArray = phoneNumbers.toArray(new String[0]);
    
    // Send bulk SMS
    SMSService.SMSResponse response = SMSService.sendBulkSMS(phoneNumberArray, message);
    
    if (response.isSuccess()) {
        showToast("Announcement SMS sent to " + phoneNumbers.size() + " residents!");
    } else {
        showAlert("SMS Failed", "Failed to send announcement SMS: " + response.getMessage());
    }
}

/**
 * Get all resident phone numbers from database
 */
private List<String> getAllResidentPhoneNumbers() {
    List<String> phoneNumbers = new ArrayList<>();
    
    // Query all residents with phone numbers
    String sql = "SELECT phone_number FROM residents WHERE phone_number IS NOT NULL AND phone_number != ''";
    try (Connection conn = DatabaseHelper.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {
        
        while (rs.next()) {
            String phoneNumber = rs.getString("phone_number");
            if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
                phoneNumbers.add(phoneNumber);
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    
    return phoneNumbers;
}
```

### Send Emergency Alert
```java
/**
 * Send emergency alert to all residents (priority SMS)
 */
public void sendEmergencyAlert(String emergencyMessage) {
    List<String> phoneNumbers = getAllResidentPhoneNumbers();
    
    if (phoneNumbers.isEmpty()) {
        showAlert("No Recipients", "No residents have phone numbers registered.");
        return;
    }
    
    String template = DatabaseHelper.getSMSTemplate("Emergency Alert");
    if (template == null) {
        template = "EMERGENCY: {content} Please stay safe. - Barangay San Isidro";
    }
    
    String message = template.replace("{content}", emergencyMessage);
    
    // Send as priority SMS for faster delivery
    for (String phoneNumber : phoneNumbers) {
        SMSService.sendPrioritySMS(phoneNumber, message);
    }
    
    showToast("Emergency alert sent to " + phoneNumbers.size() + " residents!");
}
```

### Integration Example: Announcement with SMS Option
```java
// In your announcement creation form
CheckBox sendSMSCheckbox = new CheckBox("Send SMS to all residents");
sendSMSCheckbox.setSelected(false);

postButton.setOnAction(e -> {
    String title = titleField.getText();
    String content = contentArea.getText();
    String type = typeComboBox.getValue();
    
    // Create announcement in database
    Announcement announcement = new Announcement(
        0, title, content, type,
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
        currentUsername, "Active", null, null, 0
    );
    
    int announcementId = DatabaseHelper.createAnnouncement(announcement);
    
    // Send SMS if checkbox is selected
    if (sendSMSCheckbox.isSelected()) {
        if (type.equals("Emergency Alert")) {
            sendEmergencyAlert(content);
        } else {
            broadcastAnnouncementSMS(title, content);
        }
    }
    
    showToast("Announcement posted" + (sendSMSCheckbox.isSelected() ? " and SMS sent!" : "!"));
    refreshAnnouncementsTable();
});
```

---

## 5. OTP Verification

### Generate and Send OTP
```java
/**
 * Generate and send OTP for password reset
 */
public String sendPasswordResetOTP(String username) {
    // Get user phone number (you'll need to add phone_number to users table)
    String phoneNumber = getUserPhoneNumber(username);
    
    if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
        showAlert("No Phone Number", "No phone number registered for this user.");
        return null;
    }
    
    // Generate 6-digit OTP
    String otp = generateOTP(6);
    
    // Store OTP in database with expiry (10 minutes)
    storeOTP(username, otp, 10);
    
    // Get template
    String template = DatabaseHelper.getSMSTemplate("OTP Code");
    if (template == null) {
        template = "Your verification code is: {otp}. Valid for 10 minutes. Do not share this code. - Barangay San Isidro";
    }
    
    // Send OTP SMS
    SMSService.SMSResponse response = SMSService.sendOTP(phoneNumber, otp, template);
    
    if (response.isSuccess()) {
        showToast("OTP sent to your phone!");
        return otp;
    } else {
        showAlert("SMS Failed", "Failed to send OTP: " + response.getMessage());
        return null;
    }
}

/**
 * Generate random OTP code
 */
private String generateOTP(int length) {
    Random random = new Random();
    StringBuilder otp = new StringBuilder();
    for (int i = 0; i < length; i++) {
        otp.append(random.nextInt(10));
    }
    return otp.toString();
}

/**
 * Store OTP in database with expiry
 */
private void storeOTP(String username, String otp, int expiryMinutes) {
    String sql = "INSERT INTO otp_codes (username, otp, expiry_time) VALUES (?, ?, ?)";
    try (Connection conn = DatabaseHelper.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(expiryMinutes);
        
        pstmt.setString(1, username);
        pstmt.setString(2, otp);
        pstmt.setString(3, expiryTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        pstmt.executeUpdate();
        
    } catch (SQLException e) {
        e.printStackTrace();
    }
}

/**
 * Verify OTP code
 */
public boolean verifyOTP(String username, String otp) {
    String sql = "SELECT * FROM otp_codes WHERE username = ? AND otp = ? AND expiry_time > ?";
    try (Connection conn = DatabaseHelper.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        pstmt.setString(1, username);
        pstmt.setString(2, otp);
        pstmt.setString(3, currentTime);
        
        ResultSet rs = pstmt.executeQuery();
        
        if (rs.next()) {
            // OTP is valid, delete it
            deleteOTP(username, otp);
            return true;
        }
        
    } catch (SQLException e) {
        e.printStackTrace();
    }
    
    return false;
}

/**
 * Delete used OTP
 */
private void deleteOTP(String username, String otp) {
    String sql = "DELETE FROM otp_codes WHERE username = ? AND otp = ?";
    try (Connection conn = DatabaseHelper.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, username);
        pstmt.setString(2, otp);
        pstmt.executeUpdate();
    } catch (SQLException e) {
        e.printStackTrace();
    }
}
```

---

## 6. SMS Configuration UI

### Create SMS Configuration Panel
```java
/**
 * Create SMS configuration panel for Maintenance tab
 */
private VBox createSMSConfigurationPanel() {
    VBox panel = new VBox(16);
    panel.setPadding(new Insets(20));
    panel.getStyleClass().add("content-box");
    
    // Title
    Label titleLabel = new Label("SMS Configuration");
    titleLabel.getStyleClass().add("section-title");
    
    // API Key field
    Label apiKeyLabel = new Label("Semaphore API Key:");
    PasswordField apiKeyField = new PasswordField();
    apiKeyField.setPromptText("Enter your Semaphore API key");
    apiKeyField.setPrefWidth(400);
    
    // Load current API key (show masked)
    String currentApiKey = DatabaseHelper.getSMSApiKey();
    if (currentApiKey != null && !currentApiKey.trim().isEmpty()) {
        apiKeyField.setText(currentApiKey);
    }
    
    // Sender Name field
    Label senderNameLabel = new Label("Sender Name (max 11 characters):");
    TextField senderNameField = new TextField();
    senderNameField.setPromptText("e.g., BDMS or BrgySanIsi");
    senderNameField.setPrefWidth(200);
    
    // Load current sender name
    String currentSenderName = DatabaseHelper.getSMSSenderName();
    senderNameField.setText(currentSenderName);
    
    // Limit sender name to 11 characters
    senderNameField.textProperty().addListener((obs, oldVal, newVal) -> {
        if (newVal.length() > 11) {
            senderNameField.setText(oldVal);
        }
    });
    
    // Enable/Disable toggle
    Label enableLabel = new Label("Enable SMS Notifications:");
    CheckBox enableCheckbox = new CheckBox("Enable");
    enableCheckbox.setSelected(DatabaseHelper.isSMSEnabled());
    
    // Save button
    Button saveButton = new Button("Save Configuration");
    saveButton.getStyleClass().add("button-primary");
    saveButton.setOnAction(e -> {
        String apiKey = apiKeyField.getText().trim();
        String senderName = senderNameField.getText().trim();
        boolean enabled = enableCheckbox.isSelected();
        
        if (apiKey.isEmpty()) {
            showAlert("Validation Error", "API key is required.");
            return;
        }
        
        if (senderName.isEmpty()) {
            senderName = "BDMS";
        }
        
        // Save configuration
        DatabaseHelper.saveSMSConfig(apiKey, senderName, enabled);
        
        showToast("SMS configuration saved successfully!");
    });
    
    // Test SMS button
    Button testButton = new Button("Send Test SMS");
    testButton.getStyleClass().add("button-secondary");
    testButton.setOnAction(e -> {
        // Show dialog to enter test phone number
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Test SMS");
        dialog.setHeaderText("Send Test SMS");
        dialog.setContentText("Enter phone number:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(phoneNumber -> {
            SMSService.SMSResponse response = SMSService.sendSMS(
                phoneNumber, 
                "This is a test message from Barangay San Isidro BDMS. If you received this, SMS is working!"
            );
            
            if (response.isSuccess()) {
                showAlert("Success", "Test SMS sent successfully!\nMessage ID: " + response.getMessageId());
            } else {
                showAlert("Failed", "Failed to send test SMS:\n" + response.getMessage());
            }
        });
    });
    
    // SMS Statistics
    Label statsLabel = new Label("SMS Statistics:");
    statsLabel.getStyleClass().add("section-subtitle");
    
    Map<String, Integer> stats = DatabaseHelper.getSMSStatistics();
    int totalSent = stats.getOrDefault("SENT", 0) + 
                    stats.getOrDefault("SENT_PRIORITY", 0) + 
                    stats.getOrDefault("SENT_OTP", 0) + 
                    stats.getOrDefault("SENT_BULK", 0);
    int totalFailed = stats.getOrDefault("FAILED", 0) + stats.getOrDefault("ERROR", 0);
    
    Label sentLabel = new Label("✅ Total Sent: " + totalSent);
    Label failedLabel = new Label("❌ Total Failed: " + totalFailed);
    
    // View SMS Log button
    Button viewLogButton = new Button("View SMS Log");
    viewLogButton.getStyleClass().add("button-secondary");
    viewLogButton.setOnAction(e -> showSMSLogDialog());
    
    // Layout
    HBox buttonBox = new HBox(10, saveButton, testButton, viewLogButton);
    
    panel.getChildren().addAll(
        titleLabel,
        new Separator(),
        apiKeyLabel, apiKeyField,
        senderNameLabel, senderNameField,
        enableLabel, enableCheckbox,
        buttonBox,
        new Separator(),
        statsLabel,
        sentLabel, failedLabel
    );
    
    return panel;
}
```

---

## 7. SMS Log Viewer

### Create SMS Log Dialog
```java
/**
 * Show SMS log in a dialog
 */
private void showSMSLogDialog() {
    Stage dialog = new Stage();
    dialog.setTitle("SMS Log");
    dialog.initModality(Modality.APPLICATION_MODAL);
    
    // Create table
    TableView<SMSLogEntry> table = new TableView<>();
    table.setPrefWidth(800);
    table.setPrefHeight(500);
    
    // Columns
    TableColumn<SMSLogEntry, String> timeCol = new TableColumn<>("Time");
    timeCol.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
    timeCol.setPrefWidth(150);
    
    TableColumn<SMSLogEntry, String> phoneCol = new TableColumn<>("Phone Number");
    phoneCol.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
    phoneCol.setPrefWidth(120);
    
    TableColumn<SMSLogEntry, String> messageCol = new TableColumn<>("Message");
    messageCol.setCellValueFactory(new PropertyValueFactory<>("message"));
    messageCol.setPrefWidth(300);
    
    TableColumn<SMSLogEntry, String> statusCol = new TableColumn<>("Status");
    statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
    statusCol.setPrefWidth(100);
    
    // Color-code status
    statusCol.setCellFactory(col -> new TableCell<SMSLogEntry, String>() {
        @Override
        protected void updateItem(String status, boolean empty) {
            super.updateItem(status, empty);
            if (empty || status == null) {
                setText(null);
                setStyle("");
            } else {
                setText(status);
                if (status.startsWith("SENT")) {
                    setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                } else if (status.equals("FAILED") || status.equals("ERROR")) {
                    setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                } else {
                    setStyle("");
                }
            }
        }
    });
    
    TableColumn<SMSLogEntry, String> errorCol = new TableColumn<>("Error");
    errorCol.setCellValueFactory(new PropertyValueFactory<>("errorCode"));
    errorCol.setPrefWidth(130);
    
    table.getColumns().addAll(timeCol, phoneCol, messageCol, statusCol, errorCol);
    
    // Load data
    ObservableList<SMSLogEntry> logs = DatabaseHelper.getSMSLog(100);
    table.setItems(logs);
    
    // Refresh button
    Button refreshButton = new Button("Refresh");
    refreshButton.setOnAction(e -> {
        table.setItems(DatabaseHelper.getSMSLog(100));
    });
    
    // Close button
    Button closeButton = new Button("Close");
    closeButton.setOnAction(e -> dialog.close());
    
    HBox buttonBox = new HBox(10, refreshButton, closeButton);
    buttonBox.setPadding(new Insets(10));
    buttonBox.setAlignment(Pos.CENTER_RIGHT);
    
    VBox layout = new VBox(10, table, buttonBox);
    layout.setPadding(new Insets(10));
    
    Scene scene = new Scene(layout);
    dialog.setScene(scene);
    dialog.show();
}
```

---

## 🔧 Database Setup

### Add Phone Number Column to Residents Table
```sql
-- Add phone_number column to residents table
ALTER TABLE residents ADD COLUMN phone_number VARCHAR(20);

-- Update existing residents with phone numbers (example)
UPDATE residents SET phone_number = '09171234567' WHERE id = 1;
UPDATE residents SET phone_number = '09181234567' WHERE id = 2;
```

### Create OTP Table (for OTP verification)
```sql
CREATE TABLE IF NOT EXISTS otp_codes (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    otp VARCHAR(10) NOT NULL,
    expiry_time DATETIME NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

---

## 📝 Quick Integration Checklist

- [ ] Add phone_number column to residents table
- [ ] Add SMS configuration panel to Maintenance tab
- [ ] Add SMS notification to document approval workflow
- [ ] Add SMS notification to complaint resolution workflow
- [ ] Add SMS broadcast option to announcement creation
- [ ] Add SMS log viewer
- [ ] Test with real Semaphore API key
- [ ] Train staff on SMS configuration

---

*These code examples are ready to copy and paste into your BDMS application!*
