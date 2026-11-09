
 package com.frescuratotal.erp.suppliers.model;

import com.frescuratotal.erp.common.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SupplierReportDaoImpl implements SupplierReportDao {
    @Override
    public List<SupplierLotRow> findAllLots() {
        String sql = """
                SELECT l.id_lote, l.cantidad_inicial, e.nombre AS estado_lote,
                       p.id_proveedor, p.nombre AS nombre_proveedor
                FROM lotes_inventario l
                INNER JOIN proveedores p ON l.id_proveedor = p.id_proveedor
                INNER JOIN estados_lote e ON l.id_estado = e.id_estado
                """;
        List<SupplierLotRow> rows = new ArrayList<>();
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add(new SupplierLotRow(
                        rs.getInt("id_lote"),
                        rs.getBigDecimal("cantidad_inicial"),
                        rs.getString("estado_lote"),
                        rs.getInt("id_proveedor"),
                        rs.getString("nombre_proveedor")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error searching lots", e);
        }
        return rows;
    }
}