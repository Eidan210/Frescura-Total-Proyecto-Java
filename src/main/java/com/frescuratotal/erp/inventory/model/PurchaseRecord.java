package com.frescuratotal.erp.inventory.model;

import java.math.BigDecimal;

public class PurchaseRecord {
    private final int lotId;
    private final String supplierName;
    private final BigDecimal totalAmount;

    public PurchaseRecord(int lotId, String supplierName, BigDecimal totalAmount) {
        this.lotId = lotId;
        this.supplierName = supplierName;
        this.totalAmount = totalAmount;
    }

    public int getLotId() {
        return lotId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
}
