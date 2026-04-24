-- Check SMS Log Table
-- Run this in your H2 Console or database tool

-- 1. Check if sms_log table exists
SELECT TABLE_NAME, COLUMN_NAME, TYPE_NAME 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'SMS_LOG'
ORDER BY ORDINAL_POSITION;

-- 2. Count total SMS log entries
SELECT COUNT(*) as total_sms_logs FROM sms_log;

-- 3. View all SMS logs
SELECT * FROM sms_log ORDER BY timestamp DESC;

-- 4. View SMS logs by status
SELECT status, COUNT(*) as count 
FROM sms_log 
GROUP BY status;

-- 5. View recent SMS logs (last 10)
SELECT 
    id,
    phone_number,
    LEFT(message, 50) as message_preview,
    status,
    message_id,
    error_code,
    timestamp
FROM sms_log 
ORDER BY timestamp DESC 
LIMIT 10;

-- 6. Check SMS configuration
SELECT * FROM sms_config;

-- 7. If table is empty, manually insert a test log entry
-- INSERT INTO sms_log (phone_number, message, status, message_id, error_code, timestamp) 
-- VALUES ('639171234567', 'Test message', 'SENT', 'test123', NULL, CURRENT_TIMESTAMP);

-- 8. Check if there are any constraints or issues
SELECT * FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS 
WHERE TABLE_NAME = 'SMS_LOG';
