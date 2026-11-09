package com.frescuratotal.erp.suppliers.model;

import java.util.List;

public interface StaticDao {
    List<SupplierLotRow> findAllStatRows();
}
