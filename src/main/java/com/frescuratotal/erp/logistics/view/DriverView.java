package com.frescuratotal.erp.logistics.view;

import com.frescuratotal.erp.logistics.controller.DriverController;
import com.frescuratotal.erp.logistics.controller.LogisticsException;
import com.frescuratotal.erp.logistics.model.Driver;

import java.util.List;

/**
 * Vista de consola para administrar conductores.
 */
public class DriverView {

    private final DriverController controller;
    private final ConsoleInput input;

    public DriverView(DriverController controller, ConsoleInput input) {
        this.controller = controller;
        this.input = input;
    }

    public void showMenu() {
        int option;
        do {
            ViewUtils.printTitle("Gestión de conductores");
            System.out.println("1. Registrar conductor");
            System.out.println("2. Listar todos los conductores");
            System.out.println("3. Listar conductores activos");
            System.out.println("4. Buscar conductor por id");
            System.out.println("5. Editar conductor");
            System.out.println("6. Activar / desactivar conductor");
            System.out.println("7. Eliminar conductor");
            System.out.println("0. Volver");
            option = input.readInt("Seleccione una opción: ");

            try {
                switch (option) {
                    case 1:
                        registerDriver();
                        break;
                    case 2:
                        ViewUtils.printTitle("Lista de conductores");
                        printTable(controller.listDrivers());
                        break;
                    case 3:
                        ViewUtils.printTitle("Conductores activos");
                        printTable(controller.listActiveDrivers());
                        break;
                    case 4:
                        findDriver();
                        break;
                    case 5:
                        editDriver();
                        break;
                    case 6:
                        toggleActive();
                        break;
                    case 7:
                        deleteDriver();
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

    private void registerDriver() throws LogisticsException {
        ViewUtils.printTitle("Registrar conductor");
        String name = input.readText("Nombre completo: ");
        String license = input.readText("Número de licencia: ");
        String phone = input.readOptionalText("Teléfono (opcional): ");
        String email = input.readOptionalText("Email (opcional): ");

        Driver driver = controller.registerDriver(name, license, phone, email);
        ViewUtils.printSuccess("Conductor registrado con id " + driver.getDriverId());
    }

    private void findDriver() throws LogisticsException {
        int id = input.readPositiveInt("Id del conductor: ");
        printDetail(controller.findDriver(id));
    }

    private void editDriver() throws LogisticsException {
        int id = input.readPositiveInt("Id del conductor a editar: ");
        Driver current = controller.findDriver(id);
        printDetail(current);

        System.out.println("Presione Enter para dejar el valor actual.");
        String name = input.readTextOrKeep("Nombre", current.getName());
        String license = input.readTextOrKeep("Licencia", current.getLicenseNumber());
        String phone = input.readTextOrKeep("Teléfono", current.getPhone());
        String email = input.readTextOrKeep("Email", current.getEmail());

        controller.updateDriver(id, name, license, phone, email);
        ViewUtils.printSuccess("Conductor actualizado");
    }

    private void toggleActive() throws LogisticsException {
        int id = input.readPositiveInt("Id del conductor: ");
        Driver driver = controller.findDriver(id);

        boolean newState = !driver.isActive();
        String action = newState ? "activar" : "desactivar";

        if (input.confirm("El conductor " + driver.getName() + " está "
                + activeText(driver.isActive()) + ". ¿Desea " + action + "lo?")) {
            controller.setDriverActive(id, newState);
            ViewUtils.printSuccess("Conductor ahora está " + activeText(newState));
        } else {
            ViewUtils.printInfo("Operación cancelada");
        }
    }

    private void deleteDriver() throws LogisticsException {
        int id = input.readPositiveInt("Id del conductor a eliminar: ");
        printDetail(controller.findDriver(id));

        if (input.confirm("¿Seguro que desea eliminar este conductor?")) {
            controller.deleteDriver(id);
            ViewUtils.printSuccess("Conductor eliminado");
        } else {
            ViewUtils.printInfo("Operación cancelada");
        }
    }

    // ===================== MÉTODOS DE APOYO =====================

    private String activeText(boolean active) {
        return active ? "activo" : "inactivo";
    }

    private void printTable(List<Driver> drivers) {
        if (drivers.isEmpty()) {
            ViewUtils.printInfo("No hay conductores para mostrar");
            return;
        }
        System.out.printf("%-5s %-25s %-15s %-13s %-25s %-8s%n", "ID", "NOMBRE", "LICENCIA", "TELÉFONO", "EMAIL", "ESTADO");
        ViewUtils.printLine();
        for (Driver d : drivers) {
            System.out.printf("%-5d %-25s %-15s %-13s %-25s %-8s%n",
                    d.getDriverId(), d.getName(), d.getLicenseNumber(),
                    ViewUtils.valueOrDash(d.getPhone()), ViewUtils.valueOrDash(d.getEmail()),
                    activeText(d.isActive()));
        }
        System.out.println("Total: " + drivers.size());
    }

    private void printDetail(Driver d) {
        System.out.println();
        System.out.println("Id:       " + d.getDriverId());
        System.out.println("Nombre:   " + d.getName());
        System.out.println("Licencia: " + d.getLicenseNumber());
        System.out.println("Teléfono: " + ViewUtils.valueOrDash(d.getPhone()));
        System.out.println("Email:    " + ViewUtils.valueOrDash(d.getEmail()));
        System.out.println("Estado:   " + activeText(d.isActive()));
        System.out.println();
    }
}
