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
                    "address VARCHAR(500))";
            stmt.execute(createResidents);

            // Ensure middle_name column exists for older DBs
            try {
                stmt.execute("ALTER TABLE residents ADD COLUMN middle_name VARCHAR(100)");
            } catch (SQLException ignored) {
                // Column probably already exists; ignore
            }

            // Ensure address column exists for older DBs
            try {
                stmt.execute("ALTER TABLE residents ADD COLUMN address VARCHAR(500)");
            } catch (SQLException ignored) {
                // Column probably already exists; ignore
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

            // Insert sample residents only if the table is empty
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM residents")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    System.out.println("Residents table is empty. Inserting sample data...");
                    String[] sampleResidents = {
                        "INSERT INTO residents (first_name, last_name, birth_date, gender, address) VALUES ('Juan', 'Dela Cruz', '1990-05-15', 'Male', 'Purok 1, Brgy. San Marino')",
                        "INSERT INTO residents (first_name, last_name, birth_date, gender, address) VALUES ('Maria', 'Clara', '1992-10-20', 'Female', 'Purok 2, Brgy. San Marino')",
                        "INSERT INTO residents (first_name, last_name, birth_date, gender, address) VALUES ('Jose', 'Rizal', '1861-06-19', 'Male', 'Calamba, Laguna')"
                    };
                    for (String insert : sampleResidents) {
                        stmt.execute(insert);
                    }
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
            case "Resident":
                permissions.put("Resident Data", "Own Only");
                permissions.put("Financials", "None");
                permissions.put("Blotter/Legal", "None");
                permissions.put("System Settings", "None");
                break;
            default:
                permissions.put("All", "None");
        }
        return permissions;
    }

    public static ObservableList<SystemUser> getSystemUsers() {
        ObservableList<SystemUser> users = FXCollections.observableArrayList();
        String sql = "SELECT id, username, role FROM users";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(new SystemUser(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("role")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
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
                residents.add(new Resident(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("middle_name"),
                        rs.getString("last_name"),
                        rs.getString("birth_date"),
                        rs.getString("gender"),
                        rs.getString("address")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return residents;
    }

    public static void addResident(Resident resident) {
        String sql = "INSERT INTO residents(first_name, middle_name, last_name, birth_date, gender, address) VALUES(?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, resident.getFirstName());
            pstmt.setString(2, resident.getMiddleName());
            pstmt.setString(3, resident.getLastName());
            pstmt.setString(4, resident.getBirthDate());
            pstmt.setString(5, resident.getGender());
            pstmt.setString(6, resident.getAddress());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateResident(Resident resident) {
        String sql = "UPDATE residents SET first_name = ?, middle_name = ?, last_name = ?, birth_date = ?, gender = ?, address = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, resident.getFirstName());
            pstmt.setString(2, resident.getMiddleName());
            pstmt.setString(3, resident.getLastName());
            pstmt.setString(4, resident.getBirthDate());
            pstmt.setString(5, resident.getGender());
            pstmt.setString(6, resident.getAddress());
            pstmt.setInt(7, resident.getId());
            pstmt.executeUpdate();
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
                return Optional.of(new Resident(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("middle_name"),
                        rs.getString("last_name"),
                        rs.getString("birth_date"),
                        rs.getString("gender"),
                        rs.getString("address")));
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

    public static class SystemUser {
        private final int id;
        private final String username;
        private final String role;

        public SystemUser(int id, String username, String role) {
            this.id = id;
            this.username = username;
            this.role = role;
        }

        public int getId() { return id; }
        public String getUsername() { return username; }
        public String getRole() { return role; }
    }
}