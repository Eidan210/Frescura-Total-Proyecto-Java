package com.frescuratotal.erp.inventory.model;

import java.nio.file.Path;

public record PurchaseReportResult(Path file, int records) {
}