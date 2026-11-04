package com.frescuratotal.erp.inventory.model;

import java.time.LocalDate;

public enum LotStatus {
    AVAILABLE(1),
    NEAR_EXPIRY(2),
    EXPIRED(3),
    IN_QUARANTINE(4);

    private final int id;

    LotStatus(int id){
        this.id = id;
    }

    public int getId(){
        return id;
    }

    /** The status a lot deserves by date alone: past its date, 7 days or less left, or more. */
    public static LotStatus forExpiration(LocalDate expirationDate) {
        LocalDate today = LocalDate.now();
        if (expirationDate.isBefore(today)) {
            return EXPIRED;
        }
        return expirationDate.isAfter(today.plusDays(7)) ? AVAILABLE : NEAR_EXPIRY;
    }

    public static LotStatus fromId(int id){
        for(LotStatus status : values()){
            if(status.getId() == id){
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown lot status id: " + id);
    }

}
