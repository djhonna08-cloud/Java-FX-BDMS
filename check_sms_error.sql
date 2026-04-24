-- Check the SMS error details
SELECT 
    id,
    phone_number,
    message,
    status,
    message_id,
    error_code,
    sent_at
FROM sms_log 
WHERE phone_number = '639563052862'
ORDER BY sent_at DESC 
LIMIT 1;
