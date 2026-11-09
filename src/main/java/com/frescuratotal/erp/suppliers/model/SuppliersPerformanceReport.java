package com.frescuratotal.erp.suppliers.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.reducing;

/** Estadísticas de rendimiento de proveedores calculadas con Stream API sobre los lotes. */
public class SuppliersPerformanceReport {

    /** Proveedor con mayor suma de cantidad_inicial de todos sus lotes (ante empate, cualquiera). */
    public static Optional<String> topSupplierByVolume(List<SupplierData> lots) {
        Map<String, BigDecimal> volumeBySupplier = lots.stream()
                .collect(groupingBy(SupplierData::supplierName,
                        reducing(BigDecimal.ZERO, SupplierData::initialQuantity, BigDecimal::add)));

        return volumeBySupplier.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
    }

    /** Nombres únicos de proveedores con al menos un lote en estado "Vencido". */
    public static List<String> suppliersWithExpiredLots(List<SupplierData> lots) {
        return lots.stream()
                .filter(lot -> "Vencido".equalsIgnoreCase(lot.lotStatus().trim()))
                .map(SupplierData::supplierName)
                .distinct()
                .toList();
    }
}
