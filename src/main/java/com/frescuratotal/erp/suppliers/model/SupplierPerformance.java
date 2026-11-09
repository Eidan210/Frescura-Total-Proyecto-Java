package com.frescuratotal.erp.suppliers.model;

import java.util.List;

public record SupplierPerformance(boolean hasData, String topSupplier, List<String> expiredSuppliers) {
}


