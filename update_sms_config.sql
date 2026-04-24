-- Quick SMS Configuration Update Script
-- Copy this and replace YOUR_API_KEY with your actual UniSMS API key

-- Method 1: Update existing configuration
UPDATE sms_config 
SET api_key = 'YOUR_API_KEY',
    api_base_url = 'https://unismsapi.com/api',
    enabled = TRUE,
    last_updated = CURRENT_TIMESTAMP;

-- Method 2: If update doesn't work (no rows), insert new configuration
INSERT INTO sms_config (api_key, api_base_url, sender_name, enabled, last_updated) 
SELECT 'YOUR_API_KEY', 'https://unismsapi.com/api', 'BDMS', TRUE, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sms_config);

-- Verify the configuration
SELECT 
    CONCAT(SUBSTRING(api_key, 1, 6), '...', SUBSTRING(api_key, LENGTH(api_key)-3, 4)) as masked_api_key,
    api_base_url,
    sender_name,
    enabled,
    last_updated
FROM sms_config;
