package com.frescuratotal.erp.sales.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Invoice {
    private int id;
    private Sale sale;
    private String invoiceNumber;
    private String cufe;
    private String qrLink;
    private LocalDateTime issueDate;
    private BigDecimal subtotal;
    private BigDecimal totalDiscount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;


    public Invoice() {
    }

    public Invoice(Sale sale, String invoiceNumber, String cufe, String qrLink) {
        this.sale = sale;
        this.invoiceNumber = invoiceNumber;
        this.cufe = cufe;
        this.qrLink = qrLink;
        this.subtotal = sale.getSubtotal();
        this.totalDiscount= sale.getTotalDiscount();
        this.taxAmount=sale.getTotalTax();
        this.totalAmount=sale.getTotalAmount();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Sale getSale() {
        return sale;
    }

    public void setSale(Sale sale) {
        this.sale = sale;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getCufe() {
        return cufe;
    }

    public void setCufe(String cufe) {
        this.cufe = cufe;
    }

    public String getQrLink() {
        return qrLink;
    }

    public void setQrLink(String qrLink) {
        this.qrLink = qrLink;
    }

    public LocalDateTime getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDateTime issueDate) {
        this.issueDate = issueDate;
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

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    @Override
    public String toString() {
        return "Invoice{" +
                "id=" + id +
                ", sale=" + sale +
                ", invoiceNumber='" + invoiceNumber + '\'' +
                ", cufe='" + cufe + '\'' +
                ", qrLink='" + qrLink + '\'' +
                ", issueDate=" + issueDate +
                ", subtotal=" + subtotal +
                ", totalDiscount=" + totalDiscount +
                ", taxAmount=" + taxAmount +
                ", totalAmount=" + totalAmount +
                '}';
    }
}
