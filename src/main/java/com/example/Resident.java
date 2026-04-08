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
    private final StringProperty photoPath;

    public Resident(int id, String firstName, String middleName, String lastName, String birthDate, String gender, String address) {
        this(id, firstName, middleName, lastName, birthDate, gender, address, null);
    }

    public Resident(int id, String firstName, String middleName, String lastName, String birthDate, String gender, String address, String photoPath) {
        this.id = new SimpleIntegerProperty(id);
        this.firstName = new SimpleStringProperty(firstName);
        this.middleName = new SimpleStringProperty(middleName);
        this.lastName = new SimpleStringProperty(lastName);
        this.birthDate = new SimpleStringProperty(birthDate);
        this.gender = new SimpleStringProperty(gender);
        this.address = new SimpleStringProperty(address);
        this.photoPath = new SimpleStringProperty(photoPath);
    }

    // Getters for property values
    public int getId() { return id.get(); }
    public String getFirstName() { return firstName.get(); }
    public String getMiddleName() { return middleName.get(); }
    public String getLastName() { return lastName.get(); }
    public String getBirthDate() { return birthDate.get(); }
    public String getGender() { return gender.get(); }
    public String getAddress() { return address.get(); }
    public String getPhotoPath() { return photoPath.get(); }

    // Setters for property values
    public void setFirstName(String value) { firstName.set(value); }
    public void setMiddleName(String value) { middleName.set(value); }
    public void setLastName(String value) { lastName.set(value); }
    public void setBirthDate(String value) { birthDate.set(value); }
    public void setGender(String value) { gender.set(value); }
    public void setAddress(String value) { address.set(value); }
    public void setPhotoPath(String value) { photoPath.set(value); }

    // Getters for JavaFX properties (for TableView)
    public IntegerProperty idProperty() { return id; }
    public StringProperty firstNameProperty() { return firstName; }
    public StringProperty middleNameProperty() { return middleName; }
    public StringProperty lastNameProperty() { return lastName; }
    public StringProperty birthDateProperty() { return birthDate; }
    public StringProperty genderProperty() { return gender; }
    public StringProperty addressProperty() { return address; }
    public StringProperty photoPathProperty() { return photoPath; }
}