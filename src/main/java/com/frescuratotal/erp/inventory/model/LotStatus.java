package com.frescuratotal.erp.inventory.model;

public enum LotStatus {
    AVAILABLE(1),
    NEAR_EXPIRY(2),
    EXPIRED(3),
    IN_QUARANTINE(4);

    public final int id;

    LotStatus(int id){
        this.id = id;
    }
}
