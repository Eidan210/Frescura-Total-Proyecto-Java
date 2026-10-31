package com.frescuratotal.erp.logistics.model.LogisticsDao;

import com.frescuratotal.erp.logistics.model.Vehicle;
import com.frescuratotal.erp.logistics.model.status.VehicleStatus;

import java.util.List;
import java.util.Optional;

public interface VehicleDao {
    void saveVehicle(Vehicle vehicle);
    Optional<Vehicle> findVehicleById(int vehicleId);
    List<Vehicle> findAllVehicles();
    List<Vehicle> findByStatus(VehicleStatus status);
    void updateVehicle(Vehicle vehicle);
    void deleteVehicle(int vehicleId);
}
