package com.frescuratotal.erp.sales.model.stats;

import com.frescuratotal.erp.sales.model.SaleStatus;

import java.math.BigDecimal;

/** One sale detail joined with its category, customer and sale status (Exercise 5). */
public record SaleStatisticsRow(int saleId, BigDecimal quantity, String categoryName,
                                String customerName, SaleStatus status) {
}
