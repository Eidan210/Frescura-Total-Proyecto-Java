package com.frescuratotal.erp.logistics.view;

import com.frescuratotal.erp.logistics.controller.LogisticsException;
import com.frescuratotal.erp.logistics.controller.VehicleController;
import com.frescuratotal.erp.logistics.model.Vehicle;
import com.frescuratotal.erp.logistics.model.status.VehicleStatus;

import java.util.List;

/**
 * Vista de consola para administrar vehículos.
 * La vista solo pide datos y muestra resultados; las reglas están en el controlador.
 */
public class VehicleView {

    private final VehicleController controller;
    private final ConsoleInput input;

    public VehicleView(VehicleController controller, ConsoleInput input) {
        this.controller = controller;
        this.input = input;
    }

    public void showMenu() {
        int option;
        do {
            ViewUtils.printTitle("Gestión de vehículos");
            System.out.println("1. Registrar vehículo");
            System.out.println("2. Listar todos los vehículos");
            System.out.println("3. Buscar vehículo por id");
            System.out.println("4. Listar vehículos por estado");
            System.out.println("5. Editar vehículo");
            System.out.println("6. Cambiar estado de un vehículo");
            System.out.println("7. Eliminar vehículo");
            System.out.println("0. Volver");
            option = input.readInt("Seleccione una opción: ");

            try {
                switch (option) {
                    case 1:
                        registerVehicle();
                        break;
                    case 2:
                        listVehicles();
                        break;
                    case 3:
                        findVehicle();
                        break;
                    case 4:
                        listByStatus();
                        break;
                    case 5:
                        editVehicle();
                        break;
                    case 6:
                        changeStatus();
                        break;
                    case 7:
                        deleteVehicle();
                        break;
                    case 0:
                        break;
                    default:
                        ViewUtils.printError("Opción no válida");
                }
            } catch (LogisticsException e) {
                ViewUtils.printError(e.getMessage());
            }

            if (option != 0) {
                input.pause();
            }
        } while (option != 0);
    }

    // ===================== OPCIONES DEL MENÚ =====================

    private void registerVehicle() throws LogisticsException {
        ViewUtils.printTitle("Registrar vehículo");
        String plate = input.readText("Placa: ");
        String model = input.readText("Modelo: ");
        int capacity = input.readPositiveInt("Capacidad (kg): ");

        Vehicle vehicle = controller.registerVehicle(plate, model, capacity);
        ViewUtils.printSuccess("Vehículo registrado con id " + vehicle.getVehicleId());
    }

    private void listVehicles() throws LogisticsException {
        ViewUtils.printTitle("Lista de vehículos");
        printTable(controller.listVehicles());
    }

    private void findVehicle() throws LogisticsException {
        int id = input.readPositiveInt("Id del vehículo: ");
        printDetail(controller.findVehicle(id));
    }

    private void listByStatus() throws LogisticsException {
        VehicleStatus status = chooseStatus();
        ViewUtils.printTitle("Vehículos en estado " + status.getDbValue());
        printTable(controller.listVehiclesByStatus(status));
    }

    private void editVehicle() throws LogisticsException {
        int id = input.readPositiveInt("Id del vehículo a editar: ");
        Vehicle current = controller.findVehicle(id);
        printDetail(current);

        System.out.println("Presione Enter para dejar el valor actual.");
        String plate = input.readTextOrKeep("Placa", current.getLicensePlate());
        String model = input.readTextOrKeep("Modelo", current.getModel());
        int capacity = input.readPositiveIntOrKeep("Capacidad (kg)", current.getCapacity());

        controller.updateVehicle(id, plate, model, capacity);
        ViewUtils.printSuccess("Vehículo actualizado");
    }

    private void changeStatus() throws LogisticsException {
        int id = input.readPositiveInt("Id del vehículo: ");
        Vehicle current = controller.findVehicle(id);
        System.out.println("Estado actual: " + current.getStatus().getDbValue());

        VehicleStatus newStatus = chooseStatus();
        controller.changeStatus(id, newStatus);
        ViewUtils.printSuccess("Estado cambiado a " + newStatus.getDbValue());
    }

    private void deleteVehicle() throws LogisticsException {
        int id = input.readPositiveInt("Id del vehículo a eliminar: ");
        Vehicle vehicle = controller.findVehicle(id);
        printDetail(vehicle);

        if (input.confirm("¿Seguro que desea eliminar este vehículo?")) {
            controller.deleteVehicle(id);
            ViewUtils.printSuccess("Vehículo eliminado");
        } else {
            ViewUtils.printInfo("Operación cancelada");
        }
    }

    // ===================== MÉTODOS DE APOYO =====================

    /** Muestra los estados posibles y devuelve el que elija el usuario. */
    private VehicleStatus chooseStatus() {
        VehicleStatus[] statuses = VehicleStatus.values();
        System.out.println("Estados disponibles:");
        for (int i = 0; i < statuses.length; i++) {
            System.out.println("  " + (i + 1) + ". " + statuses[i].getDbValue());
        }
        while (true) {
            int option = input.readInt("Seleccione el estado: ");
            if (option >= 1 && option <= statuses.length) {
                return statuses[option - 1];
            }
            ViewUtils.printError("Opción fuera de rango");
        }
    }

    private void printTable(List<Vehicle> vehicles) {
        if (vehicles.isEmpty()) {
            ViewUtils.printInfo("No hay vehículos para mostrar");
            return;
        }
        System.out.printf("%-5s %-12s %-25s %-12s %-15s%n", "ID", "PLACA", "MODELO", "CAPACIDAD", "ESTADO");
        ViewUtils.printLine();
        for (Vehicle v : vehicles) {
            System.out.printf("%-5d %-12s %-25s %-12d %-15s%n",
                    v.getVehicleId(), v.getLicensePlate(), v.getModel(), v.getCapacity(), v.getStatus().getDbValue());
        }
        System.out.println("Total: " + vehicles.size());
    }

    private void printDetail(Vehicle v) {
        System.out.println();
        System.out.println("Id:        " + v.getVehicleId());
        System.out.println("Placa:     " + v.getLicensePlate());
        System.out.println("Modelo:    " + v.getModel());
        System.out.println("Capacidad: " + v.getCapacity() + " kg");
        System.out.println("Estado:    " + v.getStatus().getDbValue());
        System.out.println();
    }
}
