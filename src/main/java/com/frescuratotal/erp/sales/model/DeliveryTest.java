package com.frescuratotal.erp.sales.model;

import java.math.BigDecimal;

public class DeliveryTest {
    public static void main(String[] args) {
        DeliveryService deliveryService = new DeliveryService();

        test(deliveryService, "100000", DeliveryType.STANDARD);  // esperado 100000
        test(deliveryService, "100000", DeliveryType.URGENT);    // esperado 112000
        test(deliveryService, "250000", DeliveryType.URGENT);    // esperado 280000
        test(deliveryService, "50000", DeliveryType.STANDARD);   // esperado 50000
        test(deliveryService, "0", DeliveryType.URGENT);         // esperado 0
    }

    private static void test(DeliveryService service, String amount, DeliveryType type) {
        Sale sale = new Sale("TEST", null, "tester");
        sale.setTotalAmount(new BigDecimal(amount));
        BigDecimal result = service.calculateFinalTotal(sale, type);
        System.out.println("Compra " + amount + ", entrega " + type + " → total " + result);
    }
}
