package com.frescuratotal.erp.suppliers.model;

import java.math.BigDecimal;

/**
 * One purchase of goods for the daily report (Exercise 2).
 * In this ERP a purchase is a lot received from a supplier (table lotes_inventario):
 * its number is id_lote and its value is cantidad_inicial x precio_compra.
 */
public record PurchaseRecord(int purchaseNumber, String supplierName, BigDecimal totalAmount) {
}
