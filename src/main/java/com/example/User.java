package com.example;

import javafx.beans.property.*;

public class User {
    private final IntegerProperty id;
    private final StringProperty username;
    private final StringProperty role;
    private final StringProperty createdDate;
    private final StringProperty lastLogin;
    private final BooleanProperty isActive;
    private final IntegerProperty residentId;

    public User(int id, String username, String role, String createdDate, String lastLogin, boolean isActive, int residentId) {
        this.id = new SimpleIntegerProperty(id);
        this.username = new SimpleStringProperty(username);
        this.role = new SimpleStringProperty(role);
        this.createdDate = new SimpleStringProperty(createdDate);
        this.lastLogin = new SimpleStringProperty(lastLogin);
        this.isActive = new SimpleBooleanProperty(isActive);
        this.residentId = new SimpleIntegerProperty(residentId);
    }

    public User(int id, String username, String role, String createdDate, String lastLogin, boolean isActive) {
        this(id, username, role, createdDate, lastLogin, isActive, 0);
    }

    public User(String username, String role) {
        this(0, username, role, java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), "Never", true, 0);
    }

    // Getters
    public int getId() { return id.get(); }
    public String getUsername() { return username.get(); }
    public String getRole() { return role.get(); }
    public String getCreatedDate() { return createdDate.get(); }
    public String getLastLogin() { return lastLogin.get(); }
    public boolean isActive() { return isActive.get(); }
    public int getResidentId() { return residentId.get(); }

    // Setters
    public void setId(int value) { id.set(value); }
    public void setUsername(String value) { username.set(value); }
    public void setRole(String value) { role.set(value); }
    public void setCreatedDate(String value) { createdDate.set(value); }
    public void setLastLogin(String value) { lastLogin.set(value); }
    public void setActive(boolean value) { isActive.set(value); }
    public void setResidentId(int value) { residentId.set(value); }

    // Properties
    public IntegerProperty idProperty() { return id; }
    public StringProperty usernameProperty() { return username; }
    public StringProperty roleProperty() { return role; }
    public StringProperty createdDateProperty() { return createdDate; }
    public StringProperty lastLoginProperty() { return lastLogin; }
    public BooleanProperty isActiveProperty() { return isActive; }
    public IntegerProperty residentIdProperty() { return residentId; }
}