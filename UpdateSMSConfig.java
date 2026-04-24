import java.sql.*;

/**
 * Update SMS Configuration with UniSMS API Key
 */
public class UpdateSMSConfig {
    
    public static void main(String[] args) {
        String dbUrl = "jdbc:h2:~/bdms_v2";
        String user = "sa";
        String password = "";
        
        System.out.println("=== Updating SMS Configuration ===\n");
        
        try (Connection conn = DriverManager.getConnection(dbUrl, user, password)) {
            
            // Step 1: Delete existing config
            System.out.println("Step 1: Clearing existing configuration...");
            String deleteSql = "DELETE FROM sms_config";
            try (Statement stmt = conn.createStatement()) {
                int deleted = stmt.executeUpdate(deleteSql);
                System.out.println("   ✓ Deleted " + deleted + " old config(s)");
            }
            
            // Step 2: Insert new config
            System.out.println("\nStep 2: Inserting new configuration...");
            String insertSql = "INSERT INTO sms_config (api_key, api_base_url, sender_name, enabled, last_updated) " +
                              "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
            
            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setString(1, "sk_6bb6f6f6-391a-4b34-8146-d38bacf73d4b");
                pstmt.setString(2, "https://unismsapi.com/api");
                pstmt.setString(3, "BDMS");
                pstmt.setBoolean(4, true);
                
                int inserted = pstmt.executeUpdate();
                System.out.println("   ✓ Inserted " + inserted + " new config");
            }
            
            // Step 3: Verify configuration
            System.out.println("\nStep 3: Verifying configuration...");
            String selectSql = "SELECT * FROM sms_config";
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(selectSql)) {
                
                if (rs.next()) {
                    String apiKey = rs.getString("api_key");
                    String apiBaseUrl = rs.getString("api_base_url");
                    String senderName = rs.getString("sender_name");
                    boolean enabled = rs.getBoolean("enabled");
                    Timestamp lastUpdated = rs.getTimestamp("last_updated");
                    
                    System.out.println("\n" + "=".repeat(60));
                    System.out.println("SMS Configuration:");
                    System.out.println("=".repeat(60));
                    System.out.println("API Key: " + maskApiKey(apiKey));
                    System.out.println("API Base URL: " + apiBaseUrl);
                    System.out.println("Sender Name: " + senderName);
                    System.out.println("Enabled: " + enabled);
                    System.out.println("Last Updated: " + lastUpdated);
                    System.out.println("=".repeat(60));
                    
                    // Validation
                    System.out.println("\nValidation:");
                    System.out.println("   " + (apiKey.startsWith("sk_") ? "✓" : "✗") + " API key format");
                    System.out.println("   " + (apiBaseUrl.equals("https://unismsapi.com/api") ? "✓" : "✗") + " API base URL");
                    System.out.println("   " + (enabled ? "✓" : "✗") + " SMS enabled");
                    
                    if (apiKey.startsWith("sk_") && apiBaseUrl.equals("https://unismsapi.com/api") && enabled) {
                        System.out.println("\n✅ Configuration is correct! Ready to send SMS.");
                    } else {
                        System.err.println("\n⚠️  Configuration has issues. Please check the values.");
                    }
                    
                } else {
                    System.err.println("   ✗ No configuration found!");
                }
            }
            
            System.out.println("\n=== Configuration Update Complete ===");
            
        } catch (SQLException e) {
            System.err.println("❌ Database error: " + e.getMessage());
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
}
