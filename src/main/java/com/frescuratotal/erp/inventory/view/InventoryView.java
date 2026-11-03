package com.frescuratotal.erp.inventory.view;

import com.frescuratotal.erp.inventory.model.*;

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
                "4. Put lot in quarantine\n" +
                "5. Inspect quarantined lot\n" +
                "6. Register waste\n" +
                "7. Run expiration check\n" +
                "8. Trace lot\n" +
                "9. Back");
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

    public QuarantineRecord readNewQuarantine() {
        QuarantineRecord quarantine = new QuarantineRecord();
        quarantine.setLotId(readInt("Lot ID: "));
        quarantine.setReason(readLine("Reason: "));
        quarantine.setRegisteredBy(readLine("Registered by: "));

        return quarantine;
    }

    public QualityInspection readNewInspection(){
        QualityInspection inspection = new QualityInspection();

        inspection.setLotId(readInt("Lot ID: "));

        String answer = readLine("Approved? (y/n): ");

        if (answer.equalsIgnoreCase("y")) {
            inspection.setResult(InspectionResult.APPROVED);
        } else {
            inspection.setResult(InspectionResult.REJECTED);
        }

        inspection.setInspectorName(readLine("Inspector Name: "));

        if (inspection.getResult() == InspectionResult.REJECTED){
            inspection.setRejectionReason(readLine("Rejection Reason: "));
        }

        inspection.setNotes(readLine("Notes: "));

        return inspection;
    }

    public WasteRecord readNewWaste() {
        WasteRecord waste = new WasteRecord();

        waste.setLotId(readInt("Lot ID: "));
        waste.setLostQuantity(readBigDecimal("Lost quantity: "));
        waste.setReason(readLine("Reason: "));
        waste.setRegisteredBy(readLine("Registered by: "));
        return waste;
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

    public int readLotId() {
        return readInt("Lot ID: ");
    }

    public void showTrace(LotTrace trace, List<LotDelivery> deliveries) {
        System.out.println("=== Lot " + trace.lotNumber() + " ===");
        System.out.println("Product:  " + trace.productName());
        System.out.println("Supplier: " + trace.supplierName() + " (NIT " + trace.supplierNit() + ")");
        System.out.println("Quantity: " + trace.quantity() + " of " + trace.initialQuantity()
                + " | Status: " + trace.status()
                + " | Expires: " + trace.expirationDate());

        System.out.println();
        System.out.println("Customers who received this lot:");

        if (deliveries.isEmpty()) {
            System.out.println("This lot has not been sold yet.");
            return;
        }

        System.out.printf("%-12s %-20s %-25s %-15s %-10s%n",
                "Sale", "Date", "Customer", "Document", "Quantity");

        for (LotDelivery d : deliveries) {
            System.out.printf("%-12s %-20s %-25s %-15s %-10s%n",
                    d.saleNumber(), d.saleDate(), d.customerName(),
                    d.customerDocument(), d.quantity());
        }
    }

}