package com.frescuratotal.erp.sales.model;

import java.math.BigDecimal;

public interface DeliveryStrategy {
    BigDecimal calculateTotal(BigDecimal amount);
}
