package com.example;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Resident {
    private final IntegerProperty id;
    private final StringProperty firstName;
    private final StringProperty lastName;
    private final StringProperty birthDate;
    private final StringProperty gender;
    private final StringProperty purok;

    public Resident(int id, String firstName, String lastName, String birthDate, String gender, String purok) {
        this.id = new SimpleIntegerProperty(id);
        this.firstName = new SimpleStringProperty(firstName);
        this.lastName = new SimpleStringProperty(lastName);
        this.birthDate = new SimpleStringProperty(birthDate);
        this.gender = new SimpleStringProperty(gender);
        this.purok = new SimpleStringProperty(purok);
    }

    // Getters for property values
    public int getId() { return id.get(); }
    public String getFirstName() { return firstName.get(); }
    public String getLastName() { return lastName.get(); }
    public String getBirthDate() { return birthDate.get(); }
    public String getGender() { return gender.get(); }
    public String getPurok() { return purok.get(); }

    // Setters for property values
    public void setFirstName(String value) { firstName.set(value); }
    public void setLastName(String value) { lastName.set(value); }
    public void setBirthDate(String value) { birthDate.set(value); }
    public void setGender(String value) { gender.set(value); }
    public void setPurok(String value) { purok.set(value); }

    // Getters for JavaFX properties (for TableView)
    public IntegerProperty idProperty() { return id; }
    public StringProperty firstNameProperty() { return firstName; }
    public StringProperty lastNameProperty() { return lastName; }
    public StringProperty birthDateProperty() { return birthDate; }
    public StringProperty genderProperty() { return gender; }
    public StringProperty purokProperty() { return purok; }
}