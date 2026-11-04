package com.frescuratotal.erp.common.util;

import java.util.Scanner;

public class InputUtil {

    private InputUtil() {
    }

    /** Keeps asking until the user types a whole number, so a letter never closes the program. */
    public static int readInt(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("'" + line + "' is not a valid number.");
            }
        }
    }
}
