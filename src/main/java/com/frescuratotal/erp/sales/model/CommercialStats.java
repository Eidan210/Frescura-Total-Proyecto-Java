package com.frescuratotal.erp.sales.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * EJERCICIO 5 - Resultado de las estadísticas.
 *
 * topCategory es Optional: puede haber ventas pero ninguna confirmada (solo devoluciones).
 * customersWithReturns puede ser una lista vacía (no hay devoluciones) -> NO es null.
 */
public record CommercialStats(Optional<CategoryTotal> topCategory, List<String> customersWithReturns) {

    /** Una categoría con su total de unidades. */
    public record CategoryTotal(String category, BigDecimal units) {
    }
}
