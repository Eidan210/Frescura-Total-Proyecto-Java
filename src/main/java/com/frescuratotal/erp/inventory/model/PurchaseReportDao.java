package com.frescuratotal.erp.inventory.model;

import java.time.LocalDate;
import java.util.List;

/** EJERCICIO 2 - Acceso a datos del reporte de compras. */
public interface PurchaseReportDao {
    List<DailyPurchase> findPurchasesByDate(LocalDate date);
}
