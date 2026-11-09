package com.frescuratotal.erp.sales.model;

import java.util.List;
import java.util.Optional;

/**
 * Customer service with the cache in front of the database (Exercise 1).
 *
 * It implements CustomerDao, so the controllers that already received a CustomerDao
 * (sales and logistics) keep calling findById / findByDocument exactly as before: Main just
 * passes this service instead of the plain DAO. Whether the data comes from memory or from
 * SQL is decided here and nowhere else.
 */
public class CustomerService implements CustomerDao {

    private final CustomerDao customerDao;
    private final CustomerCache cache = CustomerCache.getInstance();

    public CustomerService(CustomerDao customerDao) {
        this.customerDao = customerDao;
    }

    /** First time: SQL and store. Next times: the copy in memory, without SQL. */
    @Override
    public Optional<Customer> findById(int id) {
        if (id <= 0) {
            return Optional.empty();
        }
        Optional<Customer> cached = cache.get(id);
        if (cached.isPresent()) {
            return cached;
        }
        Optional<Customer> fromDatabase = customerDao.findById(id);
        fromDatabase.ifPresent(cache::put); // a customer that does not exist is never stored
        return fromDatabase;
    }

    @Override
    public Optional<Customer> findByDocument(String document) {
        if (document == null || document.isBlank()) {
            return Optional.empty();
        }
        Optional<Customer> cached = cache.getByDocument(document);
        if (cached.isPresent()) {
            return cached;
        }
        Optional<Customer> fromDatabase = customerDao.findByDocument(document);
        fromDatabase.ifPresent(cache::put);
        return fromDatabase;
    }

    /** The full list always comes from the database: new customers would be missing from the cache. */
    @Override
    public List<Customer> findAll() {
        return customerDao.findAll();
    }

    @Override
    public boolean save(Customer customer) {
        return customerDao.save(customer);
    }

    @Override
    public boolean update(Customer customer) {
        boolean updated = customerDao.update(customer);
        cache.remove(customer.getId()); // the copy in memory is now old
        return updated;
    }

    @Override
    public boolean delete(int id) {
        boolean deleted = customerDao.delete(id);
        cache.remove(id);
        return deleted;
    }

    /** Same instance that SaleService uses. */
    public CustomerCache getCache() {
        return cache;
    }
}
