package com.example;

import java.sql.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.HashMap;
import java.util.Optional;
import java.util.Map;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.mindrot.jbcrypt.BCrypt;


public class DatabaseHelper {
    // Changed DB name to ensure fresh schema creation and added AUTO_SERVER for better locking handling
    private static final String DB_URL = "jdbc:h2:~/bdms_v2;DB_CLOSE_DELAY=-1;AUTO_SERVER=TRUE";
    private static final String USER = "sa";
    private static final String PASS = "";

    static {
        try {
            // Explicitly load H2 driver to prevent 'No suitable driver found' errors
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Failed to load H2 Driver: " + e.getMessage());
        }
        initializeDatabase();
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    private static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Create users table with VARCHAR(255) for BCrypt hashes
            String createUsers = "CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTO_INCREMENT, " +
                    "username VARCHAR(50) UNIQUE NOT NULL, " +
                    "password VARCHAR(255) NOT NULL, " +
                    "role VARCHAR(20) NOT NULL)";
            stmt.execute(createUsers);
            
            // Alter existing users table to support BCrypt hashes if needed
            try {
                stmt.execute("ALTER TABLE users ALTER COLUMN password VARCHAR(255)");
            } catch (SQLException ignored) {
                // Column already correct size or other non-critical error
            }

            // Create residents table
            String createResidents = "CREATE TABLE IF NOT EXISTS residents (" +
                    "id INTEGER PRIMARY KEY AUTO_INCREMENT, " +
                    "first_name VARCHAR(100) NOT NULL, " +
                    "middle_name VARCHAR(100), " +
                    "last_name VARCHAR(100) NOT NULL, " +
                    "birth_date VARCHAR(20), " +
                    "gender VARCHAR(10), " +
                    "address VARCHAR(500), " +
                    "image_path VARCHAR(500), " +
                    "role VARCHAR(50))";
            stmt.execute(createResidents);
            
            // Add phone_number column if it doesn't exist
            try {
                stmt.execute("ALTER TABLE residents ADD COLUMN IF NOT EXISTS phone_number VARCHAR(20)");
                System.out.println("✓ Phone number column added to residents table");
            } catch (SQLException ignored) {
                // Column already exists or other non-critical error
            }

            // Create audit_log table for tracking all system operations
            String createAuditLog = "CREATE TABLE IF NOT EXISTS audit_log (" +
                    "id INTEGER PRIMARY KEY AUTO_INCREMENT, " +
                    "timestamp VARCHAR(30) NOT NULL, " +
                    "username VARCHAR(50) DEFAULT 'System', " +
                    "action VARCHAR(200) NOT NULL, " +
                    "details VARCHAR(500), " +
                    "category VARCHAR(50))";
            stmt.execute(createAuditLog);

            // Create document_requests table for certificate and clearance tracking
            String createDocumentRequests = "CREATE TABLE IF NOT EXISTS document_requests (" +
                    "id INTEGER PRIMARY KEY AUTO_INCREMENT, " +
                    "resident_id INTEGER NOT NULL, " +
                    "resident_name VARCHAR(200) NOT NULL, " +
                    "document_type VARCHAR(100) NOT NULL, " +
                    "status VARCHAR(20) DEFAULT 'PENDING', " +
                    "request_date VARCHAR(30) NOT NULL, " +
                    "approval_date VARCHAR(30), " +
                    "approved_by VARCHAR(100), " +
                    "fee DECIMAL(10, 2) DEFAULT 0, " +
                    "payment_status VARCHAR(20) DEFAULT 'UNPAID', " +
                    "purpose VARCHAR(500), " +
                    "notes VARCHAR(500), " +
                    "FOREIGN KEY (resident_id) REFERENCES residents(id))";
            stmt.execute(createDocumentRequests);

            // Create complaints table for incident/complaint tracking
            String createComplaints = "CREATE TABLE IF NOT EXISTS complaints (" +
                    "id INTEGER PRIMARY KEY AUTO_INCREMENT, " +
                    "resident_id INTEGER NOT NULL, " +
                    "resident_name VARCHAR(200) NOT NULL, " +
                    "title VARCHAR(200) NOT NULL, " +
                    "description VARCHAR(2000) NOT NULL, " +
                    "status VARCHAR(20) DEFAULT 'Pending', " +
                    "date_submitted VARCHAR(30) NOT NULL, " +
                    "last_updated VARCHAR(30) NOT NULL, " +
                    "photo_path VARCHAR(500), " +
                    "admin_notes VARCHAR(2000), " +
                    "assigned_to VARCHAR(100), " +
                    "FOREIGN KEY (resident_id) REFERENCES residents(id))";
            stmt.execute(createComplaints);

            // Create announcements table for events, alerts, and programs
            String createAnnouncements = "CREATE TABLE IF NOT EXISTS announcements (" +
                    "id INTEGER PRIMARY KEY AUTO_INCREMENT, " +
                    "title VARCHAR(200) NOT NULL, " +
                    "content VARCHAR(5000) NOT NULL, " +
                    "type VARCHAR(50) NOT NULL, " +
                    "posted_date VARCHAR(30) NOT NULL, " +
                    "posted_by VARCHAR(100) NOT NULL, " +
                    "status VARCHAR(20) DEFAULT 'Active', " +
                    "start_date VARCHAR(30), " +
                    "end_date VARCHAR(30), " +
                    "views INTEGER DEFAULT 0)";
            stmt.execute(createAnnouncements);

            // Ensure middle_name column exists for older DBs
            try {
                stmt.execute("ALTER TABLE residents ADD COLUMN middle_name VARCHAR(100)");
            } catch (SQLException ignored) {
                // Column probably already exists; ignore
            }

            // Ensure image_path column exists for older DBs
            try {
                stmt.execute("ALTER TABLE residents ADD COLUMN image_path VARCHAR(500)");
            } catch (SQLException ignored) {
                // Column probably already exists; ignore
            }

            // Ensure role column exists for older DBs
            try {
                stmt.execute("ALTER TABLE residents ADD COLUMN role VARCHAR(50)");
            } catch (SQLException ignored) {
                // Column probably already exists; ignore
            }

            // Add new columns for extended resident information (professor's CSV format)
            try {
                stmt.execute("ALTER TABLE residents ADD COLUMN IF NOT EXISTS family_id INTEGER");
            } catch (SQLException ignored) {
                // Column probably already exists; ignore
            }

            try {
                stmt.execute("ALTER TABLE residents ADD COLUMN IF NOT EXISTS house_unit VARCHAR(20)");
            } catch (SQLException ignored) {
                // Column probably already exists; ignore
            }

            try {
                stmt.execute("ALTER TABLE residents ADD COLUMN IF NOT EXISTS street VARCHAR(200)");
            } catch (SQLException ignored) {
                // Column probably already exists; ignore
            }

            try {
                stmt.execute("ALTER TABLE residents ADD COLUMN IF NOT EXISTS subdivision VARCHAR(200)");
            } catch (SQLException ignored) {
                // Column probably already exists; ignore
            }

            try {
                stmt.execute("ALTER TABLE residents ADD COLUMN IF NOT EXISTS gate_color VARCHAR(50)");
            } catch (SQLException ignored) {
                // Column probably already exists; ignore
            }

            try {
                stmt.execute("ALTER TABLE residents ADD COLUMN IF NOT EXISTS vaccination_count INTEGER DEFAULT 0");
            } catch (SQLException ignored) {
                // Column probably already exists; ignore
            }

            // Create index for family queries
            try {
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_residents_family_id ON residents(family_id)");
            } catch (SQLException ignored) {
                // Index probably already exists; ignore
            }

            // Create roles table for managing custom roles
            String createRoles = "CREATE TABLE IF NOT EXISTS roles (" +
                    "id INTEGER PRIMARY KEY AUTO_INCREMENT, " +
                    "name VARCHAR(100) UNIQUE NOT NULL, " +
                    "description VARCHAR(500))";
            stmt.execute(createRoles);

            // Create role_permissions table for dynamic permission management
            String createRolePermissions = "CREATE TABLE IF NOT EXISTS role_permissions (" +
                    "id INTEGER PRIMARY KEY AUTO_INCREMENT, " +
                    "role_name VARCHAR(100) NOT NULL, " +
                    "module_name VARCHAR(100) NOT NULL, " +
                    "permission_level VARCHAR(20) NOT NULL, " +
                    "UNIQUE(role_name, module_name))";
            stmt.execute(createRolePermissions);

            // Initialize default roles
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM roles")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    System.out.println("Roles table is empty. Inserting default roles...");
                    String[] defaultRoles = {
                        "INSERT INTO roles (name, description) VALUES ('Barangay Captain', 'Head of the barangay with full administrative access')",
                        "INSERT INTO roles (name, description) VALUES ('Barangay Secretary', 'Manages resident data, legal cases, and correspondence')",
                        "INSERT INTO roles (name, description) VALUES ('Barangay Treasurer', 'Manages financial records and budgets')",
                        "INSERT INTO roles (name, description) VALUES ('Kagawads', 'Barangay council members with limited access')",
                        "INSERT INTO roles (name, description) VALUES ('Barangay Health Workers', 'Manages health and resident information')",
                        "INSERT INTO roles (name, description) VALUES ('Barangay Tanods', 'Peace and order officers with basic access')"
                    };
                    for (String insert : defaultRoles) {
                        stmt.execute(insert);
                    }
                    System.out.println("Default roles inserted.");
                }
            }

            // Insert sample users only if the table is empty. This is a more robust pattern.
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    System.out.println("Users table is empty. Inserting default users...");
                    String[] users = {
                        "INSERT INTO users (username, password, role) VALUES ('superadmin', 'admin123', 'Super Admin')",
                        "INSERT INTO users (username, password, role) VALUES ('owner', 'owner123', 'Owner')",
                        "INSERT INTO users (username, password, role) VALUES ('captain', 'captain123', 'Barangay Captain')",
                        "INSERT INTO users (username, password, role) VALUES ('secretary', 'secretary123', 'Barangay Secretary')",
                        "INSERT INTO users (username, password, role) VALUES ('treasurer', 'treasurer123', 'Barangay Treasurer')",
                        "INSERT INTO users (username, password, role) VALUES ('kagawad', 'kagawad123', 'Kagawads')",
                        "INSERT INTO users (username, password, role) VALUES ('healthworker', 'health123', 'Barangay Health Workers')",
                        "INSERT INTO users (username, password, role) VALUES ('tanod', 'tanod123', 'Barangay Tanods')"
                    };
                    for (String insert : users) {
                        stmt.execute(insert);
                    }
                    System.out.println("✓ Default users inserted for all roles.");
                }
            }
            
            // Initialize default permissions in database
            initializeDefaultPermissions();
            
            // Update user table schema to include new columns
            updateUserTableSchema();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Authenticate a user with username and password
     * Supports both plain text passwords (legacy) and BCrypt hashed passwords
     * @param username The username (case-insensitive)
     * @param password The password
     * @return The user's role if authentication succeeds, null otherwise
     */
    public static String authenticate(String username, String password) {
        // Use LOWER() for case-insensitive username matching
        String sql = "SELECT role, password FROM users WHERE LOWER(username) = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username.toLowerCase());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String storedPassword = rs.getString("password");
                String role = rs.getString("role");
                
                // Check if password is BCrypt hashed (starts with $2a$, $2b$, or $2y$)
                if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$")) {
                    // BCrypt hashed password - use BCrypt.checkpw()
                    if (BCrypt.checkpw(password, storedPassword)) {
                        return role;
                    }
                } else {
                    // Plain text password (legacy) - direct comparison
                    if (password.equals(storedPassword)) {
                        // Auto-upgrade to BCrypt hash
                        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(12));
                        updateUserPassword(username, hashedPassword);
                        System.out.println("✓ Auto-upgraded password to BCrypt for user: " + username);
                        return role;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Update a user's password (internal method)
     * @param username The username
     * @param hashedPassword The BCrypt hashed password
     */
    private static void updateUserPassword(String username, String hashedPassword) {
        String sql = "UPDATE users SET password = ? WHERE LOWER(username) = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, hashedPassword);
            pstmt.setString(2, username.toLowerCase());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Hash all plain text passwords in the database with BCrypt
     * @return Number of passwords hashed
     */
    public static int hashAllPlainTextPasswords() {
        int count = 0;
        String selectSql = "SELECT username, password FROM users";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(selectSql)) {
            
            while (rs.next()) {
                String username = rs.getString("username");
                String password = rs.getString("password");
                
                // Check if password is NOT already hashed
                if (!password.startsWith("$2a$") && !password.startsWith("$2b$") && !password.startsWith("$2y$")) {
                    // Hash the plain text password
                    String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(12));
                    updateUserPassword(username, hashedPassword);
                    count++;
                    System.out.println("✓ Hashed password for user: " + username);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }
    
    /**
     * Create a new user with BCrypt hashed password
     * @param username The username
     * @param password The plain text password (will be hashed)
     * @param role The user's role
     * @return true if user created successfully, false otherwise
     */
    public static boolean createUser(String username, String password, String role) {
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(12));
        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, role);
            pstmt.executeUpdate();
            logAction("System", "User Created", "Created user: " + username + " with role: " + role, "Security");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Change a user's password with BCrypt hashing
     * @param username The username
     * @param newPassword The new plain text password (will be hashed)
     * @return true if password changed successfully, false otherwise
     */
    public static boolean changeUserPassword(String username, String newPassword) {
        String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt(12));
        updateUserPassword(username, hashedPassword);
        logAction("System", "Password Changed", "Changed password for user: " + username, "Security");
        return true;
    }

    // ==================== USER MANAGEMENT METHODS ====================

    /**
     * Get all users from the database
     * @return ObservableList of User objects
     */
    public static ObservableList<User> getAllUsers() {
        ObservableList<User> users = FXCollections.observableArrayList();
        String sql = "SELECT id, username, role, created_date, last_login, is_active, resident_id FROM users ORDER BY username";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("role"),
                    rs.getString("created_date") != null ? rs.getString("created_date") : "Unknown",
                    rs.getString("last_login") != null ? rs.getString("last_login") : "Never",
                    rs.getBoolean("is_active"),
                    rs.getInt("resident_id")
                ));
            }
        } catch (SQLException e) {
            // Handle case where new columns don't exist yet
            String fallbackSql = "SELECT id, username, role FROM users ORDER BY username";
            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(fallbackSql)) {
                while (rs.next()) {
                    users.add(new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("role"),
                        "Unknown",
                        "Never",
                        true,
                        0
                    ));
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return users;
    }

    /**
     * Update user information
     * @param userId The user ID
     * @param username The new username
     * @param role The new role
     * @param isActive The active status
     * @return true if update successful, false otherwise
     */
    public static boolean updateUser(int userId, String username, String role, boolean isActive) {
        String sql = "UPDATE users SET username = ?, role = ?, is_active = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, role);
            pstmt.setBoolean(3, isActive);
            pstmt.setInt(4, userId);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                logAction("System", "User Updated", "Updated user: " + username + " (ID: " + userId + ")", "User Management");
                return true;
            }
        } catch (SQLException e) {
            // Try fallback without is_active column
            String fallbackSql = "UPDATE users SET username = ?, role = ? WHERE id = ?";
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(fallbackSql)) {
                pstmt.setString(1, username);
                pstmt.setString(2, role);
                pstmt.setInt(3, userId);
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    logAction("System", "User Updated", "Updated user: " + username + " (ID: " + userId + ")", "User Management");
                    return true;
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Delete a user
     * @param userId The user ID to delete
     * @return true if deletion successful, false otherwise
     */
    public static boolean deleteUser(int userId) {
        // First get username for logging
        String username = "";
        String getUserSql = "SELECT username FROM users WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(getUserSql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                username = rs.getString("username");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                logAction("System", "User Deleted", "Deleted user: " + username + " (ID: " + userId + ")", "User Management");
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Search users by username or role
     * @param searchTerm The search term
     * @return ObservableList of matching users
     */
    public static ObservableList<User> searchUsers(String searchTerm) {
        ObservableList<User> users = FXCollections.observableArrayList();
        String sql = "SELECT id, username, role, created_date, last_login, is_active, resident_id FROM users " +
                    "WHERE LOWER(username) LIKE ? OR LOWER(role) LIKE ? ORDER BY username";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String searchPattern = "%" + searchTerm.toLowerCase() + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                users.add(new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("role"),
                    rs.getString("created_date") != null ? rs.getString("created_date") : "Unknown",
                    rs.getString("last_login") != null ? rs.getString("last_login") : "Never",
                    rs.getBoolean("is_active"),
                    rs.getInt("resident_id")
                ));
            }
        } catch (SQLException e) {
            // Fallback for older schema
            String fallbackSql = "SELECT id, username, role FROM users " +
                                "WHERE LOWER(username) LIKE ? OR LOWER(role) LIKE ? ORDER BY username";
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(fallbackSql)) {
                String searchPattern = "%" + searchTerm.toLowerCase() + "%";
                pstmt.setString(1, searchPattern);
                pstmt.setString(2, searchPattern);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    users.add(new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("role"),
                        "Unknown",
                        "Never",
                        true,
                        0
                    ));
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return users;
    }

    /**
     * Filter users by role
     * @param role The role to filter by
     * @return ObservableList of users with the specified role
     */
    public static ObservableList<User> filterUsersByRole(String role) {
        ObservableList<User> users = FXCollections.observableArrayList();
        String sql = "SELECT id, username, role, created_date, last_login, is_active, resident_id FROM users " +
                    "WHERE role = ? ORDER BY username";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, role);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                users.add(new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("role"),
                    rs.getString("created_date") != null ? rs.getString("created_date") : "Unknown",
                    rs.getString("last_login") != null ? rs.getString("last_login") : "Never",
                    rs.getBoolean("is_active"),
                    rs.getInt("resident_id")
                ));
            }
        } catch (SQLException e) {
            // Fallback for older schema
            String fallbackSql = "SELECT id, username, role FROM users WHERE role = ? ORDER BY username";
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(fallbackSql)) {
                pstmt.setString(1, role);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    users.add(new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("role"),
                        "Unknown",
                        "Never",
                        true,
                        0
                    ));
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return users;
    }

    /**
     * Update user table schema to include new columns
     */
    public static void updateUserTableSchema() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Add created_date column if it doesn't exist
            try {
                stmt.execute("ALTER TABLE users ADD COLUMN created_date VARCHAR(30) DEFAULT '" + 
                    java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "'");
                System.out.println("✓ Added created_date column to users table");
            } catch (SQLException e) {
                // Column already exists or other error
            }
            
            // Add last_login column if it doesn't exist
            try {
                stmt.execute("ALTER TABLE users ADD COLUMN last_login VARCHAR(30) DEFAULT 'Never'");
                System.out.println("✓ Added last_login column to users table");
            } catch (SQLException e) {
                // Column already exists or other error
            }
            
            // Add is_active column if it doesn't exist
            try {
                stmt.execute("ALTER TABLE users ADD COLUMN is_active BOOLEAN DEFAULT TRUE");
                System.out.println("✓ Added is_active column to users table");
            } catch (SQLException e) {
                // Column already exists or other error
            }
            
            // Add resident_id column to link users to residents
            try {
                stmt.execute("ALTER TABLE users ADD COLUMN resident_id INTEGER DEFAULT NULL");
                System.out.println("✓ Added resident_id column to users table");
            } catch (SQLException e) {
                // Column already exists or other error
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create user account from existing resident
     * @param residentId The resident ID to create account for
     * @param username The username for the account
     * @param password The password for the account
     * @param role The role to assign
     * @return true if successful, false otherwise
     */
    public static boolean createUserFromResident(int residentId, String username, String password, String role) {
        // Check if resident exists
        Optional<Resident> resident = getResidentById(residentId);
        if (!resident.isPresent()) {
            return false;
        }
        
        // Check if resident already has a user account
        if (getUserByResidentId(residentId) != null) {
            return false; // Resident already has an account
        }
        
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(12));
        String sql = "INSERT INTO users (username, password, role, resident_id, created_date, is_active) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, role);
            pstmt.setInt(4, residentId);
            pstmt.setString(5, java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            pstmt.setBoolean(6, true);
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                Resident res = resident.get();
                logAction("System", "User Account Created", 
                    "Created user account '" + username + "' for resident: " + res.getFirstName() + " " + res.getLastName() + 
                    " (Role: " + role + ")", "User Management");
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Get user by resident ID
     * @param residentId The resident ID
     * @return User object if found, null otherwise
     */
    public static User getUserByResidentId(int residentId) {
        String sql = "SELECT id, username, role, created_date, last_login, is_active, resident_id FROM users WHERE resident_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, residentId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("role"),
                    rs.getString("created_date") != null ? rs.getString("created_date") : "Unknown",
                    rs.getString("last_login") != null ? rs.getString("last_login") : "Never",
                    rs.getBoolean("is_active"),
                    rs.getInt("resident_id")
                );
            }
        } catch (SQLException e) {
            // Fallback for older schema without resident_id
            return null;
        }
        return null;
    }

    /**
     * Get residents who don't have user accounts yet
     * @return ObservableList of residents without user accounts
     */
    public static ObservableList<Resident> getResidentsWithoutAccounts() {
        ObservableList<Resident> residents = FXCollections.observableArrayList();
        String sql = "SELECT r.* FROM residents r LEFT JOIN users u ON r.id = u.resident_id WHERE u.resident_id IS NULL ORDER BY r.last_name, r.first_name";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Resident resident = new Resident(
                    rs.getInt("id"),
                    rs.getString("first_name"),
                    rs.getString("middle_name"),
                    rs.getString("last_name"),
                    rs.getString("birth_date"),
                    rs.getString("gender"),
                    rs.getString("address"),
                    rs.getInt("family_id"),
                    rs.getString("house_unit"),
                    rs.getString("street"),
                    rs.getString("subdivision"),
                    rs.getString("gate_color"),
                    rs.getInt("vaccination_count")
                );
                resident.setImagePath(rs.getString("image_path"));
                resident.setRole(rs.getString("role"));
                resident.setPhoneNumber(rs.getString("phone_number"));
                residents.add(resident);
            }
        } catch (SQLException e) {
            // Fallback - get all residents if join fails
            return getAllResidentsSimple();
        }
        return residents;
    }

    /**
     * Simple method to get all residents (fallback)
     */
    private static ObservableList<Resident> getAllResidentsSimple() {
        return getResidents("", 0, 1000, "last_name", "ASC");
    }

    /**
     * Get resident information for a user
     * @param userId The user ID
     * @return Resident object if found, null otherwise
     */
    public static Resident getResidentForUser(int userId) {
        String sql = "SELECT u.resident_id FROM users u WHERE u.id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int residentId = rs.getInt("resident_id");
                if (residentId > 0) {
                    Optional<Resident> resident = getResidentById(residentId);
                    return resident.orElse(null);
                }
            }
        } catch (SQLException e) {
            // Column doesn't exist yet
        }
        return null;
    }

    /**
     * Get role-based permissions for a user
     * @param role The user's role
     * @return Map of permission categories to access levels
     */
    public static Map<String, String> getPermissions(String role) {
        Map<String, String> permissions = new HashMap<>();
        switch (role) {
            case "Super Admin":
                permissions.put("Analytics & Overview", "Full Access");
                permissions.put("User & Access", "Full Access");
                permissions.put("Resident Data", "Full Access");
                permissions.put("Certificates & Clearances", "Full Access");
                permissions.put("Complaints & Incidents", "Full Access");
                permissions.put("Announcements", "Full Access");
                permissions.put("Financial Reports", "Full Access");
                permissions.put("Security Features", "Full Access");
                permissions.put("System Config", "Full Access");
                permissions.put("Maintenance", "Full Access");
                break;
            case "Owner":
                permissions.put("Analytics & Overview", "Full Access");
                permissions.put("User & Access", "Manage");
                permissions.put("Resident Data", "Full Access");
                permissions.put("Certificates & Clearances", "Full Access");
                permissions.put("Complaints & Incidents", "Manage");
                permissions.put("Announcements", "Manage");
                permissions.put("Financial Reports", "Manage");
                permissions.put("Security Features", "Manage");
                permissions.put("System Config", "Manage");
                permissions.put("Maintenance", "View Only");
                break;
            case "Secretary":
                permissions.put("Analytics & Overview", "View Only");
                permissions.put("User & Access", "None");
                permissions.put("Resident Data", "Manage");
                permissions.put("Certificates & Clearances", "Manage");
                permissions.put("Complaints & Incidents", "Manage");
                permissions.put("Announcements", "Manage");
                permissions.put("Financial Reports", "View Only");
                permissions.put("Security Features", "None");
                permissions.put("System Config", "None");
                permissions.put("Maintenance", "None");
                break;
            case "Treasurer":
                permissions.put("Analytics & Overview", "View Only");
                permissions.put("User & Access", "None");
                permissions.put("Resident Data", "View Only");
                permissions.put("Certificates & Clearances", "View Only");
                permissions.put("Complaints & Incidents", "None");
                permissions.put("Announcements", "View Only");
                permissions.put("Financial Reports", "Manage");
                permissions.put("Security Features", "None");
                permissions.put("System Config", "None");
                permissions.put("Maintenance", "None");
                break;
            case "Captain":
                permissions.put("Analytics & Overview", "View Only");
                permissions.put("User & Access", "None");
                permissions.put("Resident Data", "View Only");
                permissions.put("Certificates & Clearances", "View Only");
                permissions.put("Complaints & Incidents", "View Only");
                permissions.put("Announcements", "View Only");
                permissions.put("Financial Reports", "View Only");
                permissions.put("Security Features", "None");
                permissions.put("System Config", "None");
                permissions.put("Maintenance", "None");
                break;
            case "Barangay Captain":
                permissions.put("Analytics & Overview", "Full Access");
                permissions.put("User & Access", "Full Access");
                permissions.put("Resident Data", "Full Access");
                permissions.put("Certificates & Clearances", "Full Access");
                permissions.put("Complaints & Incidents", "Full Access");
                permissions.put("Announcements", "Full Access");
                permissions.put("Financial Reports", "Full Access");
                permissions.put("Security Features", "Full Access");
                permissions.put("System Config", "Full Access");
                permissions.put("Maintenance", "Full Access");
                break;
            case "Barangay Secretary":
                permissions.put("Analytics & Overview", "View Only");
                permissions.put("User & Access", "None");
                permissions.put("Resident Data", "Manage");
                permissions.put("Certificates & Clearances", "Manage");
                permissions.put("Complaints & Incidents", "Manage");
                permissions.put("Announcements", "Manage");
                permissions.put("Financial Reports", "View Only");
                permissions.put("Security Features", "None");
                permissions.put("System Config", "None");
                permissions.put("Maintenance", "None");
                break;
            case "Barangay Treasurer":
                permissions.put("Analytics & Overview", "View Only");
                permissions.put("User & Access", "None");
                permissions.put("Resident Data", "View Only");
                permissions.put("Certificates & Clearances", "View Only");
                permissions.put("Complaints & Incidents", "None");
                permissions.put("Announcements", "View Only");
                permissions.put("Financial Reports", "Manage");
                permissions.put("Security Features", "None");
                permissions.put("System Config", "None");
                permissions.put("Maintenance", "None");
                break;
            case "Kagawads":
                permissions.put("Analytics & Overview", "View Only");
                permissions.put("User & Access", "None");
                permissions.put("Resident Data", "View Only");
                permissions.put("Certificates & Clearances", "View Only");
                permissions.put("Complaints & Incidents", "View Only");
                permissions.put("Announcements", "View Only");
                permissions.put("Financial Reports", "View Only");
                permissions.put("Security Features", "None");
                permissions.put("System Config", "None");
                permissions.put("Maintenance", "None");
                break;
            case "Barangay Health Workers":
                permissions.put("Analytics & Overview", "None");
                permissions.put("User & Access", "None");
                permissions.put("Resident Data", "Manage");
                permissions.put("Certificates & Clearances", "None");
                permissions.put("Complaints & Incidents", "None");
                permissions.put("Announcements", "View Only");
                permissions.put("Financial Reports", "None");
                permissions.put("Security Features", "None");
                permissions.put("System Config", "None");
                permissions.put("Maintenance", "None");
                break;
            case "Health Worker":
                permissions.put("Analytics & Overview", "None");
                permissions.put("User & Access", "None");
                permissions.put("Resident Data", "Manage");
                permissions.put("Certificates & Clearances", "None");
                permissions.put("Complaints & Incidents", "None");
                permissions.put("Announcements", "View Only");
                permissions.put("Financial Reports", "None");
                permissions.put("Security Features", "None");
                permissions.put("System Config", "None");
                permissions.put("Maintenance", "None");
                break;
            case "Barangay Tanods":
                permissions.put("Analytics & Overview", "None");
                permissions.put("User & Access", "None");
                permissions.put("Resident Data", "View Only");
                permissions.put("Certificates & Clearances", "None");
                permissions.put("Complaints & Incidents", "Manage");
                permissions.put("Announcements", "View Only");
                permissions.put("Financial Reports", "None");
                permissions.put("Security Features", "None");
                permissions.put("System Config", "None");
                permissions.put("Maintenance", "None");
                break;
            case "Tanod":
                permissions.put("Analytics & Overview", "None");
                permissions.put("User & Access", "None");
                permissions.put("Resident Data", "View Only");
                permissions.put("Certificates & Clearances", "None");
                permissions.put("Complaints & Incidents", "Manage");
                permissions.put("Announcements", "View Only");
                permissions.put("Financial Reports", "None");
                permissions.put("Security Features", "None");
                permissions.put("System Config", "None");
                permissions.put("Maintenance", "None");
                break;
            case "Resident":
                // Basic resident role - minimal permissions
                permissions.put("Analytics & Overview", "None");
                permissions.put("User & Access", "None");
                permissions.put("Resident Data", "View Only");
                permissions.put("Certificates & Clearances", "View Only");
                permissions.put("Complaints & Incidents", "View Only");
                permissions.put("Announcements", "View Only");
                permissions.put("Financial Reports", "None");
                permissions.put("Security Features", "None");
                permissions.put("System Config", "None");
                permissions.put("Maintenance", "None");
                break;
            default:
                // Default minimal permissions for unknown roles
                permissions.put("Analytics & Overview", "None");
                permissions.put("User & Access", "None");
                permissions.put("Resident Data", "View Only");
                permissions.put("Certificates & Clearances", "None");
                permissions.put("Complaints & Incidents", "None");
                permissions.put("Announcements", "View Only");
                permissions.put("Financial Reports", "None");
                permissions.put("Security Features", "None");
                permissions.put("System Config", "None");
                permissions.put("Maintenance", "None");
                break;
        }
        
        // Try to load permissions from database first
        Map<String, String> dbPermissions = getPermissionsFromDatabase(role);
        if (!dbPermissions.isEmpty()) {
            return dbPermissions;
        }
        
        return permissions;
    }

    /**
     * Get permissions from database for a role
     * @param role The role name
     * @return Map of module to permission level from database
     */
    private static Map<String, String> getPermissionsFromDatabase(String role) {
        Map<String, String> permissions = new HashMap<>();
        String sql = "SELECT module_name, permission_level FROM role_permissions WHERE role_name = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, role);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                permissions.put(rs.getString("module_name"), rs.getString("permission_level"));
            }
        } catch (SQLException e) {
            // Table might not exist yet or other error, return empty map
        }
        return permissions;
    }

    /**
     * Save permission for a role and module
     * @param roleName The role name
     * @param moduleName The module name
     * @param permissionLevel The permission level (None, View Only, Manage, Full Access)
     */
    public static void savePermission(String roleName, String moduleName, String permissionLevel) {
        String sql = "MERGE INTO role_permissions (role_name, module_name, permission_level) " +
                     "KEY(role_name, module_name) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, roleName);
            pstmt.setString(2, moduleName);
            pstmt.setString(3, permissionLevel);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initialize default permissions for all roles in the database
     * This should be called once to populate the role_permissions table
     */
    public static void initializeDefaultPermissions() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Check if permissions are already initialized
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM role_permissions");
            if (rs.next() && rs.getInt(1) > 0) {
                return; // Already initialized
            }
            
            // Get all roles and their default permissions
            String[] roles = {"Super Admin", "Owner", "Barangay Captain", "Barangay Secretary", 
                            "Barangay Treasurer", "Kagawads", "Health Worker", "Tanod", "Resident"};
            
            for (String role : roles) {
                Map<String, String> permissions = getPermissions(role);
                for (Map.Entry<String, String> entry : permissions.entrySet()) {
                    savePermission(role, entry.getKey(), entry.getValue());
                }
            }
            
            System.out.println("✓ Default permissions initialized in database");
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get total count of residents, optionally filtered by name
     * @param filter Optional search filter for first_name or last_name (case-insensitive)
     * @return Count of matching residents, 0 if none found or on error
     */
    public static int getResidentCount(String filter) {
        String sql;
        if (filter == null || filter.isEmpty()) {
            sql = "SELECT COUNT(*) FROM residents";
        } else {
            sql = "SELECT COUNT(*) FROM residents WHERE LOWER(first_name) LIKE ? OR LOWER(last_name) LIKE ?";
        }

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            if (filter != null && !filter.isEmpty()) {
                String filterPattern = "%" + filter.toLowerCase() + "%";
                pstmt.setString(1, filterPattern);
                pstmt.setString(2, filterPattern);
            }

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Get paginated list of residents with optional filtering and sorting
     * @param filter Optional search filter for first_name or last_name (case-insensitive)
     * @param pageIndex Zero-based page index
     * @param pageSize Number of records per page
     * @param sortField Column to sort by (validated against whitelist)
     * @param sortOrder Sort direction: "ASC" or "DESC"
     * @return Observable list of residents, empty list if none found or on error
     */
    public static ObservableList<Resident> getResidents(String filter, int pageIndex, int pageSize, String sortField, String sortOrder) {
        ObservableList<Resident> residents = FXCollections.observableArrayList();
        
        // Sanitize sort field to allow only valid column names
        String safeSortField = "last_name";
        if (sortField != null && !sortField.isEmpty()) {
            if (sortField.equals("first_name") || sortField.equals("middle_name") || sortField.equals("last_name") || 
                sortField.equals("birth_date") || sortField.equals("gender") || sortField.equals("address")) {
                safeSortField = sortField;
            }
        }
        
        String safeSortOrder = "ASC";
        if ("DESC".equalsIgnoreCase(sortOrder)) {
            safeSortOrder = "DESC";
        }
        
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM residents");
        if (filter != null && !filter.isEmpty()) {
            sqlBuilder.append(" WHERE LOWER(first_name) LIKE ? OR LOWER(last_name) LIKE ?");
        }
        
        sqlBuilder.append(" ORDER BY ").append(safeSortField).append(" ").append(safeSortOrder);
        // Secondary sort by first name if sorting by last name
        if ("last_name".equals(safeSortField)) {
            sqlBuilder.append(", first_name ASC");
        }
        sqlBuilder.append(" LIMIT ? OFFSET ?");
        
        String sql = sqlBuilder.toString();
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            int paramIndex = 1;
            if (filter != null && !filter.isEmpty()) {
                String filterPattern = "%" + filter.toLowerCase() + "%";
                pstmt.setString(paramIndex++, filterPattern);
                pstmt.setString(paramIndex++, filterPattern);
            }
            pstmt.setInt(paramIndex++, pageSize);
            pstmt.setInt(paramIndex++, pageIndex * pageSize);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Resident resident = new Resident(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("middle_name"),
                        rs.getString("last_name"),
                        rs.getString("birth_date"),
                        rs.getString("gender"),
                        rs.getString("address"),
                        rs.getObject("family_id", Integer.class),
                        rs.getString("house_unit"),
                        rs.getString("street"),
                        rs.getString("subdivision"),
                        rs.getString("gate_color"),
                        rs.getObject("vaccination_count", Integer.class));
                resident.setImagePath(rs.getString("image_path"));
                resident.setRole(rs.getString("role"));
                resident.setPhoneNumber(rs.getString("phone_number"));
                residents.add(resident);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return residents;
    }

    /**
     * Add a new resident to the database
     * @param resident The resident object to add
     */
    public static void addResident(Resident resident) {
        String sql = "INSERT INTO residents(first_name, middle_name, last_name, birth_date, gender, address, image_path, role, " +
                    "phone_number, family_id, house_unit, street, subdivision, gate_color, vaccination_count) " +
                    "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, resident.getFirstName());
            pstmt.setString(2, resident.getMiddleName());
            pstmt.setString(3, resident.getLastName());
            pstmt.setString(4, resident.getBirthDate());
            pstmt.setString(5, resident.getGender());
            pstmt.setString(6, resident.getAddress());
            pstmt.setString(7, resident.getImagePath());
            pstmt.setString(8, resident.getRole());
            pstmt.setString(9, resident.getPhoneNumber());
            
            // Extended fields
            Integer familyId = resident.getFamilyId();
            if (familyId != null && familyId > 0) {
                pstmt.setInt(10, familyId);
            } else {
                pstmt.setNull(10, java.sql.Types.INTEGER);
            }
            pstmt.setString(11, resident.getHouseUnit());
            pstmt.setString(12, resident.getStreet());
            pstmt.setString(13, resident.getSubdivision());
            pstmt.setString(14, resident.getGateColor());
            Integer vacCount = resident.getVaccinationCount();
            if (vacCount != null && vacCount > 0) {
                pstmt.setInt(15, vacCount);
            } else {
                pstmt.setInt(15, 0);
            }
            
            pstmt.executeUpdate();
            logAction("System", "Created new resident: " + resident.getFirstName() + " " + resident.getLastName(), "Resident " + resident.getLastName() + ", " + resident.getFirstName(), "Resident");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Bulk import residents from CSV file
     * CSV format: first_name,middle_name,last_name,birth_date,gender,address,role
     * @param csvFilePath Path to the CSV file
     * @return ImportResult containing success count, error count, and error messages
     */
    public static ImportResult bulkImportResidentsFromCSV(String csvFilePath) {
        int successCount = 0;
        int errorCount = 0;
        java.util.List<String> errors = new java.util.ArrayList<>();
        String defaultImagePath = getDefaultResidentImagePath();
        
        String sql = "INSERT INTO residents(first_name, middle_name, last_name, birth_date, gender, address, image_path, role) VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(csvFilePath));
             Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String line;
            int lineNumber = 0;
            boolean isFirstLine = true;
            
            while ((line = br.readLine()) != null) {
                lineNumber++;
                
                // Skip header line
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                
                // Skip empty lines
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                try {
                    // Parse CSV line (handles quoted fields with commas)
                    String[] values = parseCSVLine(line);
                    
                    if (values.length < 7) {
                        errors.add("Line " + lineNumber + ": Insufficient columns (expected 7, got " + values.length + ")");
                        errorCount++;
                        continue;
                    }
                    
                    String firstName = values[0].trim();
                    String middleName = values[1].trim();
                    String lastName = values[2].trim();
                    String birthDate = values[3].trim();
                    String gender = values[4].trim();
                    String address = values[5].trim();
                    String role = values[6].trim();
                    
                    // Validate required fields
                    if (firstName.isEmpty() || lastName.isEmpty()) {
                        errors.add("Line " + lineNumber + ": First name and last name are required");
                        errorCount++;
                        continue;
                    }
                    
                    // Validate date format (yyyy-MM-dd)
                    if (!birthDate.isEmpty() && !birthDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
                        errors.add("Line " + lineNumber + ": Invalid date format for '" + birthDate + "' (expected yyyy-MM-dd)");
                        errorCount++;
                        continue;
                    }
                    
                    // Insert resident
                    pstmt.setString(1, firstName);
                    pstmt.setString(2, middleName.isEmpty() ? null : middleName);
                    pstmt.setString(3, lastName);
                    pstmt.setString(4, birthDate.isEmpty() ? null : birthDate);
                    pstmt.setString(5, gender.isEmpty() ? null : gender);
                    pstmt.setString(6, address.isEmpty() ? null : address);
                    pstmt.setString(7, defaultImagePath);
                    pstmt.setString(8, role.isEmpty() ? "Resident" : role);
                    pstmt.executeUpdate();
                    
                    successCount++;
                    
                } catch (SQLException e) {
                    errors.add("Line " + lineNumber + ": Database error - " + e.getMessage());
                    errorCount++;
                } catch (Exception e) {
                    errors.add("Line " + lineNumber + ": Parse error - " + e.getMessage());
                    errorCount++;
                }
            }
            
            // Log the bulk import action
            logAction("System", "Bulk imported residents from CSV", 
                     "Success: " + successCount + ", Errors: " + errorCount, "Resident");
            
        } catch (java.io.IOException e) {
            errors.add("File error: " + e.getMessage());
            errorCount++;
        } catch (SQLException e) {
            errors.add("Database connection error: " + e.getMessage());
            errorCount++;
        }
        
        return new ImportResult(successCount, errorCount, errors);
    }
    
    /**
     * Parse a CSV line handling quoted fields with commas
     */
    private static String[] parseCSVLine(String line) {
        java.util.List<String> result = new java.util.ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        result.add(current.toString());
        
        return result.toArray(new String[0]);
    }
    
    /**
     * Get the default resident image path
     */
    public static String getDefaultResidentImagePath() {
        try {
            File defaultImage = new File("src/assets/defaultresident.jpg");
            if (defaultImage.exists()) {
                return defaultImage.getAbsolutePath();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    
    /**
     * Result class for bulk import operations
     */
    public static class ImportResult {
        private final int successCount;
        private final int errorCount;
        private final java.util.List<String> errors;
        
        public ImportResult(int successCount, int errorCount, java.util.List<String> errors) {
            this.successCount = successCount;
            this.errorCount = errorCount;
            this.errors = errors;
        }
        
        public int getSuccessCount() { return successCount; }
        public int getErrorCount() { return errorCount; }
        public java.util.List<String> getErrors() { return errors; }
        public boolean hasErrors() { return errorCount > 0; }
    }

    /**
     * Update an existing resident's information
     * @param resident The resident object with updated information
     */
    public static void updateResident(Resident resident) {
        String sql = "UPDATE residents SET first_name = ?, middle_name = ?, last_name = ?, birth_date = ?, gender = ?, address = ?, image_path = ?, role = ?, " +
                    "phone_number = ?, family_id = ?, house_unit = ?, street = ?, subdivision = ?, gate_color = ?, vaccination_count = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, resident.getFirstName());
            pstmt.setString(2, resident.getMiddleName());
            pstmt.setString(3, resident.getLastName());
            pstmt.setString(4, resident.getBirthDate());
            pstmt.setString(5, resident.getGender());
            pstmt.setString(6, resident.getAddress());
            pstmt.setString(7, resident.getImagePath());
            pstmt.setString(8, resident.getRole());
            pstmt.setString(9, resident.getPhoneNumber());
            
            // Extended fields
            Integer familyId = resident.getFamilyId();
            if (familyId != null && familyId > 0) {
                pstmt.setInt(10, familyId);
            } else {
                pstmt.setNull(10, java.sql.Types.INTEGER);
            }
            pstmt.setString(11, resident.getHouseUnit());
            pstmt.setString(12, resident.getStreet());
            pstmt.setString(13, resident.getSubdivision());
            pstmt.setString(14, resident.getGateColor());
            Integer vacCount = resident.getVaccinationCount();
            if (vacCount != null && vacCount > 0) {
                pstmt.setInt(15, vacCount);
            } else {
                pstmt.setInt(15, 0);
            }
            pstmt.setInt(16, resident.getId());
            pstmt.executeUpdate();
            logAction("System", "Updated resident information: " + resident.getFirstName() + " " + resident.getLastName(), "Resident " + resident.getLastName() + ", " + resident.getFirstName(), "Resident");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Delete a resident from the database
     * @param id The resident ID to delete
     */
    public static void deleteResident(int id) {
        String sql = "DELETE FROM residents WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            logAction("System", "Deleted resident record (ID: " + id + ")", "Resident ID " + id, "Resident");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get a resident by ID
     * @param id The resident ID
     * @return Optional containing the resident if found, empty otherwise
     */
    public static Optional<Resident> getResidentById(int id) {
        String sql = "SELECT * FROM residents WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Resident resident = new Resident(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("middle_name"),
                        rs.getString("last_name"),
                        rs.getString("birth_date"),
                        rs.getString("gender"),
                        rs.getString("address"),
                        rs.getObject("family_id", Integer.class),
                        rs.getString("house_unit"),
                        rs.getString("street"),
                        rs.getString("subdivision"),
                        rs.getString("gate_color"),
                        rs.getObject("vaccination_count", Integer.class));
                resident.setImagePath(rs.getString("image_path"));
                resident.setRole(rs.getString("role"));
                resident.setPhoneNumber(rs.getString("phone_number"));
                return Optional.of(resident);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public static Map<String, Integer> getGenderDistribution() {
        Map<String, Integer> distribution = new HashMap<>();
        String sql = "SELECT gender, COUNT(*) as count FROM residents WHERE gender IS NOT NULL GROUP BY gender ORDER BY gender";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String gender = rs.getString("gender");
                if (gender == null || gender.trim().isEmpty()) {
                    gender = "Unspecified";
                }
                distribution.put(gender, rs.getInt("count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return distribution;
    }

    public static Map<String, Integer> getAgeDistribution() {
        Map<String, Integer> distribution = new java.util.LinkedHashMap<>();
        // Initialize age groups
        distribution.put("0-10", 0);
        distribution.put("11-20", 0);
        distribution.put("21-30", 0);
        distribution.put("31-40", 0);
        distribution.put("41-50", 0);
        distribution.put("51-60", 0);
        distribution.put("61+", 0);

        String sql = "SELECT birth_date FROM residents WHERE birth_date IS NOT NULL";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            java.time.LocalDate today = java.time.LocalDate.now();
            while (rs.next()) {
                try {
                    String birthDateStr = rs.getString("birth_date");
                    java.time.LocalDate birthDate = java.time.LocalDate.parse(birthDateStr);
                    int age = today.getYear() - birthDate.getYear();
                    if (today.getMonthValue() < birthDate.getMonthValue() ||
                        (today.getMonthValue() == birthDate.getMonthValue() && today.getDayOfMonth() < birthDate.getDayOfMonth())) {
                        age--;
                    }

                    String ageGroup;
                    if (age <= 10) ageGroup = "0-10";
                    else if (age <= 20) ageGroup = "11-20";
                    else if (age <= 30) ageGroup = "21-30";
                    else if (age <= 40) ageGroup = "31-40";
                    else if (age <= 50) ageGroup = "41-50";
                    else if (age <= 60) ageGroup = "51-60";
                    else ageGroup = "61+";

                    distribution.put(ageGroup, distribution.get(ageGroup) + 1);
                } catch (Exception e) {
                    // Skip invalid dates
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return distribution;
    }

    // ==================== ROLE CRUD OPERATIONS ====================

    public static void addRole(Role role) {
        String sql = "INSERT INTO roles(name, description) VALUES(?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, role.getName());
            pstmt.setString(2, role.getDescription());
            pstmt.executeUpdate();
            logAction("System", "Created new role: " + role.getName(), role.getDescription(), "Role");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateRole(Role role) {
        String sql = "UPDATE roles SET name = ?, description = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, role.getName());
            pstmt.setString(2, role.getDescription());
            pstmt.setInt(3, role.getId());
            pstmt.executeUpdate();
            logAction("System", "Updated role: " + role.getName(), role.getDescription(), "Role");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteRole(int id) {
        String sql = "DELETE FROM roles WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            logAction("System", "Deleted role (ID: " + id + ")", "Role ID " + id, "Role");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Optional<Role> getRoleById(int id) {
        String sql = "SELECT * FROM roles WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Role role = new Role(rs.getInt("id"), rs.getString("name"), rs.getString("description"));
                return Optional.of(role);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public static ObservableList<Role> getAllRoles() {
        ObservableList<Role> roles = FXCollections.observableArrayList();
        String sql = "SELECT * FROM roles ORDER BY name ASC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Role role = new Role(rs.getInt("id"), rs.getString("name"), rs.getString("description"));
                roles.add(role);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return roles;
    }

    // ==================== AUDIT LOG OPERATIONS ====================

    public static void logAction(String username, String action, String details, String category) {
        String sql = "INSERT INTO audit_log(timestamp, username, action, details, category) VALUES(?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String timestamp = now.format(formatter);
            
            pstmt.setString(1, timestamp);
            pstmt.setString(2, username);
            pstmt.setString(3, action);
            pstmt.setString(4, details);
            pstmt.setString(5, category);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ObservableList<AuditEntry> getAuditLogs() {
        ObservableList<AuditEntry> auditLogs = FXCollections.observableArrayList();
        String sql = "SELECT * FROM audit_log ORDER BY id DESC LIMIT 100";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                AuditEntry entry = new AuditEntry(
                    rs.getInt("id"),
                    rs.getString("timestamp"),
                    rs.getString("username"),
                    rs.getString("action"),
                    rs.getString("details"),
                    rs.getString("category")
                );
                auditLogs.add(entry);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return auditLogs;
    }

    public static ObservableList<AuditEntry> getRecentActivity(int limit) {
        ObservableList<AuditEntry> activity = FXCollections.observableArrayList();
        String sql = "SELECT * FROM audit_log ORDER BY id DESC LIMIT ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                AuditEntry entry = new AuditEntry(
                    rs.getString("timestamp"),
                    rs.getString("username"),
                    rs.getString("action")
                );
                activity.add(entry);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return activity;
    }

    // ==================== DOCUMENT REQUEST OPERATIONS ====================

    public static int createDocumentRequest(DocumentRequest request) {
        String sql = "INSERT INTO document_requests(resident_id, resident_name, document_type, status, request_date, fee, payment_status, purpose, notes) " +
                     "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            java.time.LocalDate today = java.time.LocalDate.now();
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");
            
            pstmt.setInt(1, request.getResidentId());
            pstmt.setString(2, request.getResidentName());
            pstmt.setString(3, request.getDocumentType());
            pstmt.setString(4, "PENDING");
            pstmt.setString(5, today.format(formatter));
            pstmt.setDouble(6, request.getFee());
            pstmt.setString(7, "UNPAID");
            pstmt.setString(8, request.getPurpose());
            pstmt.setString(9, request.getNotes());
            
            pstmt.executeUpdate();
            
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                int requestId = rs.getInt(1);
                logAction("System", "Created document request: " + request.getDocumentType(), 
                         request.getResidentName(), "Document");
                return requestId;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static void approveDocumentRequest(int requestId, String approvedBy) {
        String sql = "UPDATE document_requests SET status = ?, approval_date = ?, approved_by = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            java.time.LocalDate today = java.time.LocalDate.now();
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");
            
            pstmt.setString(1, "APPROVED");
            pstmt.setString(2, today.format(formatter));
            pstmt.setString(3, approvedBy);
            pstmt.setInt(4, requestId);
            pstmt.executeUpdate();
            
            logAction("System", "Approved document request (ID: " + requestId + ")", "Request ID " + requestId, "Document");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void recordPayment(int requestId) {
        String sql = "UPDATE document_requests SET payment_status = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "PAID");
            pstmt.setInt(2, requestId);
            pstmt.executeUpdate();
            
            logAction("System", "Recorded payment for document request (ID: " + requestId + ")", "Request ID " + requestId, "Payment");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void completeDocumentRequest(int requestId) {
        String sql = "UPDATE document_requests SET status = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "COMPLETED");
            pstmt.setInt(2, requestId);
            pstmt.executeUpdate();
            
            logAction("System", "Completed document request (ID: " + requestId + ")", "Request ID " + requestId, "Document");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ObservableList<DocumentRequest> getAllDocumentRequests() {
        ObservableList<DocumentRequest> requests = FXCollections.observableArrayList();
        String sql = "SELECT * FROM document_requests ORDER BY request_date DESC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                DocumentRequest request = new DocumentRequest(
                    rs.getInt("id"),
                    rs.getInt("resident_id"),
                    rs.getString("resident_name"),
                    rs.getString("document_type"),
                    rs.getString("status"),
                    rs.getString("request_date"),
                    rs.getString("approval_date"),
                    rs.getString("approved_by"),
                    rs.getDouble("fee"),
                    rs.getString("payment_status"),
                    rs.getString("purpose"),
                    rs.getString("notes")
                );
                requests.add(request);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return requests;
    }

    /**
     * Get total revenue from paid document requests
     * @return Total revenue amount, 0.0 if no paid requests or on error
     */
    public static double getTotalRevenue() {
        double total = 0.0;
        String sql = "SELECT SUM(fee) as total FROM document_requests WHERE payment_status = 'PAID'";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                total = rs.getDouble("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return total;
    }

    /**
     * Get count of pending document requests (clearances/certificates)
     * Used by: Analytics & Overview dashboard stat card
     */
    public static int getPendingClearancesCount() {
        int count = 0;
        String sql = "SELECT COUNT(*) FROM document_requests WHERE status = 'PENDING'";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    /**
     * Get count of active complaints/cases
     * Used by: Analytics & Overview dashboard stat card
     * Status values: 'Pending' and 'Ongoing' are considered active
     */
    public static int getActiveCasesCount() {
        int count = 0;
        String sql = "SELECT COUNT(*) FROM complaints WHERE status IN ('Pending', 'Ongoing')";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    /**
     * Get count of issued/completed document requests
     * Used by: Enhanced dashboard stat card
     */
    public static int getIssuedDocumentsCount() {
        int count = 0;
        String sql = "SELECT COUNT(*) FROM document_requests WHERE status IN ('COMPLETED', 'APPROVED')";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    /**
     * Get count of paid document requests
     * Optional: For additional dashboard metrics
     */
    public static int getPaidDocumentsCount() {
        int count = 0;
        String sql = "SELECT COUNT(*) FROM document_requests WHERE payment_status = 'PAID'";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    /**
     * Get count of approved document requests
     * Optional: For additional dashboard metrics
     */
    public static int getApprovedDocumentsCount() {
        int count = 0;
        String sql = "SELECT COUNT(*) FROM document_requests WHERE status = 'APPROVED'";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    /**
     * Get count of completed document requests
     * Optional: For additional dashboard metrics
     */
    public static int getCompletedDocumentsCount() {
        int count = 0;
        String sql = "SELECT COUNT(*) FROM document_requests WHERE status = 'COMPLETED'";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    /**
     * Get count of resolved complaints
     * Optional: For additional dashboard metrics
     */
    public static int getResolvedComplaintsCount() {
        int count = 0;
        String sql = "SELECT COUNT(*) FROM complaints WHERE status = 'Resolved'";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    public static Optional<DocumentRequest> getDocumentRequestById(int id) {
        String sql = "SELECT * FROM document_requests WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                DocumentRequest request = new DocumentRequest(
                    rs.getInt("id"),
                    rs.getInt("resident_id"),
                    rs.getString("resident_name"),
                    rs.getString("document_type"),
                    rs.getString("status"),
                    rs.getString("request_date"),
                    rs.getString("approval_date"),
                    rs.getString("approved_by"),
                    rs.getDouble("fee"),
                    rs.getString("payment_status"),
                    rs.getString("purpose"),
                    rs.getString("notes")
                );
                return Optional.of(request);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    // ==================== COMPLAINT OPERATIONS ====================

    public static int createComplaint(Complaint complaint) {
        String sql = "INSERT INTO complaints(resident_id, resident_name, title, description, status, date_submitted, last_updated, photo_path, admin_notes, assigned_to) " +
                     "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, complaint.getResidentId());
            pstmt.setString(2, complaint.getResidentName());
            pstmt.setString(3, complaint.getTitle());
            pstmt.setString(4, complaint.getDescription());
            pstmt.setString(5, complaint.getStatus());
            pstmt.setString(6, complaint.getDateSubmitted());
            pstmt.setString(7, complaint.getLastUpdated());
            pstmt.setString(8, complaint.getPhotoPath());
            pstmt.setString(9, complaint.getAdminNotes() != null ? complaint.getAdminNotes() : "");
            pstmt.setString(10, complaint.getAssignedTo() != null ? complaint.getAssignedTo() : "");
            
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                int complaintId = rs.getInt(1);
                logAction("System", "Created complaint: " + complaint.getTitle(), 
                         complaint.getResidentName(), "Complaint");
                return complaintId;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static void updateComplaintStatus(int complaintId, String status) {
        String sql = "UPDATE complaints SET status = ?, last_updated = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            
            pstmt.setString(1, status);
            pstmt.setString(2, now.format(formatter));
            pstmt.setInt(3, complaintId);
            pstmt.executeUpdate();
            
            logAction("System", "Updated complaint status to: " + status + " (ID: " + complaintId + ")", 
                     "Complaint ID " + complaintId, "Complaint");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateComplaintNotes(int complaintId, String notes, String assignedTo) {
        String sql = "UPDATE complaints SET admin_notes = ?, assigned_to = ?, last_updated = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            
            pstmt.setString(1, notes);
            pstmt.setString(2, assignedTo);
            pstmt.setString(3, now.format(formatter));
            pstmt.setInt(4, complaintId);
            pstmt.executeUpdate();
            
            logAction("System", "Updated complaint notes (ID: " + complaintId + ")", 
                     "Complaint ID " + complaintId, "Complaint");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ObservableList<Complaint> getAllComplaints() {
        ObservableList<Complaint> complaints = FXCollections.observableArrayList();
        String sql = "SELECT * FROM complaints ORDER BY date_submitted DESC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Complaint complaint = new Complaint(
                    rs.getInt("id"),
                    rs.getInt("resident_id"),
                    rs.getString("resident_name"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getString("status"),
                    rs.getString("date_submitted"),
                    rs.getString("last_updated"),
                    rs.getString("photo_path"),
                    rs.getString("admin_notes"),
                    rs.getString("assigned_to")
                );
                complaints.add(complaint);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return complaints;
    }

    public static Optional<Complaint> getComplaintById(int id) {
        String sql = "SELECT * FROM complaints WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Complaint complaint = new Complaint(
                    rs.getInt("id"),
                    rs.getInt("resident_id"),
                    rs.getString("resident_name"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getString("status"),
                    rs.getString("date_submitted"),
                    rs.getString("last_updated"),
                    rs.getString("photo_path"),
                    rs.getString("admin_notes"),
                    rs.getString("assigned_to")
                );
                return Optional.of(complaint);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public static ObservableList<Complaint> getComplaintsByResident(int residentId) {
        ObservableList<Complaint> complaints = FXCollections.observableArrayList();
        String sql = "SELECT * FROM complaints WHERE resident_id = ? ORDER BY date_submitted DESC";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, residentId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Complaint complaint = new Complaint(
                    rs.getInt("id"),
                    rs.getInt("resident_id"),
                    rs.getString("resident_name"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getString("status"),
                    rs.getString("date_submitted"),
                    rs.getString("last_updated"),
                    rs.getString("photo_path"),
                    rs.getString("admin_notes"),
                    rs.getString("assigned_to")
                );
                complaints.add(complaint);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return complaints;
    }

    // ==================== ANNOUNCEMENT OPERATIONS ====================

    public static int createAnnouncement(Announcement announcement) {
        String sql = "INSERT INTO announcements(title, content, type, posted_date, posted_by, status, start_date, end_date, views) " +
                     "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, announcement.getTitle());
            pstmt.setString(2, announcement.getContent());
            pstmt.setString(3, announcement.getType());
            pstmt.setString(4, announcement.getPostedDate());
            pstmt.setString(5, announcement.getPostedBy());
            pstmt.setString(6, announcement.getStatus());
            pstmt.setString(7, announcement.getStartDate());
            pstmt.setString(8, announcement.getEndDate());
            pstmt.setInt(9, announcement.getViews());
            
            pstmt.executeUpdate();
            
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                int announcementId = rs.getInt(1);
                logAction("System", "Posted announcement: " + announcement.getTitle(), 
                         announcement.getPostedBy(), "Announcement");
                return announcementId;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static void updateAnnouncement(int announcementId, String title, String content, String status, String endDate) {
        String sql = "UPDATE announcements SET title = ?, content = ?, status = ?, end_date = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.setString(2, content);
            pstmt.setString(3, status);
            pstmt.setString(4, endDate);
            pstmt.setInt(5, announcementId);
            pstmt.executeUpdate();
            
            logAction("System", "Updated announcement (ID: " + announcementId + ")", 
                     "Announcement ID " + announcementId, "Announcement");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteAnnouncement(int announcementId) {
        String sql = "DELETE FROM announcements WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, announcementId);
            pstmt.executeUpdate();
            
            logAction("System", "Deleted announcement (ID: " + announcementId + ")", 
                     "Announcement ID " + announcementId, "Announcement");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ObservableList<Announcement> getAllAnnouncements() {
        ObservableList<Announcement> announcements = FXCollections.observableArrayList();
        String sql = "SELECT * FROM announcements ORDER BY posted_date DESC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Announcement announcement = new Announcement(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("content"),
                    rs.getString("type"),
                    rs.getString("posted_date"),
                    rs.getString("posted_by"),
                    rs.getString("status"),
                    rs.getString("start_date"),
                    rs.getString("end_date"),
                    rs.getInt("views")
                );
                announcements.add(announcement);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return announcements;
    }

    public static ObservableList<Announcement> getAnnouncementsByType(String type) {
        ObservableList<Announcement> announcements = FXCollections.observableArrayList();
        String sql = "SELECT * FROM announcements WHERE type = ? AND status = 'Active' ORDER BY posted_date DESC";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, type);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Announcement announcement = new Announcement(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("content"),
                    rs.getString("type"),
                    rs.getString("posted_date"),
                    rs.getString("posted_by"),
                    rs.getString("status"),
                    rs.getString("start_date"),
                    rs.getString("end_date"),
                    rs.getInt("views")
                );
                announcements.add(announcement);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return announcements;
    }

    public static Optional<Announcement> getAnnouncementById(int id) {
        String sql = "SELECT * FROM announcements WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Announcement announcement = new Announcement(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("content"),
                    rs.getString("type"),
                    rs.getString("posted_date"),
                    rs.getString("posted_by"),
                    rs.getString("status"),
                    rs.getString("start_date"),
                    rs.getString("end_date"),
                    rs.getInt("views")
                );
                return Optional.of(announcement);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    // ==================== FINANCIAL OPERATIONS ====================

    public static Map<String, Double> getDailyCollections() {
        Map<String, Double> collections = new java.util.LinkedHashMap<>();
        // H2 syntax: Last 30 days instead of 7 to include more recent data
        String sql = "SELECT request_date, SUM(fee) as daily_total FROM document_requests " +
                     "WHERE payment_status = 'PAID' AND CAST(request_date AS DATE) >= CAST(CURRENT_DATE - 30 AS DATE) " +
                     "GROUP BY request_date ORDER BY CAST(request_date AS DATE) ASC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            // Initialize last 30 days with 0 values
            java.time.LocalDate today = java.time.LocalDate.now();
            for (int i = 29; i >= 0; i--) {
                java.time.LocalDate date = today.minusDays(i);
                String dateStr = date.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                collections.put(dateStr, 0.0);
            }
            
            // Update with actual data from database
            while (rs.next()) {
                String date = rs.getString("request_date");
                double amount = rs.getDouble("daily_total");
                collections.put(date, amount);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // If query fails, return empty collections for today
            java.time.LocalDate today = java.time.LocalDate.now();
            for (int i = 29; i >= 0; i--) {
                java.time.LocalDate date = today.minusDays(i);
                String dateStr = date.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                collections.put(dateStr, 0.0);
            }
        }
        return collections;
    }

    public static Map<String, Double> getMonthlyIncome() {
        Map<String, Double> income = new java.util.LinkedHashMap<>();
        // H2 syntax: Use YEAR() and MONTH() on casted date, avoid reserved keywords in alias
        String sql = "SELECT YEAR(CAST(request_date AS DATE)) as yr, MONTH(CAST(request_date AS DATE)) as mo, SUM(fee) as monthly_total " +
                     "FROM document_requests " +
                     "WHERE payment_status = 'PAID' AND CAST(request_date AS DATE) >= CAST(CURRENT_DATE - INTERVAL '12' MONTH AS DATE) " +
                     "GROUP BY yr, mo ORDER BY yr DESC, mo DESC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            // Initialize last 12 months with 0 values
            java.time.LocalDate today = java.time.LocalDate.now();
            for (int i = 11; i >= 0; i--) {
                java.time.LocalDate date = today.minusMonths(i);
                String monthYear = date.format(java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy"));
                income.put(monthYear, 0.0);
            }
            
            // Update with actual data from database
            while (rs.next()) {
                int year = rs.getInt("yr");
                int month = rs.getInt("mo");
                double amount = rs.getDouble("monthly_total");
                
                java.time.LocalDate date = java.time.LocalDate.of(year, month, 1);
                String monthYear = date.format(java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy"));
                income.put(monthYear, amount);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // If query fails, return empty income for last 12 months
            java.time.LocalDate today = java.time.LocalDate.now();
            for (int i = 11; i >= 0; i--) {
                java.time.LocalDate date = today.minusMonths(i);
                String monthYear = date.format(java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy"));
                income.put(monthYear, 0.0);
            }
        }
        return income;
    }

    // ==================== NOTIFICATION METHODS ====================

    /**
     * Get revenue breakdown by document type
     * @return Map of document type to total revenue
     */
    public static Map<String, Double> getRevenueByDocumentType() {
        Map<String, Double> revenue = new java.util.LinkedHashMap<>();
        String sql = "SELECT document_type, SUM(fee) as total_revenue FROM document_requests " +
                     "WHERE payment_status = 'PAID' " +
                     "GROUP BY document_type ORDER BY total_revenue DESC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String docType = rs.getString("document_type");
                double amount = rs.getDouble("total_revenue");
                revenue.put(docType, amount);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return revenue;
    }

    /**
     * Get year-to-date financial summary
     * @return Map with YTD statistics
     */
    public static Map<String, Object> getYearToDateSummary() {
        Map<String, Object> summary = new java.util.LinkedHashMap<>();
        String sql = "SELECT " +
                     "COUNT(*) as total_transactions, " +
                     "SUM(CASE WHEN payment_status = 'PAID' THEN fee ELSE 0 END) as total_revenue, " +
                     "SUM(CASE WHEN payment_status = 'PENDING' THEN fee ELSE 0 END) as pending_revenue, " +
                     "COUNT(CASE WHEN payment_status = 'PAID' THEN 1 END) as paid_count, " +
                     "COUNT(CASE WHEN payment_status = 'PENDING' THEN 1 END) as pending_count " +
                     "FROM document_requests " +
                     "WHERE YEAR(CAST(request_date AS DATE)) = YEAR(CURRENT_DATE)";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                summary.put("total_transactions", rs.getInt("total_transactions"));
                summary.put("total_revenue", rs.getDouble("total_revenue"));
                summary.put("pending_revenue", rs.getDouble("pending_revenue"));
                summary.put("paid_count", rs.getInt("paid_count"));
                summary.put("pending_count", rs.getInt("pending_count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            summary.put("total_transactions", 0);
            summary.put("total_revenue", 0.0);
            summary.put("pending_revenue", 0.0);
            summary.put("paid_count", 0);
            summary.put("pending_count", 0);
        }
        return summary;
    }

    /**
     * Get top revenue generating days
     * @param limit Number of top days to return
     * @return Map of date to revenue
     */
    public static Map<String, Double> getTopRevenueDays(int limit) {
        Map<String, Double> topDays = new java.util.LinkedHashMap<>();
        String sql = "SELECT request_date, SUM(fee) as daily_revenue FROM document_requests " +
                     "WHERE payment_status = 'PAID' " +
                     "GROUP BY request_date ORDER BY daily_revenue DESC LIMIT " + limit;
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String date = rs.getString("request_date");
                double revenue = rs.getDouble("daily_revenue");
                topDays.put(date, revenue);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return topDays;
    }

    /**
     * Save financial export path preference
     * @param path Export folder path
     */
    public static void saveFinancialExportPath(String path) {
        String sql = "MERGE INTO system_preferences (pref_key, pref_value) KEY(pref_key) VALUES ('financial_export_path', ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, path);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get financial export path preference
     * @return Export folder path or null if not set
     */
    public static String getFinancialExportPath() {
        String sql = "SELECT pref_value FROM system_preferences WHERE pref_key = 'financial_export_path'";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getString("pref_value");
            }
        } catch (SQLException e) {
            // Table might not exist yet, return null
        }
        return null;
    }

    /**
     * Backup the database to a specified path
     * WARNING: Ensure backupPath is validated before calling this method
     * @param backupPath The file path where the backup should be created
     * @return true if backup succeeds, false otherwise
     */
    public static boolean backupDatabase(String backupPath) {
        // Validate backup path to prevent SQL injection
        if (backupPath == null || backupPath.trim().isEmpty()) {
            System.err.println("Invalid backup path: path cannot be null or empty");
            return false;
        }
        
        // Basic path validation: ensure it doesn't contain SQL injection attempts
        if (backupPath.contains("'") || backupPath.contains(";") || backupPath.contains("--")) {
            System.err.println("Invalid backup path: contains potentially dangerous characters");
            return false;
        }
        
        String sql = "BACKUP TO '" + backupPath + "'";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            logAction("System", "Database Backup", "Backup created at: " + backupPath, "System");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get database size
    public static long getDatabaseSize() {
        try {
            java.io.File dbFile = new java.io.File(System.getProperty("user.home") + "/bdms_v2.mv.db");
            if (dbFile.exists()) {
                return dbFile.length();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Get table record counts
    public static Map<String, Integer> getTableCounts() {
        Map<String, Integer> counts = new HashMap<>();
        String[] tables = {"residents", "document_requests", "complaints", "announcements", "users", "audit_log", "notifications"};
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            for (String table : tables) {
                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + table)) {
                    if (rs.next()) {
                        counts.put(table, rs.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return counts;
    }

    /**
     * Import residents from professor's CSV format
     * CSV Format: Res_ID,Res_LN,Res_FN,Res_MidN,Family_ID,House_Unit,Street,Subdivision,Gate_Color,Age,Vaccination_Count
     * @param csvFilePath Path to the CSV file
     * @return Map with "success" and "failed" counts (never null)
     */
    public static Map<String, Integer> importResidentsFromProfessorCSV(String csvFilePath) {
        Map<String, Integer> result = new HashMap<>();
        int successCount = 0;
        int failedCount = 0;
        
        String insertSQL = "INSERT INTO residents (first_name, middle_name, last_name, birth_date, gender, address, " +
                          "family_id, house_unit, street, subdivision, gate_color, vaccination_count, role, image_path) " +
                          "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSQL);
             java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(csvFilePath))) {
            
            String line;
            boolean isFirstLine = true;
            int currentYear = java.time.Year.now().getValue();
            
            while ((line = br.readLine()) != null) {
                // Skip header
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                
                if (line.trim().isEmpty()) continue;
                
                try {
                    // Use CSV parser that handles quoted fields
                    String[] fields = parseCSVLine(line);
                    
                    if (fields.length < 11) {
                        System.err.println("Skipping invalid line (expected 11 fields): " + line);
                        failedCount++;
                        continue;
                    }
                    
                    // Parse fields: Res_ID,Res_LN,Res_FN,Res_MidN,Family_ID,House_Unit,Street,Subdivision,Gate_Color,Age,Vaccination_Count
                    String lastName = fields[1].trim();
                    String firstName = fields[2].trim();
                    String middleName = fields[3].trim();
                    int familyId = Integer.parseInt(fields[4].trim());
                    String houseUnit = fields[5].trim();
                    String street = fields[6].trim();
                    String subdivision = fields[7].trim();
                    String gateColor = fields[8].trim();
                    int age = Integer.parseInt(fields[9].trim());
                    int vaccinationCount = Integer.parseInt(fields[10].trim());
                    
                    // Validate required fields
                    if (lastName.isEmpty() || firstName.isEmpty()) {
                        System.err.println("Skipping line with missing name: " + line);
                        failedCount++;
                        continue;
                    }
                    
                    // Validate age range
                    if (age < 0 || age > 120) {
                        System.err.println("Skipping line with invalid age: " + line);
                        failedCount++;
                        continue;
                    }
                    
                    // Validate vaccination count range
                    if (vaccinationCount < 0 || vaccinationCount > 20) {
                        System.err.println("Skipping line with invalid vaccination count: " + line);
                        failedCount++;
                        continue;
                    }
                    
                    // Calculate birth date from age (approximation: January 1st of birth year)
                    int birthYear = currentYear - age;
                    String birthDate = birthYear + "-01-01";
                    
                    // Construct full address
                    String fullAddress = houseUnit + ", " + street + ", " + subdivision;
                    
                    // Set parameters (handle empty optional fields)
                    pstmt.setString(1, firstName);
                    pstmt.setString(2, middleName.isEmpty() ? null : middleName);
                    pstmt.setString(3, lastName);
                    pstmt.setString(4, birthDate);
                    pstmt.setString(5, null); // gender (not in CSV, can be added later)
                    pstmt.setString(6, fullAddress);
                    pstmt.setInt(7, familyId);
                    pstmt.setString(8, houseUnit);
                    pstmt.setString(9, street);
                    pstmt.setString(10, subdivision);
                    pstmt.setString(11, gateColor.isEmpty() ? null : gateColor);
                    pstmt.setInt(12, vaccinationCount);
                    pstmt.setString(13, "Resident");
                    pstmt.setString(14, getDefaultResidentImagePath());
                    
                    pstmt.executeUpdate();
                    successCount++;
                    
                } catch (NumberFormatException e) {
                    System.err.println("Failed to parse numeric field in line: " + line);
                    System.err.println("Error: " + e.getMessage());
                    failedCount++;
                } catch (SQLException e) {
                    System.err.println("Failed to import line: " + line);
                    System.err.println("Error: " + e.getMessage());
                    failedCount++;
                }
            }
            
            // Log import action
            if (successCount > 0) {
                logAction("System", 
                         String.format("Imported %d residents from professor's CSV. Failed: %d", successCount, failedCount),
                         "Professor CSV Import", "Resident");
            }
            
        } catch (java.io.IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Database error during CSV import: " + e.getMessage());
            e.printStackTrace();
        }
        
        result.put("success", successCount);
        result.put("failed", failedCount);
        return result;
    }
    
    // ==================== SMS CONFIGURATION & LOGGING ====================
    
    /**
     * Initialize SMS-related tables
     */
    private static void initializeSMSTables() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Create SMS configuration table
            String createSMSConfig = "CREATE TABLE IF NOT EXISTS sms_config (" +
                    "id INTEGER PRIMARY KEY AUTO_INCREMENT, " +
                    "api_key VARCHAR(255), " +
                    "api_base_url VARCHAR(255) DEFAULT 'https://unismsapi.com/api', " +
                    "sender_name VARCHAR(11), " +
                    "enabled BOOLEAN DEFAULT FALSE, " +
                    "last_updated DATETIME DEFAULT CURRENT_TIMESTAMP)";
            stmt.execute(createSMSConfig);
            
            // Add api_base_url column if it doesn't exist (for existing databases)
            try {
                stmt.execute("ALTER TABLE sms_config ADD COLUMN IF NOT EXISTS api_base_url VARCHAR(255) DEFAULT 'https://unismsapi.com/api'");
            } catch (SQLException e) {
                // Column might already exist, ignore
            }
            
            // Create SMS log table
            String createSMSLog = "CREATE TABLE IF NOT EXISTS sms_log (" +
                    "id INTEGER PRIMARY KEY AUTO_INCREMENT, " +
                    "phone_number VARCHAR(50) NOT NULL, " +
                    "message VARCHAR(1000) NOT NULL, " +
                    "status VARCHAR(20) NOT NULL, " + // SENT, FAILED, ERROR, SENT_PRIORITY, SENT_OTP, SENT_BULK
                    "message_id VARCHAR(50), " +
                    "error_code VARCHAR(100), " +
                    "sent_at DATETIME DEFAULT CURRENT_TIMESTAMP)";
            stmt.execute(createSMSLog);
            
            // Create indexes separately (H2 syntax)
            try {
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_sms_log_timestamp ON sms_log(sent_at)");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_sms_log_status ON sms_log(status)");
            } catch (SQLException e) {
                // Indexes might already exist, ignore
            }
            
            // Create SMS templates table
            String createSMSTemplates = "CREATE TABLE IF NOT EXISTS sms_templates (" +
                    "id INTEGER PRIMARY KEY AUTO_INCREMENT, " +
                    "name VARCHAR(100) UNIQUE NOT NULL, " +
                    "template VARCHAR(500) NOT NULL, " +
                    "description VARCHAR(200), " +
                    "category VARCHAR(50))"; // DOCUMENT, COMPLAINT, ANNOUNCEMENT, OTP
            stmt.execute(createSMSTemplates);
            
            // Insert default SMS templates if table is empty
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM sms_templates")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    System.out.println("Inserting default SMS templates...");
                    String[] templates = {
                        "INSERT INTO sms_templates (name, template, description, category) VALUES " +
                        "('Document Approved', 'Your {document_type} request has been approved. You may claim it at the Barangay Hall. - Barangay San Isidro', 'Document request approval notification', 'DOCUMENT')",
                        
                        "INSERT INTO sms_templates (name, template, description, category) VALUES " +
                        "('Document Ready', 'Your {document_type} is now ready for pickup. Please bring a valid ID. - Barangay San Isidro', 'Document ready for pickup', 'DOCUMENT')",
                        
                        "INSERT INTO sms_templates (name, template, description, category) VALUES " +
                        "('Complaint Received', 'Your complaint \"{title}\" has been received. Reference: {complaint_id}. We will update you soon. - Barangay San Isidro', 'Complaint submission confirmation', 'COMPLAINT')",
                        
                        "INSERT INTO sms_templates (name, template, description, category) VALUES " +
                        "('Complaint Resolved', 'Your complaint \"{title}\" has been resolved. Thank you for your patience. - Barangay San Isidro', 'Complaint resolution notification', 'COMPLAINT')",
                        
                        "INSERT INTO sms_templates (name, template, description, category) VALUES " +
                        "('Announcement', '{title}: {content} - Barangay San Isidro', 'General announcement notification', 'ANNOUNCEMENT')",
                        
                        "INSERT INTO sms_templates (name, template, description, category) VALUES " +
                        "('Emergency Alert', 'EMERGENCY: {content} Please stay safe. - Barangay San Isidro', 'Emergency alert notification', 'ANNOUNCEMENT')",
                        
                        "INSERT INTO sms_templates (name, template, description, category) VALUES " +
                        "('OTP Code', 'Your verification code is: {otp}. Valid for 10 minutes. Do not share this code. - Barangay San Isidro', 'OTP verification code', 'OTP')",
                        
                        "INSERT INTO sms_templates (name, template, description, category) VALUES " +
                        "('Payment Reminder', 'Reminder: Your {document_type} payment of P{amount} is due. Please settle at the Barangay Hall. - Barangay San Isidro', 'Payment reminder', 'DOCUMENT')"
                    };
                    for (String insert : templates) {
                        stmt.execute(insert);
                    }
                    System.out.println("✓ Default SMS templates inserted");
                }
            }
            
            System.out.println("✓ SMS tables initialized");
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    static {
        initializeSMSTables();
        // Initialize with provided API key if no config exists
        initializeDefaultSMSConfig();
    }
    
    /**
     * Initialize default SMS configuration if none exists
     */
    private static void initializeDefaultSMSConfig() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Check if SMS config exists
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM sms_config");
            if (rs.next() && rs.getInt(1) == 0) {
                // Insert default configuration with provided API key
                String insertSql = "INSERT INTO sms_config (api_key, api_base_url, sender_name, enabled, last_updated) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                    pstmt.setString(1, "sk_6b87cce0-218a-4bad-8ead-571a12ef44ec"); // UniSMS API key
                    pstmt.setString(2, "https://unismsapi.com/api"); // UniSMS base URL
                    pstmt.setString(3, "BDMS");
                    pstmt.setBoolean(4, true); // Enable SMS by default
                    pstmt.setString(5, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    pstmt.executeUpdate();
                    System.out.println("✓ SMS configuration initialized with UniSMS API key");
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Get SMS API base URL from configuration
     * @return API base URL or default if not configured
     */
    public static String getSMSApiBaseUrl() {
        String sql = "SELECT api_base_url FROM sms_config LIMIT 1";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                String url = rs.getString("api_base_url");
                return url != null && !url.trim().isEmpty() ? url : "https://unismsapi.com/api";
            }
        } catch (SQLException e) {
            // Column might not exist in old databases
            e.printStackTrace();
        }
        return "https://unismsapi.com/api"; // Default UniSMS URL
    }
    
    /**
     * Get SMS API key from configuration
     * @return API key or null if not configured
     */
    public static String getSMSApiKey() {
        String sql = "SELECT api_key FROM sms_config ORDER BY id DESC LIMIT 1";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getString("api_key");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Get SMS sender name from configuration
     * @return Sender name or "BDMS" as default
     */
    public static String getSMSSenderName() {
        String sql = "SELECT sender_name FROM sms_config ORDER BY id DESC LIMIT 1";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                String senderName = rs.getString("sender_name");
                return (senderName != null && !senderName.trim().isEmpty()) ? senderName : "BDMS";
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "BDMS";
    }
    
    /**
     * Check if SMS notifications are enabled
     * @return true if enabled, false otherwise
     */
    public static boolean isSMSEnabled() {
        String sql = "SELECT enabled FROM sms_config ORDER BY id DESC LIMIT 1";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getBoolean("enabled");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Save SMS configuration
     * @param apiKey SMS API PH key
     * @param apiBaseUrl API base URL
     * @param senderName Sender name (not used by SMS API PH, kept for compatibility)
     * @param enabled Enable/disable SMS notifications
     */
    public static void saveSMSConfig(String apiKey, String apiBaseUrl, String senderName, boolean enabled) {
        // Delete existing config
        String deleteSql = "DELETE FROM sms_config";
        String insertSql = "INSERT INTO sms_config (api_key, api_base_url, sender_name, enabled, last_updated) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = getConnection();
             Statement deleteStmt = conn.createStatement();
             PreparedStatement insertPstmt = conn.prepareStatement(insertSql)) {
            
            deleteStmt.execute(deleteSql);
            
            insertPstmt.setString(1, apiKey);
            insertPstmt.setString(2, apiBaseUrl != null && !apiBaseUrl.trim().isEmpty() ? apiBaseUrl : "https://unismsapi.com/api");
            insertPstmt.setString(3, senderName);
            insertPstmt.setBoolean(4, enabled);
            insertPstmt.setString(5, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            insertPstmt.executeUpdate();
            
            logAction("System", "Updated SMS configuration", 
                     "Enabled: " + enabled + ", Sender: " + senderName + ", API URL: " + apiBaseUrl, "SMS");
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Save SMS configuration (backward compatibility)
     * @param apiKey SMS API PH key
     * @param senderName Sender name (not used by SMS API PH, kept for compatibility)
     * @param enabled Enable/disable SMS notifications
     */
    public static void saveSMSConfig(String apiKey, String senderName, boolean enabled) {
        saveSMSConfig(apiKey, null, senderName, enabled);
    }
    
    /**
     * Log SMS transaction
     * @param phoneNumber Recipient phone number
     * @param message Message content
     * @param status Status (SENT, FAILED, ERROR, etc.)
     * @param messageId SMS API PH message ID
     * @param errorCode Error code if failed
     */
    public static void logSMS(String phoneNumber, String message, String status, String messageId, String errorCode) {
        String sql = "INSERT INTO sms_log (phone_number, message, status, message_id, error_code, sent_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            pstmt.setString(1, phoneNumber);
            pstmt.setString(2, message);
            pstmt.setString(3, status);
            pstmt.setString(4, messageId);
            pstmt.setString(5, errorCode);
            pstmt.setString(6, timestamp);
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("✓ SMS log entry created: " + phoneNumber + " - " + status + " at " + timestamp);
            } else {
                System.err.println("✗ SMS log entry not created (0 rows affected)");
            }
            
        } catch (SQLException e) {
            System.err.println("✗ SQL Exception in logSMS: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Get SMS log entries
     * @param limit Maximum number of entries to return
     * @return List of SMS log entries
     */
    public static ObservableList<SMSLogEntry> getSMSLog(int limit) {
        ObservableList<SMSLogEntry> logs = FXCollections.observableArrayList();
        String sql = "SELECT * FROM sms_log ORDER BY sent_at DESC LIMIT ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                logs.add(new SMSLogEntry(
                    rs.getInt("id"),
                    rs.getString("phone_number"),
                    rs.getString("message"),
                    rs.getString("status"),
                    rs.getString("message_id"),
                    rs.getString("error_code"),
                    rs.getString("sent_at")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }
    
    /**
     * Get SMS log entries (alias for getSMSLog)
     * @param limit Maximum number of entries to return
     * @return List of SMS log entries
     */
    public static ObservableList<SMSLogEntry> getSMSLogs(int limit) {
        return getSMSLog(limit);
    }
    
    /**
     * Get SMS statistics
     * @return Map containing SMS statistics
     */
    public static Map<String, Integer> getSMSStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        String sql = "SELECT status, COUNT(*) as count FROM sms_log GROUP BY status";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                stats.put(rs.getString("status"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }
    
    /**
     * Get SMS template by name
     * @param name Template name
     * @return Template string or null if not found
     */
    public static String getSMSTemplate(String name) {
        String sql = "SELECT template FROM sms_templates WHERE name = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("template");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Get all SMS templates
     * @return List of SMS templates
     */
    public static ObservableList<SMSTemplate> getAllSMSTemplates() {
        ObservableList<SMSTemplate> templates = FXCollections.observableArrayList();
        String sql = "SELECT * FROM sms_templates ORDER BY category, name";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                templates.add(new SMSTemplate(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("template"),
                    rs.getString("description"),
                    rs.getString("category")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return templates;
    }
    
    /**
     * Save or update SMS template
     * @param name Template name
     * @param template Template content
     * @param category Template category
     * @param description Template description
     */
    public static void saveSMSTemplate(String name, String template, String category, String description) {
        // Check if template exists
        String checkSql = "SELECT id FROM sms_templates WHERE name = ?";
        String updateSql = "UPDATE sms_templates SET template = ?, category = ?, description = ? WHERE name = ?";
        String insertSql = "INSERT INTO sms_templates (name, template, category, description) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = getConnection()) {
            // Check if exists
            boolean exists = false;
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, name);
                ResultSet rs = checkStmt.executeQuery();
                exists = rs.next();
            }
            
            if (exists) {
                // Update existing
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setString(1, template);
                    updateStmt.setString(2, category);
                    updateStmt.setString(3, description);
                    updateStmt.setString(4, name);
                    updateStmt.executeUpdate();
                }
            } else {
                // Insert new
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setString(1, name);
                    insertStmt.setString(2, template);
                    insertStmt.setString(3, category);
                    insertStmt.setString(4, description);
                    insertStmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Update SMS template
     * @param id Template ID
     * @param template New template content
     */
    public static void updateSMSTemplate(int id, String template) {
        String sql = "UPDATE sms_templates SET template = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, template);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
            logAction("System", "Updated SMS template (ID: " + id + ")", template, "SMS");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Get resident phone number by ID
     * @param residentId Resident ID
     * @return Phone number or null if not found
     */
    public static String getResidentPhoneNumber(int residentId) {
        String sql = "SELECT phone_number FROM residents WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, residentId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("phone_number");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}