# 🔧 SMS Error Troubleshooting Guide

**Your SMS failed with status: FAILED**  
**Phone: 639563052862**

---

## 🔍 Step 1: Check the Error Details

Run this SQL to see the exact error:

```sql
SELECT 
    phone_number,
    status,
    error_code,
    message_id,
    sent_at
FROM sms_log 
WHERE phone_number = '639563052862'
ORDER BY sent_at DESC 
LIMIT 1;
```

Look at the `error_code` column - this tells us what went wrong.

---

## ❌ Common Errors and Solutions

### Error: "API_ERROR" or "401 Unauthorized"

**Cause:** Invalid or expired API key

**Solution:**
1. Check your API key in database:
   ```sql
   SELECT api_key FROM sms_config;
   ```

2. Verify it starts with `sk_`

3. Login to https://unismsapi.com/dashboard

4. Copy your API Secret Key

5. Update database:
   ```sql
   UPDATE sms_config SET api_key = 'sk_YOUR_CORRECT_KEY_HERE';
   ```

---

### Error: "Insufficient credits" or "402"

**Cause:** No credits in your UniSMS account

**Solution:**
1. Login to https://unismsapi.com
2. Go to Billing → Add Credits
3. Purchase SMS credits
4. Try sending again

---

### Error: "Invalid recipient" or "422"

**Cause:** Phone number format is wrong

**Solution:**
- Phone must be in E.164 format: `+639XXXXXXXXX`
- Your code normalizes it automatically
- Check if number is valid Philippine mobile (starts with 639)

**Your number:** 639563052862 ✅ (looks correct)

---

### Error: "Invalid API key format"

**Cause:** API key doesn't match UniSMS format

**Solution:**
1. UniSMS API keys start with `sk_`
2. Check your key:
   ```sql
   SELECT SUBSTRING(api_key, 1, 3) as key_prefix FROM sms_config;
   ```
3. If it doesn't start with `sk_`, you might have the wrong key
4. Get the correct key from UniSMS dashboard

---

### Error: "Rate limit exceeded"

**Cause:** Sending too many SMS too quickly

**Solution:**
- Wait 1 second between messages
- Your code has rate limiting built-in
- Try again after 1 minute

---

## 🔧 Step 2: Run Diagnostic Tool

I created a diagnostic tool for you. Run it to see detailed error info:

```bash
javac -cp ".;lib/*" src/main/java/com/example/DiagnoseSMS.java
java -cp ".;lib/*;src/main/java" com.example.DiagnoseSMS
```

This will:
- Check your API key configuration
- Show the last error details
- Test the API connection
- Provide specific troubleshooting steps

---

## 🧪 Step 3: Test with Debug Logging

I added debug logging to `SMSService.java`. When you send SMS now, you'll see:

```
=== SMS API Request ===
URL: https://unismsapi.com/api/sms
API Key: sk_abc...xyz
Request Body: {"recipient":"+639563052862","content":"Test message"}
Response Code: 401
Response Body: {"error":"Invalid API key"}
======================
```

This shows exactly what the API returned.

**Try sending SMS again** and check the console output for the error message.

---

## 🔑 Step 4: Verify Your API Key

### Check what's in your database:

```sql
SELECT 
    SUBSTRING(api_key, 1, 10) as key_start,
    LENGTH(api_key) as key_length,
    api_base_url,
    enabled
FROM sms_config;
```

### What you should see:

- `key_start`: Should be `sk_` followed by random characters
- `key_length`: Usually 30-50 characters
- `api_base_url`: `https://unismsapi.com/api`
- `enabled`: `TRUE`

### If your key doesn't start with `sk_`:

You might have a Semaphore API key instead of UniSMS. They're different!

**Semaphore key format:** Random alphanumeric (e.g., `bec69d98d78c3d0a14f4f69a8fb5a312`)  
**UniSMS key format:** Starts with `sk_` (e.g., `sk_abc123xyz789...`)

If you have a Semaphore key, you need to either:
- **Option A:** Get a UniSMS API key from https://unismsapi.com
- **Option B:** Let me rewrite the code to use Semaphore API instead

---

## 🧪 Step 5: Test with cURL

Test your API key directly with cURL to verify it works:

```bash
curl -X POST https://unismsapi.com/api/sms \
  -u "YOUR_API_KEY:" \
  -H "Content-Type: application/json" \
  -d '{"recipient": "+639563052862", "content": "Test from cURL"}'
```

Replace `YOUR_API_KEY` with your actual key.

**Expected response (success):**
```json
{
  "message": {
    "status": "sent",
    "reference_id": "msg_xxxxx",
    "recipient": "+639563052862"
  }
}
```

**Expected response (error):**
```json
{
  "error": "Invalid API key"
}
```

This confirms if the issue is with your API key or the code.

---

## 📊 Step 6: Check UniSMS Dashboard

1. Login to https://unismsapi.com
2. Check:
   - ✅ Account is active
   - ✅ API key is enabled
   - ✅ Account has credits
   - ✅ No restrictions on your account

---

## 🎯 Most Likely Causes

Based on "FAILED" status, the most common causes are:

1. **Invalid API Key (80% of cases)**
   - Key is wrong or expired
   - Key doesn't start with `sk_`
   - Using Semaphore key instead of UniSMS key

2. **No Credits (15% of cases)**
   - Account has zero balance
   - Need to purchase credits

3. **API Endpoint Wrong (5% of cases)**
   - URL is incorrect
   - Should be: `https://unismsapi.com/api`

---

## ✅ Quick Fix Checklist

- [ ] API key starts with `sk_`
- [ ] API key copied correctly (no spaces)
- [ ] API base URL is `https://unismsapi.com/api`
- [ ] SMS is enabled in config
- [ ] UniSMS account has credits
- [ ] Phone number is valid (639XXXXXXXXX)
- [ ] Ran diagnostic tool
- [ ] Checked console output for error message

---

## 🆘 Still Not Working?

### Option 1: Share the Error Details

Run this and share the output:

```sql
SELECT error_code FROM sms_log ORDER BY sent_at DESC LIMIT 1;
```

Also check your console output when sending SMS - it now shows the API response.

### Option 2: Verify API Key Type

What API service are you using?
- **UniSMS** (https://unismsapi.com) - Keys start with `sk_`
- **Semaphore** (https://semaphore.co) - Keys are alphanumeric

If you're using Semaphore, your code needs to be updated.

### Option 3: Test with Different Number

Try sending to a different phone number to rule out number-specific issues.

---

## 📞 Next Steps

1. **Run the diagnostic tool:**
   ```bash
   java -cp ".;lib/*;src/main/java" com.example.DiagnoseSMS
   ```

2. **Check the console output** when sending SMS (now has debug logging)

3. **Verify your API key** at https://unismsapi.com/dashboard

4. **Share the error_code** from the database so I can help further

---

**Let me know what error code you see, and I'll help you fix it!** 🔧
