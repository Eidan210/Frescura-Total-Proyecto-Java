package com.frescuratotal.erp.sales.model.delivery;

/** Delivery modes offered to the customer. The label is what the guide and the user call them. */
public enum DeliveryMode {
    STANDARD("ESTANDAR", "no surcharge"),
    URGENT("URGENTE", "+12%");

    private final String label;
    private final String description;

    DeliveryMode(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    /** Reads the value stored in ventas.modalidad_entrega. */
    public static DeliveryMode fromName(String name) {
        return name == null ? STANDARD : valueOf(name);
    }
}
