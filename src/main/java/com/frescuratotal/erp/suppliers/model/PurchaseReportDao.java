package com.frescuratotal.erp.suppliers.model;

import java.time.LocalDate;
import java.util.List;

public interface PurchaseReportDao {
    /** Purchases (received lots) of that day only, with the name of their supplier. */
    List<PurchaseRecord> findPurchasesByDate(LocalDate date);
}
