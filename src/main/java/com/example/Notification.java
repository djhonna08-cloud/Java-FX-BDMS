package com.example;

import javafx.beans.property.*;

public class Notification {
    private final IntegerProperty id;
    private final StringProperty title;
    private final StringProperty message;
    private final StringProperty type; // INFO, WARNING, SUCCESS, ERROR
    private final StringProperty timestamp;
    private final BooleanProperty isRead;
    private final StringProperty actionUrl; // Optional: link to related section
    private final StringProperty icon; // FontAwesome icon code

    public Notification(int id, String title, String message, String type, String timestamp, boolean isRead, String actionUrl, String icon) {
        this.id = new SimpleIntegerProperty(id);
        this.title = new SimpleStringProperty(title);
        this.message = new SimpleStringProperty(message);
        this.type = new SimpleStringProperty(type);
        this.timestamp = new SimpleStringProperty(timestamp);
        this.isRead = new SimpleBooleanProperty(isRead);
        this.actionUrl = new SimpleStringProperty(actionUrl);
        this.icon = new SimpleStringProperty(icon);
    }

    public Notification(String title, String message, String type, String icon) {
        this(0, title, message, type, java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), false, null, icon);
    }

    // Getters
    public int getId() { return id.get(); }
    public String getTitle() { return title.get(); }
    public String getMessage() { return message.get(); }
    public String getType() { return type.get(); }
    public String getTimestamp() { return timestamp.get(); }
    public boolean isRead() { return isRead.get(); }
    public String getActionUrl() { return actionUrl.get(); }
    public String getIcon() { return icon.get(); }

    // Setters
    public void setId(int value) { id.set(value); }
    public void setTitle(String value) { title.set(value); }
    public void setMessage(String value) { message.set(value); }
    public void setType(String value) { type.set(value); }
    public void setTimestamp(String value) { timestamp.set(value); }
    public void setRead(boolean value) { isRead.set(value); }
    public void setActionUrl(String value) { actionUrl.set(value); }
    public void setIcon(String value) { icon.set(value); }

    // Properties
    public IntegerProperty idProperty() { return id; }
    public StringProperty titleProperty() { return title; }
    public StringProperty messageProperty() { return message; }
    public StringProperty typeProperty() { return type; }
    public StringProperty timestampProperty() { return timestamp; }
    public BooleanProperty isReadProperty() { return isRead; }
    public StringProperty actionUrlProperty() { return actionUrl; }
    public StringProperty iconProperty() { return icon; }
}
