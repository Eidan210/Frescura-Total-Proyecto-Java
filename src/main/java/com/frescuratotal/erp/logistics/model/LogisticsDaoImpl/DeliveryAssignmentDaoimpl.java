package com.frescuratotal.erp.logistics.model.LogisticsDaoImpl;

import com.frescuratotal.erp.common.DatabaseConnection;
import com.frescuratotal.erp.logistics.model.DeliveryAssignment;
import com.frescuratotal.erp.logistics.model.DeliveryRoute;
import com.frescuratotal.erp.logistics.model.Driver;
import com.frescuratotal.erp.logistics.model.LogisticsDao.DeliveryAssignmentDao;
import com.frescuratotal.erp.logistics.model.Vehicle;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DeliveryAssignmentDaoimpl implements DeliveryAssignmentDao {

    private static final String SELECT_BASE =
            "SELECT id_asignacion, id_ruta, id_vehiculo, id_conductor, fecha_asignacion, usuario_asignacion, notas "
                    + "FROM asignaciones_entrega";

    // La conexión es un singleton compartido: no se cierra aquí.
    private Connection getConnection() {
        return DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public void saveAssignment(DeliveryAssignment assignment) {
        validateReferences(assignment);
        String sql = "INSERT INTO asignaciones_entrega (id_ruta, id_vehiculo, id_conductor, fecha_asignacion, "
                + "usuario_asignacion, notas) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (assignment.getAssignmentDate() == null) {
                assignment.setAssignmentDate(LocalDateTime.now());
            }
            stmt.setInt(1, assignment.getRoute().getRouteId());
            stmt.setInt(2, assignment.getVehicle().getVehicleId());
            stmt.setInt(3, assignment.getDriver().getDriverId());
            stmt.setTimestamp(4, toTimestamp(assignment.getAssignmentDate()));
            stmt.setString(5, assignment.getAssignedBy());
            stmt.setString(6, assignment.getNotes());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    assignment.setDeliveryAssignmentId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar la asignación de la ruta " + assignment.getRoute().getRouteId(), e);
        }
    }

    @Override
    public Optional<DeliveryAssignment> findAssignmentById(int assignmentId) {
        List<DeliveryAssignment> result = findList(SELECT_BASE + " WHERE id_asignacion = ?", assignmentId,
                "Error al buscar la asignación con id " + assignmentId);
        if (result.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(result.get(0));
    }

    @Override
    public List<DeliveryAssignment> findAllAssignments() {
        List<DeliveryAssignment> assignments = new ArrayList<>();
        try (PreparedStatement stmt = getConnection().prepareStatement(SELECT_BASE);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                assignments.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar las asignaciones de entrega", e);
        }
        return assignments;
    }

    @Override
    public List<DeliveryAssignment> findAssignmentsByRoute(int routeId) {
        return findList(SELECT_BASE + " WHERE id_ruta = ?", routeId,
                "Error al buscar asignaciones de la ruta " + routeId);
    }

    @Override
    public List<DeliveryAssignment> findAssignmentsByDriver(int driverId) {
        return findList(SELECT_BASE + " WHERE id_conductor = ?", driverId,
                "Error al buscar asignaciones del conductor " + driverId);
    }

    @Override
    public List<DeliveryAssignment> findAssignmentsByVehicle(int vehicleId) {
        return findList(SELECT_BASE + " WHERE id_vehiculo = ?", vehicleId,
                "Error al buscar asignaciones del vehículo " + vehicleId);
    }

    @Override
    public void updateAssignment(DeliveryAssignment assignment) {
        validateReferences(assignment);
        String sql = "UPDATE asignaciones_entrega SET id_ruta = ?, id_vehiculo = ?, id_conductor = ?, "
                + "fecha_asignacion = ?, usuario_asignacion = ?, notas = ? WHERE id_asignacion = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, assignment.getRoute().getRouteId());
            stmt.setInt(2, assignment.getVehicle().getVehicleId());
            stmt.setInt(3, assignment.getDriver().getDriverId());
            stmt.setTimestamp(4, toTimestamp(assignment.getAssignmentDate()));
            stmt.setString(5, assignment.getAssignedBy());
            stmt.setString(6, assignment.getNotes());
            stmt.setInt(7, assignment.getDeliveryAssignmentId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar la asignación con id " + assignment.getDeliveryAssignmentId(), e);
        }
    }

    @Override
    public void deleteAssignment(int assignmentId) {
        String sql = "DELETE FROM asignaciones_entrega WHERE id_asignacion = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, assignmentId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar la asignación con id " + assignmentId, e);
        }
    }

    private List<DeliveryAssignment> findList(String sql, int id, String errorMessage) {
        List<DeliveryAssignment> assignments = new ArrayList<>();
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    assignments.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(errorMessage, e);
        }
        return assignments;
    }

    // id_ruta, id_vehiculo e id_conductor son NOT NULL en la BD.
    private void validateReferences(DeliveryAssignment assignment) {
        if (assignment.getRoute() == null || assignment.getVehicle() == null || assignment.getDriver() == null) {
            throw new IllegalArgumentException("La asignación debe tener ruta, vehículo y conductor");
        }
    }

    // Ruta, vehículo y conductor se cargan solo con su id; usar sus DAOs si se necesitan completos.
    private DeliveryAssignment mapRow(ResultSet rs) throws SQLException {
        DeliveryRoute route = new DeliveryRoute();
        route.setRouteId(rs.getInt("id_ruta"));
        Vehicle vehicle = new Vehicle();
        vehicle.setVehicleId(rs.getInt("id_vehiculo"));
        Driver driver = new Driver();
        driver.setDriverId(rs.getInt("id_conductor"));

        DeliveryAssignment assignment = new DeliveryAssignment();
        assignment.setDeliveryAssignmentId(rs.getInt("id_asignacion"));
        assignment.setRoute(route);
        assignment.setVehicle(vehicle);
        assignment.setDriver(driver);
        assignment.setAssignmentDate(toLocalDateTime(rs.getTimestamp("fecha_asignacion")));
        assignment.setAssignedBy(rs.getString("usuario_asignacion"));
        assignment.setNotes(rs.getString("notas"));
        return assignment;
    }

    private Timestamp toTimestamp(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return Timestamp.valueOf(dateTime);
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toLocalDateTime();
    }
}
