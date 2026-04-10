package com.example;

import javafx.beans.property.*;

public class Announcement {
    private final SimpleIntegerProperty id;
    private final SimpleStringProperty title;
    private final SimpleStringProperty content;
    private final SimpleStringProperty type; // Event, Emergency Alert, Program
    private final SimpleStringProperty postedDate;
    private final SimpleStringProperty postedBy;
    private final SimpleStringProperty status; // Active, Archive
    private final SimpleStringProperty startDate;
    private final SimpleStringProperty endDate;
    private final SimpleIntegerProperty views;

    public Announcement(int id, String title, String content, String type, String postedDate, 
                       String postedBy, String status, String startDate, String endDate, int views) {
        this.id = new SimpleIntegerProperty(id);
        this.title = new SimpleStringProperty(title);
        this.content = new SimpleStringProperty(content);
        this.type = new SimpleStringProperty(type);
        this.postedDate = new SimpleStringProperty(postedDate);
        this.postedBy = new SimpleStringProperty(postedBy);
        this.status = new SimpleStringProperty(status);
        this.startDate = new SimpleStringProperty(startDate);
        this.endDate = new SimpleStringProperty(endDate);
        this.views = new SimpleIntegerProperty(views);
    }

    // For creating new announcements
    public Announcement(String title, String content, String type, String postedBy, 
                       String startDate, String endDate) {
        this.id = new SimpleIntegerProperty(0);
        this.title = new SimpleStringProperty(title);
        this.content = new SimpleStringProperty(content);
        this.type = new SimpleStringProperty(type);
        this.postedDate = new SimpleStringProperty(java.time.LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        this.postedBy = new SimpleStringProperty(postedBy);
        this.status = new SimpleStringProperty("Active");
        this.startDate = new SimpleStringProperty(startDate);
        this.endDate = new SimpleStringProperty(endDate);
        this.views = new SimpleIntegerProperty(0);
    }

    // Getters and setters
    public int getId() { return id.get(); }
    public void setId(int value) { id.set(value); }
    public IntegerProperty idProperty() { return id; }

    public String getTitle() { return title.get(); }
    public void setTitle(String value) { title.set(value); }
    public StringProperty titleProperty() { return title; }

    public String getContent() { return content.get(); }
    public void setContent(String value) { content.set(value); }
    public StringProperty contentProperty() { return content; }

    public String getType() { return type.get(); }
    public void setType(String value) { type.set(value); }
    public StringProperty typeProperty() { return type; }

    public String getPostedDate() { return postedDate.get(); }
    public void setPostedDate(String value) { postedDate.set(value); }
    public StringProperty postedDateProperty() { return postedDate; }

    public String getPostedBy() { return postedBy.get(); }
    public void setPostedBy(String value) { postedBy.set(value); }
    public StringProperty postedByProperty() { return postedBy; }

    public String getStatus() { return status.get(); }
    public void setStatus(String value) { status.set(value); }
    public StringProperty statusProperty() { return status; }

    public String getStartDate() { return startDate.get(); }
    public void setStartDate(String value) { startDate.set(value); }
    public StringProperty startDateProperty() { return startDate; }

    public String getEndDate() { return endDate.get(); }
    public void setEndDate(String value) { endDate.set(value); }
    public StringProperty endDateProperty() { return endDate; }

    public int getViews() { return views.get(); }
    public void setViews(int value) { views.set(value); }
    public IntegerProperty viewsProperty() { return views; }
}
