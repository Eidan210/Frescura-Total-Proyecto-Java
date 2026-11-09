package com.frescuratotal.erp.sales.model;

public enum SaleStatus {
    REGISTERED(1),
    IN_PREPARATION(2),
    IN_TRANSIT(3),
    DELIVERED(4),
    CANCELLED(5),
    RETURNED(6);

    private final int id;

    SaleStatus(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    /**
     * "CONFIRMADA" in the statistics guide: a sale that was confirmed and still counts as sold.
     * Cancelled and returned sales do not.
     */
    public boolean isConfirmed() {
        return this != CANCELLED && this != RETURNED;
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


