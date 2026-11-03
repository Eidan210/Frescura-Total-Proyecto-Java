package com.frescuratotal.erp.logistics.model.status;

public enum VehicleStatus {
    ACTIVE("Activo"),
    INACTIVE("Inactivo"),
    MAINTENANCE("Mantenimiento");

    private final String dbValue;

    VehicleStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static VehicleStatus fromDbValue(String value) {
        for (VehicleStatus status : values()) {
            if (status.dbValue.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown vehicle status: " + value);
    }

}
