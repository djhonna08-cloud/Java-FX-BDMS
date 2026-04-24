package com.example;

/**
 * Simple SMS Testing Utility
 * Use this to test your UniSMS API integration
 */
public class TestSMS {
    
    public static void main(String[] args) {
        System.out.println("=== UniSMS API Test Utility ===\n");
        
        // Step 1: Check SMS Configuration
        System.out.println("Step 1: Checking SMS Configuration...");
        String apiKey = DatabaseHelper.getSMSApiKey();
        String apiBaseUrl = DatabaseHelper.getSMSApiBaseUrl();
        boolean isEnabled = DatabaseHelper.isSMSEnabled();
        
        if (apiKey == null || apiKey.trim().isEmpty()) {
            System.err.println("❌ ERROR: SMS API key not configured!");
            System.err.println("\nPlease run configure_unisms.sql and set your API key.");
            System.err.println("Example:");
            System.err.println("  UPDATE sms_config SET api_key = 'sk_YOUR_KEY_HERE';");
            return;
        }
        
        System.out.println("✓ API Key: " + maskApiKey(apiKey));
        System.out.println("✓ API Base URL: " + apiBaseUrl);
        System.out.println("✓ SMS Enabled: " + isEnabled);
        
        if (!isEnabled) {
            System.err.println("\n❌ WARNING: SMS is disabled in configuration!");
            System.err.println("Enable it with: UPDATE sms_config SET enabled = TRUE;");
            return;
        }
        
        // Step 2: Validate Phone Number
        System.out.println("\nStep 2: Testing Phone Number Validation...");
        String testPhone = "09171234567"; // Change this to your phone number
        
        System.out.println("Testing phone: " + testPhone);
        
        // Step 3: Send Test SMS
        System.out.println("\nStep 3: Sending Test SMS...");
        System.out.println("Recipient: " + testPhone);
        
        String message = "Hello! This is a test message from Barangay BDMS using UniSMS API. " +
                        "If you receive this, SMS integration is working! 🎉";
        
        System.out.println("Message: " + message);
        System.out.println("\nSending...");
        
        SMSService.SMSResponse response = SMSService.sendSMS(testPhone, message);
        
        // Step 4: Display Results
        System.out.println("\n=== RESULT ===");
        System.out.println("Success: " + response.isSuccess());
        System.out.println("Message: " + response.getMessage());
        System.out.println("Message ID: " + response.getMessageId());
        System.out.println("Error Code: " + response.getErrorCode());
        
        if (response.isSuccess()) {
            System.out.println("\n✅ SUCCESS! SMS sent successfully!");
            System.out.println("📱 Check your phone for the message.");
            System.out.println("🆔 Message ID: " + response.getMessageId());
            System.out.println("\nNote: SMS delivery may take 1-5 minutes.");
        } else {
            System.err.println("\n❌ FAILED! SMS was not sent.");
            System.err.println("Error: " + response.getMessage());
            System.err.println("Error Code: " + response.getErrorCode());
            
            // Provide troubleshooting tips
            System.err.println("\n🔍 Troubleshooting:");
            if (response.getErrorCode().equals("API_ERROR")) {
                System.err.println("- Check if your API key is correct");
                System.err.println("- Verify your UniSMS account has credits");
                System.err.println("- Check if API key starts with 'sk_'");
            } else if (response.getErrorCode().equals("INVALID_NUMBER")) {
                System.err.println("- Use Philippine mobile number format: 09XXXXXXXXX");
                System.err.println("- Must be 11 digits starting with 09");
            }
        }
        
        // Step 5: Check SMS Log
        System.out.println("\n=== SMS LOG ===");
        System.out.println("Check database with:");
        System.out.println("  SELECT * FROM sms_log ORDER BY sent_at DESC LIMIT 5;");
    }
    
    /**
     * Mask API key for security (show only first 6 and last 4 characters)
     */
    private static String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() < 10) {
            return "***";
        }
        String prefix = apiKey.substring(0, Math.min(6, apiKey.length()));
        String suffix = apiKey.substring(Math.max(0, apiKey.length() - 4));
        return prefix + "..." + suffix;
    }
}
