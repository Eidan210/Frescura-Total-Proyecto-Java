package com.frescuratotal.erp.suppliers.model;


import java.util.List;

public interface SupplierReportDao {
    List<SupplierLotRow> findAllLots();
}