package com.frescuratotal.erp.sales.model.delivery;

import java.math.BigDecimal;

/**
 * Common contract for every delivery mode (Exercise 3, Strategy pattern).
 * Each implementation owns its percentage and its calculation.
 */
public interface DeliveryFeeStrategy {

    /**
     * @param purchaseAmount original value of the purchase (zero or more)
     * @return the value the customer will pay with this delivery mode
     */
    BigDecimal calculateFinalAmount(BigDecimal purchaseAmount);
}
