package com.frescuratotal.erp.common.exception;

import java.util.List;

/**
 * EJERCICIO 4 - Error de la importación de clientes.
 *
 * Extiende RuntimeException (excepción "no chequeada"): no obliga a poner throws en cada método.
 * Además del mensaje principal (el que pide el enunciado), guarda el DETALLE de cada fila
 * inválida para que la vista pueda mostrar qué falló y en qué línea.
 */
public class ImportException extends RuntimeException {

    private final List<String> details;

    public ImportException(String message) {
        this(message, List.of());
    }

    public ImportException(String message, List<String> details) {
        super(message);
        this.details = details;
    }

    public List<String> getDetails() {
        return details;
    }
}
