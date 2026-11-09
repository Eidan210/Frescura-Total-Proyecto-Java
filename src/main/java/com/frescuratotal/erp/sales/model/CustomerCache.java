package com.frescuratotal.erp.sales.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * EJERCICIO 1 - Caché de clientes en memoria (patrón Singleton).
 *
 * Analogía: la caché es la NEVERA. Solo guarda y devuelve; NO va al supermercado (la BD).
 * Quien decide "¿está en la nevera o voy a la BD?" es CustomerService (requerimiento 5).
 * Por eso esta clase no importa DAOs ni Connection.
 *
 * Los 3 ingredientes del Singleton (igual que DatabaseConnection):
 *   1. private static ... instance   -> una sola copia, pertenece a la CLASE
 *   2. constructor private           -> "new CustomerCache()" desde afuera NO compila
 *   3. public static getInstance()   -> único punto de acceso; la crea la 1a vez (lazy initialization)
 *
 * ¿Por qué Singleton? Para que SaleService y CustomerService compartan la MISMA caché.
 * Aunque existan dos objetos CustomerService, los dos piden getInstance() y reciben la misma.
 */
public class CustomerCache {

    // 1. La única instancia
    private static CustomerCache instance;

    // Map y no List: con List habría que recorrerla toda para encontrar el ID 5;
    // con Map se pide map.get(5) directo. Llave = ID (Integer, porque las colecciones
    // solo guardan objetos), valor = el cliente completo.
    private final Map<Integer, Customer> customers = new HashMap<>();

    // 2. Constructor privado
    private CustomerCache() {
    }

    // 3. Punto único de acceso
    public static CustomerCache getInstance() {
        if (instance == null) {          // primera vez: la creo
            instance = new CustomerCache();
        }
        return instance;                 // siguientes veces: devuelvo la misma
    }

    // SACAR de la nevera. Optional = una "caja" que puede venir llena o vacía.
    // ofNullable decide sola: si map.get devuelve null -> caja vacía; si no -> caja con el cliente.
    // No necesita if: ofNullable ya hace esa decisión.
    public Optional<Customer> get(int id) {
        return Optional.ofNullable(customers.get(id));
    }

    // GUARDAR en la nevera. Aquí va la validación (caso de borde: no guardar nulos ni inválidos).
    // Cláusula de guarda: si es malo, return y no sigo.
    // Orden del ||: primero el null; si customer es null, Java NO evalúa getId() (evita NullPointerException).
    // getId() <= 0: un cliente que aún no existe en la BD (id_cliente AUTO_INCREMENT empieza en 1).
    public void put(Customer customer) {
        if (customer == null || customer.getId() <= 0) {
            return;
        }
        customers.put(customer.getId(), customer);
    }

    // Útil para las pruebas: cuántos clientes hay guardados.
    public int size() {
        return customers.size();
    }
}
