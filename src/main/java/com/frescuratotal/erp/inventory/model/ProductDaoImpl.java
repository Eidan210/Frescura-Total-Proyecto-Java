package com.frescuratotal.erp.inventory.model;

import com.frescuratotal.erp.common.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductDaoImpl implements ProductDao {
    private Product mapRowToProduct(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setId(rs.getInt("id_producto"));
        product.setName(rs.getString("nombre"));
        product.setDescription(rs.getString("descripcion"));
        product.setCategoryId(rs.getInt("id_categoria"));
        product.setUnitOfMeasure(rs.getString("unidad_medida"));
        product.setBasePrice(rs.getBigDecimal("precio_base"));
        return product;
    }

    @Override
    public List<Product> findAll() {
        String sql = "SELECT * FROM productos ORDER BY nombre ASC";

        List<Product> products = new ArrayList<>();

        Connection conn = DatabaseConnection.getInstance().getConnection();

        try(PreparedStatement ps = conn.prepareStatement(sql)){
            try(ResultSet rs = ps.executeQuery()){
                while(rs.next()){
                    products.add(mapRowToProduct(rs));
                }
            }
        }catch (SQLException e){
            throw new RuntimeException("Error trying to find all lots",e);
        }
        return products;
    }

    @Override
    public Product findById(int productId) {
        String sql = """
                SELECT * FROM productos WHERE id_producto = ?
                """;
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try(PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()){
                if  (rs.next()) {
                    return mapRowToProduct(rs);
                }
                return null;
            }
        }catch(SQLException e){
            throw new RuntimeException("Error trying to find lot by id "+productId,e);
        }
    }
}
