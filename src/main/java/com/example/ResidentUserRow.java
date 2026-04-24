package com.example;

import javafx.beans.property.*;

/**
 * Unified view model combining a Resident with their optional User account.
 * Used in the User Management table to show ALL residents and their account status.
 */
public class ResidentUserRow {
    // Resident fields
    private final IntegerProperty residentId;
    private final StringProperty fullName;
    private final StringProperty phoneNumber;
    private final StringProperty address;
    private final StringProperty gender;

    // User account fields (null/empty if no account)
    private final IntegerProperty userId;       // 0 = no account
    private final StringProperty username;      // "" = no account
    private final StringProperty role;          // "" = no role assigned
    private final BooleanProperty hasAccount;
    private final BooleanProperty isActive;
    private final StringProperty lastLogin;

    public ResidentUserRow(
            int residentId, String fullName, String phoneNumber, String address, String gender,
            int userId, String username, String role, boolean isActive, String lastLogin) {
        this.residentId  = new SimpleIntegerProperty(residentId);
        this.fullName    = new SimpleStringProperty(fullName != null ? fullName : "");
        this.phoneNumber = new SimpleStringProperty(phoneNumber != null ? phoneNumber : "");
        this.address     = new SimpleStringProperty(address != null ? address : "");
        this.gender      = new SimpleStringProperty(gender != null ? gender : "");
        this.userId      = new SimpleIntegerProperty(userId);
        this.username    = new SimpleStringProperty(username != null ? username : "");
        this.role        = new SimpleStringProperty(role != null ? role : "");
        this.hasAccount  = new SimpleBooleanProperty(userId > 0);
        this.isActive    = new SimpleBooleanProperty(isActive);
        this.lastLogin   = new SimpleStringProperty(lastLogin != null ? lastLogin : "Never");
    }

    // Getters
    public int getResidentId()   { return residentId.get(); }
    public String getFullName()  { return fullName.get(); }
    public String getPhoneNumber() { return phoneNumber.get(); }
    public String getAddress()   { return address.get(); }
    public String getGender()    { return gender.get(); }
    public int getUserId()       { return userId.get(); }
    public String getUsername()  { return username.get(); }
    public String getRole()      { return role.get(); }
    public boolean hasAccount()  { return hasAccount.get(); }
    public boolean isActive()    { return isActive.get(); }
    public String getLastLogin() { return lastLogin.get(); }

    // Setters (for live updates)
    public void setRole(String value)     { role.set(value != null ? value : ""); }
    public void setUsername(String value) { username.set(value != null ? value : ""); }
    public void setUserId(int value)      { userId.set(value); hasAccount.set(value > 0); }
    public void setActive(boolean value)  { isActive.set(value); }

    // Properties
    public IntegerProperty residentIdProperty()  { return residentId; }
    public StringProperty fullNameProperty()     { return fullName; }
    public StringProperty phoneNumberProperty()  { return phoneNumber; }
    public StringProperty addressProperty()      { return address; }
    public StringProperty genderProperty()       { return gender; }
    public IntegerProperty userIdProperty()      { return userId; }
    public StringProperty usernameProperty()     { return username; }
    public StringProperty roleProperty()         { return role; }
    public BooleanProperty hasAccountProperty()  { return hasAccount; }
    public BooleanProperty isActiveProperty()    { return isActive; }
    public StringProperty lastLoginProperty()    { return lastLogin; }
}
