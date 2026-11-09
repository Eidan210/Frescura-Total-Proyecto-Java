package com.frescuratotal.erp.suppliers.model;


import java.math.BigDecimal;

public record SupplierLotRow(int lotId, BigDecimal initialQuantity, String status,
                             int supplierId, String supplierName) {
}
