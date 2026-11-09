package com.frescuratotal.erp.inventory.model;

import java.time.LocalDate;
import java.util.List;

public interface PurchaseReportDao {
    List<PurchaseRecord> findPurchasesByDate(LocalDate date);
}
