package com.frescuratotal.erp.sales.model;

import java.util.List;

/** EJERCICIO 5 - Acceso a datos para las estadísticas (capa DAO: solo SQL, nada de lógica). */
public interface SalesStatsDao {
    List<SaleStatRow> findSaleDetailsForStats();
}
