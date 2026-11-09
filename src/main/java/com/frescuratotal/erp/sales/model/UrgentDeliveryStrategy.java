package com.frescuratotal.erp.sales.model;

import java.math.BigDecimal;

/**
 * EJERCICIO 3 - Estrategia concreta URGENTE (recargo 12%).
 *
 * El porcentaje vive SOLO aquí (requerimiento 5): ni el servicio ni la vista lo conocen.
 */
public class UrgentDeliveryStrategy implements DeliveryStrategy {

    // Constante: private (solo esta clase la ve), static (una sola copia para la clase),
    // final (no se puede cambiar). Nombre en MAYUSCULAS_CON_GUION_BAJO.
    // Es el MULTIPLICADOR (1.12 = 100% + 12%), no el recargo (0.12).
    // BigDecimal.valueOf(1.12) o new BigDecimal("1.12") son seguros;
    // new BigDecimal(1.12) (double sin comillas) NO: queda impreciso.
    private static final BigDecimal URGENT_DELIVERY_MULTIPLIER = new BigDecimal("1.12");

    @Override
    public BigDecimal calculateTotal(BigDecimal originalPurchasePrice) {
        // Con BigDecimal no se usa "*": se usa multiply(). Es inmutable: devuelve un valor NUEVO.
        // Ojo: 100000.00 x 1.12 = 112000.0000 (los decimales se SUMAN: 2 + 2).
        // El redondeo a 2 decimales se hace UNA sola vez en DeliveryService.
        return originalPurchasePrice.multiply(URGENT_DELIVERY_MULTIPLIER);
    }

    @Override
    public String getDescription() {
        return "A 12% fee was applied to your delivery [URGENT]";
    }
}
