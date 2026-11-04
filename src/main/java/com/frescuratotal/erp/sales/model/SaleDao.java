package com.frescuratotal.erp.sales.model;

import java.math.BigDecimal;
import java.util.List;

public interface SaleDao {
    boolean save(Sale sale);
    List<Sale> findAll();
    boolean updateStatus(int saleId, SaleStatus status);
    /** Changes the status only if the sale is still in the expected one. */
    boolean updateStatus(int saleId, SaleStatus expected, SaleStatus status);
    BigDecimal findTotalQuantity(int saleId);
}
