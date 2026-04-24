-- Check SMS Configuration and Last Error

-- 1. Check SMS Configuration
SELECT 
    SUBSTRING(api_key, 1, 3) as key_prefix,
    SUBSTRING(api_key, 1, 10) as key_start,
    LENGTH(api_key) as key_length,
    api_base_url,
    sender_name,
    enabled,
    last_updated
FROM sms_config;

-- 2. Check Last SMS Error
SELECT 
    id,
    phone_number,
    LEFT(message, 50) as message_preview,
    status,
    message_id,
    error_code,
    sent_at
FROM sms_log 
ORDER BY sent_at DESC 
LIMIT 5;

-- 3. Count SMS by Status
SELECT 
    status,
    COUNT(*) as count
FROM sms_log
GROUP BY status;
