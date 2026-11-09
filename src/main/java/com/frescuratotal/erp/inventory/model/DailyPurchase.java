package com.frescuratotal.erp.inventory.model;

import java.math.BigDecimal;

/**
 * EJERCICIO 2 - Una "compra" del día.
 *
 * El ERP no tiene tabla de compras. Equivalencia usada (dila en el examen):
 *   una COMPRA = la RECEPCIÓN de un lote (lotes_inventario), que viene de un proveedor.
 *   NumeroCompra    = numero_lote
 *   NombreProveedor = proveedores.nombre (JOIN por id_proveedor)
 *   MontoTotal      = cantidad_inicial x precio_compra (lo que se pagó por el lote)
 */
public record DailyPurchase(String purchaseNumber, String supplierName, BigDecimal totalAmount) {
}
