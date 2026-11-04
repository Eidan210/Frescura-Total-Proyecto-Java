package com.frescuratotal.erp.logistics.Dao.Impl;

import com.frescuratotal.erp.common.DatabaseConnection;
import com.frescuratotal.erp.logistics.Dao.RouteSaleDao;
import com.frescuratotal.erp.logistics.model.DeliveryRoute;
import com.frescuratotal.erp.logistics.model.RouteSale;
import com.frescuratotal.erp.sales.model.Sale;
import com.frescuratotal.erp.sales.model.SaleStatus;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RouteSaleDaoimpl implements RouteSaleDao {

    private static final String SELECT_BASE =
            "SELECT vr.id, vr.id_ruta, vr.id_venta, vr.orden_entrega, vr.direccion_destino, vr.fecha_entrega, "
                    + "vr.recibido_por, vr.observaciones, v.numero_venta, v.id_estado "
                    + "FROM ventas_ruta vr INNER JOIN ventas v ON v.id_venta = vr.id_venta";

    // The connection is a shared singleton: it must not be closed here.
    private Connection getConnection() {
        return DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public void saveRouteSale(RouteSale routeSale) {
        String sql = "INSERT INTO ventas_ruta (id_ruta, id_venta, orden_entrega, direccion_destino, fecha_entrega, "
                + "recibido_por, observaciones) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, routeSale.getRoute().getRouteId());
            stmt.setInt(2, routeSale.getSale().getId());
            stmt.setInt(3, routeSale.getDeliveryOrder());
            stmt.setString(4, routeSale.getDestinationAddress());
            stmt.setTimestamp(5, toTimestamp(routeSale.getDeliveryDate()));
            stmt.setString(6, routeSale.getReceivedBy());
            stmt.setString(7, routeSale.getNotes());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    routeSale.setRouteSaleId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving sale " + routeSale.getSale().getId() + " in the route", e);
        }
    }

    @Override
    public Optional<RouteSale> findRouteSaleById(int routeSaleId) {
        List<RouteSale> result = findList(SELECT_BASE + " WHERE vr.id = ?", routeSaleId,
                "Error searching route sale with id " + routeSaleId);
        if (result.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(result.get(0));
    }

    @Override
    public List<RouteSale> findRouteSalesByRoute(int routeId) {
        return findList(SELECT_BASE + " WHERE vr.id_ruta = ? ORDER BY vr.orden_entrega", routeId,
                "Error searching the sales of route " + routeId);
    }

    @Override
    public List<RouteSale> findRouteSalesBySale(int saleId) {
        return findList(SELECT_BASE + " WHERE vr.id_venta = ?", saleId,
                "Error searching the routes of sale " + saleId);
    }

    @Override
    public void updateRouteSale(RouteSale routeSale) {
        String sql = "UPDATE ventas_ruta SET id_ruta = ?, id_venta = ?, orden_entrega = ?, direccion_destino = ?, "
                + "fecha_entrega = ?, recibido_por = ?, observaciones = ? WHERE id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, routeSale.getRoute().getRouteId());
            stmt.setInt(2, routeSale.getSale().getId());
            stmt.setInt(3, routeSale.getDeliveryOrder());
            stmt.setString(4, routeSale.getDestinationAddress());
            stmt.setTimestamp(5, toTimestamp(routeSale.getDeliveryDate()));
            stmt.setString(6, routeSale.getReceivedBy());
            stmt.setString(7, routeSale.getNotes());
            stmt.setInt(8, routeSale.getRouteSaleId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating route sale with id " + routeSale.getRouteSaleId(), e);
        }
    }

    @Override
    public void deleteRouteSale(int routeSaleId) {
        String sql = "DELETE FROM ventas_ruta WHERE id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, routeSaleId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting route sale with id " + routeSaleId, e);
        }
    }

    private List<RouteSale> findList(String sql, int id, String errorMessage) {
        List<RouteSale> routeSales = new ArrayList<>();
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    routeSales.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(errorMessage, e);
        }
        return routeSales;
    }

    // The route only carries its id; the sale carries id, number and status from the JOIN.
    private RouteSale mapRow(ResultSet rs) throws SQLException {
        DeliveryRoute route = new DeliveryRoute();
        route.setRouteId(rs.getInt("id_ruta"));

        Sale sale = new Sale();
        sale.setId(rs.getInt("id_venta"));
        sale.setSaleNumber(rs.getString("numero_venta"));
        sale.setSaleStatus(SaleStatus.fromId(rs.getInt("id_estado")));

        RouteSale routeSale = new RouteSale();
        routeSale.setRouteSaleId(rs.getInt("id"));
        routeSale.setRoute(route);
        routeSale.setSale(sale);
        routeSale.setDeliveryOrder(rs.getInt("orden_entrega"));
        routeSale.setDestinationAddress(rs.getString("direccion_destino"));
        routeSale.setDeliveryDate(toLocalDateTime(rs.getTimestamp("fecha_entrega")));
        routeSale.setReceivedBy(rs.getString("recibido_por"));
        routeSale.setNotes(rs.getString("observaciones"));
        return routeSale;
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