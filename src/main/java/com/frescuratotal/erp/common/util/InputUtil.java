package com.frescuratotal.erp.common.util;

import java.math.BigDecimal;
import java.util.Scanner;

public class InputUtil {

    private static final BigDecimal MAX_AMOUNT = new BigDecimal("99999999.99");

    private InputUtil() {
    }

    /** Quantities and prices are DECIMAL(10,2): MySQL would round or reject anything else. */
    public static void requireAmount(BigDecimal value, String name) {
        if (value == null || value.signum() <= 0 || value.compareTo(MAX_AMOUNT) > 0
                || value.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException(name + " must be between 0.01 and " + MAX_AMOUNT
                    + ", with at most 2 decimals.");
        }
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
