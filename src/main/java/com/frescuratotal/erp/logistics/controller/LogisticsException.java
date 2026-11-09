package com.frescuratotal.erp.logistics.controller;

/**
 * Excepción propia del módulo de logística.
 *
 * Los controladores la lanzan en dos casos:
 *  1. Cuando un dato que escribió el usuario no es válido (placa vacía, fechas al revés, etc.).
 *  2. Cuando falla la base de datos (el DAO lanza RuntimeException y aquí la "envolvemos").
 *
 * Así la vista solo tiene que atrapar UNA excepción y mostrar getMessage() al usuario.
 */
public class LogisticsException extends Exception {

    private static final long serialVersionUID = 1L;

    public LogisticsException(String message) {
        super(message);
    }

    public LogisticsException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Crea una LogisticsException a partir de un error del DAO.
     *
     * @param action lo que se estaba intentando hacer, por ejemplo "registrar el vehículo"
     * @param cause  la excepción original que lanzó el DAO
     */
    public static LogisticsException fromDatabase(String action, RuntimeException cause) {
        return new LogisticsException("No se pudo " + action + ". Detalle: " + cause.getMessage(), cause);
    }
}
