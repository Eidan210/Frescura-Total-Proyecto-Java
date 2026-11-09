package com.frescuratotal.erp.sales.model;

import java.util.List;

/** EJERCICIO 5: contrato para traer de la base de datos las filas de estadisticas. */
public interface SalesStatsDao {
    List<SalesStatRow> findAllStatRows();
}
