# ✅ API Base URL Configuration Feature Added

**Date:** April 21, 2026  
**Feature:** Configurable API Base URL for SMS API PH

---

## 🎯 What's New

You can now configure the API base URL in the SMS Configuration tab! This allows you to:

- ✅ Change the API endpoint if SMS API PH updates their URL
- ✅ Use a different SMS API PH server (e.g., testing, staging, production)
- ✅ Switch to a custom SMS gateway that uses the same API format
- ✅ Easily update the endpoint without modifying code

---

## 📋 Changes Made

### 1. Database Schema Updated

**New column added to `sms_config` table:**
```sql
api_base_url VARCHAR(255) DEFAULT 'https://smsapiph.onrender.com/api/v1'
```

**For existing databases:**
- The column is automatically added when you restart the application
- Default value: `https://smsapiph.onrender.com/api/v1`

### 2. DatabaseHelper.java

**New method:**
```java
public static String getSMSApiBaseUrl()
```
- Returns the configured API base URL
- Falls back to default if not configured

**Updated method:**
```java
public static void saveSMSConfig(String apiKey, String apiBaseUrl, String senderName, boolean enabled)
```
- Now accepts API base URL parameter
- Backward compatible (old method still works)

### 3. SMSService.java

**Updated to use dynamic API base URL:**
- Reads API base URL from database instead of hardcoded constant
- Falls back to default if not configured
- All SMS methods now use the configured URL

### 4. App.java (UI)

**New field in SMS Configuration tab:**
- **API Base URL** text field
- Default value shown: `https://smsapiph.onrender.com/api/v1`
- Hint text explaining when to change it
- Saves to database when configuration is saved

---

## 🚀 How to Use

### Step 1: Open SMS Configuration

1. Run your application
2. Login as admin
3. Go to **Maintenance** tab
4. Click **SMS Configuration** sub-tab

### Step 2: Configure API Base URL

You'll see a new field:

```
API Base URL:
┌─────────────────────────────────────────────────────┐
│ https://smsapiph.onrender.com/api/v1               │
└─────────────────────────────────────────────────────┘
💡 Default: https://smsapiph.onrender.com/api/v1 
   (leave as is unless using a different endpoint)
```

### Step 3: Change if Needed

**When to change:**
- SMS API PH announces a new endpoint
- You want to use a testing/staging server
- You're using a custom SMS gateway

**Example values:**
- Production: `https://smsapiph.onrender.com/api/v1` (default)
- Testing: `https://test-smsapiph.onrender.com/api/v1`
- Custom: `https://your-custom-sms-api.com/api/v1`

### Step 4: Save Configuration

Click **"Save Configuration"** button to apply changes.

---

## 📊 Configuration Example

### Current Configuration View:

```
┌─────────────────────────────────────────────────────┐
│ SMS API PH - FREE SMS Service                       │
│ Configure your SMS API PH credentials...            │
└─────────────────────────────────────────────────────┘

API Key:
┌─────────────────────────────────────────────────────┐
│ sk-2b103paubez1pdgnat07k0c8jexljlbw                │
└─────────────────────────────────────────────────────┘

API Base URL:                                    ← NEW!
┌─────────────────────────────────────────────────────┐
│ https://smsapiph.onrender.com/api/v1               │
└─────────────────────────────────────────────────────┘
💡 Default: https://smsapiph.onrender.com/api/v1

Sender Name (max 11 characters):
┌─────────────────────────────────────────────────────┐
│ BDMS                                                │
└─────────────────────────────────────────────────────┘

☑ Enable SMS Notifications

[Save Configuration]
```

---

## 🔍 Database Queries

### Check Current Configuration

```sql
SELECT * FROM sms_config;
```

**Expected output:**
```
id | api_key                          | api_base_url                              | sender_name | enabled | last_updated
1  | sk-2b103paubez1pdgnat07k0c8jexl | https://smsapiph.onrender.com/api/v1     | BDMS        | TRUE    | 2026-04-21 16:30:00
```

### Update API Base URL Manually

```sql
UPDATE sms_config 
SET api_base_url = 'https://your-new-endpoint.com/api/v1',
    last_updated = CURRENT_TIMESTAMP;
```

### Reset to Default

```sql
UPDATE sms_config 
SET api_base_url = 'https://smsapiph.onrender.com/api/v1',
    last_updated = CURRENT_TIMESTAMP;
```

---

## 🧪 Testing Different Endpoints

### Test with Default Endpoint

1. Set API Base URL to: `https://smsapiph.onrender.com/api/v1`
2. Save configuration
3. Send test SMS
4. Verify it works

### Test with Custom Endpoint

1. Set API Base URL to your custom endpoint
2. Save configuration
3. Send test SMS
4. Check console for connection details

---

## 🔐 Security Notes

### Valid URL Formats

The API base URL should:
- ✅ Start with `https://` (secure)
- ✅ Not include the `/send/sms` path (added automatically)
- ✅ End with `/api/v1` or your API version

**Examples:**
- ✅ `https://smsapiph.onrender.com/api/v1`
- ✅ `https://api.example.com/v1`
- ❌ `http://insecure-api.com` (not HTTPS)
- ❌ `https://api.com/api/v1/send/sms` (includes endpoint path)

### Validation

The system will:
- Use the configured URL as-is
- Append `/send/sms` automatically
- Fall back to default if URL is empty

---

## 📝 Code Examples

### Get Current API Base URL

```java
String apiBaseUrl = DatabaseHelper.getSMSApiBaseUrl();
System.out.println("Current API URL: " + apiBaseUrl);
```

### Update API Base URL Programmatically

```java
DatabaseHelper.saveSMSConfig(
    "sk-2b103paubez1pdgnat07k0c8jexljlbw",  // API key
    "https://new-endpoint.com/api/v1",       // API base URL
    "BDMS",                                   // Sender name
    true                                      // Enabled
);
```

### Send SMS with Custom Endpoint

```java
// Just send SMS normally - it will use the configured endpoint
SMSService.sendSMS("09171234567", "Test message");
```

---

## 🐛 Troubleshooting

### Issue: "Connection refused" after changing URL

**Cause:** Invalid or unreachable endpoint

**Solution:**
1. Verify the URL is correct
2. Check if the endpoint is accessible
3. Reset to default: `https://smsapiph.onrender.com/api/v1`

### Issue: "404 Not Found"

**Cause:** Incorrect API path

**Solution:**
- Ensure URL ends with `/api/v1` (not `/api/v1/send/sms`)
- The `/send/sms` path is added automatically

### Issue: Column not found error

**Cause:** Old database without `api_base_url` column

**Solution:**
```sql
-- Add column manually
ALTER TABLE sms_config 
ADD COLUMN api_base_url VARCHAR(255) 
DEFAULT 'https://smsapiph.onrender.com/api/v1';
```

Or restart the application (it will add automatically).

---

## ✅ Migration for Existing Users

### Automatic Migration

When you restart your application:
1. ✅ `api_base_url` column is added automatically
2. ✅ Default value is set to `https://smsapiph.onrender.com/api/v1`
3. ✅ Existing SMS functionality continues to work
4. ✅ No manual intervention required

### Manual Verification

```sql
-- Check if column exists
SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'SMS_CONFIG' 
AND COLUMN_NAME = 'API_BASE_URL';

-- If not exists, add it
ALTER TABLE sms_config 
ADD COLUMN IF NOT EXISTS api_base_url VARCHAR(255) 
DEFAULT 'https://smsapiph.onrender.com/api/v1';
```

---

## 🎯 Use Cases

### 1. SMS API PH Updates Their Endpoint

**Scenario:** SMS API PH moves to a new server

**Solution:**
1. Go to SMS Configuration
2. Update API Base URL to new endpoint
3. Save configuration
4. Test SMS

### 2. Testing Before Production

**Scenario:** You want to test with a staging server

**Solution:**
1. Set API Base URL to staging: `https://staging-smsapi.com/api/v1`
2. Test your SMS functionality
3. Switch back to production when ready

### 3. Using Multiple Environments

**Scenario:** Different environments (dev, staging, prod)

**Solution:**
- Dev database: `https://dev-smsapi.com/api/v1`
- Staging database: `https://staging-smsapi.com/api/v1`
- Production database: `https://smsapiph.onrender.com/api/v1`

### 4. Custom SMS Gateway

**Scenario:** You want to use your own SMS gateway

**Solution:**
1. Ensure your gateway uses the same API format
2. Update API Base URL to your gateway
3. Update API key if needed
4. Test SMS functionality

---

## 📞 Support

### SMS API PH Endpoints

**Production (Default):**
```
https://smsapiph.onrender.com/api/v1
```

**Documentation:**
- Website: https://smsapiph.netlify.app
- Docs: https://smsapiph.netlify.app/documentation

### Check Current Endpoint

```sql
SELECT api_base_url FROM sms_config;
```

### Test Endpoint Connectivity

```bash
# Test if endpoint is reachable
curl -X POST https://smsapiph.onrender.com/api/v1/send/sms \
  -H "Content-Type: application/json" \
  -H "x-api-key: YOUR_API_KEY" \
  -d '{"recipient":"+639171234567","message":"Test"}'
```

---

## 🎉 Summary

You now have full control over the SMS API endpoint!

**Benefits:**
- ✅ Easy to update if SMS API PH changes their URL
- ✅ Support for testing/staging environments
- ✅ Flexibility to use custom SMS gateways
- ✅ No code changes required to switch endpoints
- ✅ Backward compatible with existing setup

**Default Configuration:**
- API Base URL: `https://smsapiph.onrender.com/api/v1`
- Works out of the box
- Change only if needed

---

*Feature added: April 21, 2026*  
*Ready to use! 🚀*
