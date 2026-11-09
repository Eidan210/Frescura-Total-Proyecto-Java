package com.frescuratotal.erp.sales.model.delivery;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** URGENTE delivery: adds 12% to the value of the purchase. */
public class UrgentDeliveryStrategy implements DeliveryFeeStrategy {

    private static final BigDecimal SURCHARGE_RATE = new BigDecimal("0.12");

    @Override
    public BigDecimal calculateFinalAmount(BigDecimal purchaseAmount) {
        DeliveryAmounts.requireValid(purchaseAmount);
        BigDecimal surcharge = purchaseAmount.multiply(SURCHARGE_RATE);
        return purchaseAmount.add(surcharge).setScale(2, RoundingMode.HALF_UP);
    }
}
