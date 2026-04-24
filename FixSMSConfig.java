import java.sql.*;

/**
 * Fix SMS Configuration - Update API Key
 */
public class FixSMSConfig {
    
    public static void main(String[] args) {
        String dbUrl = "jdbc:h2:~/bdms_v2";
        String user = "sa";
        String password = "";
        
        System.out.println("=== Fixing SMS Configuration ===\n");
        
        try (Connection conn = DriverManager.getConnection(dbUrl, user, password)) {
            
            // Check current config
            System.out.println("Current Configuration:");
            String selectSql = "SELECT * FROM sms_config";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(selectSql)) {
                
                if (rs.next()) {
                    System.out.println("  API Key: " + maskApiKey(rs.getString("api_key")));
                    System.out.println("  URL: " + rs.getString("api_base_url"));
                    System.out.println("  Enabled: " + rs.getBoolean("enabled"));
                }
            }
            
            // Update with correct API key
            System.out.println("\nUpdating to correct API key...");
            String updateSql = "UPDATE sms_config SET api_key = ?, api_base_url = ?, enabled = ?, last_updated = CURRENT_TIMESTAMP";
            
            try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                pstmt.setString(1, "sk_6bb6f6f6-391a-4b34-8146-d38bacf73d4b");
                pstmt.setString(2, "https://unismsapi.com/api");
                pstmt.setBoolean(3, true);
                
                int updated = pstmt.executeUpdate();
                System.out.println("  ✓ Updated " + updated + " record(s)");
            }
            
            // Verify
            System.out.println("\nNew Configuration:");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(selectSql)) {
                
                if (rs.next()) {
                    String apiKey = rs.getString("api_key");
                    String url = rs.getString("api_base_url");
                    boolean enabled = rs.getBoolean("enabled");
                    
                    System.out.println("  API Key: " + maskApiKey(apiKey));
                    System.out.println("  URL: " + url);
                    System.out.println("  Enabled: " + enabled);
                    
                    if (apiKey.equals("sk_6bb6f6f6-391a-4b34-8146-d38bacf73d4b") && 
                        url.equals("https://unismsapi.com/api") && enabled) {
                        System.out.println("\n✅ Configuration is correct!");
                    } else {
                        System.err.println("\n⚠️  Configuration mismatch!");
                    }
                }
            }
            
            System.out.println("\n=== Fix Complete ===");
            
        } catch (SQLException e) {
            System.err.println("❌ Error: " + e.getMessage());
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
