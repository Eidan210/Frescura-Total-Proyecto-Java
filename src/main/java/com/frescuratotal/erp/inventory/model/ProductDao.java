package com.frescuratotal.erp.inventory.model;

import java.util.List;

public interface ProductDao {
    List<Product> findAll();
    Product findById(int productId);

}
