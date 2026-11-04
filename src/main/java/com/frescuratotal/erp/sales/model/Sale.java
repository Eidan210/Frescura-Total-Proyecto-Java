package com.frescuratotal.erp.sales.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Sale {
    private int id;
    private String saleNumber;
    private Customer customer;
    private LocalDateTime saleDate;
    private BigDecimal subtotal;
    private BigDecimal totalDiscount;
    private BigDecimal totalTax;
    private BigDecimal totalAmount;
    private SaleStatus saleStatus;
    private String registeredBy;
    private List<SaleDetail> saleDetails = new ArrayList<>();


    public Sale() {
    }

    public Sale(String saleNumber, Customer customer, String registeredBy) {

        this.saleNumber = saleNumber;
        this.customer = customer;
        this.registeredBy = registeredBy;
        this.saleStatus = SaleStatus.REGISTERED;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSaleNumber() {
        return saleNumber;
    }

    public void setSaleNumber(String saleNumber) {
        this.saleNumber = saleNumber;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public LocalDateTime getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(LocalDateTime saleDate) {
        this.saleDate = saleDate;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getTotalDiscount() {
        return totalDiscount;
    }

    public void setTotalDiscount(BigDecimal totalDiscount) {
        this.totalDiscount = totalDiscount;
    }

    public BigDecimal getTotalTax() {
        return totalTax;
    }

    public void setTotalTax(BigDecimal totalTax) {
        this.totalTax = totalTax;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public SaleStatus getSaleStatus() {
        return saleStatus;
    }

    public void setSaleStatus(SaleStatus saleStatus) {
        this.saleStatus = saleStatus;
    }

    public String getRegisteredBy() {
        return registeredBy;
    }

    public void setRegisteredBy(String registeredBy) {
        this.registeredBy = registeredBy;
    }

    public List<SaleDetail> getSaleDetails() {
        return saleDetails;
    }

    public void setSaleDetails(List<SaleDetail> saleDetails) {
        this.saleDetails = saleDetails;
    }

    @Override
    public String toString() {
        return "Sale{" +
                "id=" + id +
                ", saleNumber='" + saleNumber + '\'' +
                ", customer=" + customer +
                ", saleDate=" + saleDate +
                ", subtotal=" + subtotal +
                ", totalDiscount=" + totalDiscount +
                ", totalTax=" + totalTax +
                ", totalAmount=" + totalAmount +
                ", saleStatus=" + saleStatus +
                ", registeredBy='" + registeredBy + '\'' +
                ", saleDetails=" + saleDetails +
                '}';
    }
}
