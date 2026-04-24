# 🚀 UniSMS API Setup Guide

**Quick setup guide for configuring UniSMS API in your BDMS**

---

## ✅ Prerequisites

- [x] You have a UniSMS API key (starts with `sk_`)
- [x] Your code is already configured for UniSMS API
- [x] Database tables are created

---

## 📝 Step-by-Step Setup

### Step 1: Update Database Configuration

**Option A: Using SQL Script**

1. Open `configure_unisms.sql`
2. Replace `YOUR_UNISMS_API_KEY` with your actual API key
3. Run the script in your database

**Option B: Using H2 Console**

1. Open H2 Console: http://localhost:8082
2. Connect to: `jdbc:h2:~/bdms_v2`
3. Run this command (replace with your actual key):

```sql
DELETE FROM sms_config;

INSERT INTO sms_config (api_key, api_base_url, sender_name, enabled, last_updated) 
VALUES (
    'sk_YOUR_ACTUAL_API_KEY_HERE',
    'https://unismsapi.com/api',
    'BDMS',
    TRUE,
    CURRENT_TIMESTAMP
);
```

4. Verify:
```sql
SELECT * FROM sms_config;
```

You should see:
- api_key: `sk_...` (your key)
- api_base_url: `https://unismsapi.com/api`
- sender_name: `BDMS`
- enabled: `TRUE`

---

### Step 2: Add Phone Number Column (if not exists)

Check if the column exists:
```sql
SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'RESIDENTS' AND COLUMN_NAME = 'PHONE_NUMBER';
```

If it doesn't exist, add it:
```sql
ALTER TABLE residents ADD COLUMN phone_number VARCHAR(20);
```

---

### Step 3: Test SMS Sending

**Option A: Using Test Utility (Recommended)**

1. Open `TestSMS.java`
2. Change line 35 to your phone number:
   ```java
   String testPhone = "09171234567"; // Your actual phone number
   ```
3. Compile and run:
   ```bash
   javac -cp ".;lib/*" src/main/java/com/example/TestSMS.java
   java -cp ".;lib/*;src/main/java" com.example.TestSMS
   ```

**Option B: Using Your Application**

1. Run your BDMS application
2. Go to Residents tab
3. Add yourself as a resident with your phone number
4. Use the "Test SMS" button (if available)

---

### Step 4: Verify SMS Delivery

1. **Check your phone** - SMS should arrive within 1-5 minutes

2. **Check SMS log in database:**
   ```sql
   SELECT 
       id,
       phone_number,
       message,
       status,
       message_id,
       sent_at
   FROM sms_log 
   ORDER BY sent_at DESC 
   LIMIT 5;
   ```

3. **Expected result:**
   - Status: `SENT`
   - Message ID: `msg_xxxxx` (from UniSMS)
   - Phone Number: Your number in format `639XXXXXXXXX`

---

## 🔍 Troubleshooting

### Issue: "SMS API key not configured"

**Solution:**
```sql
-- Check if config exists
SELECT * FROM sms_config;

-- If empty, insert your key
INSERT INTO sms_config (api_key, api_base_url, sender_name, enabled, last_updated) 
VALUES ('sk_YOUR_KEY', 'https://unismsapi.com/api', 'BDMS', TRUE, CURRENT_TIMESTAMP);
```

---

### Issue: "Invalid Philippine mobile number"

**Valid formats:**
- ✅ `09171234567` (11 digits)
- ✅ `+639171234567` (with country code)
- ✅ `639171234567` (without +)

**Invalid formats:**
- ❌ `9171234567` (missing 0 or 63)
- ❌ `12345` (too short)
- ❌ `08171234567` (must start with 09)

---

### Issue: "Failed to send SMS" or API Error

**Check these:**

1. **API Key is correct:**
   - Must start with `sk_`
   - Copy exactly from UniSMS dashboard
   - No extra spaces

2. **UniSMS account has credits:**
   - Login to https://unismsapi.com
   - Check your account balance
   - Purchase credits if needed

3. **API endpoint is correct:**
   ```sql
   SELECT api_base_url FROM sms_config;
   ```
   Should be: `https://unismsapi.com/api`

4. **SMS is enabled:**
   ```sql
   SELECT enabled FROM sms_config;
   ```
   Should be: `TRUE`

5. **Check error in SMS log:**
   ```sql
   SELECT phone_number, status, error_code, sent_at 
   FROM sms_log 
   WHERE status IN ('FAILED', 'ERROR')
   ORDER BY sent_at DESC;
   ```

---

### Issue: SMS not received on phone

**Possible causes:**

1. **Network delay** - Can take up to 5 minutes
   - Wait and check again

2. **Phone is off or out of coverage**
   - Check if phone is on and has signal

3. **Number format issue**
   - Check SMS log for the exact number sent
   - Verify it matches your phone number

4. **UniSMS service issue**
   - Check UniSMS status page
   - Contact UniSMS support

---

## 📊 Testing Checklist

- [ ] API key configured in database
- [ ] API base URL is `https://unismsapi.com/api`
- [ ] SMS is enabled (`enabled = TRUE`)
- [ ] Phone number column exists in residents table
- [ ] Test SMS sent successfully
- [ ] SMS received on phone
- [ ] SMS log shows `SENT` status
- [ ] Message ID recorded in log

---

## 🎯 Quick Test Commands

### Check Configuration
```sql
SELECT 
    CONCAT(SUBSTRING(api_key, 1, 6), '...', SUBSTRING(api_key, -4)) as masked_key,
    api_base_url,
    sender_name,
    enabled
FROM sms_config;
```

### View Recent SMS
```sql
SELECT 
    phone_number,
    LEFT(message, 50) as message_preview,
    status,
    sent_at
FROM sms_log 
ORDER BY sent_at DESC 
LIMIT 10;
```

### Count SMS by Status
```sql
SELECT 
    status,
    COUNT(*) as count
FROM sms_log
GROUP BY status;
```

---

## 📱 UniSMS API Features

Your code already supports:

✅ **Single SMS** - Send to one recipient
```java
SMSService.sendSMS("09171234567", "Your message");
```

✅ **Bulk SMS** - Send to multiple recipients
```java
String[] phones = {"09171234567", "09181234567"};
SMSService.sendBulkSMS(phones, "Bulk message");
```

✅ **Priority SMS** - Urgent messages
```java
SMSService.sendPrioritySMS("09171234567", "Urgent message");
```

✅ **OTP SMS** - One-time passwords
```java
SMSService.sendOTP("09171234567", "123456", "Your OTP is {otp}");
```

✅ **SMS Templates** - Pre-configured messages
```java
String template = DatabaseHelper.getSMSTemplate("Document Approved");
String message = template.replace("{document_type}", "Clearance");
SMSService.sendSMS("09171234567", message);
```

---

## 💰 UniSMS Pricing

Check current pricing at: https://unismsapi.com/pricing

Typical rates:
- Standard SMS: ~₱0.50-1.00 per message
- Bulk discounts available
- Pay-as-you-go or monthly plans

---

## 🔐 Security Notes

✅ API key is stored in database (not in code)  
✅ API key is masked in logs  
✅ Only Philippine mobile numbers accepted  
✅ Rate limiting enabled (1 message per second)  
✅ All SMS transactions logged  

---

## 📞 Support

**UniSMS Support:**
- Website: https://unismsapi.com
- Documentation: https://unismsapi.com/api (the docs you provided)
- Check your dashboard for account status

**BDMS SMS Support:**
- Check SMS log: `SELECT * FROM sms_log ORDER BY sent_at DESC`
- Review error codes in log
- Test with `TestSMS.java` utility

---

## ✅ You're Ready!

Once you complete the setup:

1. ✅ API key configured
2. ✅ Test SMS sent and received
3. ✅ SMS log shows successful delivery

**Next steps:**
- Add phone numbers to residents
- Integrate SMS into document workflow
- Set up SMS notifications for complaints
- Create announcement broadcast feature

---

**Happy texting!** 📱✨

*Setup Guide Created: April 21, 2026*  
*UniSMS API Version: Latest*
