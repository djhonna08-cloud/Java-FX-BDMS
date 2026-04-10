package com.example;

public class AuditEntry {
    private int id;
    private String timestamp;
    private String user;
    private String action;
    private String details;
    private String category; // e.g., "Resident", "Role", "System"

    // Constructor
    public AuditEntry(int id, String timestamp, String user, String action, String details, String category) {
        this.id = id;
        this.timestamp = timestamp;
        this.user = user;
        this.action = action;
        this.details = details;
        this.category = category;
    }

    // Simplified constructor for display (without id)
    public AuditEntry(String timestamp, String user, String action) {
        this.timestamp = timestamp;
        this.user = user;
        this.action = action;
        this.details = "";
        this.category = "";
    }

    // Full constructor
    public AuditEntry(String timestamp, String user, String action, String details, String category) {
        this.id = 0;
        this.timestamp = timestamp;
        this.user = user;
        this.action = action;
        this.details = details;
        this.category = category;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
