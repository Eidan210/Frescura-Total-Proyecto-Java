package com.frescuratotal.erp.inventory.view;

import com.frescuratotal.erp.inventory.model.Lot;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class InventoryView {
    private final Scanner sc;

    public InventoryView(Scanner sc) {
        this.sc = sc;
    }

    public int showMenu() {
        System.out.println("====== Inventory ======");
        System.out.println("1. List all lots\n" +
                "2. List available lots by product (FEFO)\n" +
                "3. Register goods reception\n" +
                "4. Back");
        return readInt("Option: ");
    }

    public int readProductId() {
        return readInt("Product ID: ");
    }

    public void showLots(List<Lot> lots) {
        if (lots.isEmpty()) {
            System.out.println("No lots found.");
            return;
        }

        System.out.printf("%-5s %-15s %-8s %-12s %-10s%n", "ID", "Lot", "Product", "Expires", "Quantity");

        for (Lot lot : lots) {
            System.out.printf("%-5d %-15s %-8d %-12s %-10s%n",
                    lot.getId(), lot.getLotNumber(), lot.getProductId(),
                    lot.getExpirationDate(), lot.getQuantity());
        }
    }

    public void showMessage(String message) {
        System.out.println(message);
    }

    public Lot readNewLot() {
        Lot lot = new Lot();

        lot.setProductId(readInt("Product ID: "));
        lot.setSupplierId(readInt("Supplier ID: "));
        lot.setExpirationDate(readDate("Expiration date (yyyy-MM-dd): "));
        lot.setQuantity(readBigDecimal("Quantity: "));
        lot.setPurchasePrice(readBigDecimal("Purchase price: "));
        lot.setSalePrice(readBigDecimal("Sale price: "));
        lot.setAisle(readLine("Aisle: "));
        lot.setShelf(readLine("Shelf: "));

        return lot;
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            if (line.isEmpty()) {
                continue;
            }
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("'" + line + "' is not a valid number.");
            }
        }
    }

    private BigDecimal readBigDecimal(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            if (line.isEmpty()) {
                continue;
            }
            try {
                return new BigDecimal(line);
            } catch (NumberFormatException e) {
                System.out.println("'" + line + "' is not a valid number.");
            }
        }
    }

    private LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            if (line.isEmpty()) {
                continue;
            }
            try {
                return LocalDate.parse(line);
            } catch (DateTimeParseException e) {
                System.out.println("'" + line + "' is not a valid date. Use yyyy-MM-dd.");
            }
        }
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        String line = sc.nextLine().trim();
        return line;
    }

}