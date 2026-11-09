package com.frescuratotal.erp.suppliers.model.stats;

import com.frescuratotal.erp.inventory.model.LotStatus;
import com.frescuratotal.erp.sales.model.SaleStatus;

import java.math.BigDecimal;


public record SupplierStatisticsRow(int loteId, int cantidad_inicial,  LotStatus estado_lote,int id_proveedor,
                                String nombre_proveedor) {
}
