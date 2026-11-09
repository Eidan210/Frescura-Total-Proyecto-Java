package com.frescuratotal.erp.sales.model;

import java.util.Optional;

/**
 * EJERCICIO 1 - El servicio que DECIDE entre memoria (caché) y base de datos.
 *
 * La receta:
 *   1. Pregunto a la caché.
 *   2. Si está -> lo devuelvo SIN SQL.
 *   3. Si no está -> pregunto a la BD.
 *   4. Si la BD lo tiene -> lo guardo en la caché y lo devuelvo.
 *   5. Si la BD tampoco (ID 999) -> devuelvo la caja vacía y NO guardo nada.
 *
 * Quien llama (SaleService, el controlador) recibe un Optional<Customer> y no se entera
 * de dónde vino: eso es "integración transparente" (requerimiento 5).
 */
public class CustomerService {

    // La caché NO se crea con new (constructor privado): se pide con getInstance().
    private final CustomerCache cache = CustomerCache.getInstance();
    private final CustomerDao customerDao;

    public CustomerService() {
        this(new CustomerDaoImpl());
    }

    // Constructor que recibe el DAO: permite probar con un DAO falso sin base de datos.
    public CustomerService(CustomerDao customerDao) {
        this.customerDao = customerDao;
    }

    public Optional<Customer> findById(int id) {
        // 1. Pregunto a la caché
        Optional<Customer> fromCache = cache.get(id);

        // 2. Si está -> lo devuelvo sin SQL (devuelvo la caja completa, no hace falta .get())
        if (fromCache.isPresent()) {
            // En un proyecto real esto sería un logger; para la demostración basta un println.
            System.out.println("[CACHE] Customer " + id + " found in memory (no SQL)");
            return fromCache;
        }

        // 3. Si no está -> pregunto a la BD
        System.out.println("[SQL] Customer " + id + " not in cache, querying the database");
        Optional<Customer> fromDb = customerDao.findById(id);

        // 4. Si la BD lo tiene -> lo guardo en la caché.
        // ifPresent: ejecuta la acción SOLO si la caja trae algo, pasándole el valor.
        // cache::put es una "referencia a método" = forma corta de  c -> cache.put(c)
        // Equivale a:  if (fromDb.isPresent()) { cache.put(fromDb.get()); }
        fromDb.ifPresent(cache::put);

        // 5. Devuelvo lo que haya traído la BD (llena o vacía). Sin if: si no existe, ya viene vacía.
        return fromDb;
    }
}
