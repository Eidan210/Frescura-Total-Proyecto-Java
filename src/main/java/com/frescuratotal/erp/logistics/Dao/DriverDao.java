package com.frescuratotal.erp.logistics.Dao;

import com.frescuratotal.erp.logistics.model.Driver;

import java.util.List;
import java.util.Optional;

public interface DriverDao {
    void saveDriver(Driver driver);
    Optional<Driver> findDriverById(int driverId);
    List<Driver> findAllDrivers();
    List<Driver> findActiveDrivers();
    void updateDriver(Driver driver);
    void deleteDriver(int driverId);
}
