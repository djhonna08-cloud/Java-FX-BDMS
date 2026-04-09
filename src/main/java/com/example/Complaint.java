package com.example;

import javafx.beans.property.*;

public class Complaint {
    private final SimpleIntegerProperty id;
    private final SimpleIntegerProperty residentId;
    private final SimpleStringProperty residentName;
    private final SimpleStringProperty title;
    private final SimpleStringProperty description;
    private final SimpleStringProperty status; // Pending, Ongoing, Resolved
    private final SimpleStringProperty dateSubmitted;
    private final SimpleStringProperty lastUpdated;
    private final SimpleStringProperty photoPath;
    private final SimpleStringProperty adminNotes;
    private final SimpleStringProperty assignedTo;

    public Complaint(int id, int residentId, String residentName, String title, String description, 
                     String status, String dateSubmitted, String lastUpdated, String photoPath, 
                     String adminNotes, String assignedTo) {
        this.id = new SimpleIntegerProperty(id);
        this.residentId = new SimpleIntegerProperty(residentId);
        this.residentName = new SimpleStringProperty(residentName);
        this.title = new SimpleStringProperty(title);
        this.description = new SimpleStringProperty(description);
        this.status = new SimpleStringProperty(status);
        this.dateSubmitted = new SimpleStringProperty(dateSubmitted);
        this.lastUpdated = new SimpleStringProperty(lastUpdated);
        this.photoPath = new SimpleStringProperty(photoPath);
        this.adminNotes = new SimpleStringProperty(adminNotes);
        this.assignedTo = new SimpleStringProperty(assignedTo);
    }

    // For creating new complaints
    public Complaint(int residentId, String residentName, String title, String description, String photoPath) {
        this.id = new SimpleIntegerProperty(0);
        this.residentId = new SimpleIntegerProperty(residentId);
        this.residentName = new SimpleStringProperty(residentName);
        this.title = new SimpleStringProperty(title);
        this.description = new SimpleStringProperty(description);
        this.status = new SimpleStringProperty("Pending");
        this.dateSubmitted = new SimpleStringProperty(java.time.LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        this.lastUpdated = new SimpleStringProperty(this.dateSubmitted.get());
        this.photoPath = new SimpleStringProperty(photoPath);
        this.adminNotes = new SimpleStringProperty("");
        this.assignedTo = new SimpleStringProperty("");
    }

    // Getters and setters
    public int getId() { return id.get(); }
    public void setId(int value) { id.set(value); }
    public IntegerProperty idProperty() { return id; }

    public int getResidentId() { return residentId.get(); }
    public void setResidentId(int value) { residentId.set(value); }
    public IntegerProperty residentIdProperty() { return residentId; }

    public String getResidentName() { return residentName.get(); }
    public void setResidentName(String value) { residentName.set(value); }
    public StringProperty residentNameProperty() { return residentName; }

    public String getTitle() { return title.get(); }
    public void setTitle(String value) { title.set(value); }
    public StringProperty titleProperty() { return title; }

    public String getDescription() { return description.get(); }
    public void setDescription(String value) { description.set(value); }
    public StringProperty descriptionProperty() { return description; }

    public String getStatus() { return status.get(); }
    public void setStatus(String value) { status.set(value); }
    public StringProperty statusProperty() { return status; }

    public String getDateSubmitted() { return dateSubmitted.get(); }
    public void setDateSubmitted(String value) { dateSubmitted.set(value); }
    public StringProperty dateSubmittedProperty() { return dateSubmitted; }

    public String getLastUpdated() { return lastUpdated.get(); }
    public void setLastUpdated(String value) { lastUpdated.set(value); }
    public StringProperty lastUpdatedProperty() { return lastUpdated; }

    public String getPhotoPath() { return photoPath.get(); }
    public void setPhotoPath(String value) { photoPath.set(value); }
    public StringProperty photoPathProperty() { return photoPath; }

    public String getAdminNotes() { return adminNotes.get(); }
    public void setAdminNotes(String value) { adminNotes.set(value); }
    public StringProperty adminNotesProperty() { return adminNotes; }

    public String getAssignedTo() { return assignedTo.get(); }
    public void setAssignedTo(String value) { assignedTo.set(value); }
    public StringProperty assignedToProperty() { return assignedTo; }
}
