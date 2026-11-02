package com.frescuratotal.erp.sales.model;

import com.frescuratotal.erp.common.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
                    Customer customer = new Customer();
                    customer.setId(rs.getInt("id_cliente"));
                    customer.setName(rs.getString("nombre"));
                    customer.setDocument(rs.getString("documento"));
                    customer.setEmail(rs.getString("email"));
                    customer.setPhone(rs.getString("telefono"));
                    customer.setAddress(rs.getString("direccion"));
                    customer.setType(CustomerType.fromId(rs.getInt("id_tipo")));
                    customer.setRegistrationDate(rs.getTimestamp("fecha_registro").toLocalDateTime());

                    return Optional.of(customer);
                }
                return Optional.empty();
            }

        }catch(SQLException e){
            throw new RuntimeException("Error searching customer ",e);
        }

    }

    @Override
    public Optional<Customer> findByDocument(String document) {
        return Optional.empty();
    }

    @Override
    public List<Customer> findAll() {
        return List.of();
    }

    @Override
    public boolean update(Customer customer) {
        return false;
    }

    @Override
    public boolean delete(int id) {
        return false;
    }
}
