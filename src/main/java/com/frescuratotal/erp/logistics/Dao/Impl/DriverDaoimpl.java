package com.frescuratotal.erp.logistics.Dao.Impl;

import com.frescuratotal.erp.common.DatabaseConnection;
import com.frescuratotal.erp.logistics.model.Driver;
import com.frescuratotal.erp.logistics.Dao.DriverDao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DriverDaoimpl implements DriverDao {

    private static final String SELECT_BASE =
            "SELECT id_conductor, nombre, numero_licencia, telefono, email, activo FROM conductores";

    // La conexión es un singleton compartido: no se cierra aquí.
    private Connection getConnection() {
        return DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public void saveDriver(Driver driver) {
        String sql = "INSERT INTO conductores (nombre, numero_licencia, telefono, email, activo) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, driver.getName());
            stmt.setString(2, driver.getLicenseNumber());
            stmt.setString(3, driver.getPhone());
            stmt.setString(4, driver.getEmail());
            stmt.setBoolean(5, driver.isActive());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    driver.setDriverId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar el conductor con licencia " + driver.getLicenseNumber(), e);
        }
    }

    @Override
    public Optional<Driver> findDriverById(int driverId) {
        String sql = SELECT_BASE + " WHERE id_conductor = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, driverId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar el conductor con id " + driverId, e);
        }
    }

    @Override
    public List<Driver> findAllDrivers() {
        return findList(SELECT_BASE, "Error al listar los conductores");
    }

    @Override
    public List<Driver> findActiveDrivers() {
        return findList(SELECT_BASE + " WHERE activo = TRUE", "Error al listar los conductores activos");
    }

    @Override
    public void updateDriver(Driver driver) {
        String sql = "UPDATE conductores SET nombre = ?, numero_licencia = ?, telefono = ?, email = ?, activo = ? WHERE id_conductor = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, driver.getName());
            stmt.setString(2, driver.getLicenseNumber());
            stmt.setString(3, driver.getPhone());
            stmt.setString(4, driver.getEmail());
            stmt.setBoolean(5, driver.isActive());
            stmt.setInt(6, driver.getDriverId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar el conductor con id " + driver.getDriverId(), e);
        }
    }

    @Override
    public void deleteDriver(int driverId) {
        String sql = "DELETE FROM conductores WHERE id_conductor = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, driverId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar el conductor con id " + driverId, e);
        }
    }

    private List<Driver> findList(String sql, String errorMessage) {
        List<Driver> drivers = new ArrayList<>();
        try (PreparedStatement stmt = getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                drivers.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(errorMessage, e);
        }
        return drivers;
    }

    private Driver mapRow(ResultSet rs) throws SQLException {
        Driver driver = new Driver();
        driver.setDriverId(rs.getInt("id_conductor"));
        driver.setName(rs.getString("nombre"));
        driver.setLicenseNumber(rs.getString("numero_licencia"));
        driver.setPhone(rs.getString("telefono"));
        driver.setEmail(rs.getString("email"));
        driver.setActive(rs.getBoolean("activo"));
        return driver;
    }
}
