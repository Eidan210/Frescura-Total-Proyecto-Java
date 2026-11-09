package com.frescuratotal.erp.logistics.controller;

import com.frescuratotal.erp.logistics.Dao.DriverDao;
import com.frescuratotal.erp.logistics.model.Driver;

import java.util.List;
import java.util.Optional;

/**
 * Controlador de conductores.
 * Valida los datos que llegan de la vista y luego llama al DAO.
 */
public class DriverController {

    private static final int MIN_PHONE_LENGTH = 7;

    private final DriverDao driverDao;

    public DriverController(DriverDao driverDao) {
        this.driverDao = driverDao;
    }

    // ===================== CREAR =====================

    public Driver registerDriver(String name, String licenseNumber, String phone, String email) throws LogisticsException {
        validateDriverData(name, licenseNumber, phone, email);

        String license = licenseNumber.trim();
        if (licenseExists(license, 0)) {
            throw new LogisticsException("Ya existe un conductor con la licencia " + license);
        }

        Driver driver = new Driver();
        driver.setName(name.trim());
        driver.setLicenseNumber(license);
        driver.setPhone(cleanOptional(phone));
        driver.setEmail(cleanOptional(email));
        driver.setActive(true);

        try {
            driverDao.saveDriver(driver);
        } catch (RuntimeException e) {
            throw LogisticsException.fromDatabase("registrar el conductor", e);
        }
        return driver;
    }

    // ===================== CONSULTAR =====================

    public Driver findDriver(int driverId) throws LogisticsException {
        if (driverId <= 0) {
            throw new LogisticsException("El id debe ser un número mayor que cero");
        }

        Optional<Driver> result;
        try {
            result = driverDao.findDriverById(driverId);
        } catch (RuntimeException e) {
            throw LogisticsException.fromDatabase("buscar el conductor", e);
        }

        if (!result.isPresent()) {
            throw new LogisticsException("No existe un conductor con id " + driverId);
        }
        return result.get();
    }

    public List<Driver> listDrivers() throws LogisticsException {
        try {
            return driverDao.findAllDrivers();
        } catch (RuntimeException e) {
            throw LogisticsException.fromDatabase("listar los conductores", e);
        }
    }

    public List<Driver> listActiveDrivers() throws LogisticsException {
        try {
            return driverDao.findActiveDrivers();
        } catch (RuntimeException e) {
            throw LogisticsException.fromDatabase("listar los conductores activos", e);
        }
    }

    // ===================== ACTUALIZAR =====================

    public Driver updateDriver(int driverId, String name, String licenseNumber, String phone, String email) throws LogisticsException {
        validateDriverData(name, licenseNumber, phone, email);
        Driver driver = findDriver(driverId);

        String license = licenseNumber.trim();
        if (licenseExists(license, driverId)) {
            throw new LogisticsException("Otro conductor ya tiene la licencia " + license);
        }

        driver.setName(name.trim());
        driver.setLicenseNumber(license);
        driver.setPhone(cleanOptional(phone));
        driver.setEmail(cleanOptional(email));
        saveChanges(driver);
        return driver;
    }

    /** Activa o desactiva un conductor (en vez de borrarlo). */
    public Driver setDriverActive(int driverId, boolean active) throws LogisticsException {
        Driver driver = findDriver(driverId);
        if (driver.isActive() == active) {
            String state = active ? "activo" : "inactivo";
            throw new LogisticsException("El conductor ya está " + state);
        }
        driver.setActive(active);
        saveChanges(driver);
        return driver;
    }

    // ===================== ELIMINAR =====================

    public void deleteDriver(int driverId) throws LogisticsException {
        findDriver(driverId);
        try {
            driverDao.deleteDriver(driverId);
        } catch (RuntimeException e) {
            throw LogisticsException.fromDatabase("eliminar el conductor (puede tener asignaciones, mejor desactívelo)", e);
        }
    }

    // ===================== MÉTODOS PRIVADOS =====================

    private void saveChanges(Driver driver) throws LogisticsException {
        try {
            driverDao.updateDriver(driver);
        } catch (RuntimeException e) {
            throw LogisticsException.fromDatabase("actualizar el conductor", e);
        }
    }

    private void validateDriverData(String name, String licenseNumber, String phone, String email) throws LogisticsException {
        if (isBlank(name)) {
            throw new LogisticsException("El nombre es obligatorio");
        }
        if (isBlank(licenseNumber)) {
            throw new LogisticsException("El número de licencia es obligatorio");
        }
        // Teléfono y email son opcionales, pero si los escriben deben ser válidos.
        if (!isBlank(phone) && !isValidPhone(phone.trim())) {
            throw new LogisticsException("El teléfono solo puede tener números y mínimo " + MIN_PHONE_LENGTH + " dígitos");
        }
        if (!isBlank(email) && !isValidEmail(email.trim())) {
            throw new LogisticsException("El email no tiene un formato válido (ejemplo: nombre@correo.com)");
        }
    }

    private boolean isValidPhone(String phone) {
        if (phone.length() < MIN_PHONE_LENGTH) {
            return false;
        }
        for (int i = 0; i < phone.length(); i++) {
            if (!Character.isDigit(phone.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /** Validación sencilla: tiene una @ y después de la @ hay un punto. */
    private boolean isValidEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 0) {
            return false;
        }
        int dot = email.indexOf('.', at);
        return dot > at + 1 && dot < email.length() - 1;
    }

    private boolean licenseExists(String license, int ignoreId) throws LogisticsException {
        List<Driver> drivers = listDrivers();
        for (Driver d : drivers) {
            if (d.getDriverId() != ignoreId && license.equalsIgnoreCase(d.getLicenseNumber())) {
                return true;
            }
        }
        return false;
    }

    /** Convierte un texto vacío en null para guardarlo como NULL en la BD. */
    private String cleanOptional(String text) {
        if (isBlank(text)) {
            return null;
        }
        return text.trim();
    }

    private boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }
}
