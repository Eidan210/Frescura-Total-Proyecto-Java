package com.frescuratotal.erp.sales.model;

import com.frescuratotal.erp.common.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomerDaoImpl implements CustomerDao {

    @Override
    public boolean save(Customer customer) {
        String sql = "INSERT INTO clientes ( nombre, documento, email, telefono,direccion,id_tipo) VALUES(?,?,?,?,?,?)";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try(PreparedStatement ps = conn.prepareStatement(sql)){

            ps.setString(1,customer.getName());
            ps.setString(2,customer.getDocument());
            ps.setString(3,customer.getEmail());
            ps.setString(4, customer.getPhone());
            ps.setString(5, customer.getAddress());
            ps.setInt(6,customer.getType().getId());

            /*if (ps.executeUpdate()>0){
                return true;
            }else{
                return false;
            } Simplified version below */

            return ps.executeUpdate() > 0;

        }catch(SQLException e){
            throw new RuntimeException("Error saving customer ",e);
        }
    }

    @Override
    public Optional<Customer> findById(int id) {
        String sql = "SELECT * FROM clientes WHERE id_cliente = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();

        try(PreparedStatement ps = conn.prepareStatement(sql)){

            ps.setInt(1,id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {

                    return Optional.of(mapCustomer(rs));
                }
                return Optional.empty();
            }

        }catch(SQLException e){
            throw new RuntimeException("Error searching customer ",e);
        }

    }

    @Override
    public Optional<Customer> findByDocument(String document) {
        String sql = "SELECT * FROM clientes WHERE documento = ?";

        Connection conn = DatabaseConnection.getInstance().getConnection();

        try(PreparedStatement ps = conn.prepareStatement(sql)){

            ps.setString(1,document);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {

                    return Optional.of(mapCustomer(rs));
                }
                return Optional.empty();
            }

        }catch(SQLException e){
            throw new RuntimeException("Error searching customer ",e);
        }


    }

    @Override
    public List<Customer> findAll() {
        String sql = "SELECT * FROM clientes";
        List<Customer> customers = new ArrayList<>();

        Connection conn = DatabaseConnection.getInstance().getConnection();

        try(PreparedStatement ps = conn.prepareStatement(sql)){

            try (ResultSet rs = ps.executeQuery()) {
                while(rs.next()) {

                    customers.add(mapCustomer(rs));
                }
            }

        }catch(SQLException e){
            throw new RuntimeException("Error searching customers ",e);
        }

        return customers;
    }

    @Override
    public boolean update(Customer customer) {
        String sql = "UPDATE clientes SET nombre = ?, documento = ?, email = ?, telefono = ?, direccion = ?, id_tipo = ? WHERE id_cliente = ?";

        Connection conn = DatabaseConnection.getInstance().getConnection();
        try(PreparedStatement ps = conn.prepareStatement(sql)){

            ps.setString(1,customer.getName());
            ps.setString(2,customer.getDocument());
            ps.setString(3,customer.getEmail());
            ps.setString(4, customer.getPhone());
            ps.setString(5, customer.getAddress());
            ps.setInt(6,customer.getType().getId());

            ps.setInt(7,customer.getId());

            return ps.executeUpdate() > 0;

        }catch(SQLException e){
            throw new RuntimeException("Error updating customer ",e);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM clientes WHERE id_cliente = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try(PreparedStatement ps = conn.prepareStatement(sql)){

            ps.setInt(1,id);

            return ps.executeUpdate() > 0;

        }catch(SQLException e){
            throw new RuntimeException("Error deleting customer ",e);
        }
    }

    private Customer mapCustomer(ResultSet rs) throws SQLException {

        Customer customer = new Customer();
        customer.setId(rs.getInt("id_cliente"));
        customer.setName(rs.getString("nombre"));
        customer.setDocument(rs.getString("documento"));
        customer.setEmail(rs.getString("email"));
        customer.setPhone(rs.getString("telefono"));
        customer.setAddress(rs.getString("direccion"));
        customer.setType(CustomerType.fromId(rs.getInt("id_tipo")));
        customer.setRegistrationDate(rs.getTimestamp("fecha_registro").toLocalDateTime());

        return customer;
    }
}
