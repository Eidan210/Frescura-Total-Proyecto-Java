package com.frescuratotal.erp.suppliers.model;

import com.frescuratotal.erp.common.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PurchaseReportDaoImpl implements PurchaseReportDao {

    /**
     * From 00:00 of the day to 00:00 of the next one: earlier days stay out and the
     * index on fecha_ingreso can still be used (DATE(fecha_ingreso) = ? could not use it).
     */
    @Override
    public List<PurchaseRecord> findPurchasesByDate(LocalDate date) {
        String sql = """
                SELECT l.id_lote,
                       p.nombre AS proveedor,
                       l.cantidad_inicial * l.precio_compra AS monto_total
                FROM lotes_inventario l
                INNER JOIN proveedores p ON p.id_proveedor = l.id_proveedor
                WHERE l.fecha_ingreso >= ? AND l.fecha_ingreso < ?
                ORDER BY l.id_lote""";

        List<PurchaseRecord> purchases = new ArrayList<>();
        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(date.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(date.plusDays(1).atStartOfDay()));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    purchases.add(new PurchaseRecord(
                            rs.getInt("id_lote"),
                            rs.getString("proveedor"),
                            rs.getBigDecimal("monto_total")));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error reading the purchases of " + date, e);
        }
        return purchases;
    }
}
