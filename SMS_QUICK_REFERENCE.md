# SMS Features - Quick Reference Guide

## 🚀 Quick Start

### Where to Find SMS Features:

| Feature | Location | Button Name | Icon |
|---------|----------|-------------|------|
| Document SMS | Document Requests Tab | "Send SMS" | 📱 SMS |
| Complaint SMS | Manage Complaints Tab | "Send SMS" | 📱 SMS |
| Announcement Broadcast | Manage Announcements Tab | "Broadcast SMS" | 📢 Bullhorn |
| Template Editor | System Config → SMS Testing | "SMS Templates" | ✏️ Edit |
| Test SMS | System Config → SMS Testing | "Test SMS" | 🧪 Test |

---

## 📱 How to Send SMS

### Individual SMS (Documents & Complaints):

1. **Select** the record from the table
2. **Click** "Send SMS" button
3. **Choose** a template or select "Custom Message"
4. **Review** the message and character count
5. **Click** OK to send

**Tips:**
- Keep messages under 160 characters (1 SMS credit)
- Messages over 160 chars will use multiple credits
- Orange text = over 160 characters

---

### Broadcast SMS (Announcements):

1. **Select** an announcement from the table
2. **Click** "Broadcast SMS" button
3. **Choose** a template or select "Custom Message"
4. **Review** recipient count and cost estimate
5. **Confirm** to send to all residents

**Important:**
- Shows how many residents will receive SMS
- Displays estimated SMS credit cost
- Requires confirmation before sending
- Sends to ALL residents with phone numbers

---

## 📝 Available Templates

### Document SMS Templates:
1. ✅ **Document Ready for Pickup** - When document is ready
2. ⏳ **Document Approved** - When request is approved
3. 🕐 **Document Pending** - When still processing
4. ✏️ **Custom Message** - Write your own

### Complaint SMS Templates:
1. 📥 **Complaint Received** - Acknowledge receipt
2. 🔍 **Complaint Under Investigation** - Update on progress
3. ✅ **Complaint Resolved** - Notify resolution
4. ✏️ **Custom Message** - Write your own

### Announcement SMS Templates:
1. 📢 **Announcement Notification** - General announcements
2. 📅 **Event Reminder** - Event reminders
3. 🚨 **Emergency Alert** - Urgent alerts
4. ✏️ **Custom Message** - Write your own

---

## 💰 SMS Credit Information

- **Available Credits:** 97 SMS credits
- **Cost per SMS:** 1 credit per 160 characters
- **Character Limit:** 160 chars = 1 SMS, 320 chars = 2 SMS

**Cost Examples:**
- 1 resident × 150 chars = 1 credit
- 1 resident × 200 chars = 2 credits
- 50 residents × 150 chars = 50 credits
- 50 residents × 200 chars = 100 credits

---

## ⚙️ SMS Configuration

**Location:** System Configuration → SMS Testing

**Settings:**
- ✅ SMS Enabled/Disabled
- 🔑 API Key: `sk_6bb6f6f6-391a-4b34-8146-d38bacf73d4b`
- 🌐 API URL: `https://unismsapi.com/api`

---

## ✏️ Editing Templates

1. Go to **System Configuration**
2. Click **"SMS Testing"** tab
3. Click **"SMS Templates"** sub-tab
4. Edit any template
5. Click **"Save Template"** to save changes

---

## 🧪 Testing SMS

**Location:** System Configuration → SMS Testing

1. Enter a test phone number (09XXXXXXXXX)
2. Write a test message
3. Click "Send Test SMS"
4. Check phone for delivery (1-5 minutes)

---

## ⚠️ Troubleshooting

### SMS Not Sending?

**Check:**
1. ✅ SMS is enabled in System Config
2. 📱 Phone number is correct (09XXXXXXXXX format)
3. 💳 Sufficient SMS credits available
4. 🔑 API key is configured correctly
5. 🌐 Internet connection is active

### Common Issues:

| Issue | Solution |
|-------|----------|
| "No phone number" | Add phone number to resident profile |
| "SMS disabled" | Enable SMS in System Config |
| "API key missing" | Configure API key in System Config |
| "Invalid number" | Use Philippine format (09XXXXXXXXX) |
| "Rate limit exceeded" | Wait 1 second between messages |

---

## 📊 SMS Logs

**Location:** Database → `sms_log` table

**View logs to:**
- Check SMS delivery status
- Track sent messages
- Debug issues
- Monitor SMS usage

---

## 🎯 Best Practices

### Writing SMS Messages:

✅ **DO:**
- Keep messages professional
- Include reference numbers
- Provide contact information
- Use clear, concise language
- Stay under 160 characters when possible

❌ **DON'T:**
- Use spam-like wording
- Send unnecessary messages
- Include sensitive information
- Use all caps (except for URGENT)
- Forget to proofread

### Broadcasting:

✅ **DO:**
- Review recipient count before sending
- Check cost estimate
- Use appropriate template
- Confirm message is correct
- Send during business hours

❌ **DON'T:**
- Broadcast without confirmation
- Send late at night
- Waste credits on test broadcasts
- Send duplicate messages
- Ignore cost estimates

---

## 📞 Quick Help

### Need Help?
1. Check this guide first
2. Review SMS logs in database
3. Test with "Test SMS" feature
4. Check console for error messages
5. Verify SMS configuration

### Emergency?
- Use "Emergency Alert" template
- Keep message brief and clear
- Broadcast to all residents
- Follow up with official notice

---

## 🎉 Success Tips

1. **Test First** - Use "Test SMS" before broadcasting
2. **Check Credits** - Monitor SMS credit balance
3. **Use Templates** - Save time with pre-configured messages
4. **Review Before Sending** - Double-check message content
5. **Track Results** - Check SMS logs for delivery status

---

*Quick Reference Guide - SMS Features*
*Last Updated: April 21, 2026*
