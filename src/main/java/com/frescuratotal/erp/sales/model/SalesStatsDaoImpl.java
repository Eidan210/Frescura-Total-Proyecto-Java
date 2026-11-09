package com.frescuratotal.erp.sales.model;

import com.frescuratotal.erp.common.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SalesStatsDaoImpl implements SalesStatsDao {

    /*
     * EJERCICIO 5 - JOIN de 6 tablas.
     * OJO con el camino: detalles_venta NO tiene id_producto, tiene id_lote.
     *   ventas -> detalles_venta -> lotes_inventario -> productos -> categorias_producto
     *   ventas -> clientes
     * Alias (v, dv, l...) para escribir menos; "AS" para que el nombre de la columna sea claro.
     * El SQL solo TRAE los datos; filtrar, agrupar y sumar se hace en Java con Streams (requerimiento 5).
     */
    private static final String SQL = """
            SELECT v.id_venta,
                   dv.cantidad_vendida,
                   c.nombre  AS categoria,
                   cl.nombre AS cliente,
                   v.id_estado
            FROM ventas v
            JOIN detalles_venta dv      ON dv.id_venta = v.id_venta
            JOIN lotes_inventario l     ON l.id_lote = dv.id_lote
            JOIN productos p            ON p.id_producto = l.id_producto
            JOIN categorias_producto c  ON c.id_categoria = p.id_categoria
            JOIN clientes cl            ON cl.id_cliente = v.id_cliente
            """;

    @Override
    public List<SaleStatRow> findSaleDetailsForStats() {
        List<SaleStatRow> rows = new ArrayList<>();
        Connection conn = DatabaseConnection.getInstance().getConnection();

        // try-with-resources: cierra PreparedStatement y ResultSet solos al terminar.
        try (PreparedStatement ps = conn.prepareStatement(SQL);
             ResultSet rs = ps.executeQuery()) {

            // Mientras haya filas: convierto cada fila en un objeto Java (requerimiento 2).
            // Los nombres van entre comillas como están en el SELECT (columna o alias), NO como variables Java.
            while (rs.next()) {
                rows.add(new SaleStatRow(
                        rs.getInt("id_venta"),
                        rs.getBigDecimal("cantidad_vendida"),
                        rs.getString("categoria"),
                        rs.getString("cliente"),
                        SaleStatus.fromId(rs.getInt("id_estado"))
                ));
            }
            return rows;
        } catch (SQLException e) {
            throw new RuntimeException("Error loading sales statistics", e);
        }
    }
}
