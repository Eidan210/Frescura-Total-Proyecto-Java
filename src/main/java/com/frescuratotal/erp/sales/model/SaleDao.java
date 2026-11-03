package com.frescuratotal.erp.sales.model;

import java.util.List;

public interface SaleDao {
    boolean save(Sale sale);
    List<Sale> findAll();
    boolean updateStatus(int saleId, SaleStatus status);
}
