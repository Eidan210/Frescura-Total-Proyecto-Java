package com.frescuratotal.erp.sales.model;

import java.math.BigDecimal;
import java.util.Map;

public class DeliveryService {
    private final Map<DeliveryType, DeliveryStrategy> strategies = Map.of(
            DeliveryType.STANDARD, new StandardDelivery(),
            DeliveryType.URGENT, new UrgentDelivery()
    );

    public BigDecimal calculateFinalTotal(Sale sale, DeliveryType type) {
        DeliveryStrategy strategy = strategies.get(type);                 // C
        return strategy.calculateTotal(sale.getTotalAmount());                       // D
    }
}

