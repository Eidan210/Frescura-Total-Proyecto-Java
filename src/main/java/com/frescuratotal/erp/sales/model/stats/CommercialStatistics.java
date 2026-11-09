package com.frescuratotal.erp.sales.model.stats;

import java.util.List;
import java.util.Optional;

/**
 * Result of the commercial statistics.
 *
 * @param hasData            false when there are no sales at all
 * @param topCategory        empty when there is no confirmed sale
 * @param customersWithReturns names without repetitions (may be empty)
 */
public record CommercialStatistics(boolean hasData, Optional<CategoryTotal> topCategory,
                                   List<String> customersWithReturns) {
}
