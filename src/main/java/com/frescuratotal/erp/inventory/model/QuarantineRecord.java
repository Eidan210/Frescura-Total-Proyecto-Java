package com.frescuratotal.erp.inventory.model;

import java.time.LocalDateTime;

public class QuarantineRecord {
    private int id;
    private int lotId;
    private String reason;
    private String registeredBy;
    private LocalDateTime registrationDate;
    private boolean resolved;
    private LocalDateTime resolutionDate;
    private String resolutionNotes;

    public QuarantineRecord(int lotId, String reason, String registeredBy, LocalDateTime registrationDate, boolean resolved, LocalDateTime resolutionDate, String resolutionNotes) {
        this.lotId = lotId;
        this.reason = reason;
        this.registeredBy = registeredBy;
        this.registrationDate = registrationDate;
        this.resolved = resolved;
        this.resolutionDate = resolutionDate;
        this.resolutionNotes = resolutionNotes;
    }

    public QuarantineRecord() {}

    // GETTERS


    public int getId() {
        return id;
    }

    public int getLotId() {
        return lotId;
    }

    public String getReason() {
        return reason;
    }

    public String getRegisteredBy() {
        return registeredBy;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public boolean isResolved() {
        return resolved;
    }

    public LocalDateTime getResolutionDate() {
        return resolutionDate;
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }


    // SETTERS


    public void setId(int id) {
        this.id = id;
    }

    public void setLotId(int lotId) {
        this.lotId = lotId;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setRegisteredBy(String registeredBy) {
        this.registeredBy = registeredBy;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }

    public void setResolutionDate(LocalDateTime resolutionDate) {
        this.resolutionDate = resolutionDate;
    }

    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }

    // TOSTRING

    @Override
    public String toString() {
        return "QuarantineRecord{" +
                "id=" + id +
                ", lotId=" + lotId +
                ", reason='" + reason + '\'' +
                ", registeredBy='" + registeredBy + '\'' +
                ", registrationDate=" + registrationDate +
                ", resolved=" + resolved +
                ", resolutionDate=" + resolutionDate +
                ", resolutionNotes='" + resolutionNotes + '\'' +
                '}';
    }
}
