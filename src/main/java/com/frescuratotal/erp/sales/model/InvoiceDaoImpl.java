package com.frescuratotal.erp.sales.model;

import com.frescuratotal.erp.common.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

public class InvoiceDaoImpl implements InvoiceDao {

    @Override
    public boolean save(Invoice invoice) {
        String sql = """
                INSERT INTO facturas (id_venta, numero_factura, cufe, enlace_qr, fecha_emision,
                                      subtotal, total_descuento, monto_impuesto, monto_total)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)""";

        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, invoice.getSale().getId());
            ps.setString(2, invoice.getInvoiceNumber());
            ps.setString(3, invoice.getCufe());
            ps.setString(4, invoice.getQrLink());
            ps.setTimestamp(5, Timestamp.valueOf(invoice.getIssueDate()));
            ps.setBigDecimal(6, invoice.getSubtotal());
            ps.setBigDecimal(7, invoice.getTotalDiscount());
            ps.setBigDecimal(8, invoice.getTaxAmount());
            ps.setBigDecimal(9, invoice.getTotalAmount());

            boolean saved = ps.executeUpdate() > 0;

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    invoice.setId(keys.getInt(1));
                }
            }
            return saved;

        } catch (SQLException e) {
            throw new RuntimeException("Error saving invoice ", e);
        }
    }

    @Override
    public CompanyInfo findCompanyInfo() {
        String sql = "SELECT * FROM informacion_empresa LIMIT 1";
        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                CompanyInfo company = new CompanyInfo();
                company.setId(rs.getInt("id_empresa"));
                company.setName(rs.getString("nombre_empresa"));
                company.setNit(rs.getString("nit"));
                company.setAddress(rs.getString("direccion"));
                company.setCity(rs.getString("ciudad"));
                company.setPhone(rs.getString("telefono"));
                company.setEmail(rs.getString("email"));
                return company;
            }
            throw new IllegalStateException("Company information is missing in table informacion_empresa");

        } catch (SQLException e) {
            throw new RuntimeException("Error searching company information ", e);
        }
    }
}
