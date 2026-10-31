package com.frescuratotal.erp.logistics.Dao.Impl;

import com.frescuratotal.erp.common.DatabaseConnection;
import com.frescuratotal.erp.logistics.model.DeliveryRoute;
import com.frescuratotal.erp.logistics.Dao.DeliveryRouteDao;
import com.frescuratotal.erp.logistics.model.status.DeliveryRouteStatus;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DeliveryRouteDaoimpl implements DeliveryRouteDao {

    private static final String SELECT_BASE =
            "SELECT id_ruta, nombre_ruta, fecha_creacion, usuario_creacion, hora_salida_estimada, "
                    + "hora_llegada_estimada, hora_salida_real, hora_llegada_real, estado FROM rutas_entrega";

    // La conexión es un singleton compartido: no se cierra aquí.
    private Connection getConnection() {
        return DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public void save(DeliveryRoute route) {
        String sql = "INSERT INTO rutas_entrega (nombre_ruta, fecha_creacion, usuario_creacion, hora_salida_estimada, "
                + "hora_llegada_estimada, hora_salida_real, hora_llegada_real, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (route.getCreatedAt() == null) {
                route.setCreatedAt(LocalDateTime.now());
            }
            stmt.setString(1, route.getRouteName());
            stmt.setTimestamp(2, toTimestamp(route.getCreatedAt()));
            stmt.setString(3, route.getCreatedBy());
            stmt.setTimestamp(4, toTimestamp(route.getEstimatedDepartureTime()));
            stmt.setTimestamp(5, toTimestamp(route.getEstimatedArrivalTime()));
            stmt.setTimestamp(6, toTimestamp(route.getActualDepartureTime()));
            stmt.setTimestamp(7, toTimestamp(route.getActualArrivalTime()));
            stmt.setString(8, route.getStatus().getDbValue());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    route.setRouteId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar la ruta " + route.getRouteName(), e);
        }
    }

    @Override
    public Optional<DeliveryRoute> findById(int routeId) {
        String sql = SELECT_BASE + " WHERE id_ruta = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, routeId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar la ruta con id " + routeId, e);
        }
    }

    @Override
    public List<DeliveryRoute> findAll() {
        List<DeliveryRoute> routes = new ArrayList<>();
        try (PreparedStatement stmt = getConnection().prepareStatement(SELECT_BASE);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                routes.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar las rutas de entrega", e);
        }
        return routes;
    }

    @Override
    public List<DeliveryRoute> findByStatus(DeliveryRouteStatus status) {
        List<DeliveryRoute> routes = new ArrayList<>();
        String sql = SELECT_BASE + " WHERE estado = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, status.getDbValue());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    routes.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar rutas con estado " + status, e);
        }
        return routes;
    }

    @Override
    public void update(DeliveryRoute route) {
        String sql = "UPDATE rutas_entrega SET nombre_ruta = ?, usuario_creacion = ?, hora_salida_estimada = ?, "
                + "hora_llegada_estimada = ?, hora_salida_real = ?, hora_llegada_real = ?, estado = ? WHERE id_ruta = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, route.getRouteName());
            stmt.setString(2, route.getCreatedBy());
            stmt.setTimestamp(3, toTimestamp(route.getEstimatedDepartureTime()));
            stmt.setTimestamp(4, toTimestamp(route.getEstimatedArrivalTime()));
            stmt.setTimestamp(5, toTimestamp(route.getActualDepartureTime()));
            stmt.setTimestamp(6, toTimestamp(route.getActualArrivalTime()));
            stmt.setString(7, route.getStatus().getDbValue());
            stmt.setInt(8, route.getRouteId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar la ruta con id " + route.getRouteId(), e);
        }
    }

    @Override
    public void delete(int routeId) {
        String sql = "DELETE FROM rutas_entrega WHERE id_ruta = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, routeId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar la ruta con id " + routeId, e);
        }
    }

    private DeliveryRoute mapRow(ResultSet rs) throws SQLException {
        DeliveryRoute route = new DeliveryRoute();
        route.setRouteId(rs.getInt("id_ruta"));
        route.setRouteName(rs.getString("nombre_ruta"));
        route.setCreatedAt(toLocalDateTime(rs.getTimestamp("fecha_creacion")));
        route.setCreatedBy(rs.getString("usuario_creacion"));
        route.setEstimatedDepartureTime(toLocalDateTime(rs.getTimestamp("hora_salida_estimada")));
        route.setEstimatedArrivalTime(toLocalDateTime(rs.getTimestamp("hora_llegada_estimada")));
        route.setActualDepartureTime(toLocalDateTime(rs.getTimestamp("hora_salida_real")));
        route.setActualArrivalTime(toLocalDateTime(rs.getTimestamp("hora_llegada_real")));
        route.setStatus(DeliveryRouteStatus.fromDbValue(rs.getString("estado")));
        return route;
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
