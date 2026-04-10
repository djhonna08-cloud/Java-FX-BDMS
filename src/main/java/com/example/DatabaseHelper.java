package com.example;

import java.sql.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.HashMap;
import java.util.Optional;
import java.util.Map;

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

    private static void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             Statement stmt = conn.createStatement()) {

            // Create users table
            String createUsers = "CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTO_INCREMENT, " +
                    "username VARCHAR(50) UNIQUE NOT NULL, " +
                    "password VARCHAR(50) NOT NULL, " +
                    "role VARCHAR(20) NOT NULL)";
            stmt.execute(createUsers);

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

            // Create roles table for managing custom roles
            String createRoles = "CREATE TABLE IF NOT EXISTS roles (" +
                    "id INTEGER PRIMARY KEY AUTO_INCREMENT, " +
                    "name VARCHAR(100) UNIQUE NOT NULL, " +
                    "description VARCHAR(500))";
            stmt.execute(createRoles);

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
                        "INSERT INTO users (username, password, role) VALUES ('superadmin', 'pass', 'Super Admin')",
                        "INSERT INTO users (username, password, role) VALUES ('owner', 'pass', 'Owner')",
                        "INSERT INTO users (username, password, role) VALUES ('secretary', 'pass', 'Secretary')",
                        "INSERT INTO users (username, password, role) VALUES ('treasurer', 'pass', 'Treasurer')",
                        "INSERT INTO users (username, password, role) VALUES ('captain', 'pass', 'Captain')",
                        "INSERT INTO users (username, password, role) VALUES ('resident', 'pass', 'Resident')"
                    };
                    for (String insert : users) {
                        stmt.execute(insert);
                    }
                    System.out.println("Default users inserted.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String authenticate(String username, String password) {
        // Use LOWER() for case-insensitive username matching
        String sql = "SELECT role FROM users WHERE LOWER(username) = ? AND password = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username.toLowerCase());
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("role");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Map<String, String> getPermissions(String role) {
        Map<String, String> permissions = new HashMap<>();
        switch (role) {
            case "Super Admin":
                permissions.put("Resident Data", "Full Access");
                permissions.put("Financials", "Full Access");
                permissions.put("Blotter/Legal", "Full Access");
                permissions.put("System Settings", "Full Access");
                break;
            case "Owner":
                permissions.put("Resident Data", "Secretary");
                permissions.put("Financials", "Manage");
                permissions.put("Blotter/Legal", "View Only");
                permissions.put("System Settings", "Manage");
                break;
            case "Secretary":
                permissions.put("Resident Data", "Manage");
                permissions.put("Financials", "View Only");
                permissions.put("Blotter/Legal", "Manage");
                permissions.put("System Settings", "None");
                break;
            case "Treasurer":
                permissions.put("Resident Data", "View Only");
                permissions.put("Financials", "Manage");
                permissions.put("Blotter/Legal", "None");
                permissions.put("System Settings", "None");
                break;
            case "Captain":
                permissions.put("Resident Data", "View Only");
                permissions.put("Financials", "View Only");
                permissions.put("Blotter/Legal", "View Only");
                permissions.put("System Settings", "None");
                break;
            case "Barangay Captain":
                permissions.put("Resident Data", "Full Access");
                permissions.put("Financials", "Full Access");
                permissions.put("Blotter/Legal", "Full Access");
                permissions.put("System Settings", "Full Access");
                break;
            case "Barangay Secretary":
                permissions.put("Resident Data", "Manage");
                permissions.put("Financials", "View Only");
                permissions.put("Blotter/Legal", "Manage");
                permissions.put("System Settings", "None");
                break;
            case "Barangay Treasurer":
                permissions.put("Resident Data", "View Only");
                permissions.put("Financials", "Manage");
                permissions.put("Blotter/Legal", "None");
                permissions.put("System Settings", "None");
                break;
            case "Kagawads":
                permissions.put("Resident Data", "View Only");
                permissions.put("Financials", "View Only");
                permissions.put("Blotter/Legal", "View Only");
                permissions.put("System Settings", "None");
                break;
            case "Barangay Health Workers":
                permissions.put("Resident Data", "Manage");
                permissions.put("Financials", "None");
                permissions.put("Blotter/Legal", "None");
                permissions.put("System Settings", "None");
                break;
            case "Barangay Tanods":
                permissions.put("Resident Data", "View Only");
                permissions.put("Financials", "None");
                permissions.put("Blotter/Legal", "Manage");
                permissions.put("System Settings", "None");
                break;
        }
        return permissions;
    }

    public static int getResidentCount(String filter) {
        String sql;
        if (filter == null || filter.isEmpty()) {
            sql = "SELECT COUNT(*) FROM residents";
        } else {
            sql = "SELECT COUNT(*) FROM residents WHERE LOWER(first_name) LIKE ? OR LOWER(last_name) LIKE ?";
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
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
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
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
                        rs.getString("address"));
                resident.setImagePath(rs.getString("image_path"));
                resident.setRole(rs.getString("role"));
                residents.add(resident);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return residents;
    }

    public static void addResident(Resident resident) {
        String sql = "INSERT INTO residents(first_name, middle_name, last_name, birth_date, gender, address, image_path, role) VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, resident.getFirstName());
            pstmt.setString(2, resident.getMiddleName());
            pstmt.setString(3, resident.getLastName());
            pstmt.setString(4, resident.getBirthDate());
            pstmt.setString(5, resident.getGender());
            pstmt.setString(6, resident.getAddress());
            pstmt.setString(7, resident.getImagePath());
            pstmt.setString(8, resident.getRole());
            pstmt.executeUpdate();
            logAction("System", "Created new resident: " + resident.getFirstName() + " " + resident.getLastName(), "Resident " + resident.getLastName() + ", " + resident.getFirstName(), "Resident");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateResident(Resident resident) {
        String sql = "UPDATE residents SET first_name = ?, middle_name = ?, last_name = ?, birth_date = ?, gender = ?, address = ?, image_path = ?, role = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, resident.getFirstName());
            pstmt.setString(2, resident.getMiddleName());
            pstmt.setString(3, resident.getLastName());
            pstmt.setString(4, resident.getBirthDate());
            pstmt.setString(5, resident.getGender());
            pstmt.setString(6, resident.getAddress());
            pstmt.setString(7, resident.getImagePath());
            pstmt.setString(8, resident.getRole());
            pstmt.setInt(9, resident.getId());
            pstmt.executeUpdate();
            logAction("System", "Updated resident information: " + resident.getFirstName() + " " + resident.getLastName(), "Resident " + resident.getLastName() + ", " + resident.getFirstName(), "Resident");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteResident(int id) {
        String sql = "DELETE FROM residents WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            logAction("System", "Deleted resident record (ID: " + id + ")", "Resident ID " + id, "Resident");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Optional<Resident> getResidentById(int id) {
        String sql = "SELECT * FROM residents WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
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
                        rs.getString("address"));
                resident.setImagePath(rs.getString("image_path"));
                resident.setRole(rs.getString("role"));
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
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
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
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
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
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
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
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
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
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
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
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
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
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
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
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
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
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
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
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
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
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
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
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
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
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
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
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
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
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
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

    public static Optional<DocumentRequest> getDocumentRequestById(int id) {
        String sql = "SELECT * FROM document_requests WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
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
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            
            System.out.println("Creating complaint: " + complaint.getTitle());
            
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
            
            int result = pstmt.executeUpdate();
            System.out.println("Insert result: " + result);
            
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                int complaintId = rs.getInt(1);
                System.out.println("Complaint created with ID: " + complaintId);
                logAction("System", "Created complaint: " + complaint.getTitle(), 
                         complaint.getResidentName(), "Complaint");
                return complaintId;
            } else {
                System.out.println("No generated keys returned");
            }
        } catch (SQLException e) {
            System.err.println("Error creating complaint: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    public static void updateComplaintStatus(int complaintId, String status) {
        String sql = "UPDATE complaints SET status = ?, last_updated = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
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
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
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
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
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
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
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
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
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
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
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
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
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
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
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
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
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
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
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
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
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
        String sql = "SELECT request_date, SUM(fee) as daily_total FROM document_requests " +
                     "WHERE payment_status = 'PAID' AND request_date >= DATE_SUB(CURRENT_DATE, INTERVAL 7 DAY) " +
                     "GROUP BY request_date ORDER BY request_date ASC";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            // Initialize last 7 days with 0 values
            java.time.LocalDate today = java.time.LocalDate.now();
            for (int i = 6; i >= 0; i--) {
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
            for (int i = 6; i >= 0; i--) {
                java.time.LocalDate date = today.minusDays(i);
                String dateStr = date.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                collections.put(dateStr, 0.0);
            }
        }
        return collections;
    }

    public static Map<String, Double> getMonthlyIncome() {
        Map<String, Double> income = new java.util.LinkedHashMap<>();
        String sql = "SELECT YEAR(request_date) as year, MONTH(request_date) as month, SUM(fee) as monthly_total " +
                     "FROM document_requests " +
                     "WHERE payment_status = 'PAID' AND request_date >= DATE_SUB(CURRENT_DATE, INTERVAL 12 MONTH) " +
                     "GROUP BY YEAR(request_date), MONTH(request_date) ORDER BY year ASC, month ASC";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
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
                int year = rs.getInt("year");
                int month = rs.getInt("month");
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
}