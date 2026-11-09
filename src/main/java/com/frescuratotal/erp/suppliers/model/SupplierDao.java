package com.frescuratotal.erp.suppliers.model;

import java.util.List;
import java.util.Optional;

public interface SupplierDao {
    boolean save(Supplier supplier);
    Optional<Supplier> findById(int id);
    List<Supplier> findAll();
    boolean update(Supplier supplier);
    boolean delete(int id);
    List<SupplierData> stadistics();
}
