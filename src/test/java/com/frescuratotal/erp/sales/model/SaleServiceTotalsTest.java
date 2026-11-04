package com.frescuratotal.erp.sales.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SaleServiceTotalsTest {

    @Test
    void saleTotalsAreTheSumOfTheDetails() {
        Sale sale = new Sale("S-TEST", null, "test");
        sale.getSaleDetails().add(detail("100.00", "50.00", "9.50", "59.50"));
        sale.getSaleDetails().add(detail("200.00", "0.00", "38.00", "238.00"));

        new SaleService().calculateTotals(sale);

        assertEquals(new BigDecimal("300.00"), sale.getSubtotal());
        assertEquals(new BigDecimal("50.00"), sale.getTotalDiscount());
        assertEquals(new BigDecimal("47.50"), sale.getTotalTax());
        assertEquals(new BigDecimal("297.50"), sale.getTotalAmount());
    }

    private static SaleDetail detail(String subtotal, String discount, String tax, String lineTotal) {
        SaleDetail detail = new SaleDetail();
        detail.setSubtotal(new BigDecimal(subtotal));
        detail.setDiscountAmount(new BigDecimal(discount));
        detail.setTaxAmount(new BigDecimal(tax));
        detail.setLineTotal(new BigDecimal(lineTotal));
        return detail;
    }
}
