# 🚀 SMS Features Implementation Guide

## 📋 Changes to Make

### 1. Document Requests Tab - Add SMS Button

**Location:** `App.java` around line 1377-1424

**Add after the existing buttons:**
```java
// SMS Button for Document Requests
Button sendSMSBtn = new Button("Send SMS", new FontIcon(FontAwesomeSolid.MOBILE_ALT));
sendSMSBtn.getStyleClass().add("button-info");
sendSMSBtn.setDisable(true);
sendSMSBtn.setOnAction(e -> {
    DocumentRequest selected = documentRequestsTable.getSelectionModel().getSelectedItem();
    if (selected != null) {
        sendDocumentSMS(selected);
    }
});

// Enable SMS button when document is selected and resident has phone
documentRequestsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
    boolean isSelected = newVal != null;
    boolean hasPhone = newVal != null && getResidentPhone(newVal.getResidentId()) != null;
    sendSMSBtn.setDisable(!isSelected || !hasPhone);
    // ... existing button enable/disable logic
});

// Update toolbar
ToolBar toolBar = new ToolBar(approveBtn, paymentBtn, generateBtn, sendSMSBtn);
```

**Add SMS sending method:**
```java
private void sendDocumentSMS(DocumentRequest request) {
    // Get resident phone number
    String phone = getResidentPhone(request.getResidentId());
    if (phone == null || phone.trim().isEmpty()) {
        showAlert("No Phone Number", "This resident doesn't have a phone number registered.");
        return;
    }
    
    // Create dialog for SMS options
    Dialog<String> dialog = new Dialog<>();
    dialog.setTitle("Send SMS Notification");
    dialog.setHeaderText("Send SMS to resident about: " + request.getDocumentType());
    
    // Message templates
    ComboBox<String> templateCombo = new ComboBox<>();
    templateCombo.getItems().addAll(
        "Document Ready for Pickup",
        "Document Approved",
        "Document Rejected",
        "Custom Message"
    );
    templateCombo.setValue("Document Ready for Pickup");
    
    TextArea messageArea = new TextArea();
    messageArea.setPrefRowCount(4);
    messageArea.setWrapText(true);
    
    // Update message when template changes
    templateCombo.setOnAction(e -> {
        String template = templateCombo.getValue();
        String message = "";
        
        switch (template) {
            case "Document Ready for Pickup":
                message = String.format(
                    "Your %s has been approved and is ready for pickup at Barangay %s. " +
                    "Please bring valid ID. Office hours: Mon-Fri 8AM-5PM. Thank you!",
                    request.getDocumentType(),
                    "San Marino" // Replace with actual barangay name
                );
                break;
            case "Document Approved":
                message = String.format(
                    "Good news! Your %s request has been approved. " +
                    "Processing time: 3-5 business days. Reference: %s",
                    request.getDocumentType(),
                    request.getId()
                );
                break;
            case "Document Rejected":
                message = String.format(
                    "Your %s request has been declined. " +
                    "Please contact our office for more information. Reference: %s",
                    request.getDocumentType(),
                    request.getId()
                );
                break;
            case "Custom Message":
                message = ""; // Let user type custom message
                break;
        }
        
        messageArea.setText(message);
    });
    
    // Trigger initial message
    templateCombo.fireEvent(new ActionEvent());
    
    VBox content = new VBox(10);
    content.getChildren().addAll(
        new Label("Phone: " + phone),
        new Label("Template:"),
        templateCombo,
        new Label("Message:"),
        messageArea,
        new Label("Character count: " + messageArea.getText().length())
    );
    
    dialog.getDialogPane().setContent(content);
    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
    
    // Update character count
    messageArea.textProperty().addListener((obs, old, newVal) -> {
        Label charCount = (Label) content.getChildren().get(5);
        charCount.setText("Character count: " + newVal.length());
    });
    
    dialog.setResultConverter(button -> {
        if (button == ButtonType.OK) {
            return messageArea.getText();
        }
        return null;
    });
    
    Optional<String> result = dialog.showAndWait();
    result.ifPresent(message -> {
        if (message != null && !message.trim().isEmpty()) {
            // Send SMS
            SMSService.SMSResponse response = SMSService.sendSMS(phone, message);
            
            if (response.isSuccess()) {
                showAlert("SMS Sent", 
                    "SMS sent successfully to " + phone + "\n" +
                    "Message ID: " + response.getMessageId());
            } else {
                showAlert("SMS Failed", 
                    "Failed to send SMS: " + response.getMessage());
            }
        }
    });
}

private String getResidentPhone(int residentId) {
    String sql = "SELECT phone_number FROM residents WHERE id = ?";
    try (Connection conn = DatabaseHelper.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setInt(1, residentId);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            return rs.getString("phone_number");
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return null;
}
```

---

### 2. Complaints Tab - Add SMS Button

**Similar implementation for complaints:**

```java
// SMS Button for Complaints
Button sendComplaintSMSBtn = new Button("Send SMS", new FontIcon(FontAwesomeSolid.MOBILE_ALT));
sendComplaintSMSBtn.getStyleClass().add("button-info");
sendComplaintSMSBtn.setDisable(true);
sendComplaintSMSBtn.setOnAction(e -> {
    Complaint selected = complaintsTable.getSelectionModel().getSelectedItem();
    if (selected != null) {
        sendComplaintSMS(selected);
    }
});
```

**SMS templates for complaints:**
- "Complaint Received" - Confirmation
- "Complaint In Progress" - Status update
- "Complaint Resolved" - Resolution notification
- "Custom Message"

---

### 3. Announcements Tab - Add Broadcast Button

```java
// Broadcast SMS Button for Announcements
Button broadcastSMSBtn = new Button("Broadcast SMS", new FontIcon(FontAwesomeSolid.BULLHORN));
broadcastSMSBtn.getStyleClass().add("button-warning");
broadcastSMSBtn.setOnAction(e -> {
    Announcement selected = announcementsTable.getSelectionModel().getSelectedItem();
    if (selected != null) {
        broadcastAnnouncementSMS(selected);
    }
});
```

**Broadcast implementation:**
```java
private void broadcastAnnouncementSMS(Announcement announcement) {
    // Get all residents with phone numbers
    List<String> phoneNumbers = new ArrayList<>();
    ObservableList<Resident> residents = DatabaseHelper.getResidents(null, 0, 10000, "id", "ASC");
    
    for (Resident resident : residents) {
        String phone = resident.getPhoneNumber();
        if (phone != null && !phone.trim().isEmpty()) {
            phoneNumbers.add(phone);
        }
    }
    
    if (phoneNumbers.isEmpty()) {
        showAlert("No Recipients", "No residents have phone numbers registered.");
        return;
    }
    
    // Confirmation dialog
    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
    confirm.setTitle("Broadcast SMS");
    confirm.setHeaderText("Send SMS to " + phoneNumbers.size() + " residents?");
    
    String message = String.format(
        "Barangay Announcement: %s. %s For more info, contact our office.",
        announcement.getTitle(),
        announcement.getContent().length() > 100 ? 
            announcement.getContent().substring(0, 100) + "..." : 
            announcement.getContent()
    );
    
    TextArea messagePreview = new TextArea(message);
    messagePreview.setEditable(true);
    messagePreview.setWrapText(true);
    messagePreview.setPrefRowCount(5);
    
    VBox content = new VBox(10);
    content.getChildren().addAll(
        new Label("Recipients: " + phoneNumbers.size() + " residents"),
        new Label("Estimated cost: ₱" + (phoneNumbers.size() * 0.50)),
        new Label("Message:"),
        messagePreview
    );
    
    confirm.getDialogPane().setContent(content);
    
    Optional<ButtonType> result = confirm.showAndWait();
    if (result.isPresent() && result.get() == ButtonType.OK) {
        // Send bulk SMS
        String finalMessage = messagePreview.getText();
        String[] phoneArray = phoneNumbers.toArray(new String[0]);
        
        // Show progress
        ProgressDialog progress = new ProgressDialog("Sending SMS...", phoneNumbers.size());
        progress.show();
        
        // Send in background thread
        Task<SMSService.SMSResponse> task = new Task<>() {
            @Override
            protected SMSService.SMSResponse call() {
                return SMSService.sendBulkSMS(phoneArray, finalMessage);
            }
        };
        
        task.setOnSucceeded(e -> {
            progress.close();
            SMSService.SMSResponse response = task.getValue();
            showAlert("Broadcast Complete", response.getMessage());
        });
        
        task.setOnFailed(e -> {
            progress.close();
            showAlert("Broadcast Failed", "Failed to send SMS: " + task.getException().getMessage());
        });
        
        new Thread(task).start();
    }
}
```

---

## 🎯 Additional Features to Add

### 1. Auto-Send Toggle

Add checkbox in SMS Settings:
```java
CheckBox autoSendDocumentSMS = new CheckBox("Auto-send SMS when document is approved");
CheckBox autoSendComplaintSMS = new CheckBox("Auto-send SMS when complaint status changes");
```

Store in database:
```sql
ALTER TABLE sms_config ADD COLUMN auto_send_documents BOOLEAN DEFAULT FALSE;
ALTER TABLE sms_config ADD COLUMN auto_send_complaints BOOLEAN DEFAULT FALSE;
```

### 2. SMS History View

Add tab in each module to show SMS history:
```java
TableView<SMSLogEntry> smsHistoryTable = new TableView<>();
// Columns: Date, Phone, Message, Status, Message ID
```

Filter by resident/complaint/document:
```sql
SELECT * FROM sms_log 
WHERE phone_number = ? 
ORDER BY sent_at DESC;
```

### 3. SMS Statistics

Add to dashboard:
```java
Label todaySMSCount = new Label("SMS Today: " + getTodaySMSCount());
Label monthSMSCount = new Label("SMS This Month: " + getMonthSMSCount());
Label successRate = new Label("Success Rate: " + getSuccessRate() + "%");
```

---

## 📝 Implementation Checklist

- [ ] Add SMS button to Document Requests
- [ ] Add SMS button to Complaints
- [ ] Add Broadcast button to Announcements
- [ ] Implement sendDocumentSMS() method
- [ ] Implement sendComplaintSMS() method
- [ ] Implement broadcastAnnouncementSMS() method
- [ ] Add message templates
- [ ] Add SMS history views
- [ ] Add auto-send toggles
- [ ] Add SMS statistics
- [ ] Test all features
- [ ] Update user documentation

---

**Ready to implement? I'll start adding these features to your App.java file.**
