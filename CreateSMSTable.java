import java.sql.*;

/**
 * Create SMS Log Table with correct syntax
 */
public class CreateSMSTable {
    
    public static void main(String[] args) {
        String dbUrl = "jdbc:h2:~/bdms_v2";
        String user = "sa";
        String password = "";
        
        System.out.println("=== Creating SMS Log Table ===\n");
        
        try (Connection conn = DriverManager.getConnection(dbUrl, user, password);
             Statement stmt = conn.createStatement()) {
            
            // Drop existing table if it has issues
            try {
                stmt.execute("DROP TABLE IF EXISTS sms_log");
                System.out.println("✓ Dropped old sms_log table");
            } catch (SQLException e) {
                System.out.println("  (No old table to drop)");
            }
            
            // Create SMS log table with correct syntax
            String createTable = "CREATE TABLE sms_log (" +
                    "id INTEGER PRIMARY KEY AUTO_INCREMENT, " +
                    "phone_number VARCHAR(50) NOT NULL, " +
                    "message VARCHAR(1000) NOT NULL, " +
                    "status VARCHAR(20) NOT NULL, " +
                    "message_id VARCHAR(50), " +
                    "error_code VARCHAR(100), " +
                    "sent_at DATETIME DEFAULT CURRENT_TIMESTAMP)";
            
            stmt.execute(createTable);
            System.out.println("✓ Created sms_log table");
            
            // Create indexes
            stmt.execute("CREATE INDEX idx_sms_log_sent_at ON sms_log(sent_at)");
            System.out.println("✓ Created index on sent_at");
            
            stmt.execute("CREATE INDEX idx_sms_log_status ON sms_log(status)");
            System.out.println("✓ Created index on status");
            
            // Verify table structure
            System.out.println("\nTable Structure:");
            ResultSet rs = stmt.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'SMS_LOG' ORDER BY ORDINAL_POSITION");
            
            while (rs.next()) {
                System.out.println("  " + rs.getString("COLUMN_NAME") + " - " + rs.getString("TYPE_NAME"));
            }
            
            System.out.println("\n✅ SMS Log Table Created Successfully!");
            
        } catch (SQLException e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
