package com.frescuratotal.erp.suppliers.model;

import java.math.BigDecimal;

/** Una fila de la consulta lote + proveedor + estado, usada para las estadísticas. */
public record SupplierData(int lotId, BigDecimal initialQuantity, String lotStatus, int supplierId, String supplierName) {
}
