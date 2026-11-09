package com.frescuratotal.erp.logistics.view;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * Clase de ayuda para leer datos desde la consola.
 *
 * Siempre usamos nextLine() (y no nextInt()) para evitar el problema
 * del "salto de línea" que queda guardado en el Scanner.
 * Si el usuario escribe algo inválido, se le vuelve a preguntar.
 */
public class ConsoleInput {

    public static final String DATE_PATTERN = "dd/MM/yyyy HH:mm";
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern(DATE_PATTERN);

    private final Scanner scanner;

    public ConsoleInput(Scanner scanner) {
        this.scanner = scanner;
    }

    /** Lee un texto obligatorio (no acepta vacío). */
    public String readText(String prompt) {
        while (true) {
            System.out.print(prompt);
            String text = scanner.nextLine().trim();
            if (!text.isEmpty()) {
                return text;
            }
            System.out.println("  -> Este campo es obligatorio.");
        }
    }

    /** Lee un texto que puede quedar vacío. */
    public String readOptionalText(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    /**
     * Para editar: muestra el valor actual y si el usuario presiona Enter
     * sin escribir nada, se conserva ese valor.
     */
    public String readTextOrKeep(String prompt, String currentValue) {
        String shown = (currentValue == null) ? "" : currentValue;
        System.out.print(prompt + " [" + shown + "]: ");
        String text = scanner.nextLine().trim();
        if (text.isEmpty()) {
            return currentValue;
        }
        return text;
    }

    /** Lee un número entero. */
    public int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String text = scanner.nextLine().trim();
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException e) {
                System.out.println("  -> Debe escribir un número entero.");
            }
        }
    }

    /** Lee un número entero mayor que cero (útil para ids y capacidades). */
    public int readPositiveInt(String prompt) {
        while (true) {
            int number = readInt(prompt);
            if (number > 0) {
                return number;
            }
            System.out.println("  -> El número debe ser mayor que cero.");
        }
    }

    /** Igual que readTextOrKeep pero para números enteros positivos. */
    public int readPositiveIntOrKeep(String prompt, int currentValue) {
        while (true) {
            System.out.print(prompt + " [" + currentValue + "]: ");
            String text = scanner.nextLine().trim();
            if (text.isEmpty()) {
                return currentValue;
            }
            try {
                int number = Integer.parseInt(text);
                if (number > 0) {
                    return number;
                }
                System.out.println("  -> El número debe ser mayor que cero.");
            } catch (NumberFormatException e) {
                System.out.println("  -> Debe escribir un número entero.");
            }
        }
    }

    /** Lee una fecha y hora con el formato dd/MM/yyyy HH:mm. */
    public LocalDateTime readDateTime(String prompt) {
        while (true) {
            System.out.print(prompt + " (" + DATE_PATTERN + "): ");
            String text = scanner.nextLine().trim();
            try {
                return LocalDateTime.parse(text, DATE_FORMAT);
            } catch (DateTimeParseException e) {
                System.out.println("  -> Fecha inválida. Ejemplo: 25/12/2026 08:30");
            }
        }
    }

    /** Igual que readDateTime pero con Enter se conserva la fecha actual. */
    public LocalDateTime readDateTimeOrKeep(String prompt, LocalDateTime currentValue) {
        while (true) {
            System.out.print(prompt + " (" + DATE_PATTERN + ") [" + ViewUtils.formatDate(currentValue) + "]: ");
            String text = scanner.nextLine().trim();
            if (text.isEmpty()) {
                return currentValue;
            }
            try {
                return LocalDateTime.parse(text, DATE_FORMAT);
            } catch (DateTimeParseException e) {
                System.out.println("  -> Fecha inválida. Ejemplo: 25/12/2026 08:30");
            }
        }
    }

    /** Pregunta sí o no. Devuelve true si responde "s". */
    public boolean confirm(String prompt) {
        while (true) {
            System.out.print(prompt + " (s/n): ");
            String answer = scanner.nextLine().trim().toLowerCase();
            if (answer.equals("s")) {
                return true;
            }
            if (answer.equals("n")) {
                return false;
            }
            System.out.println("  -> Responda s o n.");
        }
    }

    /** Espera a que el usuario presione Enter. */
    public void pause() {
        System.out.print("\nPresione Enter para continuar...");
        scanner.nextLine();
    }
}
