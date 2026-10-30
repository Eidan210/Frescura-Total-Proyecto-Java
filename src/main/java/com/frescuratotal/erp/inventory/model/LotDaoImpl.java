package com.frescuratotal.erp.inventory.model;

import com.frescuratotal.erp.common.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class LotDaoImpl implements LotDao {

    @Override
    public List<Lot> findAvailableLots(int productId) {
        return List.of();
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
