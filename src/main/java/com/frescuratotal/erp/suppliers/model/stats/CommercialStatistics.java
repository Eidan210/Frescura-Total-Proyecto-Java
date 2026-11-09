package com.frescuratotal.erp.suppliers.model.stats;

import java.util.List;
import java.util.Optional;


public record CommercialStatistics(boolean hasData, Optional<LoteTotal> topLote,
                                   List<String> SuppliersWithReturns) {
}
