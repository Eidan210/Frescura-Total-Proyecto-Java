package com.frescuratotal.erp.sales.model;

import java.math.BigDecimal;

/**
 * EJERCICIO 3 - Prueba de los 5 casos obligatorios (no necesita base de datos).
 * Ejecutar: clic derecho sobre esta clase -> Run 'StrategyTest.main()'.
 */
public class StrategyTest {

    public static void main(String[] args) {
        DeliveryService service = new DeliveryService();

        check(service, "100000", DeliveryType.STANDARD, "100000.00");
        check(service, "100000", DeliveryType.URGENT, "112000.00");
        check(service, "250000", DeliveryType.URGENT, "280000.00");
        check(service, "50000", DeliveryType.STANDARD, "50000.00");
        check(service, "0", DeliveryType.URGENT, "0.00");
    }

    private static void check(DeliveryService service, String amount, DeliveryType type, String expected) {
        // Sale vacía + setTotalAmount: solo necesitamos el monto para probar.
        Sale sale = new Sale();
        sale.setTotalAmount(new BigDecimal(amount));

        BigDecimal result = service.calculateFinalAmount(sale, type);

        // compareTo y no equals: con BigDecimal, equals compara también la escala
        // (100000 y 100000.00 NO son "equals"); compareTo solo compara el valor.
        String ok = result.compareTo(new BigDecimal(expected)) == 0 ? "OK" : "FAIL";
        System.out.println(amount + " " + type + " -> " + result + " (expected " + expected + ") " + ok);
    }
}
