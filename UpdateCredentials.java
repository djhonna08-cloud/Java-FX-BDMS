import java.sql.*;

public class UpdateCredentials {
    private static final String DB_URL = "jdbc:h2:~/bdms_v2;DB_CLOSE_DELAY=-1;AUTO_SERVER=TRUE";
    private static final String USER = "sa";
    private static final String PASS = "";

    public static void main(String[] args) {
        System.out.println("Connecting to database...");
        
        try {
            // Load H2 driver explicitly
            Class.forName("org.h2.Driver");
            
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
            Statement stmt = conn.createStatement();
            
            System.out.println("✓ Connected to database");
            System.out.println("\nUpdating user credentials...\n");
            
            // Update existing users or insert if they don't exist
            String[][] users = {
                {"superadmin", "admin123", "Super Admin"},
                {"owner", "owner123", "Owner"},
                {"captain", "captain123", "Barangay Captain"},
                {"secretary", "secretary123", "Barangay Secretary"},
                {"treasurer", "treasurer123", "Barangay Treasurer"},
                {"kagawad", "kagawad123", "Kagawads"},
                {"healthworker", "health123", "Health Worker"},
                {"tanod", "tanod123", "Tanod"}
            };
            
            for (String[] user : users) {
                String username = user[0];
                String password = user[1];
                String role = user[2];
                
                // Check if user exists
                String checkSql = "SELECT COUNT(*) FROM users WHERE username = ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                checkStmt.setString(1, username);
                ResultSet rs = checkStmt.executeQuery();
                rs.next();
                int count = rs.getInt(1);
                
                if (count > 0) {
                    // Update existing user
                    String updateSql = "UPDATE users SET password = ?, role = ? WHERE username = ?";
                    PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                    updateStmt.setString(1, password);
                    updateStmt.setString(2, role);
                    updateStmt.setString(3, username);
                    updateStmt.executeUpdate();
                    System.out.println("✓ Updated: " + username + " / " + password + " (" + role + ")");
                    updateStmt.close();
                } else {
                    // Insert new user
                    String insertSql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
                    PreparedStatement insertStmt = conn.prepareStatement(insertSql);
                    insertStmt.setString(1, username);
                    insertStmt.setString(2, password);
                    insertStmt.setString(3, role);
                    insertStmt.executeUpdate();
                    System.out.println("✓ Created: " + username + " / " + password + " (" + role + ")");
                    insertStmt.close();
                }
                checkStmt.close();
            }
            
            System.out.println("\n✓ All credentials updated successfully!");
            System.out.println("\nYou can now login with:");
            System.out.println("  superadmin / admin123");
            System.out.println("  owner / owner123");
            System.out.println("  captain / captain123");
            System.out.println("  secretary / secretary123");
            System.out.println("  treasurer / treasurer123");
            System.out.println("  kagawad / kagawad123");
            System.out.println("  healthworker / health123");
            System.out.println("  tanod / tanod123");
            
            stmt.close();
            conn.close();
            
        } catch (ClassNotFoundException e) {
            System.err.println("Error: H2 Driver not found!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Error updating credentials:");
            e.printStackTrace();
        }
    }
}
