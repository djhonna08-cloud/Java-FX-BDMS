package com.example;

/**
 * UniSMS API Test Utility
 * Tests SMS sending with your configured API key
 */
public class TestUniSMS {
    
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║   UniSMS API Test Utility             ║");
        System.out.println("╚════════════════════════════════════════╝\n");
        
        // Step 1: Verify Configuration
        System.out.println("📋 Step 1: Verifying Configuration...");
        String apiKey = DatabaseHelper.getSMSApiKey();
        String apiBaseUrl = DatabaseHelper.getSMSApiBaseUrl();
        boolean isEnabled = DatabaseHelper.isSMSEnabled();
        
        if (apiKey == null || apiKey.trim().isEmpty()) {
            System.err.println("❌ ERROR: No API key configured!");
            System.err.println("\n🔧 Fix: Run test_unisms_complete.sql");
            return;
        }
        
        System.out.println("   ✓ API Key: " + maskApiKey(apiKey));
        System.out.println("   ✓ Base URL: " + apiBaseUrl);
        System.out.println("   ✓ Enabled: " + isEnabled);
        
        if (!apiKey.startsWith("sk_")) {
            System.err.println("\n⚠️  WARNING: API key doesn't start with 'sk_'");
            System.err.println("   UniSMS keys should start with 'sk_'");
            System.err.println("   Your key starts with: " + apiKey.substring(0, Math.min(3, apiKey.length())));
        }
        
        if (!isEnabled) {
            System.err.println("\n❌ ERROR: SMS is disabled!");
            System.err.println("🔧 Fix: UPDATE sms_config SET enabled = TRUE;");
            return;
        }
        
        // Step 2: Get Test Phone Number
        System.out.println("\n📱 Step 2: Preparing Test SMS...");
        
        // Change this to your phone number for testing
        String testPhone = "09563052862"; // Your phone number
        
        System.out.println("   Recipient: " + testPhone);
        
        // Step 3: Prepare Test Message
        String message = "Hello from BDMS! 🎉\n\n" +
                        "This is a test message from your Barangay Document Management System.\n\n" +
                        "If you receive this, your UniSMS integration is working perfectly!\n\n" +
                        "Powered by UniSMS API";
        
        System.out.println("   Message length: " + message.length() + " characters");
        
        if (message.length() > 160) {
            System.out.println("   ⚠️  Message is longer than 160 chars (will use multiple SMS credits)");
        }
        
        // Step 4: Send Test SMS
        System.out.println("\n📤 Step 3: Sending Test SMS...");
        System.out.println("   Please wait...\n");
        
        long startTime = System.currentTimeMillis();
        SMSService.SMSResponse response = SMSService.sendSMS(testPhone, message);
        long endTime = System.currentTimeMillis();
        
        System.out.println("\n" + "=".repeat(50));
        System.out.println("📊 RESULT");
        System.out.println("=".repeat(50));
        
        System.out.println("Success: " + (response.isSuccess() ? "✅ YES" : "❌ NO"));
        System.out.println("Message: " + response.getMessage());
        System.out.println("Message ID: " + (response.getMessageId() != null ? response.getMessageId() : "N/A"));
        System.out.println("Error Code: " + (response.getErrorCode() != null ? response.getErrorCode() : "N/A"));
        System.out.println("Time taken: " + (endTime - startTime) + "ms");
        
        System.out.println("=".repeat(50));
        
        if (response.isSuccess()) {
            System.out.println("\n🎉 SUCCESS! SMS sent successfully!");
            System.out.println("\n📱 Check your phone: " + testPhone);
            System.out.println("   SMS should arrive within 1-5 minutes");
            System.out.println("\n🆔 Message ID: " + response.getMessageId());
            System.out.println("   Use this ID to track delivery status");
            
            System.out.println("\n📊 Check SMS Log:");
            System.out.println("   SELECT * FROM sms_log ORDER BY sent_at DESC LIMIT 1;");
            
        } else {
            System.err.println("\n❌ FAILED! SMS was not sent.");
            System.err.println("\n🔍 Error Details:");
            System.err.println("   Message: " + response.getMessage());
            System.err.println("   Code: " + response.getErrorCode());
            
            System.err.println("\n🔧 Troubleshooting:");
            
            if (response.getErrorCode() != null) {
                if (response.getErrorCode().contains("401") || response.getErrorCode().contains("Unauthorized")) {
                    System.err.println("   ❌ Authentication Failed");
                    System.err.println("   → Check if API key is correct");
                    System.err.println("   → Verify key at https://unismsapi.com/dashboard");
                    
                } else if (response.getErrorCode().contains("402") || response.getMessage().contains("credit")) {
                    System.err.println("   ❌ Insufficient Credits");
                    System.err.println("   → Login to https://unismsapi.com");
                    System.err.println("   → Go to Billing → Add Credits");
                    
                } else if (response.getErrorCode().contains("422") || response.getErrorCode().contains("INVALID")) {
                    System.err.println("   ❌ Invalid Request");
                    System.err.println("   → Check phone number format");
                    System.err.println("   → Verify message content");
                    
                } else if (response.getErrorCode().contains("API_ERROR")) {
                    System.err.println("   ❌ API Error");
                    System.err.println("   → Check console output for API response");
                    System.err.println("   → Verify API endpoint URL");
                    
                } else {
                    System.err.println("   ❌ Unknown Error: " + response.getErrorCode());
                    System.err.println("   → Check console output for details");
                    System.err.println("   → Contact UniSMS support");
                }
            }
            
            System.err.println("\n📊 Check SMS Log for details:");
            System.err.println("   SELECT * FROM sms_log ORDER BY sent_at DESC LIMIT 1;");
        }
        
        System.out.println("\n" + "=".repeat(50));
        System.out.println("Test completed!");
        System.out.println("=".repeat(50));
    }
    
    private static String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() < 10) {
            return "***";
        }
        String prefix = apiKey.substring(0, Math.min(10, apiKey.length()));
        String suffix = apiKey.substring(Math.max(0, apiKey.length() - 4));
        return prefix + "..." + suffix;
    }
}
