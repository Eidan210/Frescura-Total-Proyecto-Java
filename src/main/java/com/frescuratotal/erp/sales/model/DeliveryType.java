package com.frescuratotal.erp.sales.model;

/**
 * EJERCICIO 3 - Modalidad de entrega que ELIGE el usuario.
 *
 * Enum y no String: si alguien escribe "urgnte" con String compila y falla al ejecutar;
 * con enum (DeliveryType.URGNTE) ni siquiera compila.
 *
 * No confundir: DeliveryType = la ELECCIÓN del usuario.
 *               DeliveryStrategy = CÓMO se calcula (la elige DeliveryService).
 */
public enum DeliveryType {
    STANDARD,
    URGENT
}
