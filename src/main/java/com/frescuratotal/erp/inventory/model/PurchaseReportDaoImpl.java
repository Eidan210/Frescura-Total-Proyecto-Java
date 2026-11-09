package com.frescuratotal.erp.inventory.model;
import com.frescuratotal.erp.common.DatabaseConnection;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PurchaseReportDaoImpl implements PurchaseReportDao{


    @Override
    public List<PurchaseRecord> findPurchasesByDate(LocalDate date) {
        String sql = """
            SELECT li.id_lote, p.nombre, li.cantidad_inicial * li.precio_compra AS monto_total
            FROM lotes_inventario li
            JOIN proveedores p ON li.id_proveedor = p.id_proveedor
            WHERE DATE(li.fecha_ingreso) = ?
            ORDER BY li.id_lote
            """;

        List<PurchaseRecord> purchases = new ArrayList<>();
        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                while(rs.next()) {
                    purchases.add(new PurchaseRecord(
                            rs.getInt("id_lote"),
                            rs.getString("nombre"),
                            rs.getBigDecimal("monto_total")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error searching purchases", e);
        }
        return purchases;
    }
}
