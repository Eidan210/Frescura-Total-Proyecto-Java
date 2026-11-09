package com.frescuratotal.erp.sales.model;

import java.math.BigDecimal;

/**
 * EJERCICIO 5 - Una fila del JOIN convertida en objeto Java.
 *
 * record: clase inmutable de solo datos. Java crea automáticamente el constructor,
 * los "getters" (saleId(), quantity()...), equals, hashCode y toString.
 * Ojo: los getters de un record NO llevan "get": se llaman row.quantity(), no row.getQuantity().
 */
public record SaleStatRow(int saleId, BigDecimal quantity, String categoryName,
                          String customerName, SaleStatus status) {
}
