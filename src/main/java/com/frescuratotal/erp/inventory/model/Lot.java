package com.frescuratotal.erp.inventory.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Lot {
    private int id;
    private String lotNumber;
    private int productId;
    private int supplierId;
    private LocalDateTime entryDate;
    private LocalDate expirationDate;
    private BigDecimal initialQuantity;
    private BigDecimal quantity;
    private BigDecimal purchasePrice;
    private BigDecimal salePrice;
    private String aisle;
    private String shelf;
    private int statusId;

    public Lot() {
    }

    public Lot(String lotNumber, int productId, int supplierId, LocalDateTime entryDate, LocalDate expirationDate, BigDecimal initialQuantity, BigDecimal quantity, BigDecimal purchasePrice, BigDecimal salePrice, String aisle, String shelf, int statusId) {
        this.lotNumber = lotNumber;
        this.productId = productId;
        this.supplierId = supplierId;
        this.entryDate = entryDate;
        this.expirationDate = expirationDate;
        this.initialQuantity = initialQuantity;
        this.quantity = quantity;
        this.purchasePrice = purchasePrice;
        this.salePrice = salePrice;
        this.aisle = aisle;
        this.shelf = shelf;
        this.statusId = statusId;
    }

    // GETTERS

    public int getId() {
        return id;
    }

    public String getLotNumber() {
        return lotNumber;
    }

    public int getProductId() {
        return productId;
    }

    public int getSupplierId() {
        return supplierId;
    }

    public LocalDateTime getEntryDate() {
        return entryDate;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public BigDecimal getInitialQuantity() {
        return initialQuantity;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public BigDecimal getSalePrice() {
        return salePrice;
    }

    public String getAisle() {
        return aisle;
    }

    public String getShelf() {
        return shelf;
    }

    public int getStatusId() {
        return statusId;
    }

    // SETTERS


    public void setId(int id) {
        this.id = id;
    }

    public void setLotNumber(String lotNumber) {
        this.lotNumber = lotNumber;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public void setSupplierId(int supplierId) {
        this.supplierId = supplierId;
    }

    public void setEntryDate(LocalDateTime entryDate) {
        this.entryDate = entryDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public void setInitialQuantity(BigDecimal initialQuantity) {
        this.initialQuantity = initialQuantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public void setSalePrice(BigDecimal salePrice) {
        this.salePrice = salePrice;
    }

    public void setAisle(String aisle) {
        this.aisle = aisle;
    }

    public void setShelf(String shelf) {
        this.shelf = shelf;
    }

    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }

    @Override
    public String toString() {
        return "Lot{" +
                "id=" + id +
                ", lotNumber='" + lotNumber + '\'' +
                ", productId=" + productId +
                ", supplierId=" + supplierId +
                ", entryDate=" + entryDate +
                ", expirationDate=" + expirationDate +
                ", initialQuantity=" + initialQuantity +
                ", quantity=" + quantity +
                ", purchasePrice=" + purchasePrice +
                ", salePrice=" + salePrice +
                ", aisle='" + aisle + '\'' +
                ", shelf='" + shelf + '\'' +
                ", statusId=" + statusId +
                '}';
    }
}
