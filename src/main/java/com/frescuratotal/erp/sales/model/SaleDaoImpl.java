package com.frescuratotal.erp.sales.model;

import com.frescuratotal.erp.common.DatabaseConnection;
import com.frescuratotal.erp.common.exception.InsufficientStockException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SaleDaoImpl implements SaleDao {

    /**
     * Saves the sale, its details and decreases each lot in ONE transaction:
     * either everything is saved (commit) or nothing is (rollback).
     */
    @Override
    public boolean save(Sale sale) {
        String saleSql = """
                INSERT INTO ventas (numero_venta, id_cliente, subtotal, total_descuento,
                                    total_impuesto, monto_total, id_estado, usuario_registro)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)""";

        String detailSql = """
                INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario,
                                            descuento_aplicado, monto_descuento, tasa_impuesto,
                                            monto_impuesto, subtotal, total_linea)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""";

        // "cantidad >= ?" protects us from selling more than the lot has right now
        String lotSql = "UPDATE lotes_inventario SET cantidad = cantidad - ? WHERE id_lote = ? AND cantidad >= ?";

        Connection conn = DatabaseConnection.getInstance().getConnection();

        try {
            conn.setAutoCommit(false); // 1. start the transaction

            // 2. Save the sale and get the id that MySQL generated (AUTO_INCREMENT)
            try (PreparedStatement ps = conn.prepareStatement(saleSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, sale.getSaleNumber());
                ps.setInt(2, sale.getCustomer().getId());
                ps.setBigDecimal(3, sale.getSubtotal());
                ps.setBigDecimal(4, sale.getTotalDiscount());
                ps.setBigDecimal(5, sale.getTotalTax());
                ps.setBigDecimal(6, sale.getTotalAmount());
                ps.setInt(7, sale.getSaleStatus().getId());
                ps.setString(8, sale.getRegisteredBy());
                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        sale.setId(keys.getInt(1));
                    }
                }
            }

            // 3. Save every detail and 4. decrease its lot
            try (PreparedStatement detailPs = conn.prepareStatement(detailSql);
                 PreparedStatement lotPs = conn.prepareStatement(lotSql)) {

                for (SaleDetail detail : sale.getSaleDetails()) {
                    detailPs.setInt(1, sale.getId());
                    detailPs.setInt(2, detail.getLot().getId());
                    detailPs.setBigDecimal(3, detail.getQuantitySold());
                    detailPs.setBigDecimal(4, detail.getUnitPrice());
                    detailPs.setBigDecimal(5, detail.getDiscountApplied());
                    detailPs.setBigDecimal(6, detail.getDiscountAmount());
                    detailPs.setBigDecimal(7, detail.getTaxRate());
                    detailPs.setBigDecimal(8, detail.getTaxAmount());
                    detailPs.setBigDecimal(9, detail.getSubtotal());
                    detailPs.setBigDecimal(10, detail.getLineTotal());
                    detailPs.executeUpdate();

                    lotPs.setBigDecimal(1, detail.getQuantitySold());
                    lotPs.setInt(2, detail.getLot().getId());
                    lotPs.setBigDecimal(3, detail.getQuantitySold());
                    if (lotPs.executeUpdate() == 0) {
                        throw new InsufficientStockException(
                                "Lot " + detail.getLot().getLotNumber() + " no longer has enough stock");
                    }
                }
            }

            conn.commit(); // 5. everything worked: save it for real
            return true;

        } catch (SQLException | RuntimeException e) {
            rollback(conn); // something failed: undo everything
            if (e instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new RuntimeException("Error saving sale ", e);
        } finally {
            restoreAutoCommit(conn);
        }
    }

    @Override
    public List<Sale> findAll() {
        String sql = """
                SELECT v.*, c.nombre, c.documento
                FROM ventas v
                INNER JOIN clientes c ON c.id_cliente = v.id_cliente
                ORDER BY v.fecha_venta DESC""";

        List<Sale> sales = new ArrayList<>();
        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Customer customer = new Customer();
                customer.setId(rs.getInt("id_cliente"));
                customer.setName(rs.getString("nombre"));
                customer.setDocument(rs.getString("documento"));

                Sale sale = new Sale();
                sale.setId(rs.getInt("id_venta"));
                sale.setSaleNumber(rs.getString("numero_venta"));
                sale.setCustomer(customer);
                sale.setSaleDate(rs.getTimestamp("fecha_venta").toLocalDateTime());
                sale.setSubtotal(rs.getBigDecimal("subtotal"));
                sale.setTotalDiscount(rs.getBigDecimal("total_descuento"));
                sale.setTotalTax(rs.getBigDecimal("total_impuesto"));
                sale.setTotalAmount(rs.getBigDecimal("monto_total"));
                sale.setSaleStatus(SaleStatus.fromId(rs.getInt("id_estado")));
                sale.setRegisteredBy(rs.getString("usuario_registro"));

                sales.add(sale);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error searching sales ", e);
        }
        return sales;
    }

    @Override
    public boolean updateStatus(int saleId, SaleStatus status) {
        String sql = "UPDATE ventas SET id_estado = ? WHERE id_venta = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, status.getId());
            ps.setInt(2, saleId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating sale status ", e);
        }
    }

    private void rollback(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException e) {
            throw new RuntimeException("Error rolling back sale ", e);
        }
    }

    private void restoreAutoCommit(Connection conn) {
        try {
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            throw new RuntimeException("Error restoring auto-commit ", e);
        }
    }
}
