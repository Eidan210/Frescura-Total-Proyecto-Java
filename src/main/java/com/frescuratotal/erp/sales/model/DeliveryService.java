package com.frescuratotal.erp.sales.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * EJERCICIO 3 - Servicio de cálculo: ELIGE la estrategia y la INVOCA.
 *
 * Regla del enunciado: el switch SOLO selecciona la estrategia; nunca calcula el recargo.
 * Si mañana llega EXPRÉS: se agrega la constante al enum, la clase nueva y un "case".
 * La línea del cálculo no se toca.
 */
public class DeliveryService {

    public BigDecimal calculateFinalAmount(Sale sale, DeliveryType deliveryType) {
        // Paso 1: ELEGIR
        DeliveryStrategy strategy = getStrategy(deliveryType);

        // Paso 2: CALCULAR (una sola llamada, sea cual sea la estrategia = polimorfismo)
        // getTotalAmount(): lo que el cliente paga ya con descuentos e IVA.
        BigDecimal total = strategy.calculateTotal(sale.getTotalAmount());

        // Paso 3: REDONDEAR a 2 decimales (monto_total es DECIMAL(12,2)).
        // Va aquí y no en cada estrategia: si alguien crea una estrategia nueva y olvida
        // redondear, igual sale bien. Es una regla del DINERO, no del recargo.
        // HALF_UP = redondeo del colegio (desde 5 sube): 373.3296 -> 373.33
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    // La descripción "sube" por las capas: estrategia -> DeliveryService -> SaleService -> controlador -> vista.
    public String getDeliveryDescription(DeliveryType deliveryType) {
        return getStrategy(deliveryType).getDescription();
    }

    // private: solo este servicio necesita elegir la estrategia.
    private DeliveryStrategy getStrategy(DeliveryType deliveryType) {
        // switch "con flechas" que DEVUELVE un valor -> por eso lleva ";" al final.
        // Sin "default": cubre TODAS las constantes del enum. Si agregas EXPRESS al enum
        // y olvidas su case, NO compila (Java te avisa antes de producción).
        return switch (deliveryType) {
            case STANDARD -> new StandardDeliveryStrategy();
            case URGENT -> new UrgentDeliveryStrategy();
        };
    }
}
