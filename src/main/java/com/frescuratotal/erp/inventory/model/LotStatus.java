package com.frescuratotal.erp.inventory.model;

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

    public static LotStatus fromId(int id){
        for(LotStatus status : values()){
            if(status.getId() == id){
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown lot status id: " + id);
    }

}
