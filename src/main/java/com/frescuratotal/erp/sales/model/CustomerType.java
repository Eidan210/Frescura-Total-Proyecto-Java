package com.frescuratotal.erp.sales.model;

public enum CustomerType {
    WHOLESALE(1),
    RETAIL(2);

    private final int id;


    CustomerType (int id){
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static CustomerType fromId(int id){

        for (CustomerType type : values()) {

            if (type.getId() == id) {
                return type;
            }
        }

        throw new IllegalArgumentException("Unknown customer type id: " + id);
    }
}
