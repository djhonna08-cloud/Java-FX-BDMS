# 🚀 Quick Test Steps - Add Yourself & Send SMS

**Super Quick Guide - 5 Minutes**

---

## ✅ Step 1: Run Application (30 seconds)

```bash
# Run your application
mvn clean javafx:run
```

**Login:**
- Username: `superadmin`
- Password: `admin123`

---

## ✅ Step 2: Add Yourself as Resident (2 minutes)

1. Click **"Residents"** in left sidebar
2. Click **"Add Resident"** button
3. Fill in YOUR information:
   - First Name: `[Your Name]`
   - Last Name: `[Your Last Name]`
   - Birth Date: `[Your Birth Date]`
   - Gender: `[Your Gender]`
   - Address: `[Your Address]`
   - **Phone Number: `09171234567`** ← YOUR ACTUAL NUMBER!
4. Click **"Save"**

---

## ✅ Step 3: Verify Phone Number Saved (30 seconds)

**Check in Table:**
- Look at the residents table
- Find the **"Phone Number"** column (scroll right if needed)
- Your phone number should be visible

**Check in Database:**
```sql
SELECT id, first_name, last_name, phone_number 
FROM residents 
WHERE phone_number IS NOT NULL;
```

---

## ✅ Step 4: Send Test SMS (1 minute)

### Option A: Add Test Button (Recommended)

Add this code to `App.java` after line 1650:

```java
Button testSMSButton = new Button("Test SMS");
testSMSButton.setGraphic(new FontIcon(FontAwesomeSolid.MOBILE_ALT));
testSMSButton.setOnAction(e -> {
    Resident selected = residentTable.getSelectionModel().getSelectedItem();
    if (selected != null && selected.getPhoneNumber() != null) {
        String msg = "Hello " + selected.getFirstName() + "! SMS is working! 🎉";
        SMSService.SMSResponse response = SMSService.sendSMS(selected.getPhoneNumber(), msg);
        showAlert(response.isSuccess() ? "Success!" : "Failed", 
                 response.isSuccess() ? "Check your phone!" : response.getMessage());
    }
});

// Add to button box
HBox buttonBox = new HBox(10, addButton, importButton, editButton, deleteButton, 
                         idButton, viewIdBtn, testSMSButton);
```

**Then:**
1. Select yourself in the table
2. Click **"Test SMS"** button
3. Wait for confirmation
4. **Check your phone!**

### Option B: Quick Java Code Test

Add this anywhere in your code (e.g., in a button click):

```java
// Replace with your phone number
String phoneNumber = "09171234567";
String message = "Test from BDMS! SMS is working! 🎉";

SMSService.SMSResponse response = SMSService.sendSMS(phoneNumber, message);

if (response.isSuccess()) {
    System.out.println("✅ SMS Sent! Message ID: " + response.getMessageId());
} else {
    System.out.println("❌ Failed: " + response.getMessage());
}
```

---

## ✅ Step 5: Verify SMS Received (1 minute)

**Check Your Phone:**
- You should receive SMS within 1-5 minutes
- From: **BDMS**
- Message: "Hello [Your Name]! SMS is working! 🎉"

**Check SMS Log:**
```sql
SELECT * FROM sms_log ORDER BY timestamp DESC LIMIT 5;
```

Should show:
- Status: `SENT`
- Phone: `639171234567`
- Message ID: `[Semaphore ID]`

---

## 🎉 Success Indicators

✅ **Application Running** - Login successful  
✅ **Resident Added** - Your name in table  
✅ **Phone Number Visible** - Shows in Phone Number column  
✅ **SMS Sent** - Success message displayed  
✅ **SMS Received** - Message on your phone  
✅ **SMS Logged** - Entry in sms_log table  

---

## 🐛 Quick Troubleshooting

**SMS Not Received?**
1. Wait 5 minutes (network delay)
2. Check phone number format: `09171234567`
3. Check Semaphore credits: [semaphore.co/dashboard](https://semaphore.co/dashboard)
4. Check SMS log for errors:
   ```sql
   SELECT * FROM sms_log WHERE status = 'FAILED';
   ```

**Phone Number Not Showing in Table?**
1. Scroll right in the table
2. Check if column was added:
   ```sql
   SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
   WHERE TABLE_NAME = 'RESIDENTS' AND COLUMN_NAME = 'PHONE_NUMBER';
   ```

**API Key Not Working?**
```sql
-- Check API key
SELECT * FROM sms_config;

-- Should show:
-- api_key: bec69d98d78c3d0a14f4f69a8fb5a312
-- enabled: TRUE
```

---

## 📱 Test SMS Examples

### Test 1: Simple Message
```java
SMSService.sendSMS("09171234567", "Hello from BDMS!");
```

### Test 2: Using Template
```java
String template = DatabaseHelper.getSMSTemplate("Document Approved");
String message = template.replace("{document_type}", "Barangay Clearance");
SMSService.sendSMS("09171234567", message);
```

### Test 3: Priority SMS
```java
SMSService.sendPrioritySMS("09171234567", "URGENT: Test message!");
```

---

## ✅ Quick Verification Queries

```sql
-- 1. Check phone number saved
SELECT id, first_name, last_name, phone_number FROM residents;

-- 2. Check SMS config
SELECT * FROM sms_config;

-- 3. Check SMS log
SELECT * FROM sms_log ORDER BY timestamp DESC LIMIT 5;

-- 4. Check SMS templates
SELECT name, category FROM sms_templates;

-- 5. Count SMS sent today
SELECT COUNT(*) FROM sms_log 
WHERE DATE(timestamp) = CURRENT_DATE AND status LIKE 'SENT%';
```

---

## 🎯 Expected Timeline

| Step | Time | Status |
|------|------|--------|
| Run application | 30s | ⏱️ |
| Add yourself | 2m | ⏱️ |
| Verify saved | 30s | ⏱️ |
| Send SMS | 1m | ⏱️ |
| Receive SMS | 1-5m | ⏱️ |
| **Total** | **5-9 minutes** | ✅ |

---

## 📞 Support

**Semaphore Dashboard:** [semaphore.co/dashboard](https://semaphore.co/dashboard)  
**API Docs:** [semaphore.co/docs](https://semaphore.co/docs)  
**Check Credits:** Login to Semaphore dashboard

---

**That's it! Your SMS system is ready to use!** 🎉

*Quick Guide Created: April 21, 2026*
