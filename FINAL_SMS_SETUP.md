# 🚀 Final SMS Setup - UniSMS API

**Your API Key:** `sk_6bb6f6f6-391a-4b34-8146-d38bacf73d4b`  
**Status:** Ready to configure and test

---

## ✅ Step 1: Update Database (30 seconds)

Run this SQL script:

```bash
# Open your database and run:
test_unisms_complete.sql
```

Or manually:

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

-- Verify
SELECT * FROM sms_config;
```

**Expected result:**
- api_key: `sk_6bb6f6f6-391a-4b34-8146-d38bacf73d4b`
- api_base_url: `https://unismsapi.com/api`
- enabled: `TRUE`

---

## ✅ Step 2: Test SMS (1 minute)

### Option A: Using Test Utility (Recommended)

1. Edit `src/main/java/com/example/TestUniSMS.java`
2. Line 48: Change to your phone number:
   ```java
   String testPhone = "09563052862"; // Your actual number
   ```

3. Compile and run:
   ```bash
   javac -cp ".;lib/*" src/main/java/com/example/TestUniSMS.java
   java -cp ".;lib/*;src/main/java" com.example.TestUniSMS
   ```

### Option B: Using Your Application

1. Run your BDMS application
2. Go to Residents tab
3. Add a resident with your phone number
4. Click "Test SMS" button

---

## ✅ Step 3: Verify (30 seconds)

### Check Console Output

You should see:

```
=== SMS API Request ===
URL: https://unismsapi.com/api/sms
API Key: sk_6bb6f6f...73d4b
Request Body: {"recipient":"+639563052862","content":"Hello from BDMS!..."}
Response Code: 201
Response Body: {"message":{"status":"sent","reference_id":"msg_xxxxx",...}}
======================

🎉 SUCCESS! SMS sent successfully!
```

### Check Your Phone

SMS should arrive within 1-5 minutes with message:
```
Hello from BDMS! 🎉

This is a test message from your Barangay Document Management System.

If you receive this, your UniSMS integration is working perfectly!

Powered by UniSMS API
```

### Check Database

```sql
SELECT * FROM sms_log ORDER BY sent_at DESC LIMIT 1;
```

Should show:
- status: `SENT`
- message_id: `msg_xxxxx`
- error_code: `SUCCESS` or `sent`

---

## 🔍 What the Code Does

Your `SMSService.java` is correctly configured for UniSMS API:

### 1. Authentication
```java
// Basic Auth: API_KEY as username, empty password
String authString = apiKey + ":";
String encodedAuth = Base64.getEncoder().encodeToString(authString.getBytes());
conn.setRequestProperty("Authorization", "Basic " + encodedAuth);
```
✅ Matches UniSMS documentation

### 2. Request Format
```java
// JSON body with E.164 phone format
{"recipient":"+639563052862","content":"Your message"}
```
✅ Matches UniSMS documentation

### 3. Endpoint
```java
POST https://unismsapi.com/api/sms
```
✅ Matches UniSMS documentation

### 4. Phone Number Normalization
```java
// Converts: 09563052862 → 639563052862
// Then adds + in JSON: +639563052862
```
✅ Correct E.164 format

---

## 📊 Expected API Responses

### Success (201 Created)
```json
{
  "message": {
    "status": "sent",
    "metadata": {},
    "content": "Hello from BDMS!...",
    "created": "2026-04-21T14:32:44Z",
    "reference_id": "msg_84e8b93b-6315-46af-a686",
    "recipient": "+639563052862",
    "fail_reason": null
  }
}
```

### Error (401 Unauthorized)
```json
{
  "error": "Invalid API key"
}
```

### Error (402 Payment Required)
```json
{
  "error": "Insufficient credits"
}
```

### Error (422 Unprocessable Entity)
```json
{
  "error": "Invalid recipient phone number"
}
```

---

## ❌ Troubleshooting

### Issue: "401 Unauthorized"

**Cause:** API key is invalid

**Solution:**
1. Verify key at https://unismsapi.com/dashboard
2. Make sure you copied the full key: `sk_6bb6f6f6-391a-4b34-8146-d38bacf73d4b`
3. Check for extra spaces or typos

### Issue: "402 Payment Required" or "Insufficient credits"

**Cause:** No credits in UniSMS account

**Solution:**
1. Login to https://unismsapi.com
2. Go to Billing → Add Credits
3. Purchase SMS credits
4. Try again

### Issue: "422 Unprocessable Entity"

**Cause:** Invalid phone number or message format

**Solution:**
1. Verify phone number is Philippine mobile (starts with 09)
2. Check message doesn't have invalid characters
3. Ensure message is not empty

### Issue: No response or timeout

**Cause:** Network or API endpoint issue

**Solution:**
1. Check internet connection
2. Verify URL: `https://unismsapi.com/api`
3. Try again in a few minutes

---

## 🎯 Quick Verification Checklist

Before testing, verify:

- [ ] API key in database: `sk_6bb6f6f6-391a-4b34-8146-d38bacf73d4b`
- [ ] API base URL: `https://unismsapi.com/api`
- [ ] SMS enabled: `TRUE`
- [ ] Phone number format: `09XXXXXXXXX` (11 digits)
- [ ] UniSMS account has credits
- [ ] Internet connection working

---

## 📱 After Successful Test

Once SMS works:

1. **Add phone numbers to residents:**
   ```sql
   UPDATE residents SET phone_number = '09171234567' WHERE id = 1;
   ```

2. **Test SMS templates:**
   ```java
   String template = DatabaseHelper.getSMSTemplate("Document Approved");
   String message = template.replace("{document_type}", "Clearance");
   SMSService.sendSMS("09563052862", message);
   ```

3. **Integrate into workflows:**
   - Document approvals → Send SMS
   - Complaint updates → Send SMS
   - Announcements → Broadcast SMS

---

## 📞 Support

**UniSMS:**
- Dashboard: https://unismsapi.com/dashboard
- API Docs: https://unismsapi.com/api
- Support: Check their website for contact info

**Your Files:**
- Test utility: `TestUniSMS.java`
- SQL setup: `test_unisms_complete.sql`
- Troubleshooting: `SMS_ERROR_TROUBLESHOOTING.md`

---

## ✅ Summary

**What's configured:**
- ✅ API Key: `sk_6bb6f6f6-391a-4b34-8146-d38bacf73d4b`
- ✅ Endpoint: `https://unismsapi.com/api/sms`
- ✅ Authentication: Basic Auth (correct format)
- ✅ Phone format: E.164 with + prefix
- ✅ Debug logging: Enabled

**Next step:**
1. Run `test_unisms_complete.sql` to update database
2. Run `TestUniSMS.java` to send test SMS
3. Check your phone for the message!

---

**Your SMS system is ready! Just run the test and you should receive the SMS.** 🚀📱
