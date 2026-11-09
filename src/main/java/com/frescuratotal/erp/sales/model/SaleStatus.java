package com.frescuratotal.erp.sales.model;

public enum SaleStatus {
    REGISTERED(1),
    IN_PREPARATION(2),
    IN_TRANSIT(3),
    DELIVERED(4),
    CANCELLED(5),
    // EJERCICIO 5: estado nuevo "Devuelta". Debe existir también en la BD con id 6
    // (ejecutar docs/practica_examen.sql). Si la BD tuviera una venta con estado 6 y el
    // enum no lo tuviera, fromId lanzaría error al listar ventas.
    RETURNED(6);

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


