package com.frescuratotal.erp.sales.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Customer "table" in memory that counts how many SQL queries would have run. */
class FakeCustomerDao implements CustomerDao {

    final Map<Integer, Customer> table = new LinkedHashMap<>();
    final List<Customer> saved = new ArrayList<>();
    int findByIdQueries;
    int findByDocumentQueries;
    String failOnDocument; // simulates an insert that MySQL rejects

    void add(int id, String name, String document) {
        Customer customer = new Customer(name, document, "c" + id + "@mail.com", "300", "Calle " + id, CustomerType.RETAIL);
        customer.setId(id);
        table.put(id, customer);
    }

    @Override
    public Optional<Customer> findById(int id) {
        findByIdQueries++;
        return Optional.ofNullable(table.get(id)).map(Customer::new);
    }

    @Override
    public Optional<Customer> findByDocument(String document) {
        findByDocumentQueries++;
        return table.values().stream().filter(c -> c.getDocument().equals(document)).findFirst().map(Customer::new);
    }

    @Override
    public List<Customer> findAll() {
        return new ArrayList<>(table.values());
    }

    @Override
    public boolean save(Customer customer) {
        if (customer.getDocument().equals(failOnDocument)) {
            throw new RuntimeException("Duplicate entry for documento");
        }
        saved.add(customer);
        return true;
    }

    @Override
    public boolean update(Customer customer) {
        table.put(customer.getId(), new Customer(customer));
        return true;
    }

    @Override
    public boolean delete(int id) {
        return table.remove(id) != null;
    }
}
