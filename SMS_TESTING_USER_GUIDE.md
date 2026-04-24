# SMS Testing - User Guide

## Quick Start

### Step 1: Configure SMS Service

1. Go to **System Config** in the sidebar
2. Click the **SMS Testing** tab
3. Click the **Configuration** sub-tab
4. Fill in the form:
   - **API Key**: Your Semaphore API key (get from https://semaphore.co/account)
   - **Sender Name**: Your organization name (max 11 characters, e.g., "BDMS")
   - **Enable SMS**: Check the box to enable SMS notifications
5. Click **Save Configuration**
6. Wait for the success message: "✓ SMS configuration saved successfully!"

### Step 2: Test SMS

1. Click the **Test SMS** sub-tab
2. You should see a green box: "✓ SMS service is ready"
3. Enter a Philippine mobile number:
   - Format: `09171234567` or `+639171234567`
4. Review the test message (or write your own)
5. Check the character counter (160 chars = 1 credit)
6. Click **Send Test SMS**
7. Wait for the result:
   - **Success**: "✓ Test SMS sent successfully! Message ID: xxxxx"
   - **Error**: "✗ Failed to send test SMS Error: [details]"
8. Check your phone for the message

### Step 3: View SMS Logs

1. Click the **SMS Logs** sub-tab
2. View the table showing all SMS transactions:
   - ID
   - Phone Number
   - Message
   - Status (SENT, FAILED, ERROR)
   - Message ID
   - Timestamp
3. Click **Refresh Logs** to update the list
4. Click column headers to sort

## Tab Overview

### Configuration Tab
**Purpose**: Edit SMS service settings

**What you can do:**
- ✏️ Edit API key
- ✏️ Edit sender name
- ✅ Enable/disable SMS service
- 💾 Save changes to database

**Status indicators:**
- Blue info box with Semaphore API information
- Success message (green) when saved
- Error message (red) if save fails

### Test SMS Tab
**Purpose**: Send test messages

**What you can do:**
- 📱 Enter phone number
- ✍️ Write test message
- 📊 See character count and credit cost
- 📤 Send test SMS
- ✅ View success/error results

**Status indicators:**
- Green box: "✓ SMS service is ready" (can send)
- Yellow box: "⚠ SMS service is not configured" (configure first)

### SMS Logs Tab
**Purpose**: View transaction history

**What you can do:**
- 📋 View all SMS transactions
- 🔄 Refresh logs
- 🔍 Sort by any column
- 📊 See status of each message

**Columns:**
- **ID**: Unique log entry number
- **Phone Number**: Recipient number
- **Message**: SMS content
- **Status**: SENT, FAILED, ERROR, etc.
- **Message ID**: Semaphore message ID
- **Timestamp**: When SMS was sent

## Common Scenarios

### Scenario 1: First-Time Setup
```
1. Configuration tab → Enter API key → Enter sender name → Enable → Save
2. Test SMS tab → Enter your number → Send test
3. Check phone for message
4. SMS Logs tab → Verify log entry
```

### Scenario 2: Testing After Changes
```
1. Configuration tab → Update settings → Save
2. Test SMS tab → Send test message
3. SMS Logs tab → Check if successful
```

### Scenario 3: Troubleshooting
```
1. SMS Logs tab → Check recent failures
2. Configuration tab → Verify API key and enabled status
3. Test SMS tab → Try sending again
4. Check error message for details
```

## Error Messages Explained

### Configuration Tab Errors

**"⚠ API Key is required"**
- You didn't enter an API key
- Solution: Enter your Semaphore API key

**"✗ Error saving configuration: [details]"**
- Database error occurred
- Solution: Check database connection

### Test SMS Tab Errors

**"⚠ Please enter a phone number"**
- Phone number field is empty
- Solution: Enter a valid Philippine mobile number

**"⚠ Please enter a message"**
- Message field is empty
- Solution: Enter a message to send

**"⚠ SMS service is not configured"**
- API key not set or SMS disabled
- Solution: Go to Configuration tab and set up SMS

**"✗ Failed to send test SMS - Error: SMS notifications are disabled"**
- SMS is disabled in configuration
- Solution: Go to Configuration tab and enable SMS

**"✗ Failed to send test SMS - Error: SMS API key not configured"**
- No API key in database
- Solution: Go to Configuration tab and enter API key

**"✗ Failed to send test SMS - Error: Invalid Philippine mobile number"**
- Phone number format is invalid
- Solution: Use format 09171234567 or +639171234567

**"✗ Failed to send test SMS - Error: Unknown response format: Your account has not yet been approved"**
- Semaphore account pending approval
- Solution: Wait for Semaphore to approve your account

**"✗ Failed to send test SMS - Error: Rate limit exceeded"**
- Too many SMS sent (120 per minute limit)
- Solution: Wait a minute and try again

### SMS Logs Tab Errors

**"Error loading logs: [details]"**
- Database error occurred
- Solution: Check database connection and try refreshing

## Tips & Best Practices

### Configuration
- ✅ Use a descriptive sender name (e.g., "BDMS", "BRGY")
- ✅ Keep sender name under 11 characters
- ✅ Save API key securely
- ✅ Enable SMS only when ready to use
- ✅ Test after any configuration change

### Testing
- ✅ Test with your own number first
- ✅ Keep test messages short to save credits
- ✅ Check character counter before sending
- ✅ Wait for result before sending another
- ✅ Verify message received on phone

### Monitoring
- ✅ Check logs regularly
- ✅ Look for failed messages
- ✅ Refresh logs after sending
- ✅ Sort by timestamp to see recent activity
- ✅ Note message IDs for tracking

## Character Count & Credits

SMS credits are calculated based on message length:

| Characters | Credits | Cost |
|-----------|---------|------|
| 1-160     | 1       | 1x   |
| 161-320   | 2       | 2x   |
| 321-480   | 3       | 3x   |
| 481-640   | 4       | 4x   |

**Example:**
- "Hello" = 5 characters = 1 credit
- "This is a test message from Barangay San Marino BDMS..." = 115 characters = 1 credit
- A very long message with 200 characters = 2 credits

## Phone Number Formats

All these formats work:
- `09171234567` (11 digits starting with 09)
- `+639171234567` (with country code)
- `639171234567` (country code without +)

The system automatically converts to: `639171234567`

## Status Codes

### SMS Status Values
- **SENT**: Successfully sent to Semaphore
- **SENT_PRIORITY**: Sent via priority endpoint
- **SENT_OTP**: Sent as OTP message
- **SENT_BULK**: Sent as bulk message
- **FAILED**: Failed to send
- **ERROR**: System error occurred
- **SMS_DISABLED**: SMS service is disabled
- **API_KEY_MISSING**: No API key configured
- **INVALID_NUMBER**: Invalid phone number format
- **RATE_LIMIT**: Too many requests

## Troubleshooting Guide

### Problem: Can't save configuration
**Check:**
1. Is database running?
2. Do you have permission to update settings?
3. Is API key field filled?

### Problem: Test SMS not sending
**Check:**
1. Is SMS enabled in Configuration tab?
2. Is API key entered correctly?
3. Is phone number in correct format?
4. Is Semaphore account approved?
5. Do you have SMS credits?

### Problem: No logs showing
**Check:**
1. Have you sent any SMS yet?
2. Is database connection working?
3. Try clicking Refresh Logs button

### Problem: "Account not approved" error
**Solution:**
1. This is normal for new Semaphore accounts
2. Wait for Semaphore to approve your account (usually 1-2 business days)
3. Check your email for approval notification
4. Contact Semaphore support if delayed

## Getting Help

### Semaphore Support
- Website: https://semaphore.co
- Documentation: https://semaphore.co/docs
- Support: support@semaphore.co

### System Administrator
- Contact your system administrator for:
  - Database access issues
  - Permission problems
  - Configuration questions

## Quick Reference

### Navigation
```
Sidebar → System Config → SMS Testing Tab
```

### Tabs
```
Configuration: Edit settings
Test SMS: Send test messages
SMS Logs: View transaction history
```

### Workflow
```
Configure → Test → Monitor
```

### Key Actions
```
Save Configuration: Ctrl+S (when in field)
Send Test SMS: Click button
Refresh Logs: Click button
```

## Summary

The SMS Testing module provides:
- ✅ **Easy Configuration**: Edit settings from UI
- ✅ **Quick Testing**: Send test SMS instantly
- ✅ **Full Visibility**: View all transactions
- ✅ **User-Friendly**: Clear feedback and instructions
- ✅ **Professional**: Integrated with existing system

You can now manage SMS functionality without database access or code changes!
