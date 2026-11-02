package com.frescuratotal.erp.inventory.view;

import com.frescuratotal.erp.inventory.model.Lot;

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
}