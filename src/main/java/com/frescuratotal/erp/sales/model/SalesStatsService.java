package com.frescuratotal.erp.sales.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * EJERCICIO 5: estadisticas comerciales con Stream API (sin for ni while).
 *
 * Adaptacion a nuestra base de datos (no existen los estados CONFIRMADA ni DEVUELTA):
 *  - "CONFIRMADA" = cualquier venta que NO este cancelada (registrada, en preparacion,
 *    en transito o entregada).
 *  - "DEVUELTA"   = venta en estado CANCELLED (Cancelada), que es el estado que
 *    representa una venta que no se concreto.
 */
public class SalesStatsService {

    private final SalesStatsDao salesStatsDao = new SalesStatsDaoImpl();

    public CommercialStats generateStats() {
        List<SalesStatRow> rows = salesStatsDao.findAllStatRows();

        // Caso vacio: no hay ninguna venta -> la vista muestra el mensaje informativo.
        if (rows.isEmpty()) {
            return new CommercialStats(false, null, null, List.of());
        }

        // ---------- Estadistica 1: categoria con mas unidades vendidas ----------
        // 1. filter: solo las ventas "confirmadas" (no canceladas)
        // 2. groupingBy: agrupa por categoria -> Map<categoria, suma de cantidades>
        //    reducing(ZERO, quantity, add) = "empieza en 0 y suma la cantidad de cada fila del grupo"
        Map<String, BigDecimal> unitsByCategory = rows.stream()
                .filter(row -> row.status() != SaleStatus.CANCELLED)
                .collect(Collectors.groupingBy(
                        SalesStatRow::category,
                        Collectors.reducing(BigDecimal.ZERO, SalesStatRow::quantity, BigDecimal::add)
                ));

        // 3. Del mapa, busca la entrada (categoria, total) con el VALOR mas alto.
        //    max devuelve Optional porque el mapa podria estar vacio (todas canceladas).
        //    Si hay empate, max devuelve cualquiera de las empatadas (lo permite el ejercicio).
        Optional<Map.Entry<String, BigDecimal>> top = unitsByCategory.entrySet().stream()
                .max(Map.Entry.comparingByValue());

        // ---------- Estadistica 2: clientes con devoluciones ----------
        // filter: solo las devueltas -> map: me quedo con el nombre del cliente
        // -> distinct: quita repetidos -> sorted: orden alfabetico -> toList: a una lista
        List<String> returnedCustomers = rows.stream()
                .filter(row -> row.status() == SaleStatus.CANCELLED)
                .map(SalesStatRow::customerName)
                .distinct()
                .sorted()
                .toList();

        // Optional.map: si hay top, saca la clave o el valor; si no, queda null.
        return new CommercialStats(
                true,
                top.map(Map.Entry::getKey).orElse(null),
                top.map(Map.Entry::getValue).orElse(null),
                returnedCustomers
        );
    }
}
