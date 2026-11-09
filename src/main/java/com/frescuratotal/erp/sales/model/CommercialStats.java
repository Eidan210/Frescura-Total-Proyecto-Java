package com.frescuratotal.erp.sales.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * EJERCICIO 5: el resultado que el servicio le entrega a la vista.
 * - hasData: false si no hay ninguna venta (para mostrar el mensaje informativo).
 * - topCategory / topQuantity: la categoria con mas unidades vendidas (null si no hay vendidas).
 * - returnedCustomers: clientes con devoluciones, sin repetidos (lista vacia si no hay).
 */
public record CommercialStats(boolean hasData, String topCategory, BigDecimal topQuantity, List<String> returnedCustomers) {
}
