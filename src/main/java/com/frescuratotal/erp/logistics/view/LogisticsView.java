package com.frescuratotal.erp.logistics.view;

import com.frescuratotal.erp.logistics.model.DeliveryAssignment;
import com.frescuratotal.erp.logistics.model.DeliveryRoute;
import com.frescuratotal.erp.logistics.model.Driver;
import com.frescuratotal.erp.logistics.model.RouteSale;
import com.frescuratotal.erp.logistics.model.Vehicle;
import com.frescuratotal.erp.logistics.model.status.DeliveryRouteStatus;
import com.frescuratotal.erp.logistics.model.status.VehicleStatus;
import com.frescuratotal.erp.sales.model.Sale;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class LogisticsView {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final Scanner sc;

    public LogisticsView(Scanner sc) {
        this.sc = sc;
    }

    public int showMenu() {
        System.out.println("====== Logistics ======");
        System.out.println("1. Dispatch panel\n" +
                "2. Create delivery route\n" +
                "3. List delivery routes\n" +
                "4. Mark sale as delivered\n" +
                "5. Register vehicle\n" +
                "6. Register driver\n" +
                "7. List vehicles\n" +
                "8. List drivers\n" +
                "9. Back");
        return readInt("Option: ");
    }

    // ---------- Input ----------

    public String readRouteName() {
        return readLine("Route name: ");
    }

    public String readCreatedBy() {
        return readLine("Created by: ");
    }

    public int readSaleId() {
        return readInt("Sale ID (0 to finish): ");
    }

    public int readVehicleId() {
        return readInt("Vehicle ID: ");
    }

    public int readDriverId() {
        return readInt("Driver ID: ");
    }

    public String readReceivedBy() {
        return readLine("Received by: ");
    }

    /** Returns the chosen status, or null to list every route. */
    public DeliveryRouteStatus readRouteStatusFilter() {
        System.out.println("Filter by status:");
        System.out.println("0. All");
        DeliveryRouteStatus[] statuses = DeliveryRouteStatus.values();
        for (int i = 0; i < statuses.length; i++) {
            System.out.println((i + 1) + ". " + statuses[i].getDbValue());
        }

        while (true) {
            int option = readInt("Option: ");
            if (option == 0) {
                return null;
            }
            if (option >= 1 && option <= statuses.length) {
                return statuses[option - 1];
            }
            System.out.println("Invalid option.");
        }
    }

    public Vehicle readNewVehicle() {
        String licensePlate = readLine("License plate: ");
        String model = readLine("Model: ");
        int capacity = readInt("Capacity: ");
        return new Vehicle(0, licensePlate, model, capacity, VehicleStatus.ACTIVE);
    }

    public Driver readNewDriver() {
        String name = readLine("Name: ");
        String licenseNumber = readLine("License number: ");
        String phone = readLine("Phone: ");
        String email = readLine("Email: ");
        return new Driver(0, name, licenseNumber, phone, email, true);
    }

    // ---------- Output ----------

    public void showSales(List<Sale> sales) {
        if (sales.isEmpty()) {
            System.out.println("No sales found.");
            return;
        }
        System.out.printf("%-5s %-20s %-30s %-17s %-12s %-15s%n",
                "ID", "Number", "Customer", "Date", "Total", "Status");
        for (Sale sale : sales) {
            System.out.printf("%-5d %-20s %-30s %-17s %-12s %-15s%n",
                    sale.getId(), sale.getSaleNumber(), sale.getCustomer().getName(),
                    formatDate(sale.getSaleDate()), sale.getTotalAmount(), sale.getSaleStatus());
        }
    }

    public void showVehicles(List<Vehicle> vehicles) {
        if (vehicles.isEmpty()) {
            System.out.println("No vehicles found.");
            return;
        }
        System.out.printf("%-5s %-12s %-25s %-10s %-12s%n", "ID", "Plate", "Model", "Capacity", "Status");
        for (Vehicle vehicle : vehicles) {
            System.out.printf("%-5d %-12s %-25s %-10d %-12s%n",
                    vehicle.getVehicleId(), vehicle.getLicensePlate(), vehicle.getModel(),
                    vehicle.getCapacity(), vehicle.getStatus());
        }
    }

    public void showDrivers(List<Driver> drivers) {
        if (drivers.isEmpty()) {
            System.out.println("No drivers found.");
            return;
        }
        System.out.printf("%-5s %-30s %-15s %-15s %-7s%n", "ID", "Name", "License", "Phone", "Active");
        for (Driver driver : drivers) {
            System.out.printf("%-5d %-30s %-15s %-15s %-7s%n",
                    driver.getDriverId(), driver.getName(), driver.getLicenseNumber(),
                    driver.getPhone(), driver.isActive() ? "Yes" : "No");
        }
    }

    public void showRoute(DeliveryRoute route, DeliveryAssignment assignment, List<RouteSale> routeSales) {
        System.out.println("------------------------------");
        System.out.println("Route " + route.getRouteId() + ": " + route.getRouteName());
        System.out.println("Status: " + route.getStatus());
        System.out.println("Departure: " + formatDate(route.getActualDepartureTime()));
        System.out.println("Arrival: " + formatDate(route.getActualArrivalTime()));

        // A route created outside this module may have no assignment.
        if (assignment == null) {
            System.out.println("Vehicle: not assigned");
            System.out.println("Driver: not assigned");
        } else {
            System.out.println("Vehicle: " + assignment.getVehicle().getLicensePlate()
                    + " (" + assignment.getVehicle().getModel() + ")");
            System.out.println("Driver: " + assignment.getDriver().getName());
        }

        System.out.println("Sales:");
        for (RouteSale routeSale : routeSales) {
            System.out.println("  " + routeSale.getDeliveryOrder() + ". "
                    + routeSale.getSale().getSaleNumber()
                    + " | " + routeSale.getSale().getSaleStatus()
                    + " | " + routeSale.getDestinationAddress()
                    + " | Delivered: " + formatDate(routeSale.getDeliveryDate()));
        }
    }

    public void showMessage(String message) {
        System.out.println(message);
    }

    // ---------- Helpers ----------

    private String formatDate(LocalDateTime date) {
        if (date == null) {
            return "-";
        }
        return date.format(DATE_FORMAT);
    }

    private int readInt(String prompt) {
        while (true) {
            String line = readLine(prompt);
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("'" + line + "' is not a valid number.");
            }
        }
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }
}
