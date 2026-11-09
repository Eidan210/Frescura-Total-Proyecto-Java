package com.frescuratotal.erp.logistics.view;

import java.time.LocalDateTime;

/**
 * Métodos pequeños que usan todas las vistas para imprimir en consola.
 */
public class ViewUtils {

    private static final String LINE = "------------------------------------------------------------------------------------------";

    // Constructor privado: esta clase solo tiene métodos estáticos.
    private ViewUtils() {
    }

    public static void printTitle(String title) {
        System.out.println();
        System.out.println(LINE);
        System.out.println("  " + title.toUpperCase());
        System.out.println(LINE);
    }

    public static void printLine() {
        System.out.println(LINE);
    }

    public static void printSuccess(String message) {
        System.out.println("[OK] " + message);
    }

    public static void printError(String message) {
        System.out.println("[ERROR] " + message);
    }

    public static void printInfo(String message) {
        System.out.println("[INFO] " + message);
    }

    /** Muestra la fecha con formato dd/MM/yyyy HH:mm, o "-" si es null. */
    public static String formatDate(LocalDateTime date) {
        if (date == null) {
            return "-";
        }
        return date.format(ConsoleInput.DATE_FORMAT);
    }

    /** Muestra "-" cuando un texto es null o vacío. */
    public static String valueOrDash(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "-";
        }
        return text;
    }
}
