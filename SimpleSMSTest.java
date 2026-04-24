import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Simple SMS Test - Direct API call without database logging
 */
public class SimpleSMSTest {
    
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║   Simple UniSMS API Test              ║");
        System.out.println("╚════════════════════════════════════════╝\n");
        
        String apiKey = "sk_6bb6f6f6-391a-4b34-8146-d38bacf73d4b";
        String apiUrl = "https://unismsapi.com/api/sms";
        String phoneNumber = "+639563052862";
        String message = "Your barangay clearance document has been approved and is ready for pickup. Please visit our office during business hours. Thank you!";
        
        System.out.println("Configuration:");
        System.out.println("  API Key: " + maskApiKey(apiKey));
        System.out.println("  API URL: " + apiUrl);
        System.out.println("  Phone: " + phoneNumber);
        System.out.println("  Message: " + message.substring(0, Math.min(50, message.length())) + "...");
        System.out.println();
        
        try {
            // Build JSON request
            String jsonBody = String.format(
                "{\"recipient\":\"%s\",\"content\":\"%s\"}",
                phoneNumber,
                escapeJson(message)
            );
            
            System.out.println("📤 Sending SMS...\n");
            System.out.println("=== API Request ===");
            System.out.println("URL: " + apiUrl);
            System.out.println("Method: POST");
            System.out.println("Auth: Basic " + maskApiKey(apiKey) + ":");
            System.out.println("Body: " + jsonBody);
            System.out.println();
            
            // Send HTTP POST request
            byte[] postDataBytes = jsonBody.getBytes(StandardCharsets.UTF_8);
            
            // Create Basic Auth header
            String authString = apiKey + ":";
            String encodedAuth = Base64.getEncoder().encodeToString(authString.getBytes(StandardCharsets.UTF_8));
            
            // Create connection
            URL url = new URL(apiUrl);
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
            
            // Display response
            System.out.println("=== API Response ===");
            System.out.println("Status Code: " + responseCode);
            System.out.println("Response Body: " + response.toString());
            System.out.println();
            
            // Analyze result
            System.out.println("==================================================");
            if (responseCode == 201 || responseCode == 200) {
                System.out.println("🎉 SUCCESS! SMS sent successfully!");
                System.out.println("==================================================");
                System.out.println();
                System.out.println("📱 Check your phone: " + phoneNumber);
                System.out.println("   SMS should arrive within 1-5 minutes");
                System.out.println();
                
                // Try to extract message ID
                String responseStr = response.toString();
                if (responseStr.contains("reference_id")) {
                    int start = responseStr.indexOf("reference_id\":\"") + 15;
                    int end = responseStr.indexOf("\"", start);
                    if (end > start) {
                        String messageId = responseStr.substring(start, end);
                        System.out.println("🆔 Message ID: " + messageId);
                    }
                }
                
            } else {
                System.out.println("❌ FAILED! SMS was not sent");
                System.out.println("==================================================");
                System.out.println();
                System.out.println("Error Code: " + responseCode);
                System.out.println("Error Message: " + response.toString());
                System.out.println();
                
                // Provide troubleshooting
                if (responseCode == 401) {
                    System.err.println("🔍 Troubleshooting:");
                    System.err.println("  ❌ Authentication Failed (401 Unauthorized)");
                    System.err.println("  → Your API key is invalid or expired");
                    System.err.println("  → Verify key at: https://unismsapi.com/dashboard");
                    System.err.println("  → Make sure you copied the full key");
                } else if (responseCode == 402) {
                    System.err.println("🔍 Troubleshooting:");
                    System.err.println("  ❌ Insufficient Credits (402 Payment Required)");
                    System.err.println("  → Login to https://unismsapi.com");
                    System.err.println("  → Go to Billing → Add Credits");
                } else if (responseCode == 422) {
                    System.err.println("🔍 Troubleshooting:");
                    System.err.println("  ❌ Invalid Request (422 Unprocessable Entity)");
                    System.err.println("  → Check phone number format");
                    System.err.println("  → Verify message content");
                } else {
                    System.err.println("🔍 Troubleshooting:");
                    System.err.println("  ❌ Unknown Error (" + responseCode + ")");
                    System.err.println("  → Check API documentation");
                    System.err.println("  → Contact UniSMS support");
                }
            }
            
            System.out.println();
            System.out.println("==================================================");
            
        } catch (Exception e) {
            System.err.println("❌ Exception occurred:");
            e.printStackTrace();
        }
    }
    
    private static String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() < 10) {
            return "***";
        }
        String prefix = apiKey.substring(0, Math.min(10, apiKey.length()));
        String suffix = apiKey.substring(Math.max(0, apiKey.length() - 4));
        return prefix + "..." + suffix;
    }
    
    private static String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}
