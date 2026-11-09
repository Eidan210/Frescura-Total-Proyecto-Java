package com.frescuratotal.erp.sales.model;

import java.math.BigDecimal;

/**
 * EJERCICIO 3 - Estrategia concreta ESTANDAR (recargo 0%).
 *
 * Nombre: lo que la diferencia (Standard) + el tipo que implementa (DeliveryStrategy),
 * igual que ArrayList / LinkedList implementan List.
 */
public class StandardDeliveryStrategy implements DeliveryStrategy {

    @Override // @Override: "este método es el que la interfaz exige"
    public BigDecimal calculateTotal(BigDecimal originalPurchasePrice) {
        // Sin recargo: el total es el mismo valor que entró.
        return originalPurchasePrice;
    }

    @Override
    public String getDescription() {
        return "No extra fee was applied [STANDARD]";
    }
}
