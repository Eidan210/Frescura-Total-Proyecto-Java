package com.frescuratotal.erp.logistics.view;

import com.frescuratotal.erp.logistics.Dao.DeliveryAssignmentDao;
import com.frescuratotal.erp.logistics.Dao.DeliveryRouteDao;
import com.frescuratotal.erp.logistics.Dao.DriverDao;
import com.frescuratotal.erp.logistics.Dao.Impl.DeliveryAssignmentDaoimpl;
import com.frescuratotal.erp.logistics.Dao.Impl.DeliveryRouteDaoimpl;
import com.frescuratotal.erp.logistics.Dao.Impl.DriverDaoimpl;
import com.frescuratotal.erp.logistics.Dao.Impl.VehicleDaoImpl;
import com.frescuratotal.erp.logistics.Dao.VehicleDao;
import com.frescuratotal.erp.logistics.controller.DeliveryAssignmentController;
import com.frescuratotal.erp.logistics.controller.DeliveryRouteController;
import com.frescuratotal.erp.logistics.controller.DriverController;
import com.frescuratotal.erp.logistics.controller.VehicleController;

import java.util.Scanner;

/**
 * Punto de entrada del módulo de logística.
 *
 * Aquí se "arma" todo: DAO -> Controller -> View.
 * Si mañana cambian a otra base de datos, solo se cambian las clases Impl aquí.
 */
public class LogisticsMain {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ConsoleInput input = new ConsoleInput(scanner);

        try {
            // 1. DAOs
            VehicleDao vehicleDao = new VehicleDaoImpl();
            DriverDao driverDao = new DriverDaoimpl();
            DeliveryRouteDao routeDao = new DeliveryRouteDaoimpl();
            DeliveryAssignmentDao assignmentDao = new DeliveryAssignmentDaoimpl();

            // 2. Controladores
            VehicleController vehicleController = new VehicleController(vehicleDao);
            DriverController driverController = new DriverController(driverDao);
            DeliveryRouteController routeController = new DeliveryRouteController(routeDao, assignmentDao);
            DeliveryAssignmentController assignmentController = new DeliveryAssignmentController(
                    assignmentDao, routeController, vehicleController, driverController);

            // 3. Usuario que está trabajando (se guarda en "creado por" / "asignado por")
            ViewUtils.printTitle("Bienvenido SECCION - Logistica");
            String currentUser = input.readText("Ingrese su usuario: ");

            // 4. Vistas
            VehicleView vehicleView = new VehicleView(vehicleController, input);
            DriverView driverView = new DriverView(driverController, input);
            DeliveryRouteView routeView = new DeliveryRouteView(routeController, input, currentUser);
            DeliveryAssignmentView assignmentView = new DeliveryAssignmentView(
                    assignmentController, routeController, vehicleController, driverController, input, currentUser);

            LogisticsMenuView menu = new LogisticsMenuView(vehicleView, driverView, routeView, assignmentView, input);
            menu.showMenu();

        } catch (RuntimeException e) {
            // Último recurso: algo que no esperábamos (por ejemplo, no hay conexión a la BD).
            ViewUtils.printError("Ocurrió un error inesperado: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
}
