# ⚡ Quick Start - UniSMS API

**Get your SMS working in 3 minutes!**

---

## 🎯 You Need

1. Your UniSMS API key (starts with `sk_`)
2. Your Philippine mobile number for testing
3. Database access (H2 Console or SQL client)

---

## 🚀 3-Step Setup

### Step 1: Configure API Key (1 minute)

Open your database and run:

```sql
-- Replace YOUR_API_KEY with your actual UniSMS API key
UPDATE sms_config 
SET api_key = 'sk_YOUR_ACTUAL_API_KEY_HERE',
    api_base_url = 'https://unismsapi.com/api',
    enabled = TRUE,
    last_updated = CURRENT_TIMESTAMP;

-- If the above doesn't work, use this instead:
DELETE FROM sms_config;
INSERT INTO sms_config (api_key, api_base_url, sender_name, enabled, last_updated) 
VALUES ('sk_YOUR_ACTUAL_API_KEY_HERE', 'https://unismsapi.com/api', 'BDMS', TRUE, CURRENT_TIMESTAMP);

-- Verify it worked:
SELECT * FROM sms_config;
```

✅ You should see your API key, URL, and enabled = TRUE

---

### Step 2: Test SMS (1 minute)

**Option A: Using TestSMS.java**

1. Edit `src/main/java/com/example/TestSMS.java`
2. Line 35: Change to your phone number:
   ```java
   String testPhone = "09171234567"; // YOUR NUMBER HERE
   ```
3. Run it:
   ```bash
   # Compile
   javac -cp ".;lib/*" src/main/java/com/example/TestSMS.java
   
   # Run
   java -cp ".;lib/*;src/main/java" com.example.TestSMS
   ```

**Option B: Using your Application**

1. Run your BDMS app
2. Go to Maintenance → SMS Settings
3. Click "Test SMS" button
4. Enter your phone number

---

### Step 3: Verify (1 minute)

1. **Check console output:**
   ```
   ✅ SUCCESS! SMS sent successfully!
   📱 Check your phone for the message.
   🆔 Message ID: msg_xxxxx
   ```

2. **Check your phone** - SMS arrives in 1-5 minutes

3. **Check database log:**
   ```sql
   SELECT * FROM sms_log ORDER BY sent_at DESC LIMIT 1;
   ```
   Should show: `status = 'SENT'`

---

## ✅ Done!

If you see "SUCCESS" and receive the SMS, you're all set! 🎉

---

## ❌ Troubleshooting

### "SMS API key not configured"
→ Run Step 1 again, make sure API key starts with `sk_`

### "Invalid Philippine mobile number"
→ Use format: `09171234567` (11 digits, starts with 09)

### "Failed to send SMS" or "API_ERROR"
→ Check:
- API key is correct (copy from UniSMS dashboard)
- UniSMS account has credits
- No typos in API key

### SMS not received
→ Wait 5 minutes, check SMS log for errors:
```sql
SELECT phone_number, status, error_code FROM sms_log ORDER BY sent_at DESC LIMIT 1;
```

---

## 📞 Need Help?

1. Check `UNISMS_SETUP_GUIDE.md` for detailed troubleshooting
2. Review SMS log: `SELECT * FROM sms_log ORDER BY sent_at DESC`
3. Verify API key in UniSMS dashboard
4. Check UniSMS account balance

---

## 🎯 Next Steps

Once SMS is working:

- [ ] Add phone numbers to residents
- [ ] Test document approval SMS
- [ ] Test complaint notification SMS
- [ ] Set up announcement broadcasts

---

**That's it! Your SMS system is ready to use.** 📱✨
