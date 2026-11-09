
package com.frescuratotal.erp.suppliers.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SupplierReportService {
    private final SupplierReportDao dao = new SupplierReportDaoImpl();

    public SupplierPerformance generateReport() {
        List<SupplierLotRow> rows = dao.findAllLots();


        if (rows.isEmpty()) {
            return new SupplierPerformance(false, null, List.of());
        }

        Map<String, BigDecimal> volumeBySupplier = rows.stream()
                .collect(Collectors.groupingBy(
                        SupplierLotRow::supplierName,
                        Collectors.reducing(BigDecimal.ZERO, SupplierLotRow::initialQuantity, BigDecimal::add)));


        String topSupplier = volumeBySupplier.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);


        List<String> expiredSuppliers = rows.stream()
                .filter(row -> row.status().equalsIgnoreCase("Vencido"))
                .map(SupplierLotRow::supplierName)
                .distinct()
                .sorted()
                .toList();

        return new SupplierPerformance(true, topSupplier, expiredSuppliers);
    }
}




