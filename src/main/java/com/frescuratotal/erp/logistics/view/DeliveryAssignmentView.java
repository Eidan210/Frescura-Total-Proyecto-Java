package com.frescuratotal.erp.logistics.view;

import com.frescuratotal.erp.logistics.controller.DeliveryAssignmentController;
import com.frescuratotal.erp.logistics.controller.DeliveryRouteController;
import com.frescuratotal.erp.logistics.controller.DriverController;
import com.frescuratotal.erp.logistics.controller.LogisticsException;
import com.frescuratotal.erp.logistics.controller.VehicleController;
import com.frescuratotal.erp.logistics.model.DeliveryAssignment;
import com.frescuratotal.erp.logistics.model.DeliveryRoute;
import com.frescuratotal.erp.logistics.model.Driver;
import com.frescuratotal.erp.logistics.model.Vehicle;
import com.frescuratotal.erp.logistics.model.status.DeliveryRouteStatus;
import com.frescuratotal.erp.logistics.model.status.VehicleStatus;

import java.util.List;

/**
 * Vista de consola para asignar vehículo y conductor a una ruta.
 *
 * Recibe también los controladores de ruta, vehículo y conductor solo para
 * mostrarle al usuario las opciones disponibles antes de pedirle un id.
 */
public class DeliveryAssignmentView {

    private final DeliveryAssignmentController controller;
    private final DeliveryRouteController routeController;
    private final VehicleController vehicleController;
    private final DriverController driverController;
    private final ConsoleInput input;
    private final String currentUser;

    public DeliveryAssignmentView(DeliveryAssignmentController controller,
                                  DeliveryRouteController routeController,
                                  VehicleController vehicleController,
                                  DriverController driverController,
                                  ConsoleInput input,
                                  String currentUser) {
        this.controller = controller;
        this.routeController = routeController;
        this.vehicleController = vehicleController;
        this.driverController = driverController;
        this.input = input;
        this.currentUser = currentUser;
    }

    public void showMenu() {
        int option;
        do {
            ViewUtils.printTitle("Asignaciones de entrega");
            System.out.println("1. Asignar vehículo y conductor a una ruta");
            System.out.println("2. Listar todas las asignaciones");
            System.out.println("3. Buscar asignación por id");
            System.out.println("4. Ver asignaciones de una ruta");
            System.out.println("5. Ver asignaciones de un vehículo");
            System.out.println("6. Ver asignaciones de un conductor");
            System.out.println("7. Cambiar vehículo / conductor de una asignación");
            System.out.println("8. Eliminar asignación");
            System.out.println("0. Volver");
            option = input.readInt("Seleccione una opción: ");

            try {
                switch (option) {
                    case 1:
                        createAssignment();
                        break;
                    case 2:
                        ViewUtils.printTitle("Lista de asignaciones");
                        printTable(controller.listAssignments());
                        break;
                    case 3:
                        findAssignment();
                        break;
                    case 4:
                        printTable(controller.listAssignmentsByRoute(input.readPositiveInt("Id de la ruta: ")));
                        break;
                    case 5:
                        printTable(controller.listAssignmentsByVehicle(input.readPositiveInt("Id del vehículo: ")));
                        break;
                    case 6:
                        printTable(controller.listAssignmentsByDriver(input.readPositiveInt("Id del conductor: ")));
                        break;
                    case 7:
                        editAssignment();
                        break;
                    case 8:
                        deleteAssignment();
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

    private void createAssignment() throws LogisticsException {
        ViewUtils.printTitle("Nueva asignación");

        showPlannedRoutes();
        int routeId = input.readPositiveInt("Id de la ruta: ");

        showActiveVehicles();
        int vehicleId = input.readPositiveInt("Id del vehículo: ");

        showActiveDrivers();
        int driverId = input.readPositiveInt("Id del conductor: ");

        String notes = input.readOptionalText("Notas (opcional): ");

        DeliveryAssignment assignment = controller.assign(routeId, vehicleId, driverId, currentUser, notes);
        ViewUtils.printSuccess("Asignación creada con id " + assignment.getDeliveryAssignmentId());
    }

    private void findAssignment() throws LogisticsException {
        int id = input.readPositiveInt("Id de la asignación: ");
        printDetail(controller.findAssignment(id));
    }

    private void editAssignment() throws LogisticsException {
        int id = input.readPositiveInt("Id de la asignación a modificar: ");
        DeliveryAssignment current = controller.findAssignment(id);
        printDetail(current);

        showActiveVehicles();
        showActiveDrivers();
        System.out.println("Presione Enter para dejar el valor actual.");
        int vehicleId = input.readPositiveIntOrKeep("Id del vehículo", current.getVehicle().getVehicleId());
        int driverId = input.readPositiveIntOrKeep("Id del conductor", current.getDriver().getDriverId());
        String notes = input.readTextOrKeep("Notas", current.getNotes());

        controller.updateAssignment(id, vehicleId, driverId, notes);
        ViewUtils.printSuccess("Asignación actualizada");
    }

    private void deleteAssignment() throws LogisticsException {
        int id = input.readPositiveInt("Id de la asignación a eliminar: ");
        printDetail(controller.findAssignment(id));

        if (input.confirm("¿Seguro que desea eliminar esta asignación?")) {
            controller.deleteAssignment(id);
            ViewUtils.printSuccess("Asignación eliminada");
        } else {
            ViewUtils.printInfo("Operación cancelada");
        }
    }

    // ===================== LISTAS DE AYUDA =====================

    private void showPlannedRoutes() throws LogisticsException {
        List<DeliveryRoute> routes = routeController.listRoutesByStatus(DeliveryRouteStatus.PLANNED);
        System.out.println("\nRutas planificadas:");
        if (routes.isEmpty()) {
            ViewUtils.printInfo("No hay rutas planificadas");
        }
        for (DeliveryRoute r : routes) {
            System.out.println("  [" + r.getRouteId() + "] " + r.getRouteName()
                    + " - salida " + ViewUtils.formatDate(r.getEstimatedDepartureTime()));
        }
    }

    private void showActiveVehicles() throws LogisticsException {
        List<Vehicle> vehicles = vehicleController.listVehiclesByStatus(VehicleStatus.ACTIVE);
        System.out.println("\nVehículos activos:");
        if (vehicles.isEmpty()) {
            ViewUtils.printInfo("No hay vehículos activos");
        }
        for (Vehicle v : vehicles) {
            System.out.println("  [" + v.getVehicleId() + "] " + v.getLicensePlate() + " - " + v.getModel());
        }
    }

    private void showActiveDrivers() throws LogisticsException {
        List<Driver> drivers = driverController.listActiveDrivers();
        System.out.println("\nConductores activos:");
        if (drivers.isEmpty()) {
            ViewUtils.printInfo("No hay conductores activos");
        }
        for (Driver d : drivers) {
            System.out.println("  [" + d.getDriverId() + "] " + d.getName() + " - licencia " + d.getLicenseNumber());
        }
    }

    // ===================== IMPRESIÓN =====================

    private void printTable(List<DeliveryAssignment> assignments) {
        if (assignments.isEmpty()) {
            ViewUtils.printInfo("No hay asignaciones para mostrar");
            return;
        }
        System.out.printf("%-5s %-22s %-13s %-10s %-22s %-18s%n", "ID", "RUTA", "ESTADO RUTA", "PLACA", "CONDUCTOR", "FECHA");
        ViewUtils.printLine();
        for (DeliveryAssignment a : assignments) {
            System.out.printf("%-5d %-22s %-13s %-10s %-22s %-18s%n",
                    a.getDeliveryAssignmentId(),
                    a.getRoute().getRouteName(),
                    a.getRoute().getStatus().getDbValue(),
                    a.getVehicle().getLicensePlate(),
                    a.getDriver().getName(),
                    ViewUtils.formatDate(a.getAssignmentDate()));
        }
        System.out.println("Total: " + assignments.size());
    }

    private void printDetail(DeliveryAssignment a) {
        System.out.println();
        System.out.println("Id asignación: " + a.getDeliveryAssignmentId());
        System.out.println("Ruta:          [" + a.getRoute().getRouteId() + "] " + a.getRoute().getRouteName()
                + " (" + a.getRoute().getStatus().getDbValue() + ")");
        System.out.println("Vehículo:      [" + a.getVehicle().getVehicleId() + "] " + a.getVehicle().getLicensePlate()
                + " - " + a.getVehicle().getModel());
        System.out.println("Conductor:     [" + a.getDriver().getDriverId() + "] " + a.getDriver().getName());
        System.out.println("Fecha:         " + ViewUtils.formatDate(a.getAssignmentDate()));
        System.out.println("Asignado por:  " + ViewUtils.valueOrDash(a.getAssignedBy()));
        System.out.println("Notas:         " + ViewUtils.valueOrDash(a.getNotes()));
        System.out.println();
    }
}
