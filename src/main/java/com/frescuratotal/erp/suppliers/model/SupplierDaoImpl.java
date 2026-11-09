package com.frescuratotal.erp.suppliers.model;

import com.frescuratotal.erp.common.DatabaseConnection;
import com.frescuratotal.erp.inventory.model.DailyPurchase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SupplierDaoImpl implements SupplierDao{

    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public boolean save(Supplier supplier) {
        String sql = "INSERT INTO proveedores (nombre, nit, persona_contacto, email, telefono, direccion, activo, fecha_registro) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            Connection conn = getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, supplier.getName());
                stmt.setString(2, supplier.getNit());
                stmt.setString(3, supplier.getContactPerson());
                stmt.setString(4, supplier.getEmail());
                stmt.setString(5, supplier.getPhone());
                stmt.setString(6, supplier.getAddress());
                stmt.setInt(7, supplier.getActive());
                stmt.setTimestamp(8, Timestamp.valueOf(
                        supplier.getRegistrationDate() != null ? supplier.getRegistrationDate() : LocalDateTime.now()));

                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return false;
        }
    }

    @Override
    public Optional<Supplier> findById(int id) {
        String sql = "SELECT id_proveedor, nombre, nit, persona_contacto, email, telefono, direccion, activo, fecha_registro FROM proveedores WHERE id_proveedor = ?";

        try {
            Connection conn = getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToSupplier(rs));
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Supplier> findAll() {
        List<Supplier> suppliers = new ArrayList<>();
        String sql = "SELECT id_proveedor, nombre, nit, persona_contacto, email, telefono, direccion, activo, fecha_registro FROM proveedores";

        try {
            Connection conn = getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    suppliers.add(mapResultSetToSupplier(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
        return suppliers;
    }

    @Override
    public boolean update(Supplier supplier) {
        String sql = "UPDATE proveedores SET nombre = ?, nit = ?, persona_contacto = ?, email = ?, telefono = ?, direccion = ?, activo = ? WHERE id_proveedor = ?";

        try {
            Connection conn = getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, supplier.getName());
                stmt.setString(2, supplier.getNit());
                stmt.setString(3, supplier.getContactPerson());
                stmt.setString(4, supplier.getEmail());
                stmt.setString(5, supplier.getPhone());
                stmt.setString(6, supplier.getAddress());
                stmt.setInt(7, supplier.getActive());
                stmt.setInt(8, supplier.getId());

                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM proveedores WHERE id_proveedor = ?";

        try {
            Connection conn = getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("Cannot delete: the supplier has registered lots or products.");
            return false;
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<SupplierData> stadistics() {
        String sql = """
                SELECT\s
                    l.id_lote,
                    l.cantidad_inicial,
                    e.nombre AS estado_lote,
                    p.id_proveedor,
                    p.nombre AS nombre_proveedor
                FROM lotes_inventario l
                INNER JOIN proveedores p ON l.id_proveedor = p.id_proveedor
                INNER JOIN estados_lote e ON l.id_estado = e.id_estado;
                """;

        List<SupplierData> lots = new ArrayList<>();
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lots.add(new SupplierData(
                        rs.getInt("id_lote"),
                        rs.getBigDecimal("cantidad_inicial"),
                        rs.getString("estado_lote"),
                        rs.getInt("id_proveedor"),
                        rs.getString("nombre_proveedor")));
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
        return lots;
    }

    private Supplier mapResultSetToSupplier(ResultSet rs) throws SQLException {
        Supplier supplier = new Supplier();
        supplier.setId(rs.getInt("id_proveedor"));
        supplier.setName(rs.getString("nombre"));
        supplier.setNit(rs.getString("nit"));
        supplier.setContactPerson(rs.getString("persona_contacto"));
        supplier.setEmail(rs.getString("email"));
        supplier.setPhone(rs.getString("telefono"));
        supplier.setAddress(rs.getString("direccion"));
        supplier.setActive(rs.getInt("activo"));

        Timestamp timestamp = rs.getTimestamp("fecha_registro");
        if (timestamp != null) {
            supplier.setRegistrationDate(timestamp.toLocalDateTime());
        }
        return supplier;
    }

}
