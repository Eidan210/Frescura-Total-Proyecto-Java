package com.frescuratotal.erp.sales.model;

import java.util.List;
import java.util.Optional;

public interface CustomerDao {
    boolean save(Customer customer);
    Optional<Customer> findById(int id);
    Optional<Customer> findByDocument(String document);
    List<Customer> findAll();
    boolean update(Customer customer);
    boolean delete(int id);



}
