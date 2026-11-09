package com.frescuratotal.erp.sales.model;

import java.math.BigDecimal;

/**
 * EJERCICIO 3 - Patrón Strategy: el CONTRATO común.
 *
 * Una interfaz dice QUÉ se debe poder hacer, pero no CÓMO.
 * Toda clase que haga "implements DeliveryStrategy" está OBLIGADA a implementar
 * los dos métodos; si le falta uno, el código NO compila.
 *
 * ¿Por qué interfaz y no un if con el 12%? Para que agregar una modalidad nueva
 * (EXPRÉS, PROGRAMADA...) sea CREAR una clase nueva, sin modificar las que ya
 * funcionan. Eso es el principio Abierto/Cerrado (la "O" de SOLID).
 */
public interface DeliveryStrategy {

    // Recibe el valor original de la compra y devuelve lo que paga el cliente.
    // BigDecimal y no double: con dinero no se aceptan errores de precisión (0.1 + 0.2 != 0.3 en double).
    BigDecimal calculateTotal(BigDecimal originalPurchasePrice);

    // Cada estrategia sabe describirse a sí misma; la vista solo imprime este texto.
    String getDescription();
}
