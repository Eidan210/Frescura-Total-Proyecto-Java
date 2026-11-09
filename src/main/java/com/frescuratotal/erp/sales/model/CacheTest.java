package com.frescuratotal.erp.sales.model;

/**
 * EJERCICIO 1 - Prueba de los 5 casos obligatorios (SÍ necesita la base de datos encendida).
 * Ejecutar: clic derecho -> Run 'CacheTest.main()'.
 * Si los IDs 5 u 8 no existen en tu BD, cámbialos por IDs que sí existan (menú Ventas -> 2).
 *
 * Salida esperada:
 *   Caso 1 -> [SQL]    (primera vez)
 *   Caso 2 -> [CACHE]  (ya estaba guardado)
 *   Caso 3 -> [SQL]    (ID distinto)
 *   Caso 4 -> [CACHE]  (OTRO servicio, MISMA caché: gracias al Singleton)
 *   Caso 5 -> [SQL] y "not found"; no se guarda (el tamaño de la caché no cambia)
 */
public class CacheTest {

    public static void main(String[] args) {
        CustomerService customerService = new CustomerService();
        SaleService saleService = new SaleService(); // "otro módulo"

        System.out.println("--- Case 1: ID 5, first time ---");
        print(customerService.findById(5).map(Customer::getName).orElse("not found"));

        System.out.println("--- Case 2: ID 5, second time ---");
        print(customerService.findById(5).map(Customer::getName).orElse("not found"));

        System.out.println("--- Case 3: ID 8 ---");
        print(customerService.findById(8).map(Customer::getName).orElse("not found"));

        System.out.println("--- Case 4: ID 5 from another module (SaleService) ---");
        print(saleService.findCustomerById(5).map(Customer::getName).orElse("not found"));

        System.out.println("--- Case 5: ID 999 (does not exist) ---");
        int before = CustomerCache.getInstance().size();
        print(customerService.findById(999).map(Customer::getName).orElse("Customer 999 not found"));
        System.out.println("Cache size before: " + before + " | after: " + CustomerCache.getInstance().size());

        // Prueba directa del Singleton: dos llamadas, mismo objeto.
        System.out.println("Same cache instance? " + (CustomerCache.getInstance() == CustomerCache.getInstance()));
    }

    private static void print(String text) {
        System.out.println("  -> " + text);
    }
}
