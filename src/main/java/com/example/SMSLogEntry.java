package com.example;

/**
 * Model class for SMS log entries
 */
public class SMSLogEntry {
    private final int id;
    private final String phoneNumber;
    private final String message;
    private final String status;
    private final String messageId;
    private final String errorCode;
    private final String timestamp;
    
    public SMSLogEntry(int id, String phoneNumber, String message, String status, 
                      String messageId, String errorCode, String timestamp) {
        this.id = id;
        this.phoneNumber = phoneNumber;
        this.message = message;
        this.status = status;
        this.messageId = messageId;
        this.errorCode = errorCode;
        this.timestamp = timestamp;
    }
    
    public int getId() { return id; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getMessage() { return message; }
    public String getStatus() { return status; }
    public String getMessageId() { return messageId; }
    public String getErrorCode() { return errorCode; }
    public String getTimestamp() { return timestamp; }
}
