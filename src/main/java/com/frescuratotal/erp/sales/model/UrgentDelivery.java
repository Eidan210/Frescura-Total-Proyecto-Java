package com.frescuratotal.erp.sales.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class UrgentDelivery implements DeliveryStrategy{

    private static final BigDecimal SURCHARGE = new BigDecimal("1.12");

    @Override
    public BigDecimal calculateTotal(BigDecimal amount) {
        return amount.multiply(SURCHARGE).setScale(2, RoundingMode.HALF_UP);
    }
}
