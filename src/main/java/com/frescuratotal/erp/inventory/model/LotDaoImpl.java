package com.frescuratotal.erp.inventory.model;

import com.frescuratotal.erp.common.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LotDaoImpl implements LotDao {

    private Lot mapRowToLot(ResultSet rs) throws SQLException {
        Lot lot = new Lot();
        lot.setId(rs.getInt("id_lote"));
        lot.setLotNumber(rs.getString("numero_lote"));
        lot.setProductId(rs.getInt("id_producto"));
        lot.setSupplierId(rs.getInt("id_proveedor"));
        lot.setEntryDate(rs.getTimestamp("fecha_ingreso").toLocalDateTime());
        lot.setExpirationDate(rs.getDate("fecha_vencimiento").toLocalDate());
        lot.setInitialQuantity(rs.getBigDecimal("cantidad_inicial"));
        lot.setQuantity(rs.getBigDecimal("cantidad"));
        lot.setPurchasePrice(rs.getBigDecimal("precio_compra"));
        lot.setSalePrice(rs.getBigDecimal("precio_venta"));
        lot.setAisle(rs.getString("pasillo"));
        lot.setShelf(rs.getString("estante"));
        lot.setStatusId(rs.getInt("id_estado"));
        return lot;
    }

    @Override
    public List<Lot> findAvailableLots(int productId) {
        String sql = """
                    SELECT *
                    FROM lotes_inventario
                    WHERE id_producto = ? AND id_estado IN (1,2) AND cantidad > 0
                    ORDER BY fecha_vencimiento ASC""";

        List<Lot> lots = new ArrayList<>();

        Connection conn = DatabaseConnection.getInstance().getConnection();

        try(PreparedStatement ps = conn.prepareStatement(sql)){

            ps.setInt(1, productId);

            try (ResultSet rs = ps.executeQuery()){
                while (rs.next()) {
                    Lot lot = new Lot();

                    lot.setId(rs.getInt("id_lote"));
                    lot.setLotNumber(rs.getString("numero_lote"));
                    lot.setProductId(rs.getInt("id_producto"));
                    lot.setSupplierId(rs.getInt("id_proveedor"));
                    lot.setEntryDate(rs.getTimestamp("fecha_ingreso").toLocalDateTime());
                    lot.setExpirationDate(rs.getDate("fecha_vencimiento").toLocalDate());
                    lot.setInitialQuantity(rs.getBigDecimal("cantidad_inicial"));
                    lot.setQuantity(rs.getBigDecimal("cantidad"));
                    lot.setPurchasePrice(rs.getBigDecimal("precio_compra"));
                    lot.setSalePrice(rs.getBigDecimal("precio_venta"));
                    lot.setAisle(rs.getString("pasillo"));
                    lot.setShelf(rs.getString("estante"));
                    lot.setStatusId(rs.getInt("id_estado"));

                    lots.add(lot);
                }
            }

        } catch(SQLException e){
            throw new RuntimeException("Error trying to find available lots",e);
        }

        return lots;
    }

    @Override
    public void decreaseQuantity(int lotId, BigDecimal amount) {
        String sql = "UPDATE lotes_inventario SET cantidad = cantidad - ? WHERE id_lote = ?";

        Connection conn = DatabaseConnection.getInstance().getConnection();
        try(PreparedStatement ps = conn.prepareStatement(sql)){

            ps.setBigDecimal(1,amount);
            ps.setInt(2,lotId);

            ps.executeUpdate();

        }catch(SQLException e){
            throw new RuntimeException("Error decreasing lot quantity ",e);
        }

    }

    @Override
    public void save(Lot lot) {
        String sql = """
                INSERT INTO lotes_inventario
                (numero_lote, id_producto, id_proveedor, fecha_vencimiento, cantidad_inicial,
                 cantidad, precio_compra, precio_venta, pasillo, estante, id_estado)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        Connection conn = DatabaseConnection.getInstance().getConnection();

        try(PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setString(1, lot.getLotNumber());
            ps.setInt(2, lot.getProductId());
            ps.setInt(3, lot.getSupplierId());
            ps.setDate(4, Date.valueOf(lot.getExpirationDate()));
            ps.setBigDecimal(5, lot.getInitialQuantity());
            ps.setBigDecimal(6, lot.getQuantity());
            ps.setBigDecimal(7, lot.getPurchasePrice());
            ps.setBigDecimal(8, lot.getSalePrice());
            ps.setString(9, lot.getAisle());
            ps.setString(10, lot.getShelf());
            ps.setInt(11, lot.getStatusId());

            ps.executeUpdate();

        }catch(SQLException e){
            throw new RuntimeException("Error saving lot "+lot,e);
        }
    }

    @Override
    public Lot findById(int lotId) {
        String sql = """
                SELECT * FROM lotes_inventario WHERE id_lote = ?
                """;
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try(PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setInt(1, lotId);
            try (ResultSet rs = ps.executeQuery()){
                if  (rs.next()) {
                    return mapRowToLot(rs);
                }
                return null;
            }
        }catch(SQLException e){
            throw new RuntimeException("Error trying to find lot by id "+lotId,e);
        }
    }

    @Override
    public List<Lot> findAll() {
        String sql = "SELECT * FROM lotes_inventario ORDER BY fecha_vencimiento ASC";

        List<Lot> lots = new ArrayList<>();

        Connection conn = DatabaseConnection.getInstance().getConnection();

        try(PreparedStatement ps = conn.prepareStatement(sql)){
            try(ResultSet rs = ps.executeQuery()){
                while(rs.next()){
                    lots.add(mapRowToLot(rs));
                }
            }
        }catch (SQLException e){
            throw new RuntimeException("Error trying to find all lots",e);
        }
        return lots;
    }

    @Override
    public void updateStatus(int lotId, LotStatus status) {
        String sql = "UPDATE lotes_inventario SET id_estado = ? WHERE id_lote = ?";

        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setInt(1, status.getId());
            ps.setInt(2, lotId);

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error trying to update the status: "+lotId, e);
        }
    }
}
