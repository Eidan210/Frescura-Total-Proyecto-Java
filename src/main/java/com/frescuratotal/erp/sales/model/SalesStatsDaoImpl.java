package com.frescuratotal.erp.sales.model;

import com.frescuratotal.erp.common.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * EJERCICIO 5: consulta con varios JOIN.
 * La cadena es: venta -> detalles -> lote -> producto -> categoria, y venta -> cliente.
 * (En este ERP el detalle apunta al LOTE, y el lote es el que sabe de que producto es.)
 */
public class SalesStatsDaoImpl implements SalesStatsDao {

    @Override
    public List<SalesStatRow> findAllStatRows() {
        String sql = """
                SELECT v.id_venta, dv.cantidad_vendida, cat.nombre AS categoria,
                       cl.nombre AS cliente, v.id_estado
                FROM ventas v
                JOIN detalles_venta dv       ON dv.id_venta = v.id_venta
                JOIN lotes_inventario li     ON li.id_lote = dv.id_lote
                JOIN productos p             ON p.id_producto = li.id_producto
                JOIN categorias_producto cat ON cat.id_categoria = p.id_categoria
                JOIN clientes cl             ON cl.id_cliente = v.id_cliente
                """;

        List<SalesStatRow> rows = new ArrayList<>();
        Connection conn = DatabaseConnection.getInstance().getConnection();

        // Misma estructura de siempre: preparar -> ejecutar -> recorrer con while -> convertir en objeto
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add(new SalesStatRow(
                        rs.getInt("id_venta"),
                        rs.getBigDecimal("cantidad_vendida"),
                        rs.getString("categoria"),
                        rs.getString("cliente"),
                        SaleStatus.fromId(rs.getInt("id_estado"))   // el numero del estado -> enum
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error searching sales statistics", e);
        }
        return rows;
    }
}
