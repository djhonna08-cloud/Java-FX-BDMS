w-- Configure UniSMS API in BDMS
-- Run this script to set up your UniSMS API key

-- Step 1: Clear any existing SMS configuration
DELETE FROM sms_config;

-- Step 2: Insert UniSMS configuration
-- REPLACE 'YOUR_UNISMS_API_KEY' with your actual API key (starts with sk_)
INSERT INTO sms_config (api_key, api_base_url, sender_name, enabled, last_updated) 
VALUES (
    'YOUR_UNISMS_API_KEY',  -- Replace with your actual UniSMS API key
    'https://unismsapi.com/api',
    'BDMS',
    TRUE,
    CURRENT_TIMESTAMP
);

-- Step 3: Verify configuration
SELECT * FROM sms_config;

-- Step 4: Check if phone_number column exists in residents table
SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'RESIDENTS' AND COLUMN_NAME = 'PHONE_NUMBER';

-- Step 5: If phone_number column doesn't exist, add it
-- ALTER TABLE residents ADD COLUMN phone_number VARCHAR(20);

-- Step 6: View SMS templates
SELECT id, name, category FROM sms_templates ORDER BY category, name;

-- Step 7: Check SMS log (should be empty initially)
SELECT * FROM sms_log ORDER BY sent_at DESC LIMIT 10;
