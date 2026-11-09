package com.frescuratotal.erp.sales.model.stats;

import com.frescuratotal.erp.sales.model.SaleStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Commercial statistics with the Stream API (Exercise 5).
 * Filtering, grouping, adding up and removing duplicates are done with streams, no for/while.
 */
public class CommercialStatisticsService {

    private final SaleStatisticsDao dao;

    public CommercialStatisticsService(SaleStatisticsDao dao) {
        this.dao = dao;
    }

    public CommercialStatistics generate() {
        return calculate(dao.findAllRows());
    }

    /** Separated from generate() so the tests can use the rows of the guide without a database. */
    public CommercialStatistics calculate(List<SaleStatisticsRow> rows) {
        if (rows == null || rows.isEmpty()) {
            return new CommercialStatistics(false, Optional.empty(), List.of());
        }
        return new CommercialStatistics(true, topCategory(rows), customersWithReturns(rows));
    }

    /** Only confirmed sales; units added up per category; the biggest total wins (a tie returns any of them). */
    Optional<CategoryTotal> topCategory(List<SaleStatisticsRow> rows) {
        Map<String, BigDecimal> unitsByCategory = rows.stream()
                .filter(row -> row.status() != null && row.status().isConfirmed())
                .collect(Collectors.groupingBy(
                        SaleStatisticsRow::categoryName,
                        Collectors.reducing(BigDecimal.ZERO, SaleStatisticsRow::quantity, BigDecimal::add)));

        return unitsByCategory.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> new CategoryTotal(entry.getKey(), entry.getValue()));
    }

    /** Customers of returned sales, each name once, in alphabetical order. */
    List<String> customersWithReturns(List<SaleStatisticsRow> rows) {
        return rows.stream()
                .filter(row -> row.status() == SaleStatus.RETURNED)
                .map(SaleStatisticsRow::customerName)
                .distinct()
                .sorted()
                .toList();
    }
}
