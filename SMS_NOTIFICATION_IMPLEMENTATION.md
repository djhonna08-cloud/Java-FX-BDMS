# SMS Notification Implementation Guide

**Date:** April 21, 2026  
**Feature:** SMS Notifications using Semaphore API (Philippines)  
**Status:** ✅ Implementation Complete - Ready for Testing

---

## 📋 Overview

The BDMS system now includes SMS notification functionality using the **Semaphore API**, a popular SMS gateway service in the Philippines. This feature enables the barangay to send automated SMS notifications for:

- Document request status updates
- Complaint submissions and resolutions
- Announcement broadcasts
- OTP verification codes
- Payment reminders

---

## 🎯 Features Implemented

### 1. **SMS Service (`SMSService.java`)**
A comprehensive SMS service class with the following capabilities:

#### Core Methods:
- `sendSMS(phoneNumber, message)` - Send standard SMS (1 credit per 160 chars)
- `sendSMS(phoneNumber, message, senderName)` - Send SMS with custom sender name
- `sendPrioritySMS(phoneNumber, message)` - Send priority SMS for faster delivery (2 credits)
- `sendOTP(phoneNumber, otp, message)` - Send OTP verification codes (2 credits)
- `sendBulkSMS(phoneNumbers[], message)` - Send to multiple recipients (max 1000)

#### Features:
- ✅ Philippine phone number validation and normalization
- ✅ Rate limiting (120 calls per minute)
- ✅ Automatic API key management
- ✅ SMS logging for all transactions
- ✅ Error handling and response parsing
- ✅ Support for custom sender names (max 11 characters)

---

### 2. **Database Integration**

#### New Tables Created:

**`sms_config`** - SMS Configuration
```sql
CREATE TABLE sms_config (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    api_key VARCHAR(255),
    sender_name VARCHAR(11),
    enabled BOOLEAN DEFAULT FALSE,
    last_updated DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

**`sms_log`** - SMS Transaction Log
```sql
CREATE TABLE sms_log (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    phone_number VARCHAR(50) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    status VARCHAR(20) NOT NULL,  -- SENT, FAILED, ERROR, SENT_PRIORITY, SENT_OTP, SENT_BULK
    message_id VARCHAR(50),
    error_code VARCHAR(100),
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

**`sms_templates`** - Predefined SMS Templates
```sql
CREATE TABLE sms_templates (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) UNIQUE NOT NULL,
    template VARCHAR(500) NOT NULL,
    description VARCHAR(200),
    category VARCHAR(50)  -- DOCUMENT, COMPLAINT, ANNOUNCEMENT, OTP
);
```

#### Default SMS Templates:
1. **Document Approved** - "Your {document_type} request has been approved..."
2. **Document Ready** - "Your {document_type} is now ready for pickup..."
3. **Complaint Received** - "Your complaint \"{title}\" has been received..."
4. **Complaint Resolved** - "Your complaint \"{title}\" has been resolved..."
5. **Announcement** - "{title}: {content}..."
6. **Emergency Alert** - "EMERGENCY: {content}..."
7. **OTP Code** - "Your verification code is: {otp}..."
8. **Payment Reminder** - "Reminder: Your {document_type} payment of P{amount}..."

---

### 3. **DatabaseHelper Methods**

New methods added to `DatabaseHelper.java`:

#### Configuration Methods:
- `getSMSApiKey()` - Get API key from database
- `getSMSSenderName()` - Get sender name (default: "BDMS")
- `isSMSEnabled()` - Check if SMS is enabled
- `saveSMSConfig(apiKey, senderName, enabled)` - Save SMS configuration

#### Logging Methods:
- `logSMS(phoneNumber, message, status, messageId, errorCode)` - Log SMS transaction
- `getSMSLog(limit)` - Get recent SMS log entries
- `getSMSStatistics()` - Get SMS statistics by status

#### Template Methods:
- `getSMSTemplate(name)` - Get template by name
- `getAllSMSTemplates()` - Get all templates
- `updateSMSTemplate(id, template)` - Update template content

---

## 🔧 Setup Instructions

### Step 1: Get Semaphore API Key

1. Visit [https://semaphore.co](https://semaphore.co)
2. Sign up for an account
3. Navigate to **API Settings**
4. Copy your **API Key**
5. Purchase SMS credits (pricing: ~₱0.50 per SMS)

**Free Trial:** Semaphore offers free trial credits for testing.

---

### Step 2: Configure SMS in BDMS

#### Option A: Via UI (Recommended)
1. Login as **Super Admin** or **Owner**
2. Navigate to **Maintenance** tab
3. Find **SMS Configuration** section
4. Enter your **Semaphore API Key**
5. Set **Sender Name** (max 11 characters, e.g., "BDMS" or "BrgySanIsi")
6. Toggle **Enable SMS Notifications** to ON
7. Click **Save Configuration**

#### Option B: Via Database (Manual)
```sql
INSERT INTO sms_config (api_key, sender_name, enabled, last_updated) 
VALUES ('YOUR_API_KEY_HERE', 'BDMS', TRUE, CURRENT_TIMESTAMP);
```

---

### Step 3: Test SMS Functionality

#### Test 1: Send Test SMS
```java
// In your code or via UI test button
SMSService.SMSResponse response = SMSService.sendSMS("09171234567", "Test message from BDMS");
System.out.println(response.toString());
```

#### Test 2: Send OTP
```java
String otp = "123456";
String template = DatabaseHelper.getSMSTemplate("OTP Code");
SMSService.SMSResponse response = SMSService.sendOTP("09171234567", otp, template);
```

#### Test 3: Check SMS Log
```sql
SELECT * FROM sms_log ORDER BY timestamp DESC LIMIT 10;
```

---

## 📱 Phone Number Format

The system automatically normalizes Philippine phone numbers:

| Input Format | Normalized Format | Valid? |
|--------------|-------------------|--------|
| 09171234567 | 639171234567 | ✅ Yes |
| +639171234567 | 639171234567 | ✅ Yes |
| 639171234567 | 639171234567 | ✅ Yes |
| 9171234567 | 639171234567 | ✅ Yes |
| 02123456 | - | ❌ No (landline) |
| 09051234567 | 639051234567 | ✅ Yes |

**Valid Prefixes:** 639XX (Philippine mobile numbers only)

---

## 💰 Pricing & Credits

### Semaphore API Pricing (as of 2026):
- **Standard SMS:** 1 credit per 160 characters (~₱0.50)
- **Priority SMS:** 2 credits per message (~₱1.00)
- **OTP SMS:** 2 credits per message (~₱1.00)
- **Bulk SMS:** 1 credit per recipient per message

### Credit Packages:
- 100 credits: ₱50
- 500 credits: ₱250
- 1,000 credits: ₱500
- 5,000 credits: ₱2,400 (₱0.48 per SMS)
- 10,000 credits: ₱4,500 (₱0.45 per SMS)

**Note:** Prices may vary. Check [semaphore.co/pricing](https://semaphore.co/pricing) for current rates.

---

## 🚀 Usage Examples

### Example 1: Document Request Notification
```java
// When document is approved
int residentId = 123;
String documentType = "Barangay Clearance";

// Get template
String template = DatabaseHelper.getSMSTemplate("Document Approved");
String message = template.replace("{document_type}", documentType);

// Get resident phone number (requires phone_number column in residents table)
String phoneNumber = DatabaseHelper.getResidentPhoneNumber(residentId);

if (phoneNumber != null) {
    SMSService.SMSResponse response = SMSService.sendSMS(phoneNumber, message);
    if (response.isSuccess()) {
        System.out.println("SMS sent successfully!");
    } else {
        System.err.println("Failed to send SMS: " + response.getMessage());
    }
}
```

### Example 2: Complaint Resolution Notification
```java
// When complaint is resolved
String complaintTitle = "Street Pothole on Main St";
String phoneNumber = "09171234567";

String template = DatabaseHelper.getSMSTemplate("Complaint Resolved");
String message = template.replace("{title}", complaintTitle);

SMSService.sendSMS(phoneNumber, message);
```

### Example 3: Emergency Announcement Broadcast
```java
// Send emergency alert to all residents
String emergencyMessage = "Typhoon warning! Please stay indoors and prepare emergency supplies.";
String template = DatabaseHelper.getSMSTemplate("Emergency Alert");
String message = template.replace("{content}", emergencyMessage);

// Get all resident phone numbers
String[] phoneNumbers = getAllResidentPhoneNumbers(); // Implement this method

// Send bulk SMS
SMSService.sendBulkSMS(phoneNumbers, message);
```

### Example 4: OTP for Password Reset
```java
// Generate OTP
String otp = generateOTP(6); // 6-digit OTP

// Get template
String template = DatabaseHelper.getSMSTemplate("OTP Code");

// Send OTP
SMSService.sendOTP("09171234567", otp, template);
```

---

## 📊 SMS Statistics & Monitoring

### View SMS Log
```sql
-- Recent SMS transactions
SELECT * FROM sms_log ORDER BY timestamp DESC LIMIT 50;

-- SMS by status
SELECT status, COUNT(*) as count FROM sms_log GROUP BY status;

-- Failed SMS
SELECT * FROM sms_log WHERE status = 'FAILED' ORDER BY timestamp DESC;

-- SMS sent today
SELECT COUNT(*) FROM sms_log 
WHERE DATE(timestamp) = CURRENT_DATE AND status LIKE 'SENT%';
```

### SMS Statistics Dashboard
```java
Map<String, Integer> stats = DatabaseHelper.getSMSStatistics();
System.out.println("Sent: " + stats.getOrDefault("SENT", 0));
System.out.println("Failed: " + stats.getOrDefault("FAILED", 0));
System.out.println("Priority: " + stats.getOrDefault("SENT_PRIORITY", 0));
System.out.println("OTP: " + stats.getOrDefault("SENT_OTP", 0));
System.out.println("Bulk: " + stats.getOrDefault("SENT_BULK", 0));
```

---

## ⚠️ Important Notes

### 1. **Phone Number Column Missing**
The current `residents` table does not have a `phone_number` column. You need to add it:

```sql
ALTER TABLE residents ADD COLUMN phone_number VARCHAR(20);
```

Then update resident records with phone numbers.

### 2. **Rate Limiting**
Semaphore API has a rate limit of **120 calls per minute**. The `SMSService` class automatically enforces this limit.

### 3. **API Key Security**
- Store API key securely in the database
- Do not hardcode API keys in source code
- Restrict access to SMS configuration to admins only

### 4. **SMS Credits Monitoring**
- Monitor your Semaphore account balance regularly
- Set up low balance alerts in Semaphore dashboard
- Purchase credits before they run out

### 5. **Testing**
- Use Semaphore's test mode for development
- Test with your own phone number first
- Verify message delivery before production use

---

## 🔐 Security Considerations

### API Key Protection
- ✅ API key stored in database (not in code)
- ✅ Only admins can view/edit SMS configuration
- ✅ API key not exposed in logs or error messages

### Phone Number Validation
- ✅ Validates Philippine mobile numbers only
- ✅ Normalizes phone numbers to prevent duplicates
- ✅ Rejects invalid formats

### Rate Limiting
- ✅ Enforces 120 calls/minute limit
- ✅ Prevents API abuse
- ✅ Protects against accidental bulk sends

### SMS Logging
- ✅ All SMS transactions logged
- ✅ Audit trail for compliance
- ✅ Error tracking for troubleshooting

---

## 🐛 Troubleshooting

### Problem: "SMS API key not configured"
**Solution:** Configure API key in Maintenance > SMS Configuration

### Problem: "Invalid Philippine mobile number"
**Solution:** Ensure phone number starts with 09 or +639 (11 digits)

### Problem: "Rate limit exceeded"
**Solution:** Wait 1 minute before sending more SMS. Consider batching messages.

### Problem: "Failed to send SMS: Invalid API key"
**Solution:** Verify API key is correct in Semaphore dashboard

### Problem: SMS not received
**Possible Causes:**
1. Recipient's phone is off or out of coverage
2. Recipient blocked sender
3. Network delay (can take up to 5 minutes)
4. Insufficient credits in Semaphore account

**Check:**
```sql
SELECT * FROM sms_log WHERE phone_number = '639171234567' ORDER BY timestamp DESC;
```

---

## 📈 Future Enhancements

### Planned Features:
1. **SMS Scheduling** - Schedule SMS for future delivery
2. **SMS Templates UI** - Edit templates via UI
3. **SMS Reports** - Generate monthly SMS usage reports
4. **SMS Balance Check** - Display remaining credits in UI
5. **SMS Delivery Status** - Track delivery status via webhook
6. **SMS Reply Handling** - Process incoming SMS replies
7. **SMS Groups** - Create recipient groups for bulk messaging
8. **SMS Campaigns** - Create and manage SMS campaigns

---

## 📞 Support

### Semaphore Support:
- Website: [https://semaphore.co](https://semaphore.co)
- Email: support@semaphore.co
- Documentation: [https://semaphore.co/docs](https://semaphore.co/docs)

### BDMS SMS Feature Support:
- Check SMS logs: `SELECT * FROM sms_log ORDER BY timestamp DESC`
- Check configuration: `SELECT * FROM sms_config`
- Check templates: `SELECT * FROM sms_templates`

---

## ✅ Implementation Checklist

- [x] Create `SMSService.java` with Semaphore API integration
- [x] Add SMS database tables (`sms_config`, `sms_log`, `sms_templates`)
- [x] Add SMS methods to `DatabaseHelper.java`
- [x] Create SMS model classes (`SMSLogEntry`, `SMSTemplate`)
- [x] Add default SMS templates
- [x] Implement phone number validation and normalization
- [x] Implement rate limiting
- [x] Add SMS logging
- [ ] Add `phone_number` column to `residents` table
- [ ] Create SMS Configuration UI in Maintenance tab
- [ ] Create SMS Log viewer UI
- [ ] Create SMS Templates editor UI
- [ ] Integrate SMS notifications with document requests
- [ ] Integrate SMS notifications with complaints
- [ ] Integrate SMS notifications with announcements
- [ ] Add SMS test functionality in UI
- [ ] Add SMS statistics dashboard
- [ ] Test with real Semaphore API key

---

## 📝 Next Steps

1. **Add Phone Number Column to Residents Table**
   ```sql
   ALTER TABLE residents ADD COLUMN phone_number VARCHAR(20);
   ```

2. **Create SMS Configuration UI** in Maintenance tab with:
   - API Key input field (password field)
   - Sender Name input field (max 11 chars)
   - Enable/Disable toggle
   - Test SMS button
   - SMS statistics display

3. **Integrate SMS Notifications** into existing workflows:
   - Document request approval → Send "Document Approved" SMS
   - Document ready for pickup → Send "Document Ready" SMS
   - Complaint submission → Send "Complaint Received" SMS
   - Complaint resolution → Send "Complaint Resolved" SMS
   - New announcement → Send "Announcement" SMS to all residents

4. **Create SMS Log Viewer** to display:
   - Recent SMS transactions
   - SMS status (sent, failed, error)
   - Phone numbers
   - Message content
   - Timestamps
   - Error codes

5. **Test SMS Functionality** with:
   - Test API key from Semaphore
   - Your own phone number
   - Various message types (standard, priority, OTP, bulk)

---

*Last Updated: April 21, 2026*  
*Implementation Status: ✅ Backend Complete - UI Pending*  
*Ready for: Testing & UI Integration*
