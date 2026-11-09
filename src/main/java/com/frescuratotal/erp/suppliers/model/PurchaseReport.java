package com.frescuratotal.erp.suppliers.model;

import java.nio.file.Path;
import java.time.LocalDate;

/** Result of the daily purchase report: where the file went and how many purchases it has. */
public record PurchaseReport(Path file, LocalDate date, int purchaseCount) {

    public boolean isEmpty() {
        return purchaseCount == 0;
    }
}
