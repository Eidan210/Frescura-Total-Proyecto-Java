package com.frescuratotal.erp.sales.model.stats;

import java.util.List;

public interface SaleStatisticsDao {
    /** Every sale detail with its category, customer and sale status. */
    List<SaleStatisticsRow> findAllRows();
}
