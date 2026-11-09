package com.frescuratotal.erp.sales.model.stats;

import java.math.BigDecimal;

/** Units sold of one category. */
public record CategoryTotal(String categoryName, BigDecimal units) {
}
