package com.frescuratotal.erp.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DatabaseConfig {

    // Stores the values read from the file
    private static final Properties props = new Properties();

    static{
        try{
            // 1. Open the file
            InputStream input = DatabaseConfig.class.getClassLoader().getResourceAsStream("db.properties");

            // 2. Check that it exists (input is null if not)
            if (input == null){
                throw new RuntimeException("Database properties file not found, please create it with (URL,USER,PASSWORD)");
            }

            // 3. Load all lines into props
            props.load(input);
        }catch(IOException e){
            throw new RuntimeException("Error reading db.properties ",e);
        }
    }

    public static String getUrl(){
        return props.getProperty("db.url");
    }
    public static String getUsername(){
        return props.getProperty("db.user");
    }
    public static String getPassword(){
        return props.getProperty("db.password");
    }

}
