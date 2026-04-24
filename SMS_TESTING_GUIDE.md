# 📱 SMS System Testing Guide

**Complete Step-by-Step Testing Instructions**  
**Date:** April 21, 2026

---

## 🎯 Testing Objectives

This guide will help you:
1. ✅ Add yourself as a resident with phone number
2. ✅ Verify phone number is saved correctly
3. ✅ Send a test SMS to yourself
4. ✅ Check SMS logs
5. ✅ Test SMS templates

---

## 📋 Prerequisites

Before starting, make sure you have:
- ✅ Your Semaphore API key configured: `bec69d98d78c3d0a14f4f69a8fb5a312`
- ✅ Your Philippine mobile number (e.g., `09171234567`)
- ✅ Application compiled and ready to run
- ✅ Sufficient SMS credits in Semaphore account

---

## 🚀 Part 1: Add Yourself as a Resident

### Step 1: Run the Application

1. Open your IDE (Eclipse, IntelliJ, or VS Code)
2. Run `Launcher.java` or use Maven:
   ```bash
   mvn clean javafx:run
   ```
3. Login with your credentials:
   - Username: `superadmin`
   - Password: `admin123`

### Step 2: Navigate to Residents Tab

1. Click on **"Residents"** in the left sidebar
2. You should see the residents table with these columns:
   - Photo
   - Name
   - Birth Date
   - Gender
   - Address
   - **Phone Number** ← NEW COLUMN!

### Step 3: Add Yourself as a Resident

1. Click the **"Add Resident"** button (top left)
2. Fill in the form with YOUR information:

   ```
   ┌─────────────────────────────────────────┐
   │  Add Resident                           │
   ├─────────────────────────────────────────┤
   │                                         │
   │  Resident Photo:    [Upload Photo]     │
   │                     (Optional)          │
   │                                         │
   │  First Name:        [Your First Name]  │
   │  Middle Name:       [Your Middle Name] │
   │  Last Name:         [Your Last Name]   │
   │  Birth Date:        [Your Birth Date]  │
   │  Gender:            [Your Gender ▼]    │
   │  Address:           [Your Address]     │
   │  Phone Number:      [09171234567]      │ ← IMPORTANT!
   │                                         │
   │              [Cancel]  [Save]           │
   └─────────────────────────────────────────┘
   ```

3. **IMPORTANT:** Enter YOUR actual phone number in the format:
   - `09171234567` (11 digits starting with 09)
   - OR `+639171234567` (with country code)
   - OR `639171234567` (without +)

4. Click **"Save"**

### Step 4: Verify Your Data

1. You should see a success message: **"Resident updated successfully"**
2. Your name should appear in the residents table
3. **Check the Phone Number column** - it should show your phone number
4. If the phone number is not visible, scroll right in the table

---

## 📱 Part 2: Send Test SMS to Yourself

### Method 1: Using Database Query (Quick Test)

1. Open your database tool (H2 Console or any SQL client)
2. Connect to the database: `jdbc:h2:~/bdms_v2`
3. Run this query to get your resident ID:
   ```sql
   SELECT id, first_name, last_name, phone_number 
   FROM residents 
   WHERE phone_number IS NOT NULL
   ORDER BY id DESC
   LIMIT 1;
   ```
4. Note your `id` (e.g., `3`)

### Method 2: Add Test SMS Button to UI (Recommended)

Add this code to your `App.java` in the `showResidentControl` method, after the button definitions:

```java
// Add Test SMS Button
Button testSMSButton = new Button("Test SMS");
testSMSButton.setGraphic(new FontIcon(FontAwesomeSolid.MOBILE_ALT));
testSMSButton.getStyleClass().add("button-success");
testSMSButton.setDisable(true);

// Enable when resident is selected
residentTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
    boolean isSelected = newSelection != null;
    testSMSButton.setDisable(!isSelected || newSelection.getPhoneNumber() == null || newSelection.getPhoneNumber().isEmpty());
    // ... other button enable/disable code
});

// Test SMS Action
testSMSButton.setOnAction(e -> {
    Resident selectedResident = residentTable.getSelectionModel().getSelectedItem();
    if (selectedResident != null) {
        String phoneNumber = selectedResident.getPhoneNumber();
        
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            showAlert("No Phone Number", "This resident doesn't have a phone number.");
            return;
        }
        
        // Send test SMS
        String message = "Hello " + selectedResident.getFirstName() + "! " +
                        "This is a test message from Barangay San Isidro BDMS. " +
                        "SMS notifications are now working! 🎉";
        
        SMSService.SMSResponse response = SMSService.sendSMS(phoneNumber, message);
        
        if (response.isSuccess()) {
            showAlert("SMS Sent Successfully!", 
                "✅ SMS sent to: " + selectedResident.getFirstName() + " " + selectedResident.getLastName() + "\n" +
                "📱 Phone: " + phoneNumber + "\n" +
                "🆔 Message ID: " + response.getMessageId() + "\n\n" +
                "Check your phone for the message!");
        } else {
            showAlert("SMS Failed", 
                "❌ Failed to send SMS\n\n" +
                "Error: " + response.getMessage() + "\n" +
                "Error Code: " + response.getErrorCode() + "\n\n" +
                "Please check:\n" +
                "1. Phone number is correct\n" +
                "2. API key is valid\n" +
                "3. Semaphore account has credits");
        }
    }
});

// Add button to toolbar
HBox buttonBox = new HBox(10, addButton, importButton, editButton, deleteButton, idButton, viewIdBtn, testSMSButton);
```

### Step 5: Send Test SMS

1. **Select yourself** in the residents table (click on your row)
2. Click the **"Test SMS"** button
3. Wait for the confirmation dialog
4. **Check your phone!** You should receive an SMS within 1-5 minutes

---

## 🔍 Part 3: Verify SMS Was Sent

### Check SMS Log in Database

1. Open H2 Console or your SQL client
2. Run this query:
   ```sql
   SELECT 
       id,
       phone_number,
       message,
       status,
       message_id,
       timestamp
   FROM sms_log 
   ORDER BY timestamp DESC 
   LIMIT 10;
   ```

3. You should see your SMS with:
   - **Status:** `SENT` (if successful)
   - **Phone Number:** Your phone number
   - **Message:** The test message
   - **Message ID:** Semaphore's message ID
   - **Timestamp:** When it was sent

### Check SMS Statistics

```sql
-- Count SMS by status
SELECT status, COUNT(*) as count 
FROM sms_log 
GROUP BY status;

-- Total SMS sent today
SELECT COUNT(*) as total_sent_today
FROM sms_log 
WHERE DATE(timestamp) = CURRENT_DATE 
AND status LIKE 'SENT%';
```

---

## 📊 Part 4: Test SMS Templates

### View Available Templates

```sql
SELECT id, name, category, template 
FROM sms_templates 
ORDER BY category, name;
```

You should see 8 templates:
1. Document Approved
2. Document Ready
3. Complaint Received
4. Complaint Resolved
5. Announcement
6. Emergency Alert
7. OTP Code
8. Payment Reminder

### Test a Template

```java
// Get template
String template = DatabaseHelper.getSMSTemplate("Document Approved");

// Replace placeholders
String message = template.replace("{document_type}", "Barangay Clearance");

// Send SMS
String phoneNumber = "09171234567"; // Your phone number
SMSService.sendSMS(phoneNumber, message);
```

---

## 🎯 Part 5: Complete Testing Checklist

### Database Verification

- [ ] Phone number column exists in residents table
  ```sql
  SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
  WHERE TABLE_NAME = 'RESIDENTS' AND COLUMN_NAME = 'PHONE_NUMBER';
  ```

- [ ] SMS configuration is set
  ```sql
  SELECT * FROM sms_config;
  ```
  Should show:
  - API Key: `bec69d98d78c3d0a14f4f69a8fb5a312`
  - Sender Name: `BDMS`
  - Enabled: `TRUE`

- [ ] SMS templates are loaded
  ```sql
  SELECT COUNT(*) FROM sms_templates;
  ```
  Should return: `8`

### UI Verification

- [ ] Residents table shows Phone Number column
- [ ] Add Resident form has Phone Number field
- [ ] Edit Resident form loads phone number
- [ ] Phone number is saved when adding resident
- [ ] Phone number is updated when editing resident
- [ ] Phone number is displayed in table

### SMS Functionality

- [ ] Test SMS button works
- [ ] SMS is sent successfully
- [ ] SMS is received on your phone
- [ ] SMS log records the transaction
- [ ] Error handling works (try invalid number)

---

## 🧪 Advanced Testing Scenarios

### Test 1: Invalid Phone Number

1. Add a resident with invalid phone number: `12345`
2. Try to send SMS
3. Should show error: "Invalid Philippine mobile number"

### Test 2: Empty Phone Number

1. Add a resident without phone number
2. Try to send SMS
3. Should show error: "No phone number for resident"

### Test 3: Multiple Recipients (Bulk SMS)

```java
// Get all residents with phone numbers
List<String> phoneNumbers = new ArrayList<>();
ObservableList<Resident> residents = DatabaseHelper.getResidents(null, 0, 100, "id", "ASC");

for (Resident resident : residents) {
    if (resident.getPhoneNumber() != null && !resident.getPhoneNumber().isEmpty()) {
        phoneNumbers.add(resident.getPhoneNumber());
    }
}

// Send bulk SMS
String[] phoneArray = phoneNumbers.toArray(new String[0]);
String message = "Test bulk SMS to all residents!";
SMSService.sendBulkSMS(phoneArray, message);
```

### Test 4: Priority SMS

```java
String phoneNumber = "09171234567";
String message = "URGENT: This is a priority message!";
SMSService.sendPrioritySMS(phoneNumber, message);
```

### Test 5: OTP SMS

```java
String phoneNumber = "09171234567";
String otp = "123456";
String template = DatabaseHelper.getSMSTemplate("OTP Code");
SMSService.sendOTP(phoneNumber, otp, template);
```

---

## 📱 Expected Results

### Successful SMS

When SMS is sent successfully, you should see:

**In Application:**
```
✅ SMS Sent Successfully!
📱 Phone: 09171234567
🆔 Message ID: 123456789
```

**On Your Phone:**
```
From: BDMS
Message: Hello Juan! This is a test message from 
Barangay San Isidro BDMS. SMS notifications are 
now working! 🎉
```

**In Database:**
```sql
id | phone_number  | status | message_id | timestamp
1  | 639171234567  | SENT   | 123456789  | 2026-04-21 16:30:00
```

---

## 🐛 Troubleshooting

### Issue: SMS Not Received

**Possible Causes:**
1. Network delay (can take up to 5 minutes)
2. Phone is off or out of coverage
3. Insufficient credits in Semaphore account
4. Invalid phone number format

**Solutions:**
1. Wait 5 minutes and check again
2. Verify phone number is correct
3. Check Semaphore account balance at [semaphore.co/dashboard](https://semaphore.co/dashboard)
4. Check SMS log for error codes

### Issue: "Invalid Philippine mobile number"

**Solution:**
- Use format: `09171234567` (11 digits)
- Must start with `09`
- Only Philippine mobile numbers are supported

### Issue: "SMS API key not configured"

**Solution:**
```sql
-- Check if API key exists
SELECT * FROM sms_config;

-- If empty, insert API key
INSERT INTO sms_config (api_key, sender_name, enabled, last_updated) 
VALUES ('bec69d98d78c3d0a14f4f69a8fb5a312', 'BDMS', TRUE, CURRENT_TIMESTAMP);
```

### Issue: "Rate limit exceeded"

**Solution:**
- Wait 1 minute (limit: 120 SMS per minute)
- Semaphore API has rate limiting

---

## 📊 Monitoring & Analytics

### Daily SMS Report

```sql
SELECT 
    DATE(timestamp) as date,
    COUNT(*) as total_sms,
    SUM(CASE WHEN status LIKE 'SENT%' THEN 1 ELSE 0 END) as successful,
    SUM(CASE WHEN status IN ('FAILED', 'ERROR') THEN 1 ELSE 0 END) as failed
FROM sms_log
WHERE timestamp >= CURRENT_DATE - 7
GROUP BY DATE(timestamp)
ORDER BY date DESC;
```

### SMS by Recipient

```sql
SELECT 
    phone_number,
    COUNT(*) as sms_count,
    MAX(timestamp) as last_sms
FROM sms_log
GROUP BY phone_number
ORDER BY sms_count DESC;
```

### Failed SMS Analysis

```sql
SELECT 
    phone_number,
    message,
    error_code,
    timestamp
FROM sms_log
WHERE status IN ('FAILED', 'ERROR')
ORDER BY timestamp DESC
LIMIT 20;
```

---

## ✅ Testing Complete!

After completing all tests, you should have:

- [x] Added yourself as a resident with phone number
- [x] Verified phone number is saved in database
- [x] Sent test SMS to yourself
- [x] Received SMS on your phone
- [x] Verified SMS log entry
- [x] Tested SMS templates
- [x] Checked SMS statistics

**Your SMS system is fully functional and ready for production use!** 🎉

---

## 🚀 Next Steps

Now that SMS is working, you can:

1. **Integrate SMS into workflows:**
   - Document approvals → Send SMS
   - Complaint resolutions → Send SMS
   - Announcements → Broadcast SMS

2. **Add more residents:**
   - Import CSV with phone numbers
   - Manually add residents

3. **Monitor SMS usage:**
   - Check Semaphore dashboard
   - Review SMS logs regularly
   - Track SMS costs

4. **Customize SMS templates:**
   - Edit templates in database
   - Add new templates
   - Personalize messages

---

**Happy Testing!** 📱✨

*Testing Guide Created: April 21, 2026*  
*SMS System Status: ✅ READY*
