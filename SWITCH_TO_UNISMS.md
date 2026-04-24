# 🔄 Switch from Semaphore to UniSMS API

## Problem Identified

Your `SMSService.java` code is configured for **UniSMS API**, but you're trying to use a **Semaphore API key**. These are incompatible.

## Solution: Get UniSMS API Key

### Step 1: Sign Up for UniSMS

1. Visit: **https://unismsapi.com**
2. Create an account
3. Get your API Secret Key (format: `sk_XXXXXXXXXXXXXXXXXXXXXXXXX`)

### Step 2: Update Database Configuration

Run this SQL command to update your SMS configuration:

```sql
-- Update SMS configuration with UniSMS API key
UPDATE sms_config 
SET api_key = 'sk_YOUR_UNISMS_API_KEY_HERE',
    api_base_url = 'https://unismsapi.com/api',
    last_updated = CURRENT_TIMESTAMP;
```

Or if the table is empty:

```sql
-- Insert new SMS configuration
INSERT INTO sms_config (api_key, api_base_url, sender_name, enabled, last_updated) 
VALUES ('sk_YOUR_UNISMS_API_KEY_HERE', 'https://unismsapi.com/api', 'BDMS', TRUE, CURRENT_TIMESTAMP);
```

### Step 3: Test SMS

Your existing code will work once you have the correct UniSMS API key:

```java
SMSService.SMSResponse response = SMSService.sendSMS("09171234567", "Test from BDMS!");
System.out.println(response.toString());
```

## UniSMS API Features

According to the documentation you provided:

- **Authentication:** Basic Auth (API_SECRET_KEY as username, empty password)
- **Base URL:** https://unismsapi.com/api
- **Endpoints:**
  - `POST /sms` - Send single SMS
  - `POST /blast` - Send same message to multiple recipients
  - `POST /bulk` - Send different messages to different recipients
  - `POST /otp` - Send OTP with automatic generation
  - `GET /sms/:reference_id` - Check SMS status

- **Phone Format:** E.164 format (e.g., +639123456789)
- **Custom Sender ID:** Available for verified businesses only

## Your Code is Already Compatible!

Your `SMSService.java` already implements:
- ✅ Basic Authentication with API key
- ✅ Correct endpoint (`/sms`)
- ✅ Correct JSON format
- ✅ Phone number normalization to E.164 format
- ✅ Response parsing

**You just need the correct UniSMS API key!**

## Quick Test

Once you have your UniSMS API key:

1. Update the database:
   ```sql
   UPDATE sms_config SET api_key = 'sk_YOUR_KEY_HERE';
   ```

2. Run your application and try sending a test SMS

3. Check the SMS log:
   ```sql
   SELECT * FROM sms_log ORDER BY sent_at DESC LIMIT 5;
   ```

## Cost Comparison

**UniSMS Pricing:** (Check their website for current rates)
- Standard SMS: ~₱0.50-1.00 per message
- OTP SMS: Similar pricing
- Bulk discounts available

**Semaphore Pricing:**
- Standard SMS: ₱0.50 per 160 characters
- Priority SMS: ₱1.00 per message

Choose based on your budget and requirements.

---

**Next Step:** Get your UniSMS API key from https://unismsapi.com and update your database!
