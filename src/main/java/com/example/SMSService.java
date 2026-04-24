package com.example;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * SMS Service using UniSMS API (Philippines)
 * API Documentation: https://unismsapi.com/api
 * 
 * Features:
 * - Send SMS notifications (paid service)
 * - Philippine numbers only
 * - Track SMS delivery status
 * - Log all SMS transactions
 * - Custom sender ID support (for verified businesses)
 */
public class SMSService {
    
    private static final String DEFAULT_API_BASE_URL = "https://unismsapi.com/api";
    private static final String SEND_SMS_ENDPOINT = "/sms";
    
    // Rate limit: Adjust based on your UniSMS plan
    private static final int RATE_LIMIT_SECONDS = 1;
    private static long lastRequestTime = 0;
    
    /**
     * Send a standard SMS message
     * 
     * @param phoneNumber Philippine mobile number (e.g., "09171234567" or "+639171234567")
     * @param message Message content (max 160 characters for 1 credit)
     * @return SMSResponse containing status and message ID
     */
    public static SMSResponse sendSMS(String phoneNumber, String message) {
        return sendSMS(phoneNumber, message, null);
    }
    
    /**
     * Send a standard SMS message with custom sender ID
     * 
     * @param phoneNumber Philippine mobile number
     * @param message Message content
     * @param senderName Custom sender ID (only for verified businesses)
     * @return SMSResponse containing status and message ID
     */
    public static SMSResponse sendSMS(String phoneNumber, String message, String senderName) {
        // Get API key and base URL from database
        String apiKey = DatabaseHelper.getSMSApiKey();
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return new SMSResponse(false, "SMS API key not configured", null, "API_KEY_MISSING");
        }
        
        String apiBaseUrl = DatabaseHelper.getSMSApiBaseUrl();
        if (apiBaseUrl == null || apiBaseUrl.trim().isEmpty()) {
            apiBaseUrl = DEFAULT_API_BASE_URL;
        }
        
        // Check if SMS is enabled
        if (!DatabaseHelper.isSMSEnabled()) {
            return new SMSResponse(false, "SMS notifications are disabled", null, "SMS_DISABLED");
        }
        
        // Validate phone number
        phoneNumber = normalizePhoneNumber(phoneNumber);
        if (!isValidPhilippineNumber(phoneNumber)) {
            return new SMSResponse(false, "Invalid Philippine mobile number", null, "INVALID_NUMBER");
        }
        
        // Check rate limit
        if (!checkRateLimit()) {
            return new SMSResponse(false, "Rate limit exceeded (1 message per second)", null, "RATE_LIMIT");
        }
        
        try {
            // Build JSON request body for UniSMS API
            // Format: {"recipient":"+639171234567","content":"Your message","sender_id":"MySender"}
            // Note: UniSMS requires E.164 format with + prefix
            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("{\"recipient\":\"+").append(phoneNumber).append("\"");
            jsonBuilder.append(",\"content\":\"").append(escapeJson(message)).append("\"");
            
            // Add sender_id if provided (only for verified businesses)
            if (senderName != null && !senderName.trim().isEmpty()) {
                jsonBuilder.append(",\"sender_id\":\"").append(escapeJson(senderName)).append("\"");
            }
            
            jsonBuilder.append("}");
            String jsonBody = jsonBuilder.toString();
            
            System.out.println("📤 Sending SMS to: +" + phoneNumber);
            
            // Send HTTP POST request with Basic Auth
            String response = sendBasicAuthPostRequest(apiBaseUrl + SEND_SMS_ENDPOINT, apiKey, jsonBody);
            
            // Parse response
            SMSResponse smsResponse = parseResponse(response);
            
            // Log SMS transaction with better error handling
            try {
                DatabaseHelper.logSMS(phoneNumber, message, smsResponse.isSuccess() ? "SENT" : "FAILED", 
                                     smsResponse.getMessageId(), smsResponse.getErrorCode());
                System.out.println("✓ SMS logged to database: " + phoneNumber + " - " + (smsResponse.isSuccess() ? "SENT" : "FAILED"));
            } catch (Exception logEx) {
                System.err.println("✗ Failed to log SMS to database: " + logEx.getMessage());
                logEx.printStackTrace();
            }
            
            return smsResponse;
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("✗ Exception sending SMS: " + e.getMessage());
            
            // Try to log the error
            try {
                DatabaseHelper.logSMS(phoneNumber, message, "ERROR", null, e.getMessage());
                System.out.println("✓ Error logged to database");
            } catch (Exception logEx) {
                System.err.println("✗ Failed to log error to database: " + logEx.getMessage());
            }
            
            return new SMSResponse(false, "Failed to send SMS: " + e.getMessage(), null, "EXCEPTION");
        }
    }
    
    /**
     * Send a priority SMS message (same as regular SMS in UniSMS)
     * 
     * @param phoneNumber Philippine mobile number
     * @param message Message content
     * @return SMSResponse containing status and message ID
     */
    public static SMSResponse sendPrioritySMS(String phoneNumber, String message) {
        // UniSMS doesn't have separate priority endpoint, use regular SMS
        SMSResponse response = sendSMS(phoneNumber, message);
        
        // Update log status to indicate it was requested as priority
        if (response.isSuccess()) {
            DatabaseHelper.logSMS(phoneNumber, message, "SENT_PRIORITY", 
                                 response.getMessageId(), response.getErrorCode());
        }
        
        return response;
    }
    
    /**
     * Send an OTP (One-Time Password) SMS
     * Note: UniSMS has a dedicated /otp endpoint, but we'll use regular SMS for simplicity
     * 
     * @param phoneNumber Philippine mobile number
     * @param otp OTP code (usually 4-6 digits)
     * @param message Custom message template (use {otp} placeholder)
     * @return SMSResponse containing status and message ID
     */
    public static SMSResponse sendOTP(String phoneNumber, String otp, String message) {
        // Replace {otp} placeholder with actual OTP
        String finalMessage = message.replace("{otp}", otp);
        
        // Send using regular SMS endpoint
        SMSResponse response = sendSMS(phoneNumber, finalMessage);
        
        // Update log status to indicate it was an OTP
        if (response.isSuccess()) {
            DatabaseHelper.logSMS(phoneNumber, finalMessage, "SENT_OTP", 
                                 response.getMessageId(), response.getErrorCode());
        }
        
        return response;
    }
    
    /**
     * Send bulk SMS to multiple recipients
     * Note: UniSMS has /blast and /bulk endpoints, but we'll send sequentially for simplicity
     * 
     * @param phoneNumbers Array of Philippine mobile numbers
     * @param message Message content
     * @return SMSResponse containing status and summary
     */
    public static SMSResponse sendBulkSMS(String[] phoneNumbers, String message) {
        if (phoneNumbers == null || phoneNumbers.length == 0) {
            return new SMSResponse(false, "No phone numbers provided", null, "NO_RECIPIENTS");
        }
        
        String apiKey = DatabaseHelper.getSMSApiKey();
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return new SMSResponse(false, "SMS API key not configured", null, "API_KEY_MISSING");
        }
        
        if (!DatabaseHelper.isSMSEnabled()) {
            return new SMSResponse(false, "SMS notifications are disabled", null, "SMS_DISABLED");
        }
        
        int successCount = 0;
        int failCount = 0;
        
        // Send to each recipient individually
        for (int i = 0; i < phoneNumbers.length; i++) {
            String phoneNumber = phoneNumbers[i];
            String normalized = normalizePhoneNumber(phoneNumber);
            
            if (!isValidPhilippineNumber(normalized)) {
                failCount++;
                continue;
            }
            
            SMSResponse response = sendSMS(normalized, message);
            if (response.isSuccess()) {
                successCount++;
            } else {
                failCount++;
            }
            
            // Wait between messages to respect rate limit (except for last message)
            if (i < phoneNumbers.length - 1) {
                try {
                    Thread.sleep(RATE_LIMIT_SECONDS * 1000L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        
        String resultMessage = String.format("Bulk SMS completed: %d sent, %d failed", successCount, failCount);
        
        // Log bulk operation
        String allNumbers = String.join(",", phoneNumbers);
        DatabaseHelper.logSMS(allNumbers, message, "SENT_BULK", 
                             String.valueOf(successCount), String.format("%d failed", failCount));
        
        return new SMSResponse(successCount > 0, resultMessage, 
                              String.valueOf(successCount), failCount > 0 ? "PARTIAL_FAILURE" : "SUCCESS");
    }
    
    /**
     * Normalize Philippine phone number to international format
     * Examples:
     * - 09171234567 → 639171234567
     * - +639171234567 → 639171234567
     * - 639171234567 → 639171234567
     */
    private static String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null) return "";
        
        // Remove all non-digit characters except +
        phoneNumber = phoneNumber.replaceAll("[^0-9+]", "");
        
        // Remove leading +
        if (phoneNumber.startsWith("+")) {
            phoneNumber = phoneNumber.substring(1);
        }
        
        // Convert 09XX to 639XX
        if (phoneNumber.startsWith("09") && phoneNumber.length() == 11) {
            phoneNumber = "63" + phoneNumber.substring(1);
        }
        
        // Ensure it starts with 63
        if (!phoneNumber.startsWith("63") && phoneNumber.length() == 10) {
            phoneNumber = "63" + phoneNumber;
        }
        
        return phoneNumber;
    }
    
    /**
     * Validate Philippine mobile number format
     * Valid formats: 639XXXXXXXXX (12 digits starting with 639)
     */
    private static boolean isValidPhilippineNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return false;
        }
        
        // Must be 12 digits starting with 63
        if (!phoneNumber.matches("^63\\d{10}$")) {
            return false;
        }
        
        // Must start with 639 (Philippine mobile prefix)
        return phoneNumber.startsWith("639");
    }
    
    /**
     * Check rate limit (1 message per second by default)
     */
    private static boolean checkRateLimit() {
        long currentTime = System.currentTimeMillis();
        long timeSinceLastRequest = currentTime - lastRequestTime;
        
        // Check if enough time has passed since last request
        if (timeSinceLastRequest < (RATE_LIMIT_SECONDS * 1000)) {
            return false;
        }
        
        lastRequestTime = currentTime;
        return true;
    }
    
    /**
     * Send HTTP POST request to UniSMS API with Basic Authentication
     * UniSMS uses Basic Auth: API_SECRET_KEY as username, empty password
     */
    private static String sendBasicAuthPostRequest(String urlString, String apiKey, String jsonBody) throws Exception {
        byte[] postDataBytes = jsonBody.getBytes(StandardCharsets.UTF_8);
        
        // Create Basic Auth header: Base64(API_KEY:)
        String authString = apiKey + ":";
        String encodedAuth = Base64.getEncoder().encodeToString(authString.getBytes(StandardCharsets.UTF_8));
        
        // Debug logging
        System.out.println("=== SMS API Request ===");
        System.out.println("URL: " + urlString);
        System.out.println("API Key: " + maskApiKey(apiKey));
        System.out.println("Request Body: " + jsonBody);
        
        // Create connection
        URL url;
        try {
            url = new URI(urlString).toURL();
        } catch (Exception e) {
            throw new Exception("Invalid URL: " + urlString, e);
        }
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Basic " + encodedAuth);
        conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
        conn.setDoOutput(true);
        
        // Send request
        try (OutputStream os = conn.getOutputStream()) {
            os.write(postDataBytes);
        }
        
        // Read response
        int responseCode = conn.getResponseCode();
        BufferedReader in;
        
        System.out.println("Response Code: " + responseCode);
        
        if (responseCode >= 200 && responseCode < 300) {
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            in = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }
        
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();
        
        System.out.println("Response Body: " + response.toString());
        System.out.println("======================");
        
        return response.toString();
    }
    
    /**
     * Mask API key for logging (show first 6 and last 4 characters)
     */
    private static String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() < 10) {
            return "***";
        }
        String prefix = apiKey.substring(0, Math.min(6, apiKey.length()));
        String suffix = apiKey.substring(Math.max(0, apiKey.length() - 4));
        return prefix + "..." + suffix;
    }
    
    /**
     * Escape special characters in JSON strings
     */
    private static String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
    
    /**
     * Parse UniSMS API response
     * Success response: {"message":{"status":"sent","reference_id":"msg_xxx",...}}
     * Error response: {"error":"Invalid credentials"} or {"errors":[...]}
     */
    private static SMSResponse parseResponse(String response) {
        try {
            // Check for error response
            if (response.contains("\"error\"") || response.contains("\"errors\"")) {
                String errorMessage = extractJsonValue(response, "error");
                if (errorMessage == null) {
                    errorMessage = "Failed to send SMS";
                }
                return new SMSResponse(false, errorMessage, null, "API_ERROR");
            }
            
            // Parse success response - extract message object
            if (response.contains("\"message\"")) {
                String referenceId = extractJsonValue(response, "reference_id");
                String status = extractJsonValue(response, "status");
                
                boolean isSuccess = "sent".equals(status) || "pending".equals(status);
                String message = isSuccess ? "SMS sent successfully" : "SMS failed to send";
                
                return new SMSResponse(isSuccess, message, referenceId, status != null ? status : "SUCCESS");
            }
            
            // Unknown response format
            return new SMSResponse(false, "Unknown response format: " + response, null, "UNKNOWN_RESPONSE");
            
        } catch (Exception e) {
            return new SMSResponse(false, "Failed to parse response: " + e.getMessage(), null, "PARSE_ERROR");
        }
    }
    
    /**
     * Extract value from JSON string (simple parser for specific fields)
     */
    private static String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int startIndex = json.indexOf(searchKey);
        if (startIndex == -1) {
            return null;
        }
        
        startIndex += searchKey.length();
        
        // Skip whitespace
        while (startIndex < json.length() && Character.isWhitespace(json.charAt(startIndex))) {
            startIndex++;
        }
        
        // Check if value is a string (starts with ")
        if (startIndex < json.length() && json.charAt(startIndex) == '"') {
            startIndex++; // Skip opening quote
            int endIndex = json.indexOf('"', startIndex);
            if (endIndex != -1) {
                return json.substring(startIndex, endIndex);
            }
        } else if (startIndex < json.length()) {
            // Value is a number or boolean
            int endIndex = startIndex;
            while (endIndex < json.length() && 
                   (Character.isLetterOrDigit(json.charAt(endIndex)) || 
                    json.charAt(endIndex) == '.' || 
                    json.charAt(endIndex) == '-')) {
                endIndex++;
            }
            return json.substring(startIndex, endIndex);
        }
        
        return null;
    }
    
    /**
     * SMS Response class
     */
    public static class SMSResponse {
        private final boolean success;
        private final String message;
        private final String messageId;
        private final String errorCode;
        
        public SMSResponse(boolean success, String message, String messageId, String errorCode) {
            this.success = success;
            this.message = message;
            this.messageId = messageId;
            this.errorCode = errorCode;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getMessageId() { return messageId; }
        public String getErrorCode() { return errorCode; }
        
        @Override
        public String toString() {
            return "SMSResponse{success=" + success + ", message='" + message + "', messageId='" + messageId + "', errorCode='" + errorCode + "'}";
        }
    }
}
