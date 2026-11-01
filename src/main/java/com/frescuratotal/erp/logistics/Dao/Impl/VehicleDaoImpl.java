package com.frescuratotal.erp.logistics.Dao.Impl;

import com.frescuratotal.erp.common.DatabaseConnection;
import com.frescuratotal.erp.logistics.Dao.VehicleDao;
import com.frescuratotal.erp.logistics.model.Vehicle;
import com.frescuratotal.erp.logistics.model.status.VehicleStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VehicleDaoImpl implements VehicleDao {

    private static final String SELECT_BASE =
            "SELECT id_vehiculo, placa, modelo, capacidad, estado FROM vehiculos";

    // La conexión es un singleton compartido: no se cierra aquí.
    private Connection getConnection() {
        return DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public void saveVehicle(Vehicle vehicle) {
        String sql = "INSERT INTO vehiculos (placa, modelo, capacidad, estado) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, vehicle.getLicensePlate());
            stmt.setString(2, vehicle.getModel());
            stmt.setInt(3, vehicle.getCapacity());
            stmt.setString(4, vehicle.getStatus().getDbValue());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    vehicle.setVehicleId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar el vehículo con placa " + vehicle.getLicensePlate(), e);
        }
    }

    @Override
    public Optional<Vehicle> findVehicleById(int vehicleId) {
        String sql = SELECT_BASE + " WHERE id_vehiculo = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, vehicleId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar el vehículo con id " + vehicleId, e);
        }
    }

    @Override
    public List<Vehicle> findAllVehicles() {
        List<Vehicle> vehicles = new ArrayList<>();
        try (PreparedStatement stmt = getConnection().prepareStatement(SELECT_BASE);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                vehicles.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar los vehículos", e);
        }
        return vehicles;
    }

    @Override
    public List<Vehicle> findByStatus(VehicleStatus status) {
        List<Vehicle> vehicles = new ArrayList<>();
        String sql = SELECT_BASE + " WHERE estado = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, status.getDbValue());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    vehicles.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar vehículos con estado " + status, e);
        }
        return vehicles;
    }

    @Override
    public void updateVehicle(Vehicle vehicle) {
        String sql = "UPDATE vehiculos SET placa = ?, modelo = ?, capacidad = ?, estado = ? WHERE id_vehiculo = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, vehicle.getLicensePlate());
            stmt.setString(2, vehicle.getModel());
            stmt.setInt(3, vehicle.getCapacity());
            stmt.setString(4, vehicle.getStatus().getDbValue());
            stmt.setInt(5, vehicle.getVehicleId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar el vehículo con id " + vehicle.getVehicleId(), e);
        }
    }

    @Override
    public void deleteVehicle(int vehicleId) {
        String sql = "DELETE FROM vehiculos WHERE id_vehiculo = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, vehicleId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar el vehículo con id " + vehicleId, e);
        }
    }

    private Vehicle mapRow(ResultSet rs) throws SQLException {
        Vehicle vehicle = new Vehicle();
        vehicle.setVehicleId(rs.getInt("id_vehiculo"));
        vehicle.setLicensePlate(rs.getString("placa"));
        vehicle.setModel(rs.getString("modelo"));
        // capacidad es DECIMAL en la BD pero int en el modelo: se pierden los decimales.
        vehicle.setCapacity(rs.getInt("capacidad"));
        vehicle.setStatus(VehicleStatus.fromDbValue(rs.getString("estado")));
        return vehicle;
    }
}
