package com.frescuratotal.erp.inventory.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class WasteRecord {

    private int id;
    private int lotId;
    private Integer inspectionId;
    private BigDecimal lostQuantity;
    private String reason;
    private LocalDateTime registrationDate;
    private String registeredBy;

    public WasteRecord() {}

    public WasteRecord(int lotId, Integer inspectionId, BigDecimal lostQuantity, String reason, LocalDateTime registrationDate, String registeredBy) {
        this.lotId = lotId;
        this.inspectionId = inspectionId;
        this.lostQuantity = lostQuantity;
        this.reason = reason;
        this.registrationDate = registrationDate;
        this.registeredBy = registeredBy;
    }

    // GETTERS


    public int getId() {
        return id;
    }

    public int getLotId() {
        return lotId;
    }

    public Integer getInspectionId() {
        return inspectionId;
    }

    public BigDecimal getLostQuantity() {
        return lostQuantity;
    }

    public String getReason() {
        return reason;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public String getRegisteredBy() {
        return registeredBy;
    }

    // SETTERS


    public void setId(int id) {
        this.id = id;
    }

    public void setLotId(int lotId) {
        this.lotId = lotId;
    }

    public void setInspectionId(Integer inspectionId) {
        this.inspectionId = inspectionId;
    }

    public void setLostQuantity(BigDecimal lostQuantity) {
        this.lostQuantity = lostQuantity;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public void setRegisteredBy(String registeredBy) {
        this.registeredBy = registeredBy;
    }

    // TOSTRING

    @Override
    public String toString() {
        return "WasteRecord{" +
                "id=" + id +
                ", lotId=" + lotId +
                ", inspectionId=" + inspectionId +
                ", lostQuantity=" + lostQuantity +
                ", reason='" + reason + '\'' +
                ", registrationDate=" + registrationDate +
                ", registeredBy='" + registeredBy + '\'' +
                '}';
    }
}
