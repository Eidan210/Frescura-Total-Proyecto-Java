package com.frescuratotal.erp;

import com.frescuratotal.erp.common.DatabaseConnection;
import com.frescuratotal.erp.suppliers.controller.SupplierController;
import com.frescuratotal.erp.suppliers.model.SupplierDaoImpl;
import com.frescuratotal.erp.suppliers.view.SupplierView;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();

        if (conn != null && conn.isValid(2)) {
            System.out.println("Connected to database successfully");

        } else {
            System.out.println("Connection Failed");
            return;
        }


        int option = 0;
        Scanner sc = new Scanner(System.in);

        do {
            // 1. Print the menu options
            System.out.println("====== ERP Frescura Total ======");
            System.out.println("1. Inventory\n" +
                    "2. Sales\n" +
                    "3. Suppliers\n" +
                    "4. Logistics\n" +
                    "5. Exit");

            // 2. Read the user's option
            System.out.print("Option: ");
            option = sc.nextInt();

            // 3. Decide what to do
            switch (option) {
                case 1:
                    // Inventory module
                    System.out.println("Coming soon");
                    break;
                case 2:
                    // Sales module
                    System.out.println("Coming soon");
                    break;
                case 3:
                    // Suppliers module
                    new SupplierController(new SupplierView(sc), new SupplierDaoImpl()).start();
                    break;
                case 4:
                    // logistics module
                    System.out.println("Coming soon");
                    break;
                case 5:
                    // Exit
                    System.out.println("Exiting...");
                    break;
                default:
                    // invalid option
                    System.out.println("Invalid option");
            }

        } while (option != 5);

        sc.close();
        conn.close();
    }
}
