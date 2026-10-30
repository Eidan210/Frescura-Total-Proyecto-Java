package com.frescuratotal.erp.inventory.model;

import com.frescuratotal.erp.common.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LotDaoImpl implements LotDao {

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
}
