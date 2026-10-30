package com.frescuratotal.erp.suppliers.model;

public class SupplierProduct {
    private int supplierId;
    private int productId;

    public SupplierProduct(int supplierId, int productId) {
        this.supplierId = supplierId;
        this.productId = productId;
    }

    public int getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(int supplierId) {
        this.supplierId = supplierId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    @Override
    public String toString() {
        return "SupplierProduct{" +
                "supplierId=" + supplierId +
                ", productId=" + productId +
                '}';
    }
}
