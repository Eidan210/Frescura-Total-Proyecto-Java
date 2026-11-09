package com.frescuratotal.erp.sales.model.delivery;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** ESTANDAR delivery: 0% surcharge, the customer pays the original value. */
public class StandardDeliveryStrategy implements DeliveryFeeStrategy {

    private static final BigDecimal SURCHARGE_RATE = BigDecimal.ZERO;

    @Override
    public BigDecimal calculateFinalAmount(BigDecimal purchaseAmount) {
        DeliveryAmounts.requireValid(purchaseAmount);
        BigDecimal surcharge = purchaseAmount.multiply(SURCHARGE_RATE);
        return purchaseAmount.add(surcharge).setScale(2, RoundingMode.HALF_UP);
    }
}
