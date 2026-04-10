package com.example;

import javafx.beans.property.*;

public class DocumentRequest {
    private final IntegerProperty id;
    private final IntegerProperty residentId;
    private final StringProperty residentName;
    private final StringProperty documentType;
    private final StringProperty status; // PENDING, APPROVED, REJECTED, COMPLETED
    private final StringProperty requestDate;
    private final StringProperty approvalDate;
    private final StringProperty approvedBy;
    private final DoubleProperty fee;
    private final StringProperty paymentStatus; // UNPAID, PAID
    private final StringProperty purpose;
    private final StringProperty notes;

    public DocumentRequest(int id, int residentId, String residentName, String documentType, String status,
                          String requestDate, String approvalDate, String approvedBy, double fee,
                          String paymentStatus, String purpose, String notes) {
        this.id = new SimpleIntegerProperty(id);
        this.residentId = new SimpleIntegerProperty(residentId);
        this.residentName = new SimpleStringProperty(residentName);
        this.documentType = new SimpleStringProperty(documentType);
        this.status = new SimpleStringProperty(status);
        this.requestDate = new SimpleStringProperty(requestDate);
        this.approvalDate = new SimpleStringProperty(approvalDate);
        this.approvedBy = new SimpleStringProperty(approvedBy);
        this.fee = new SimpleDoubleProperty(fee);
        this.paymentStatus = new SimpleStringProperty(paymentStatus);
        this.purpose = new SimpleStringProperty(purpose);
        this.notes = new SimpleStringProperty(notes);
    }

    // Alternative constructor for new requests
    public DocumentRequest(int residentId, String residentName, String documentType, String purpose) {
        this.id = new SimpleIntegerProperty(0);
        this.residentId = new SimpleIntegerProperty(residentId);
        this.residentName = new SimpleStringProperty(residentName);
        this.documentType = new SimpleStringProperty(documentType);
        this.status = new SimpleStringProperty("PENDING");
        this.requestDate = new SimpleStringProperty("");
        this.approvalDate = new SimpleStringProperty("");
        this.approvedBy = new SimpleStringProperty("");
        this.fee = new SimpleDoubleProperty(getFeeForDocumentType(documentType));
        this.paymentStatus = new SimpleStringProperty("UNPAID");
        this.purpose = new SimpleStringProperty(purpose);
        this.notes = new SimpleStringProperty("");
    }

    public static double getFeeForDocumentType(String documentType) {
        switch (documentType) {
            case "Barangay Clearance":
                return 50.0;
            case "Certificate of Residency":
                return 75.0;
            case "Indigency Certificate":
                return 100.0;
            default:
                return 0.0;
        }
    }

    // Getters and Setters
    public int getId() { return id.get(); }
    public void setId(int value) { id.set(value); }
    public IntegerProperty idProperty() { return id; }

    public int getResidentId() { return residentId.get(); }
    public void setResidentId(int value) { residentId.set(value); }
    public IntegerProperty residentIdProperty() { return residentId; }

    public String getResidentName() { return residentName.get(); }
    public void setResidentName(String value) { residentName.set(value); }
    public StringProperty residentNameProperty() { return residentName; }

    public String getDocumentType() { return documentType.get(); }
    public void setDocumentType(String value) { documentType.set(value); }
    public StringProperty documentTypeProperty() { return documentType; }

    public String getStatus() { return status.get(); }
    public void setStatus(String value) { status.set(value); }
    public StringProperty statusProperty() { return status; }

    public String getRequestDate() { return requestDate.get(); }
    public void setRequestDate(String value) { requestDate.set(value); }
    public StringProperty requestDateProperty() { return requestDate; }

    public String getApprovalDate() { return approvalDate.get(); }
    public void setApprovalDate(String value) { approvalDate.set(value); }
    public StringProperty approvalDateProperty() { return approvalDate; }

    public String getApprovedBy() { return approvedBy.get(); }
    public void setApprovedBy(String value) { approvedBy.set(value); }
    public StringProperty approvedByProperty() { return approvedBy; }

    public double getFee() { return fee.get(); }
    public void setFee(double value) { fee.set(value); }
    public DoubleProperty feeProperty() { return fee; }

    public String getPaymentStatus() { return paymentStatus.get(); }
    public void setPaymentStatus(String value) { paymentStatus.set(value); }
    public StringProperty paymentStatusProperty() { return paymentStatus; }

    public String getPurpose() { return purpose.get(); }
    public void setPurpose(String value) { purpose.set(value); }
    public StringProperty purposeProperty() { return purpose; }

    public String getNotes() { return notes.get(); }
    public void setNotes(String value) { notes.set(value); }
    public StringProperty notesProperty() { return notes; }
}
