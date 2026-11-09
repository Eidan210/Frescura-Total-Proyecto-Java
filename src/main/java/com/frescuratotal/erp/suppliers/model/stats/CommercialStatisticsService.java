package com.frescuratotal.erp.suppliers.model.stats;

import com.frescuratotal.erp.inventory.model.LotStatus;
import com.frescuratotal.erp.sales.model.SaleStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


public class CommercialStatisticsService {

    private final SupplierStatisticsDao dao;

    public CommercialStatisticsService(SupplierStatisticsDao dao) {
        this.dao = dao;
    }

    public CommercialStatistics generate() {
        return calculate(dao.findAllRows());
    }

    public CommercialStatistics calculate(List<SupplierStatisticsRow> rows) {
        if (rows == null || rows.isEmpty()) {
            return new CommercialStatistics(false, Optional.empty(), List.of());
        }
        return new CommercialStatistics(true, LoteCategory(rows), SuppliersWithReturns(rows));
    }

    Optional<LoteTotal> LoteCategory(List<SupplierStatisticsRow> rows) {
        Map<String, BigDecimal> unitsByCategory = rows.stream()
                .filter(row -> row.estado_lote() != null )
                .collect(Collectors.groupingBy(
                        SupplierStatisticsRow::nombre_proveedor,
                        Collectors.reducing(BigDecimal.ZERO, SupplierStatisticsRow::cantidad_inicial, BigDecimal::add)));

        return unitsByCategory.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> new LoteTotal(entry.getKey(), entry.getValue()));
    }


    List<String> SuppliersWithReturns(List<SupplierStatisticsRow> rows) {
        return rows.stream()
                .filter(row -> row.estado_lote() == LotStatus.AVAILABLE)
                .map(SupplierStatisticsRow::nombre_proveedor)
                .distinct()
                .sorted()
                .toList();
    }
}
