package com.frescuratotal.erp.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static DatabaseConnection instance;
    private final Connection connection;

    private DatabaseConnection(){
        try{
            this.connection = DriverManager.getConnection(
                    DatabaseConfig.getUrl(),
                    DatabaseConfig.getUsername(),
                    DatabaseConfig.getPassword()
            );
        }catch(SQLException e){
            throw new RuntimeException("Could not connect to the database. Check that MySQL is running and the credentials in db.properties are correct.", e);
        }
    }

    public static DatabaseConnection getInstance(){
        if(instance == null){
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection(){
        return connection;
    }


    public void inTransaction(Runnable work) {
        try {
            connection.setAutoCommit(false);
            try {
                work.run();
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("The database transaction failed", e);
        }
    }
}
