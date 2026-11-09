package com.frescuratotal.erp.sales.model.stats;

import com.frescuratotal.erp.common.DatabaseConnection;
import com.frescuratotal.erp.sales.model.SaleStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SaleStatisticsDaoImpl implements SaleStatisticsDao {

    /**
     * A sale detail points to a lot, and the lot to its product: that is the way to the category.
     * venta -> detalles_venta -> lotes_inventario -> productos -> categorias_producto, plus clientes.
     */
    @Override
    public List<SaleStatisticsRow> findAllRows() {
        String sql = """
                SELECT v.id_venta,
                       d.cantidad_vendida,
                       cat.nombre AS categoria,
                       c.nombre   AS cliente,
                       v.id_estado
                FROM ventas v
                INNER JOIN detalles_venta d        ON d.id_venta = v.id_venta
                INNER JOIN lotes_inventario l      ON l.id_lote = d.id_lote
                INNER JOIN productos p             ON p.id_producto = l.id_producto
                INNER JOIN categorias_producto cat ON cat.id_categoria = p.id_categoria
                INNER JOIN clientes c              ON c.id_cliente = v.id_cliente""";

        List<SaleStatisticsRow> rows = new ArrayList<>();
        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                rows.add(new SaleStatisticsRow(
                        rs.getInt("id_venta"),
                        rs.getBigDecimal("cantidad_vendida"),
                        rs.getString("categoria"),
                        rs.getString("cliente"),
                        SaleStatus.fromId(rs.getInt("id_estado"))));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error reading the sales for the statistics", e);
        }
        return rows;
    }
}
