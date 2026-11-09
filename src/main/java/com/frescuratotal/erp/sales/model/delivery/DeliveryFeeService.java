package com.frescuratotal.erp.sales.model.delivery;

import com.frescuratotal.erp.sales.model.Sale;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

/**
 * Picks the strategy of a delivery mode and lets it calculate the final value (Exercise 3).
 *
 * There is no if/switch with percentages here: the map links each mode to its strategy, so a
 * new mode only needs a new enum value, a new class and one line in the constructor.
 */
public class DeliveryFeeService {

    private final Map<DeliveryMode, DeliveryFeeStrategy> strategies = new EnumMap<>(DeliveryMode.class);

    public DeliveryFeeService() {
        strategies.put(DeliveryMode.STANDARD, new StandardDeliveryStrategy());
        strategies.put(DeliveryMode.URGENT, new UrgentDeliveryStrategy());
    }

    /** Final value of a sale whose totals were already calculated. */
    public BigDecimal calculateFinalAmount(Sale sale, DeliveryMode mode) {
        if (sale == null || sale.getTotalAmount() == null) {
            throw new IllegalArgumentException("The sale has no total to calculate the delivery.");
        }
        return calculateFinalAmount(sale.getTotalAmount(), mode);
    }

    public BigDecimal calculateFinalAmount(BigDecimal purchaseAmount, DeliveryMode mode) {
        return strategyFor(mode).calculateFinalAmount(purchaseAmount);
    }

    private DeliveryFeeStrategy strategyFor(DeliveryMode mode) {
        DeliveryFeeStrategy strategy = mode == null ? null : strategies.get(mode);
        if (strategy == null) {
            throw new IllegalArgumentException("Delivery mode not supported: " + mode);
        }
        return strategy;
    }
}
