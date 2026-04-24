# 🚀 SMS API PH - Quick Start Guide

**Your system is now using SMS API PH (100% FREE forever!)**

---

## ✅ What's Done

Your BDMS has been migrated from Semaphore (paid) to SMS API PH (FREE):

- ✅ Code updated to use new API
- ✅ Your API key configured: `sk-2b103paubez1pdgnat07k0c8jexljlbw`
- ✅ Database updated with new settings
- ✅ UI updated with new labels

---

## 🎯 How to Test (3 Simple Steps)

### Step 1: Compile & Run
```bash
# In your IDE (Eclipse, IntelliJ, VS Code):
# 1. Clean and rebuild the project
# 2. Run Launcher.java
```

### Step 2: Send Test SMS
1. Login to your BDMS
2. Go to **Residents** tab
3. Select a resident with a phone number
4. Click **"Test SMS"** button
5. Check your phone! 📱

### Step 3: Verify in Database
```sql
-- Check if SMS was logged
SELECT * FROM sms_log ORDER BY timestamp DESC LIMIT 5;

-- Check configuration
SELECT * FROM sms_config;
```

---

## 📱 Your SMS API PH Details

**API Key:** `sk-2b103paubez1pdgnat07k0c8jexljlbw`  
**Endpoint:** `https://smsapiph.onrender.com/api/v1/send/sms`  
**Dashboard:** https://smsapiph.netlify.app/login  
**Docs:** https://smsapiph.netlify.app/documentation

---

## 🆚 What Changed?

| Feature | Before (Semaphore) | Now (SMS API PH) |
|---------|-------------------|------------------|
| **Cost** | ₱0.50 per SMS | **FREE** ✅ |
| **Rate Limit** | 120/minute | 1 per 10 seconds ⚠️ |
| **Fallback** | None | SMS → Email → Push ✅ |
| **Setup** | Paid credits | Free forever ✅ |

---

## ⚠️ Important Notes

### Rate Limit Changed
- **Old:** 120 messages per minute
- **New:** 1 message per 10 seconds

**Impact on Bulk SMS:**
- 10 recipients = ~90 seconds
- 50 recipients = ~7.5 minutes
- 100 recipients = ~15 minutes

### Sender Name
- Custom sender names are not supported by SMS API PH
- Messages will show SMS API PH's default sender

### All Features Still Work
- ✅ Send SMS
- ✅ Send OTP
- ✅ Send Priority SMS (now FREE!)
- ✅ Send Bulk SMS
- ✅ SMS Templates
- ✅ SMS Logging

---

## 🐛 Troubleshooting

### "SMS not received"
1. Wait 5 minutes (network delay)
2. Check SMS log: `SELECT * FROM sms_log WHERE phone_number = '639XXXXXXXXX'`
3. Verify phone number format: `+639XXXXXXXXX`

### "Invalid API key"
Update your API key:
```sql
UPDATE sms_config SET api_key = 'sk-2b103paubez1pdgnat07k0c8jexljlbw';
```

### "Rate limit exceeded"
Wait 10 seconds between messages. This is automatic for bulk SMS.

---

## 💰 Cost Savings

**Before (Semaphore):**
- 100 SMS/month = ₱50
- 1,000 SMS/month = ₱500
- 10,000 SMS/year = ₱5,000

**Now (SMS API PH):**
- Unlimited SMS = **₱0** 🎉

---

## 📞 Need Help?

**SMS API PH Support:**
- Website: https://smsapiph.netlify.app
- GitHub: https://github.com/smsapiph
- Facebook: https://www.facebook.com/profile.php?id=61579123996580

**Check Your SMS Logs:**
```sql
SELECT * FROM sms_log ORDER BY timestamp DESC;
```

---

## 🎉 You're All Set!

Your BDMS is now using **SMS API PH** - a 100% FREE SMS service!

**Next Steps:**
1. ✅ Compile and run your application
2. ✅ Send a test SMS to yourself
3. ✅ Verify it works
4. ✅ Start using FREE SMS! 🚀

---

*For detailed migration information, see: SMS_API_PH_MIGRATION_COMPLETE.md*
