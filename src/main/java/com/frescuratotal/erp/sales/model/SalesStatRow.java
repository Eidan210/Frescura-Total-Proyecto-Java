package com.frescuratotal.erp.sales.model;

import java.math.BigDecimal;

/**
 * EJERCICIO 5: una fila de la consulta de estadisticas.
 * Cada fila = una linea de venta (detalle) con los datos que pide el ejercicio.
 *
 * Es un record: una clase cortita que solo carga datos. Java crea solo
 * el constructor y los getters: saleId(), quantity(), category(), customerName(), status().
 */
public record SalesStatRow(int saleId, BigDecimal quantity, String category, String customerName, SaleStatus status) {
}
