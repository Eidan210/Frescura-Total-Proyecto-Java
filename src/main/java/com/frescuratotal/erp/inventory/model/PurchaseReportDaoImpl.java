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

public class PurchaseReportDaoImpl implements PurchaseReportDao {

    /*
     * EJERCICIO 2 - Solo las compras (recepciones de lotes) de UNA fecha.
     * fecha_ingreso es DATETIME (fecha + hora). DATE(...) le quita la hora para comparar
     * solo el día: '2026-11-09 14:30:00' -> '2026-11-09'.
     * Así las compras de días anteriores NO entran (caso de prueba).
     */
    private static final String SQL = """
            SELECT l.numero_lote,
                   p.nombre AS proveedor,
                   l.cantidad_inicial * l.precio_compra AS monto_total
            FROM lotes_inventario l
            JOIN proveedores p ON p.id_proveedor = l.id_proveedor
            WHERE DATE(l.fecha_ingreso) = ?
            ORDER BY l.id_lote
            """;

    @Override
    public List<DailyPurchase> findPurchasesByDate(LocalDate date) {
        List<DailyPurchase> purchases = new ArrayList<>();
        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(SQL)) {
            // LocalDate (Java) -> java.sql.Date (JDBC) para el "?"
            ps.setDate(1, Date.valueOf(date));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    purchases.add(new DailyPurchase(
                            rs.getString("numero_lote"),
                            rs.getString("proveedor"),
                            rs.getBigDecimal("monto_total")));
                }
            }
            return purchases;
        } catch (SQLException e) {
            throw new RuntimeException("Error loading today's purchases", e);
        }
    }
}
