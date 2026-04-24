# 🔍 SMS Log Debugging Guide

**Issue:** SMS messages are sent successfully but not appearing in the log

---

## 🎯 Quick Diagnosis

### Step 1: Check Console Output

After sending an SMS, look for these messages in your console:

**✅ Success indicators:**
```
✓ SMS logged to database: 639171234567 - SENT
✓ SMS log entry created: 639171234567 - SENT at 2026-04-21 16:30:00
```

**❌ Error indicators:**
```
✗ Failed to log SMS to database: [error message]
✗ SQL Exception in logSMS: [error message]
✗ SMS log entry not created (0 rows affected)
```

### Step 2: Check Database

Run this query in H2 Console (`jdbc:h2:~/bdms_v2`):

```sql
SELECT * FROM sms_log ORDER BY timestamp DESC LIMIT 10;
```

**If you see 0 rows:**
- The logging is failing silently
- Check console for error messages
- Verify table exists (see Step 3)

### Step 3: Verify Table Exists

```sql
SELECT TABLE_NAME, COLUMN_NAME, TYPE_NAME 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'SMS_LOG'
ORDER BY ORDINAL_POSITION;
```

**Expected columns:**
- id (INTEGER)
- phone_number (VARCHAR)
- message (VARCHAR)
- status (VARCHAR)
- message_id (VARCHAR)
- error_code (VARCHAR)
- timestamp (DATETIME)

---

## 🔧 Common Issues & Solutions

### Issue 1: Table Doesn't Exist

**Symptom:** Query returns "Table not found: SMS_LOG"

**Solution:**
```sql
-- Create the table manually
CREATE TABLE IF NOT EXISTS sms_log (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    phone_number VARCHAR(50) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    status VARCHAR(20) NOT NULL,
    message_id VARCHAR(50),
    error_code VARCHAR(100),
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

### Issue 2: Wrong Database File

**Symptom:** SMS logs exist but you're checking a different database

**Solution:**
1. Check which database your app is using:
   - Look for `jdbc:h2:~/bdms_v2` in your code
   - Or check `DatabaseHelper.java` for the connection URL

2. Connect to the correct database:
   ```
   JDBC URL: jdbc:h2:~/bdms_v2
   User: sa
   Password: (leave empty)
   ```

### Issue 3: Timestamp Format Issue

**Symptom:** Error like "Data conversion error converting..."

**Solution:** The timestamp format has been updated to use proper formatting. Recompile your project.

### Issue 4: Silent Exception

**Symptom:** No error messages, but logs not appearing

**Solution:**
1. Check console output for any exceptions
2. Add this test code to verify logging works:

```java
// Test logging directly
public static void testSMSLogging() {
    try {
        DatabaseHelper.logSMS("639171234567", "Test message", "SENT", "test123", null);
        System.out.println("Test log created successfully");
    } catch (Exception e) {
        System.err.println("Test log failed: " + e.getMessage());
        e.printStackTrace();
    }
}
```

### Issue 5: Database Connection Issue

**Symptom:** "Connection refused" or "Database not found"

**Solution:**
1. Verify database file exists: `~/bdms_v2.mv.db`
2. Check if another instance is using the database
3. Restart your application

---

## 🧪 Manual Testing

### Test 1: Direct SQL Insert

Try inserting a log entry manually:

```sql
INSERT INTO sms_log (phone_number, message, status, message_id, error_code, timestamp) 
VALUES ('639171234567', 'Manual test message', 'SENT', 'manual123', NULL, CURRENT_TIMESTAMP);

-- Then check if it appears
SELECT * FROM sms_log ORDER BY timestamp DESC LIMIT 1;
```

**If this works:** The table is fine, issue is in the Java code  
**If this fails:** There's a database/table issue

### Test 2: Check Permissions

```sql
-- Check if you can write to the table
SELECT * FROM INFORMATION_SCHEMA.TABLE_PRIVILEGES 
WHERE TABLE_NAME = 'SMS_LOG';
```

### Test 3: Check for Triggers or Constraints

```sql
-- Check for any constraints that might prevent inserts
SELECT * FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS 
WHERE TABLE_NAME = 'SMS_LOG';

-- Check for triggers
SELECT * FROM INFORMATION_SCHEMA.TRIGGERS 
WHERE TABLE_NAME = 'SMS_LOG';
```

---

## 📊 Verification Queries

### Count SMS by Status
```sql
SELECT status, COUNT(*) as count 
FROM sms_log 
GROUP BY status;
```

### Recent SMS Activity
```sql
SELECT 
    phone_number,
    LEFT(message, 30) as message_preview,
    status,
    timestamp
FROM sms_log 
WHERE timestamp >= CURRENT_DATE
ORDER BY timestamp DESC;
```

### Failed SMS
```sql
SELECT * FROM sms_log 
WHERE status IN ('FAILED', 'ERROR')
ORDER BY timestamp DESC;
```

### SMS Sent Today
```sql
SELECT COUNT(*) as sms_today 
FROM sms_log 
WHERE DATE(timestamp) = CURRENT_DATE 
AND status LIKE 'SENT%';
```

---

## 🔍 Debug Mode

### Enable Detailed Logging

The updated code now includes detailed console logging. After recompiling, you should see:

**When SMS is sent:**
```
✓ SMS logged to database: 639171234567 - SENT
✓ SMS log entry created: 639171234567 - SENT at 2026-04-21 16:30:00
```

**When logging fails:**
```
✗ Failed to log SMS to database: [specific error]
✗ SQL Exception in logSMS: [SQL error details]
```

### Check Application Console

Look for these patterns in your console output:

1. **SMS Service messages:**
   - `✓ SMS logged to database: ...`
   - `✗ Failed to log SMS to database: ...`

2. **Database Helper messages:**
   - `✓ SMS log entry created: ...`
   - `✗ SMS log entry not created (0 rows affected)`
   - `✗ SQL Exception in logSMS: ...`

3. **Stack traces:**
   - Any `SQLException` or other exceptions

---

## 🚀 Quick Fix Steps

### Step 1: Recompile
```bash
# Clean and rebuild your project
# In Eclipse: Project > Clean > Clean all projects
# In IntelliJ: Build > Rebuild Project
# In VS Code: Clean Java Language Server Workspace
```

### Step 2: Restart Application
- Close your application completely
- Restart it
- Try sending SMS again

### Step 3: Check Console
- Look for the new debug messages
- Copy any error messages you see

### Step 4: Verify Database
```sql
-- Run this immediately after sending SMS
SELECT * FROM sms_log ORDER BY id DESC LIMIT 1;
```

### Step 5: Manual Insert Test
```sql
-- If still no logs, try manual insert
INSERT INTO sms_log (phone_number, message, status, message_id, error_code, timestamp) 
VALUES ('639999999999', 'Debug test', 'SENT', 'debug001', NULL, CURRENT_TIMESTAMP);

-- Check if it appears
SELECT COUNT(*) FROM sms_log;
```

---

## 📞 What to Report

If the issue persists, provide:

1. **Console output** after sending SMS (copy all messages)
2. **Database query results:**
   ```sql
   SELECT COUNT(*) FROM sms_log;
   SELECT * FROM sms_config;
   ```
3. **Table structure:**
   ```sql
   SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
   WHERE TABLE_NAME = 'SMS_LOG';
   ```
4. **Any error messages** from the console

---

## ✅ Expected Behavior

After the fix, when you send an SMS, you should see:

**Console:**
```
✓ SMS logged to database: 639171234567 - SENT
✓ SMS log entry created: 639171234567 - SENT at 2026-04-21 16:30:00
```

**Database:**
```sql
SELECT * FROM sms_log ORDER BY timestamp DESC LIMIT 1;

-- Should return:
-- id | phone_number  | message | status | message_id | error_code | timestamp
-- 1  | 639171234567  | Test... | SENT   | abc123     | NULL       | 2026-04-21 16:30:00
```

---

## 🎯 Next Steps

1. **Recompile** your project with the updated code
2. **Restart** your application
3. **Send a test SMS**
4. **Check console** for debug messages
5. **Query database** to verify log entry

The updated code includes detailed logging that will help identify exactly where the issue is occurring.

---

*Debug guide created: April 21, 2026*
