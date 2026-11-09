package com.frescuratotal.erp;

import com.frescuratotal.erp.common.DatabaseConnection;
import com.frescuratotal.erp.common.util.InputUtil;
import com.frescuratotal.erp.inventory.controller.InventoryController;
import com.frescuratotal.erp.inventory.model.*;
import com.frescuratotal.erp.inventory.view.InventoryView;
import com.frescuratotal.erp.logistics.Dao.Impl.DeliveryAssignmentDaoimpl;
import com.frescuratotal.erp.logistics.Dao.Impl.DeliveryRouteDaoimpl;
import com.frescuratotal.erp.logistics.Dao.Impl.DriverDaoimpl;
import com.frescuratotal.erp.logistics.Dao.Impl.RouteSaleDaoimpl;
import com.frescuratotal.erp.logistics.Dao.Impl.VehicleDaoImpl;
import com.frescuratotal.erp.logistics.controller.LogisticsController;
import com.frescuratotal.erp.logistics.view.LogisticsView;
import com.frescuratotal.erp.sales.controller.SalesController;
import com.frescuratotal.erp.sales.model.CustomerDaoImpl;
import com.frescuratotal.erp.sales.model.SaleDaoImpl;
import com.frescuratotal.erp.sales.model.SaleService;
import com.frescuratotal.erp.sales.view.SalesView;
import com.frescuratotal.erp.suppliers.controller.SupplierController;
import com.frescuratotal.erp.suppliers.model.SupplierDaoImpl;
import com.frescuratotal.erp.suppliers.view.SupplierView;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws SQLException {
        Connection conn = connect();

        if (conn != null && conn.isValid(2)) {
            System.out.println("Connected to database successfully");

        } else {
            System.out.println("Connection Failed");
            return;
        }


        Scanner sc = new Scanner(System.in);
        try {
            runMenu(sc);
        } catch (NoSuchElementException e) {
            System.out.println("Input closed. Exiting...");
        }

        sc.close();
        conn.close();
    }

    /** Stops with a clear message when db.properties is missing or MySQL does not answer. */
    private static Connection connect() {
        try {
            return DatabaseConnection.getInstance().getConnection();
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        } catch (ExceptionInInitializerError e) {
            System.out.println(e.getCause().getMessage());
        }
        System.exit(1);
        return null;
    }

    private static void runMenu(Scanner sc) {
        int option = 0;

        do {
            // 1. Print the menu options
            System.out.println("====== ERP Frescura Total ======");
            System.out.println("1. Inventory\n" +
                    "2. Sales\n" +
                    "3. Suppliers\n" +
                    "4. Logistics\n" +
                    "5. Generar Reporte de Rendimiento de Proveedores\n" +
                    "6. Exit");

            // 2. Read the user's option
            option = InputUtil.readInt(sc, "Option: ");

            // 3. Decide what to do
            switch (option) {
                case 1:
                    // Inventory module
                    LotDao lotDao = new LotDaoImpl();
                    new InventoryController(new InventoryView(sc), lotDao,
                            new InventoryService(lotDao, new ProductDaoImpl(), new SupplierDaoImpl(), new QualityDaoImpl())).start();
                    break;
                case 2:
                    // Sales module
                    new SalesController(new SalesView(sc), new CustomerDaoImpl(),
                            new ProductDaoImpl(), new SaleService()).start();
                    break;
                case 3:
                    // Suppliers module
                    new SupplierController(new SupplierView(sc), new SupplierDaoImpl()).start();
                    break;
                case 4:

                    new LogisticsController(new LogisticsView(sc), new SaleDaoImpl(), new CustomerDaoImpl(),
                            new DeliveryRouteDaoimpl(), new DeliveryAssignmentDaoimpl(), new RouteSaleDaoimpl(),
                            new VehicleDaoImpl(), new DriverDaoimpl()).start();
                    break;
                case 5:
                    new SupplierController(new SupplierView(sc), new SupplierDaoImpl()).performanceReport();
                    break;
                case 6:
                    // Exit
                    System.out.println("Exiting...");
                    break;
                default:
                    // invalid option
                    System.out.println("Invalid option");
            }

        } while (option != 6);
    }
}
