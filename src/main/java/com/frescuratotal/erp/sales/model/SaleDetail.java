package com.frescuratotal.erp.sales.model;

import com.frescuratotal.erp.inventory.model.Lot;

import java.math.BigDecimal;

public class SaleDetail {

    private int id;
    private Lot lot;
    private BigDecimal quantitySold;
    private BigDecimal unitPrice;
    private BigDecimal discountApplied;
    private BigDecimal discountAmount;
    private BigDecimal taxRate;
    private BigDecimal taxAmount;
    private BigDecimal subtotal;
    private BigDecimal lineTotal;

    public SaleDetail() {
    }

    public SaleDetail(BigDecimal quantitySold, Lot lot ) {
        this.quantitySold = quantitySold;
        this.lot = lot;
        this.unitPrice = lot.getSalePrice();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Lot getLot() {
        return lot;
    }

    public void setLot(Lot lot) {
        this.lot = lot;
    }

    public BigDecimal getQuantitySold() {
        return quantitySold;
    }

    public void setQuantitySold(BigDecimal quantitySold) {
        this.quantitySold = quantitySold;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getDiscountApplied() {
        return discountApplied;
    }

    public void setDiscountApplied(BigDecimal discountApplied) {
        this.discountApplied = discountApplied;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(BigDecimal lineTotal) {
        this.lineTotal = lineTotal;
    }

    @Override
    public String toString() {
        return "SaleDetail{" +
                "id=" + id +
                ", lot=" + lot +
                ", quantitySold=" + quantitySold +
                ", unitPrice=" + unitPrice +
                ", discountApplied=" + discountApplied +
                ", discountAmount=" + discountAmount +
                ", taxRate=" + taxRate +
                ", taxAmount=" + taxAmount +
                ", subtotal=" + subtotal +
                ", lineTotal=" + lineTotal +
                '}';
    }
}
