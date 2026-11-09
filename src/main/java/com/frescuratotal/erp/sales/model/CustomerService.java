package com.frescuratotal.erp.sales.model;

import java.util.Optional;

public class CustomerService {
    private final CustomerCache customerCache = CustomerCache.getInstance();


    public Optional<Customer> findById(int id){
        return customerCache.findById(id);
    }
}
