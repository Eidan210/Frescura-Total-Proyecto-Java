package com.frescuratotal.erp.sales.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * EJERCICIO 5 - Estadísticas comerciales con Stream API (prohibido for/while para filtrar,
 * agrupar, sumar y quitar duplicados).
 *
 * Estados: el ERP no tiene "CONFIRMADA". Equivalencia usada (dila en el examen):
 *   CONFIRMADA = toda venta que se guardó y no fue cancelada ni devuelta
 *                (REGISTERED, IN_PREPARATION, IN_TRANSIT, DELIVERED).
 *   DEVUELTA   = estado nuevo RETURNED (id 6), agregado con docs/practica_examen.sql.
 */
public class CommercialStatsService {

    private final SalesStatsDao statsDao;

    public CommercialStatsService() {
        this(new SalesStatsDaoImpl());
    }

    public CommercialStatsService(SalesStatsDao statsDao) {
        this.statsDao = statsDao;
    }

    /** Optional vacío = no hay ventas (la vista muestra "No existen datos suficientes..."). */
    public Optional<CommercialStats> generate() {
        List<SaleStatRow> rows = statsDao.findSaleDetailsForStats();
        if (rows.isEmpty()) {
            return Optional.empty(); // caso vacío: sin excepciones
        }
        return Optional.of(calculate(rows));
    }

    /** La lógica pura (sin BD): public para poder probarla con datos de ejemplo. */
    public CommercialStats calculate(List<SaleStatRow> rows) {

        // ---- Estadística 1: categoría con más unidades vendidas ----
        // stream()        -> convierte la lista en un "flujo" de elementos
        // filter(...)     -> deja pasar solo los que cumplen la condición (solo confirmadas)
        // groupingBy(...) -> agrupa por categoría: Map<categoria, ...>
        // reducing(...)   -> suma las cantidades de cada grupo con BigDecimal::add
        //                    (empieza en ZERO, toma quantity de cada fila, y las va sumando)
        // Con int sería Collectors.summingInt; con BigDecimal se usa reducing.
        Map<String, BigDecimal> unitsByCategory = rows.stream()
                .filter(row -> isConfirmed(row.status()))
                .collect(Collectors.groupingBy(
                        SaleStatRow::categoryName,
                        Collectors.reducing(BigDecimal.ZERO, SaleStatRow::quantity, BigDecimal::add)));

        // entrySet().stream() -> recorre los pares (categoría, total)
        // max(comparingByValue()) -> el par con el mayor total. Devuelve Optional (puede no haber ninguno).
        // En empate, devuelve cualquiera de los empatados (el enunciado lo permite).
        // map(...) -> transforma el par en nuestro record CategoryTotal.
        Optional<CommercialStats.CategoryTotal> top = unitsByCategory.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> new CommercialStats.CategoryTotal(entry.getKey(), entry.getValue()));

        // ---- Estadística 2: clientes con devoluciones ----
        // filter   -> solo devueltas
        // map      -> de cada fila me quedo solo con el nombre del cliente
        // distinct -> quita los repetidos (un cliente con 3 devoluciones aparece 1 vez)
        // sorted   -> orden alfabético (opcional, se ve mejor)
        // toList   -> lo vuelve lista (si no hay devoluciones, lista VACÍA, sin error)
        List<String> customersWithReturns = rows.stream()
                .filter(row -> row.status() == SaleStatus.RETURNED)
                .map(SaleStatRow::customerName)
                .distinct()
                .sorted()
                .toList();

        return new CommercialStats(top, customersWithReturns);
    }

    // Una devolución NO cuenta como unidades vendidas; una cancelación tampoco.
    private boolean isConfirmed(SaleStatus status) {
        return status != SaleStatus.CANCELLED && status != SaleStatus.RETURNED;
    }
}
