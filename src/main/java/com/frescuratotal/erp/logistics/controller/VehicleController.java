package com.frescuratotal.erp.logistics.controller;

import com.frescuratotal.erp.logistics.Dao.VehicleDao;
import com.frescuratotal.erp.logistics.model.Vehicle;
import com.frescuratotal.erp.logistics.model.status.VehicleStatus;

import java.util.List;
import java.util.Optional;

/**
 * Controlador de vehículos.
 * Valida los datos que llegan de la vista y luego llama al DAO.
 */
public class VehicleController {

    private static final int MAX_PLATE_LENGTH = 10;

    private final VehicleDao vehicleDao;

    public VehicleController(VehicleDao vehicleDao) {
        this.vehicleDao = vehicleDao;
    }

    // ===================== CREAR =====================

    public Vehicle registerVehicle(String licensePlate, String model, int capacity) throws LogisticsException {
        validateVehicleData(licensePlate, model, capacity);

        String plate = normalizePlate(licensePlate);
        if (plateExists(plate, 0)) {
            throw new LogisticsException("Ya existe un vehículo con la placa " + plate);
        }

        Vehicle vehicle = new Vehicle();
        vehicle.setLicensePlate(plate);
        vehicle.setModel(model.trim());
        vehicle.setCapacity(capacity);
        vehicle.setStatus(VehicleStatus.ACTIVE);

        try {
            vehicleDao.saveVehicle(vehicle);
        } catch (RuntimeException e) {
            throw LogisticsException.fromDatabase("registrar el vehículo", e);
        }
        return vehicle;
    }

    // ===================== CONSULTAR =====================

    public Vehicle findVehicle(int vehicleId) throws LogisticsException {
        validateId(vehicleId);

        Optional<Vehicle> result;
        try {
            result = vehicleDao.findVehicleById(vehicleId);
        } catch (RuntimeException e) {
            throw LogisticsException.fromDatabase("buscar el vehículo", e);
        }

        if (!result.isPresent()) {
            throw new LogisticsException("No existe un vehículo con id " + vehicleId);
        }
        return result.get();
    }

    public List<Vehicle> listVehicles() throws LogisticsException {
        try {
            return vehicleDao.findAllVehicles();
        } catch (RuntimeException e) {
            throw LogisticsException.fromDatabase("listar los vehículos", e);
        }
    }

    public List<Vehicle> listVehiclesByStatus(VehicleStatus status) throws LogisticsException {
        if (status == null) {
            throw new LogisticsException("Debe seleccionar un estado");
        }
        try {
            return vehicleDao.findByStatus(status);
        } catch (RuntimeException e) {
            throw LogisticsException.fromDatabase("listar los vehículos por estado", e);
        }
    }

    // ===================== ACTUALIZAR =====================

    public Vehicle updateVehicle(int vehicleId, String licensePlate, String model, int capacity) throws LogisticsException {
        validateVehicleData(licensePlate, model, capacity);
        Vehicle vehicle = findVehicle(vehicleId);

        String plate = normalizePlate(licensePlate);
        if (plateExists(plate, vehicleId)) {
            throw new LogisticsException("Otro vehículo ya tiene la placa " + plate);
        }

        vehicle.setLicensePlate(plate);
        vehicle.setModel(model.trim());
        vehicle.setCapacity(capacity);
        saveChanges(vehicle);
        return vehicle;
    }

    public Vehicle changeStatus(int vehicleId, VehicleStatus newStatus) throws LogisticsException {
        if (newStatus == null) {
            throw new LogisticsException("Debe seleccionar un estado");
        }
        Vehicle vehicle = findVehicle(vehicleId);
        if (vehicle.getStatus() == newStatus) {
            throw new LogisticsException("El vehículo ya está en estado " + newStatus.getDbValue());
        }
        vehicle.setStatus(newStatus);
        saveChanges(vehicle);
        return vehicle;
    }

    // ===================== ELIMINAR =====================

    public void deleteVehicle(int vehicleId) throws LogisticsException {
        findVehicle(vehicleId); // si no existe, lanza la excepción
        try {
            vehicleDao.deleteVehicle(vehicleId);
        } catch (RuntimeException e) {
            // Lo más común: el vehículo tiene asignaciones (llave foránea).
            throw LogisticsException.fromDatabase("eliminar el vehículo (puede tener asignaciones)", e);
        }
    }

    // ===================== MÉTODOS PRIVADOS =====================

    private void saveChanges(Vehicle vehicle) throws LogisticsException {
        try {
            vehicleDao.updateVehicle(vehicle);
        } catch (RuntimeException e) {
            throw LogisticsException.fromDatabase("actualizar el vehículo", e);
        }
    }

    private void validateVehicleData(String licensePlate, String model, int capacity) throws LogisticsException {
        if (isBlank(licensePlate)) {
            throw new LogisticsException("La placa es obligatoria");
        }
        if (licensePlate.trim().length() > MAX_PLATE_LENGTH) {
            throw new LogisticsException("La placa no puede tener más de " + MAX_PLATE_LENGTH + " caracteres");
        }
        if (isBlank(model)) {
            throw new LogisticsException("El modelo es obligatorio");
        }
        if (capacity <= 0) {
            throw new LogisticsException("La capacidad debe ser mayor que cero");
        }
    }

    /**
     * Revisa si la placa ya está usada por otro vehículo.
     * @param ignoreId id del vehículo que se está editando (0 si es uno nuevo)
     */
    private boolean plateExists(String plate, int ignoreId) throws LogisticsException {
        List<Vehicle> vehicles = listVehicles();
        for (Vehicle v : vehicles) {
            if (v.getVehicleId() != ignoreId && plate.equalsIgnoreCase(v.getLicensePlate())) {
                return true;
            }
        }
        return false;
    }

    private String normalizePlate(String plate) {
        return plate.trim().toUpperCase();
    }

    private void validateId(int id) throws LogisticsException {
        if (id <= 0) {
            throw new LogisticsException("El id debe ser un número mayor que cero");
        }
    }

    private boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }
}
