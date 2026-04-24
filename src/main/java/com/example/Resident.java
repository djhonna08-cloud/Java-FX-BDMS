package com.example;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Resident {
    private final IntegerProperty id;
    private final StringProperty firstName;
    private final StringProperty middleName;
    private final StringProperty lastName;
    private final StringProperty birthDate;
    private final StringProperty gender;
    private final StringProperty address;
    private final StringProperty imagePath;
    private final StringProperty role;
    private final StringProperty phoneNumber;
    
    // Extended fields for professor's CSV format
    private final IntegerProperty familyId;
    private final StringProperty houseUnit;
    private final StringProperty street;
    private final StringProperty subdivision;
    private final StringProperty gateColor;
    private final IntegerProperty vaccinationCount;

    public Resident(int id, String firstName, String middleName, String lastName, String birthDate, String gender, String address) {
        this.id = new SimpleIntegerProperty(id);
        this.firstName = new SimpleStringProperty(firstName);
        this.middleName = new SimpleStringProperty(middleName);
        this.lastName = new SimpleStringProperty(lastName);
        this.birthDate = new SimpleStringProperty(birthDate);
        this.gender = new SimpleStringProperty(gender);
        this.address = new SimpleStringProperty(address);
        this.imagePath = new SimpleStringProperty("");
        this.role = new SimpleStringProperty("");
        this.phoneNumber = new SimpleStringProperty("");
        
        // Initialize extended fields with defaults
        this.familyId = new SimpleIntegerProperty(0);
        this.houseUnit = new SimpleStringProperty("");
        this.street = new SimpleStringProperty("");
        this.subdivision = new SimpleStringProperty("");
        this.gateColor = new SimpleStringProperty("");
        this.vaccinationCount = new SimpleIntegerProperty(0);
    }
    
    // Extended constructor with all fields
    public Resident(int id, String firstName, String middleName, String lastName, String birthDate, String gender, String address,
                   Integer familyId, String houseUnit, String street, String subdivision, String gateColor, Integer vaccinationCount) {
        this.id = new SimpleIntegerProperty(id);
        this.firstName = new SimpleStringProperty(firstName);
        this.middleName = new SimpleStringProperty(middleName);
        this.lastName = new SimpleStringProperty(lastName);
        this.birthDate = new SimpleStringProperty(birthDate);
        this.gender = new SimpleStringProperty(gender);
        this.address = new SimpleStringProperty(address);
        this.imagePath = new SimpleStringProperty("");
        this.role = new SimpleStringProperty("");
        this.phoneNumber = new SimpleStringProperty("");
        
        // Initialize extended fields
        this.familyId = new SimpleIntegerProperty(familyId != null ? familyId : 0);
        this.houseUnit = new SimpleStringProperty(houseUnit != null ? houseUnit : "");
        this.street = new SimpleStringProperty(street != null ? street : "");
        this.subdivision = new SimpleStringProperty(subdivision != null ? subdivision : "");
        this.gateColor = new SimpleStringProperty(gateColor != null ? gateColor : "");
        this.vaccinationCount = new SimpleIntegerProperty(vaccinationCount != null ? vaccinationCount : 0);
    }

    // Getters for property values
    public int getId() { return id.get(); }
    public String getFirstName() { return firstName.get(); }
    public String getMiddleName() { return middleName.get(); }
    public String getLastName() { return lastName.get(); }
    public String getBirthDate() { return birthDate.get(); }
    public String getGender() { return gender.get(); }
    public String getAddress() { return address.get(); }

    // Setters for property values
    public void setFirstName(String value) { firstName.set(value); }
    public void setMiddleName(String value) { middleName.set(value); }
    public void setLastName(String value) { lastName.set(value); }
    public void setBirthDate(String value) { birthDate.set(value); }
    public void setGender(String value) { gender.set(value); }
    public void setAddress(String value) { address.set(value); }
    public String getImagePath() { return imagePath.get(); }
    public void setImagePath(String value) { imagePath.set(value); }
    public String getRole() { return role.get(); }
    public void setRole(String value) { role.set(value); }
    public String getPhoneNumber() { return phoneNumber.get(); }
    public void setPhoneNumber(String value) { phoneNumber.set(value); }

    // Getters for JavaFX properties (for TableView)
    public IntegerProperty idProperty() { return id; }
    public StringProperty firstNameProperty() { return firstName; }
    public StringProperty middleNameProperty() { return middleName; }
    public StringProperty lastNameProperty() { return lastName; }
    public StringProperty birthDateProperty() { return birthDate; }
    public StringProperty genderProperty() { return gender; }
    public StringProperty addressProperty() { return address; }
    public StringProperty imagePathProperty() { return imagePath; }
    public StringProperty roleProperty() { return role; }
    public StringProperty phoneNumberProperty() { return phoneNumber; }
    
    // Extended field getters
    public Integer getFamilyId() { return familyId.get(); }
    public String getHouseUnit() { return houseUnit.get(); }
    public String getStreet() { return street.get(); }
    public String getSubdivision() { return subdivision.get(); }
    public String getGateColor() { return gateColor.get(); }
    public Integer getVaccinationCount() { return vaccinationCount.get(); }
    
    // Extended field setters
    public void setFamilyId(Integer value) { familyId.set(value != null ? value : 0); }
    public void setHouseUnit(String value) { houseUnit.set(value != null ? value : ""); }
    public void setStreet(String value) { street.set(value != null ? value : ""); }
    public void setSubdivision(String value) { subdivision.set(value != null ? value : ""); }
    public void setGateColor(String value) { gateColor.set(value != null ? value : ""); }
    public void setVaccinationCount(Integer value) { vaccinationCount.set(value != null ? value : 0); }
    
    // Extended field properties
    public IntegerProperty familyIdProperty() { return familyId; }
    public StringProperty houseUnitProperty() { return houseUnit; }
    public StringProperty streetProperty() { return street; }
    public StringProperty subdivisionProperty() { return subdivision; }
    public StringProperty gateColorProperty() { return gateColor; }
    public IntegerProperty vaccinationCountProperty() { return vaccinationCount; }
    
    /**
     * Get detailed address constructed from components
     * Falls back to the address field if components are empty
     */
    public String getDetailedAddress() {
        StringBuilder addr = new StringBuilder();
        
        String unit = houseUnit.get();
        String str = street.get();
        String subdiv = subdivision.get();
        
        if (unit != null && !unit.trim().isEmpty()) {
            addr.append(unit.trim());
        }
        if (str != null && !str.trim().isEmpty()) {
            if (addr.length() > 0) addr.append(", ");
            addr.append(str.trim());
        }
        if (subdiv != null && !subdiv.trim().isEmpty()) {
            if (addr.length() > 0) addr.append(", ");
            addr.append(subdiv.trim());
        }
        
        // Fall back to address field if no components
        if (addr.length() == 0) {
            String mainAddr = address.get();
            return (mainAddr != null && !mainAddr.trim().isEmpty()) ? mainAddr : "";
        }
        
        return addr.toString();
    }
}