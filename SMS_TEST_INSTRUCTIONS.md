# 📱 SMS Test Instructions

## ⚠️ Important: Database is Currently In Use

Your BDMS application is currently running and has locked the database. You have two options:

---

## Option 1: Update via Application (Recommended)

### Step 1: Open Database Console in Your Application

1. Run your BDMS application
2. Look for a "Database Console" or "SQL Console" option in the Maintenance tab
3. Or access H2 Console at: http://localhost:8082 (if enabled)

### Step 2: Run This SQL

```sql
DELETE FROM sms_config;

INSERT INTO sms_config (api_key, api_base_url, sender_name, enabled, last_updated) 
VALUES (
    'sk_6bb6f6f6-391a-4b34-8146-d38bacf73d4b',
    'https://unismsapi.com/api',
    'BDMS',
    TRUE,
    CURRENT_TIMESTAMP
);

SELECT * FROM sms_config;
```

### Step 3: Test SMS in Your Application

1. Go to Residents tab
2. Add a resident with phone number: `09563052862`
3. Click "Test SMS" button
4. Check console output for API response
5. Check your phone for SMS (arrives in 1-5 minutes)

---

## Option 2: Close Application and Run Test Utility

### Step 1: Close Your BDMS Application

Make sure the application is completely closed.

### Step 2: Run Update Script

Double-click: `UpdateSMSConfig.class` or run:

```bash
java -cp ".;C:\Users\Razza\.m2\repository\com\h2database\h2\2.1.214\h2-2.1.214.jar" UpdateSMSConfig
```

This will update the database with your API key.

### Step 3: Run Test Utility

Double-click: `test-sms.bat` or run:

```bash
java -cp ".;target/classes;C:\Users\Razza\.m2\repository\com\h2database\h2\2.1.214\h2-2.1.214.jar" com.example.TestUniSMS
```

### Step 4: Check Results

**Console Output:**
```
=== SMS API Request ===
URL: https://unismsapi.com/api/sms
Response Code: 201
Response Body: {"message":{"status":"sent",...}}
======================

🎉 SUCCESS! SMS sent successfully!
```

**Your Phone:**
You should receive an SMS within 1-5 minutes.

**Database:**
```sql
SELECT * FROM sms_log ORDER BY sent_at DESC LIMIT 1;
```
Should show status: `SENT`

---

## 🔍 What to Look For

### Success Indicators:

✅ Console shows: `Response Code: 201`  
✅ Console shows: `"status":"sent"`  
✅ Message ID returned: `msg_xxxxx`  
✅ SMS received on phone  
✅ Database log shows: `status = 'SENT'`

### Error Indicators:

❌ Response Code: `401` → Invalid API key  
❌ Response Code: `402` → No credits  
❌ Response Code: `422` → Invalid phone number  
❌ Response Code: `500` → Server error

---

## 📊 Debug Output

With the updated code, you'll see detailed debug output:

```
=== SMS API Request ===
URL: https://unismsapi.com/api/sms
API Key: sk_6bb6f6f...73d4b
Request Body: {"recipient":"+639563052862","content":"Hello from BDMS!..."}
Response Code: 201
Response Body: {"message":{"status":"sent","reference_id":"msg_abc123",...}}
======================
```

This shows:
- Exact URL being called
- Masked API key
- Request body sent
- HTTP response code
- Full API response

---

## 🎯 Quick Test (If Application is Running)

If your application is running and has SMS testing built-in:

1. Go to Maintenance → SMS Settings
2. Update API Key: `sk_6bb6f6f6-391a-4b34-8146-d38bacf73d4b`
3. Update API URL: `https://unismsapi.com/api`
4. Enable SMS: ✓
5. Click "Save"
6. Click "Test SMS"
7. Enter phone: `09563052862`
8. Click "Send"
9. Check console for debug output
10. Check phone for SMS

---

## 📞 Troubleshooting

### Database Locked Error

**Error:** `Database may be already in use`

**Solution:**
- Close your BDMS application completely
- Wait 5 seconds
- Run the test utility again

### API Key Not Working

**Error:** `401 Unauthorized`

**Solution:**
1. Verify API key at: https://unismsapi.com/dashboard
2. Make sure you copied the full key: `sk_6bb6f6f6-391a-4b34-8146-d38bacf73d4b`
3. Check for extra spaces or typos

### No Credits

**Error:** `402 Payment Required`

**Solution:**
1. Login to https://unismsapi.com
2. Go to Billing → Add Credits
3. Purchase SMS credits

### Phone Number Invalid

**Error:** `422 Unprocessable Entity`

**Solution:**
- Use format: `09563052862` (11 digits)
- Must be Philippine mobile number
- Must start with `09`

---

## ✅ Files Created

- `UpdateSMSConfig.java` - Updates database configuration
- `UpdateSMSConfig.class` - Compiled version
- `test-sms.bat` - Easy test script
- `RUN_THIS_SQL.txt` - SQL to run manually
- `SMS_TEST_INSTRUCTIONS.md` - This file

---

## 🚀 Next Steps

Once SMS works:

1. ✅ Test with your phone number
2. ✅ Verify SMS received
3. ✅ Check SMS log in database
4. ✅ Add phone numbers to residents
5. ✅ Integrate into document workflow
6. ✅ Set up complaint notifications
7. ✅ Create announcement broadcasts

---

**Choose Option 1 if your application is running, or Option 2 if you can close it.**
