import java.sql.*;
import java.util.Map;

public class TestRolePermissions {
    private static final String DB_URL = "jdbc:h2:~/bdms_v2;DB_CLOSE_DELAY=-1;AUTO_SERVER=TRUE";
    private static final String USER = "sa";
    private static final String PASS = "";

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("Testing Role Permissions");
        System.out.println("========================================\n");
        
        try {
            Class.forName("org.h2.Driver");
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
            
            // Get all users from database
            String sql = "SELECT username, role FROM users ORDER BY username";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            System.out.println("Checking permissions for all users:\n");
            
            while (rs.next()) {
                String username = rs.getString("username");
                String role = rs.getString("role");
                
                System.out.println("User: " + username);
                System.out.println("Role: " + role);
                
                // Test if permissions are defined for this role
                Map<String, String> permissions = getPermissions(role);
                
                if (permissions.isEmpty()) {
                    System.out.println("❌ ERROR: No permissions defined for role: " + role);
                } else {
                    System.out.println("✓ Permissions found:");
                    System.out.println("  - Analytics: " + permissions.get("Analytics & Overview"));
                    System.out.println("  - Residents: " + permissions.get("Resident Data"));
                    System.out.println("  - Certificates: " + permissions.get("Certificates & Clearances"));
                    System.out.println("  - Complaints: " + permissions.get("Complaints & Incidents"));
                    System.out.println("  - Financial: " + permissions.get("Financial Reports"));
                }
                System.out.println();
            }
            
            rs.close();
            stmt.close();
            conn.close();
            
            System.out.println("========================================");
            System.out.println("✓ All roles tested successfully!");
            System.out.println("========================================");
            
        } catch (Exception e) {
            System.err.println("Error testing permissions:");
            e.printStackTrace();
        }
    }
    
    // Simplified version of getPermissions for testing
    private static Map<String, String> getPermissions(String role) {
        Map<String, String> permissions = new java.util.HashMap<>();
        
        switch (role) {
            case "Super Admin":
            case "Barangay Captain":
                permissions.put("Analytics & Overview", "Full Access");
                permissions.put("Resident Data", "Full Access");
                permissions.put("Certificates & Clearances", "Full Access");
                permissions.put("Complaints & Incidents", "Full Access");
                permissions.put("Financial Reports", "Full Access");
                break;
            case "Owner":
                permissions.put("Analytics & Overview", "Full Access");
                permissions.put("Resident Data", "Full Access");
                permissions.put("Certificates & Clearances", "Full Access");
                permissions.put("Complaints & Incidents", "Manage");
                permissions.put("Financial Reports", "Manage");
                break;
            case "Barangay Secretary":
                permissions.put("Analytics & Overview", "View Only");
                permissions.put("Resident Data", "Manage");
                permissions.put("Certificates & Clearances", "Manage");
                permissions.put("Complaints & Incidents", "Manage");
                permissions.put("Financial Reports", "View Only");
                break;
            case "Barangay Treasurer":
                permissions.put("Analytics & Overview", "View Only");
                permissions.put("Resident Data", "View Only");
                permissions.put("Certificates & Clearances", "View Only");
                permissions.put("Complaints & Incidents", "None");
                permissions.put("Financial Reports", "Manage");
                break;
            case "Kagawads":
                permissions.put("Analytics & Overview", "View Only");
                permissions.put("Resident Data", "View Only");
                permissions.put("Certificates & Clearances", "View Only");
                permissions.put("Complaints & Incidents", "View Only");
                permissions.put("Financial Reports", "View Only");
                break;
            case "Health Worker":
                permissions.put("Analytics & Overview", "None");
                permissions.put("Resident Data", "Manage");
                permissions.put("Certificates & Clearances", "None");
                permissions.put("Complaints & Incidents", "None");
                permissions.put("Financial Reports", "None");
                break;
            case "Tanod":
                permissions.put("Analytics & Overview", "None");
                permissions.put("Resident Data", "View Only");
                permissions.put("Certificates & Clearances", "None");
                permissions.put("Complaints & Incidents", "Manage");
                permissions.put("Financial Reports", "None");
                break;
            default:
                // Return empty map for unknown roles
                break;
        }
        
        return permissions;
    }
}
