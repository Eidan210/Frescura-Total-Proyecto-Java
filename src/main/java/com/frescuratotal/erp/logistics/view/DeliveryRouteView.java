package com.frescuratotal.erp.logistics.view;

import com.frescuratotal.erp.logistics.controller.DeliveryRouteController;
import com.frescuratotal.erp.logistics.controller.LogisticsException;
import com.frescuratotal.erp.logistics.model.DeliveryRoute;
import com.frescuratotal.erp.logistics.model.status.DeliveryRouteStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Vista de consola para administrar rutas de entrega.
 */
public class DeliveryRouteView {

    private final DeliveryRouteController controller;
    private final ConsoleInput input;
    private final String currentUser;

    public DeliveryRouteView(DeliveryRouteController controller, ConsoleInput input, String currentUser) {
        this.controller = controller;
        this.input = input;
        this.currentUser = currentUser;
    }

    public void showMenu() {
        int option;
        do {
            ViewUtils.printTitle("Gestión de rutas de entrega");
            System.out.println("1. Crear ruta");
            System.out.println("2. Listar todas las rutas");
            System.out.println("3. Buscar ruta por id");
            System.out.println("4. Listar rutas por estado");
            System.out.println("5. Editar ruta (solo planificadas)");
            System.out.println("6. Iniciar ruta (salida)");
            System.out.println("7. Completar ruta (llegada)");
            System.out.println("8. Cancelar ruta");
            System.out.println("9. Eliminar ruta");
            System.out.println("0. Volver");
            option = input.readInt("Seleccione una opción: ");

            try {
                switch (option) {
                    case 1:
                        createRoute();
                        break;
                    case 2:
                        ViewUtils.printTitle("Lista de rutas");
                        printTable(controller.listRoutes());
                        break;
                    case 3:
                        findRoute();
                        break;
                    case 4:
                        listByStatus();
                        break;
                    case 5:
                        editRoute();
                        break;
                    case 6:
                        startRoute();
                        break;
                    case 7:
                        completeRoute();
                        break;
                    case 8:
                        cancelRoute();
                        break;
                    case 9:
                        deleteRoute();
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

    private void createRoute() throws LogisticsException {
        ViewUtils.printTitle("Crear ruta");
        String name = input.readText("Nombre de la ruta: ");
        LocalDateTime departure = input.readDateTime("Salida estimada");
        LocalDateTime arrival = input.readDateTime("Llegada estimada");

        DeliveryRoute route = controller.createRoute(name, currentUser, departure, arrival);
        ViewUtils.printSuccess("Ruta creada con id " + route.getRouteId() + " (estado: Planificada)");
    }

    private void findRoute() throws LogisticsException {
        int id = input.readPositiveInt("Id de la ruta: ");
        printDetail(controller.findRoute(id));
    }

    private void listByStatus() throws LogisticsException {
        DeliveryRouteStatus status = chooseStatus();
        ViewUtils.printTitle("Rutas en estado " + status.getDbValue());
        printTable(controller.listRoutesByStatus(status));
    }

    private void editRoute() throws LogisticsException {
        int id = input.readPositiveInt("Id de la ruta a editar: ");
        DeliveryRoute current = controller.findRoute(id);
        printDetail(current);

        System.out.println("Presione Enter para dejar el valor actual.");
        String name = input.readTextOrKeep("Nombre", current.getRouteName());
        LocalDateTime departure = input.readDateTimeOrKeep("Salida estimada", current.getEstimatedDepartureTime());
        LocalDateTime arrival = input.readDateTimeOrKeep("Llegada estimada", current.getEstimatedArrivalTime());

        controller.updateRoute(id, name, departure, arrival);
        ViewUtils.printSuccess("Ruta actualizada");
    }

    private void startRoute() throws LogisticsException {
        int id = input.readPositiveInt("Id de la ruta a iniciar: ");
        DeliveryRoute route = controller.startRoute(id);
        ViewUtils.printSuccess("Ruta en tránsito. Salida real: " + ViewUtils.formatDate(route.getActualDepartureTime()));
    }

    private void completeRoute() throws LogisticsException {
        int id = input.readPositiveInt("Id de la ruta a completar: ");
        DeliveryRoute route = controller.completeRoute(id);
        ViewUtils.printSuccess("Ruta completada. Llegada real: " + ViewUtils.formatDate(route.getActualArrivalTime()));
    }

    private void cancelRoute() throws LogisticsException {
        int id = input.readPositiveInt("Id de la ruta a cancelar: ");
        printDetail(controller.findRoute(id));

        if (input.confirm("¿Seguro que desea cancelar esta ruta?")) {
            controller.cancelRoute(id);
            ViewUtils.printSuccess("Ruta cancelada");
        } else {
            ViewUtils.printInfo("Operación cancelada");
        }
    }

    private void deleteRoute() throws LogisticsException {
        int id = input.readPositiveInt("Id de la ruta a eliminar: ");
        printDetail(controller.findRoute(id));

        if (input.confirm("¿Seguro que desea eliminar esta ruta?")) {
            controller.deleteRoute(id);
            ViewUtils.printSuccess("Ruta eliminada");
        } else {
            ViewUtils.printInfo("Operación cancelada");
        }
    }

    // ===================== MÉTODOS DE APOYO =====================

    private DeliveryRouteStatus chooseStatus() {
        DeliveryRouteStatus[] statuses = DeliveryRouteStatus.values();
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

    private void printTable(List<DeliveryRoute> routes) {
        if (routes.isEmpty()) {
            ViewUtils.printInfo("No hay rutas para mostrar");
            return;
        }
        System.out.printf("%-5s %-25s %-18s %-18s %-13s%n", "ID", "NOMBRE", "SALIDA EST.", "LLEGADA EST.", "ESTADO");
        ViewUtils.printLine();
        for (DeliveryRoute r : routes) {
            System.out.printf("%-5d %-25s %-18s %-18s %-13s%n",
                    r.getRouteId(), r.getRouteName(),
                    ViewUtils.formatDate(r.getEstimatedDepartureTime()),
                    ViewUtils.formatDate(r.getEstimatedArrivalTime()),
                    r.getStatus().getDbValue());
        }
        System.out.println("Total: " + routes.size());
    }

    private void printDetail(DeliveryRoute r) {
        System.out.println();
        System.out.println("Id:               " + r.getRouteId());
        System.out.println("Nombre:           " + r.getRouteName());
        System.out.println("Estado:           " + r.getStatus().getDbValue());
        System.out.println("Creada el:        " + ViewUtils.formatDate(r.getCreatedAt()));
        System.out.println("Creada por:       " + ViewUtils.valueOrDash(r.getCreatedBy()));
        System.out.println("Salida estimada:  " + ViewUtils.formatDate(r.getEstimatedDepartureTime()));
        System.out.println("Llegada estimada: " + ViewUtils.formatDate(r.getEstimatedArrivalTime()));
        System.out.println("Salida real:      " + ViewUtils.formatDate(r.getActualDepartureTime()));
        System.out.println("Llegada real:     " + ViewUtils.formatDate(r.getActualArrivalTime()));
        System.out.println();
    }
}
