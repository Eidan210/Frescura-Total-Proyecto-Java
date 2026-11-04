package com.frescuratotal.erp.inventory.model;

public interface QualityDao {
    void saveQuarantine(QuarantineRecord record);
    void resolveQuarantine(int lotId, String notes);
    int saveInspection(QualityInspection inspection);
    void saveWaste(WasteRecord waste);
}
