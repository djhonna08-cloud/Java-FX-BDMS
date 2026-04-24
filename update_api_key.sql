-- Update SMS Configuration with your UniSMS API Key
-- API Key: sk_6bb6f6f6-391a-4b34-8146-d38bacf73d4b

-- Clear existing configuration
DELETE FROM sms_config;

-- Insert new configuration with your API key
INSERT INTO sms_config (api_key, api_base_url, sender_name, enabled, last_updated) 
VALUES (
    'sk_6bb6f6f6-391a-4b34-8146-d38bacf73d4b',
    'https://unismsapi.com/api',
    'BDMS',
    TRUE,
    CURRENT_TIMESTAMP
);

-- Verify configuration
SELECT 
    SUBSTRING(api_key, 1, 10) as key_start,
    api_base_url,
    sender_name,
    enabled
FROM sms_config;

-- Expected output:
-- key_start: sk_6bb6f6f
-- api_base_url: https://unismsapi.com/api
-- sender_name: BDMS
-- enabled: TRUE
