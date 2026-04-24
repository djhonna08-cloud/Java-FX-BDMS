-- Complete UniSMS API Test Script
-- This script will set up and verify your SMS configuration

-- Step 1: Update API Key
DELETE FROM sms_config;

INSERT INTO sms_config (api_key, api_base_url, sender_name, enabled, last_updated) 
VALUES (
    'sk_6bb6f6f6-391a-4b34-8146-d38bacf73d4b',
    'https://unismsapi.com/api',
    'BDMS',
    TRUE,
    CURRENT_TIMESTAMP
);

-- Step 2: Verify Configuration
SELECT 
    'Configuration Check' as test_step,
    CASE 
        WHEN api_key LIKE 'sk_%' THEN '✓ API Key format correct'
        ELSE '✗ API Key format wrong'
    END as api_key_check,
    CASE 
        WHEN api_base_url = 'https://unismsapi.com/api' THEN '✓ URL correct'
        ELSE '✗ URL wrong: ' || api_base_url
    END as url_check,
    CASE 
        WHEN enabled = TRUE THEN '✓ SMS enabled'
        ELSE '✗ SMS disabled'
    END as enabled_check
FROM sms_config;

-- Step 3: Check if phone_number column exists
SELECT 
    'Database Schema Check' as test_step,
    CASE 
        WHEN COUNT(*) > 0 THEN '✓ phone_number column exists'
        ELSE '✗ phone_number column missing - run: ALTER TABLE residents ADD COLUMN phone_number VARCHAR(20);'
    END as schema_check
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'RESIDENTS' AND COLUMN_NAME = 'PHONE_NUMBER';

-- Step 4: Check SMS Templates
SELECT 
    'SMS Templates Check' as test_step,
    COUNT(*) || ' templates available' as template_count
FROM sms_templates;

-- Step 5: View Recent SMS Log
SELECT 
    'Recent SMS Log' as test_step,
    phone_number,
    status,
    error_code,
    sent_at
FROM sms_log 
ORDER BY sent_at DESC 
LIMIT 5;

-- Step 6: SMS Statistics
SELECT 
    'SMS Statistics' as test_step,
    status,
    COUNT(*) as count
FROM sms_log
GROUP BY status;

-- Ready to test!
SELECT '✓ Configuration complete! Ready to send test SMS.' as status;
