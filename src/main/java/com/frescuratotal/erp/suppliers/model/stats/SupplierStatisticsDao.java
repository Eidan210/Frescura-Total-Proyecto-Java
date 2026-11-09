package com.frescuratotal.erp.suppliers.model.stats;

import java.util.List;

public interface SupplierStatisticsDao {

    List<SupplierStatisticsRow> findAllRows();
}
