# 🚀 START HERE - SMS Testing

## ⚠️ Your Database is Currently Locked

Your BDMS application is running and has the database locked.

---

## 🎯 Quick Solution (2 Options)

### Option A: Use Your Running Application (Easiest)

1. **In your BDMS application**, find the SMS Settings or Database Console
2. **Run this SQL:**

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
```

3. **Test SMS** in your application (Residents tab → Test SMS button)
4. **Check console output** for API response
5. **Check your phone** for SMS

---

### Option B: Close Application and Run Test Utility

1. **Close your BDMS application completely**
2. **Double-click:** `test-sms.bat`
3. **Check console output** for results
4. **Check your phone** for SMS

---

## 📱 What You'll See

### Success:
```
=== SMS API Request ===
Response Code: 201
Response Body: {"message":{"status":"sent",...}}

🎉 SUCCESS! SMS sent successfully!
📱 Check your phone: 09563052862
```

### Error:
```
Response Code: 401
❌ FAILED! Authentication error
→ Check API key
```

---

## 📋 Files Ready for You

- ✅ `test-sms.bat` - Double-click to test SMS
- ✅ `UpdateSMSConfig.class` - Updates database
- ✅ `RUN_THIS_SQL.txt` - SQL to copy/paste
- ✅ `SMS_TEST_INSTRUCTIONS.md` - Detailed guide

---

## 🔑 Your API Key

```
sk_6bb6f6f6-391a-4b34-8146-d38bacf73d4b
```

This is already configured in the SQL above.

---

## ✅ Choose Your Path

**If application is running:** Use Option A (run SQL in app)  
**If you can close it:** Use Option B (run test-sms.bat)

---

**Both options will work - choose whichever is easier for you!** 🚀
