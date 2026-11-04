package com.frescuratotal.erp.sales.model;

public enum SaleStatus {
    REGISTERED(1),
    IN_PREPARATION(2),
    IN_TRANSIT(3),
    DELIVERED(4),
    CANCELLED(5);

    private final int id;

    SaleStatus(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static SaleStatus fromId(int id){
        for (SaleStatus status : values()) {

            if (status.getId() == id) {
                return status;
            }
        }

        throw new IllegalArgumentException("Unknown sale status id: " + id);
    }
}


