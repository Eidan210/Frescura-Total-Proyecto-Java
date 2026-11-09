package com.frescuratotal.erp.suppliers.view;

import com.frescuratotal.erp.suppliers.model.Supplier;
import com.frescuratotal.erp.suppliers.model.SupplierPerformance;

import java.util.List;
import java.util.Scanner;

public class SupplierView {
    private final Scanner sc;

    public SupplierView(Scanner sc) {
        this.sc = sc;
    }

    public int showMenu() {
        System.out.println("====== Suppliers ======");
        System.out.println("1. Register supplier\n" +
                "2. List suppliers\n" +
                "3. Find supplier by ID\n" +
                "4. Update supplier\n" +
                "5. Delete supplier\n" +
                "6. Search supplier by name\n" +
                "7. Supplier performance report" +
                "8. Back");
        return readInt("Option: ");
    }

    public void showPerformance(SupplierPerformance report) {
        if (!report.hasData()) {
            System.out.println("No hay datos de inventario suficientes para generar estadísticas.");
            return;
        }
        System.out.println("*** Reporte de Rendimiento de Proveedores ***");
        System.out.println(">> Proveedor con Mayor Volumen de Suministro:");
        System.out.println(report.topSupplier());
        System.out.println(">> Proveedores con Lotes Vencidos Registrados:");
        report.expiredSuppliers().forEach(name -> System.out.println("- " + name));
    }

    public Supplier readNewSupplier() {
        Supplier s = new Supplier();
        s.setName(readRequired("Name: "));
        s.setNit(readRequired("NIT: "));
        s.setContactPerson(readLine("Contact person: "));
        s.setEmail(readRequired("Email: "));
        s.setPhone(readLine("Phone: "));
        s.setAddress(readLine("Address: "));
        s.setActive(1);
        return s;
    }

    // Blank input keeps the current value
    public void readSupplierChanges(Supplier s) {
        System.out.println("Press Enter to keep the current value.");
        s.setName(readOrKeep("Name", s.getName()));
        s.setNit(readOrKeep("NIT", s.getNit()));
        s.setContactPerson(readOrKeep("Contact person", s.getContactPerson()));
        s.setEmail(readOrKeep("Email", s.getEmail()));
        s.setPhone(readOrKeep("Phone", s.getPhone()));
        s.setAddress(readOrKeep("Address", s.getAddress()));
        String active = readOrKeep("Active (1 = yes, 0 = no)", String.valueOf(s.getActive()));
        s.setActive("0".equals(active) ? 0 : 1);
    }

    public String readSearchText() {
        return readLine("Name contains: ");
    }

    public int readId() {
        return readInt("Supplier ID: ");
    }

    public boolean confirm(String message) {
        return readLine(message + " (y/n): ").equalsIgnoreCase("y");
    }

    public void showSupplier(Supplier s) {
        System.out.println("ID: " + s.getId() +
                "\nName: " + s.getName() +
                "\nNIT: " + s.getNit() +
                "\nContact person: " + s.getContactPerson() +
                "\nEmail: " + s.getEmail() +
                "\nPhone: " + s.getPhone() +
                "\nAddress: " + s.getAddress() +
                "\nActive: " + (s.getActive() == 1 ? "Yes" : "No") +
                "\nRegistered: " + s.getRegistrationDate());
    }


    public void showSuppliers(List<Supplier> suppliers) {
        if (suppliers.isEmpty()) {
            showMessage("No suppliers found.");
            return;
        }
        System.out.printf("%-5s %-30s %-15s %-20s %-6s%n", "ID", "Name", "NIT", "Phone", "Active");
        for (Supplier s : suppliers) {
            System.out.printf("%-5d %-30s %-15s %-20s %-6s%n",
                    s.getId(), s.getName(), s.getNit(), s.getPhone(), s.getActive() == 1 ? "Yes" : "No");
        }
    }

    public void showMessage(String message) {
        System.out.println(message);
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }

    private String readRequired(String prompt) {
        String value = readLine(prompt);
        while (value.isEmpty()) {
            showMessage("This field is required.");
            value = readLine(prompt);
        }
        return value;
    }

    private String readOrKeep(String label, String current) {
        String value = readLine(label + " [" + current + "]: ");
        return value.isEmpty() ? current : value;
    }

    private int readInt(String prompt) {
        while (true) {
            String value = readLine(prompt);
            // Skip the newline left behind by a previous nextInt() in Main
            if (value.isEmpty()) continue;
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                showMessage("Please enter a valid number.");
            }
        }
    }
}
