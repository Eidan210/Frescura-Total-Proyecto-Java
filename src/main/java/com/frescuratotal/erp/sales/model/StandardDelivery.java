package com.frescuratotal.erp.sales.model;

import java.math.BigDecimal;

public class StandardDelivery implements DeliveryStrategy{
    @Override
    public BigDecimal calculateTotal(BigDecimal amount) {
        return amount;
    }
}
