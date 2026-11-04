package com.frescuratotal.erp.logistics.model.status;

public enum DeliveryRouteStatus {
    PLANNED("Planificada"),
    IN_TRANSIT("En Transito"),
    COMPLETED("Completada"),
    CANCELLED("Cancelada");

    private final String dbValue;

    DeliveryRouteStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static DeliveryRouteStatus fromDbValue(String value) {
        for (DeliveryRouteStatus status : values()) {
            if (status.dbValue.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown route status: " + value);
    }
}
