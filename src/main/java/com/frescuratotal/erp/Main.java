package com.frescuratotal.erp;

import com.frescuratotal.erp.common.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();

        if (conn != null && conn.isValid(2)) {
            System.out.println("Connected to database successfully");

        } else {
            System.out.println("Connection Failed");
        }
    }
}
