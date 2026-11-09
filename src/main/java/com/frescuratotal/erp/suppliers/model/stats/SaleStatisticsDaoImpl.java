package com.frescuratotal.erp.suppliers.model.stats;

import com.frescuratotal.erp.common.DatabaseConnection;
import com.frescuratotal.erp.inventory.model.LotStatus;
import com.frescuratotal.erp.sales.model.SaleStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SupplierStatisticsDaoImpl implements SupplierStatisticsDao {

    /**
     * A sale detail points to a lot, and the lot to its product: that is the way to the category.
     * venta -> detalles_venta -> lotes_inventario -> productos -> categorias_producto, plus clientes.
     */
    @Override
    public List<SupplierStatisticsRow> findAllRows() {
        String sql = """
                SELECT
    l.id_lote AS id_lote ,
    l.cantidad_incial AS cantidad_incial,
    e.nombre AS id_estado,
                p.id_proveedor AS id_proveedor ,
    p.nombre AS nombre_proveedor
FROM lotes_inventario l
INNER JOIN proveedores p ON l.id_proevedor = p.id_proveedor
INNER JOIN estados_lote e ON l.id_esado = e.id_estado;""";

        List<SupplierStatisticsRow> rows = new ArrayList<>();
        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                rows.add(new SupplierStatisticsRow(
                        rs.getInt("id_lote "),
                        rs.getInt("cantidad_incial"),
                        LotStatus.fromId(rs.getInt("id_estado")),
                        rs.getInt("id_proveedor"),
                        rs.getString("nombre_proveedor")
                ));

            }
        } catch (SQLException e) {
            throw new RuntimeException("Error reading the sales for the statistics", e);
        }
        return rows;
    }
}
