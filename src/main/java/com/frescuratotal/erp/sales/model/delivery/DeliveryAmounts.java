package com.frescuratotal.erp.sales.model.delivery;

import java.math.BigDecimal;

/** Shared input check for the strategies: a purchase value can be zero but never null or negative. */
final class DeliveryAmounts {

    private DeliveryAmounts() {
    }

    static void requireValid(BigDecimal purchaseAmount) {
        if (purchaseAmount == null || purchaseAmount.signum() < 0) {
            throw new IllegalArgumentException("The purchase value must be zero or more.");
        }
    }
}
