package com.frescuratotal.erp.sales.model;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In-memory cache of customers, keyed by their ID (Exercise 1).
 *
 * Singleton: the only instance is created when the class is loaded (thread safe without
 * synchronized) and the constructor is private, so CustomerService and SaleService always
 * share the same store. The map never leaves the class: it is read and written only through
 * the methods below, which hand out copies so a caller cannot change a cached customer.
 */
public final class CustomerCache {

    private static final CustomerCache INSTANCE = new CustomerCache();

    private final Map<Integer, Customer> customers = new ConcurrentHashMap<>();
    private final AtomicInteger hits = new AtomicInteger();
    private final AtomicInteger misses = new AtomicInteger();

    private CustomerCache() {
    }

    public static CustomerCache getInstance() {
        return INSTANCE;
    }

    /** Returns a copy of the cached customer, or empty if it was never loaded. */
    public Optional<Customer> get(int id) {
        Customer cached = customers.get(id);
        if (cached == null) {
            misses.incrementAndGet();
            return Optional.empty();
        }
        hits.incrementAndGet();
        return Optional.of(new Customer(cached));
    }

    /** Looks for an already loaded customer by document, so a sale by document does not repeat the SQL. */
    public Optional<Customer> getByDocument(String document) {
        if (document == null) {
            return Optional.empty();
        }
        Optional<Customer> found = customers.values().stream()
                .filter(customer -> document.equals(customer.getDocument()))
                .findFirst()
                .map(Customer::new);
        if (found.isPresent()) {
            hits.incrementAndGet();
        } else {
            misses.incrementAndGet();
        }
        return found;
    }

    /** Stores a copy. Null or incomplete customers are ignored, so the cache never holds bad data. */
    public void put(Customer customer) {
        if (!isValid(customer)) {
            return;
        }
        customers.put(customer.getId(), new Customer(customer));
    }

    /** Called after an update or delete, so the next read goes to the database again. */
    public void remove(int id) {
        customers.remove(id);
    }

    public boolean contains(int id) {
        return customers.containsKey(id);
    }

    public int size() {
        return customers.size();
    }

    public int getHits() {
        return hits.get();
    }

    public int getMisses() {
        return misses.get();
    }

    /** Empties the cache and its counters (used by the tests). */
    public void clear() {
        customers.clear();
        hits.set(0);
        misses.set(0);
    }

    private static boolean isValid(Customer customer) {
        return customer != null
                && customer.getId() > 0
                && customer.getName() != null && !customer.getName().isBlank()
                && customer.getDocument() != null && !customer.getDocument().isBlank();
    }
}
