package com.frescuratotal.erp.inventory.model;

import java.time.LocalDateTime;

public class QualityInspection {

    private int id;
    private int lotId;
    private LocalDateTime inspectionDate;
    private InspectionResult result;
    private String inspectorName;
    private String rejectionReason;
    private String notes;

    public QualityInspection(){}

    public QualityInspection(int lotId, LocalDateTime inspectionDate, InspectionResult result, String inspectorName, String rejectionReason, String notes) {
        this.lotId = lotId;
        this.inspectionDate = inspectionDate;
        this.result = result;
        this.inspectorName = inspectorName;
        this.rejectionReason = rejectionReason;
        this.notes = notes;
    }

    // GETTERS

    public int getId() {
        return id;
    }

    public int getLotId() {
        return lotId;
    }

    public LocalDateTime getInspectionDate() {
        return inspectionDate;
    }

    public InspectionResult getResult() {
        return result;
    }

    public String getInspectorName() {
        return inspectorName;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public String getNotes() {
        return notes;
    }


    // SETTERS


    public void setId(int id) {
        this.id = id;
    }

    public void setLotId(int lotId) {
        this.lotId = lotId;
    }

    public void setInspectionDate(LocalDateTime inspectionDate) {
        this.inspectionDate = inspectionDate;
    }

    public void setResult(InspectionResult result) {
        this.result = result;
    }

    public void setInspectorName(String inspectorName) {
        this.inspectorName = inspectorName;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    // TOSTRING


    @Override
    public String toString() {
        return "QualityInspection{" +
                "id=" + id +
                ", lotId=" + lotId +
                ", inspectionDate=" + inspectionDate +
                ", result=" + result +
                ", inspectorName='" + inspectorName + '\'' +
                ", rejectionReason='" + rejectionReason + '\'' +
                ", notes='" + notes + '\'' +
                '}';
    }
}
