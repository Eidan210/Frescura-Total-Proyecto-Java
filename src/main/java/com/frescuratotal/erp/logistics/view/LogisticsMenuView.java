package com.frescuratotal.erp.logistics.view;

/**
 * Menú principal del módulo de logística.
 * Solo muestra las opciones y llama a la vista que corresponde.
 */
public class LogisticsMenuView {

    private final VehicleView vehicleView;
    private final DriverView driverView;
    private final DeliveryRouteView routeView;
    private final DeliveryAssignmentView assignmentView;
    private final ConsoleInput input;

    public LogisticsMenuView(VehicleView vehicleView, DriverView driverView,
                             DeliveryRouteView routeView, DeliveryAssignmentView assignmentView,
                             ConsoleInput input) {
        this.vehicleView = vehicleView;
        this.driverView = driverView;
        this.routeView = routeView;
        this.assignmentView = assignmentView;
        this.input = input;
    }

    public void showMenu() {
        int option;
        do {
            ViewUtils.printTitle("Frescura Total ERP - Módulo de logística");
            System.out.println("1. Vehículos");
            System.out.println("2. Conductores");
            System.out.println("3. Rutas de entrega");
            System.out.println("4. Asignaciones (ruta + vehículo + conductor)");
            System.out.println("0. Salir");
            option = input.readInt("Seleccione una opción: ");

            switch (option) {
                case 1:
                    vehicleView.showMenu();
                    break;
                case 2:
                    driverView.showMenu();
                    break;
                case 3:
                    routeView.showMenu();
                    break;
                case 4:
                    assignmentView.showMenu();
                    break;
                case 0:
                    System.out.println("Hasta luego.");
                    break;
                default:
                    ViewUtils.printError("Opción no válida");
            }
        } while (option != 0);
    }
}
