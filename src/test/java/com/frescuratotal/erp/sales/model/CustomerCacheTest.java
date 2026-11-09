package com.frescuratotal.erp.sales.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/** Exercise 1: mandatory test cases of the guide. */
class CustomerCacheTest {

    private FakeCustomerDao dao;
    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        CustomerCache.getInstance().clear(); // the singleton survives between tests
        dao = new FakeCustomerDao();
        dao.add(5, "Distribuidora Central", "900111");
        dao.add(8, "Tienda La Esquina", "900222");
        customerService = new CustomerService(dao);
    }

    @Test
    void firstSearchRunsSqlAndStoresTheCustomer() {
        Optional<Customer> customer = customerService.findById(5);

        assertEquals("Distribuidora Central", customer.orElseThrow().getName());
        assertEquals(1, dao.findByIdQueries);
        assertTrue(CustomerCache.getInstance().contains(5));
    }

    @Test
    void secondSearchOfTheSameIdDoesNotRunSql() {
        Customer first = customerService.findById(5).orElseThrow();
        Customer second = customerService.findById(5).orElseThrow();

        assertEquals(1, dao.findByIdQueries);
        assertEquals(first.getName(), second.getName());
        assertEquals(first.getDocument(), second.getDocument());
    }

    @Test
    void anotherIdRunsItsOwnSql() {
        customerService.findById(5);
        customerService.findById(8);

        assertEquals(2, dao.findByIdQueries);
        assertEquals(2, CustomerCache.getInstance().size());
    }

    @Test
    void saleServiceReusesTheSameCache() {
        customerService.findById(5);

        // Another module with its own service and DAO: it still finds ID 5 in the shared cache
        FakeCustomerDao otherDao = new FakeCustomerDao();
        SaleService saleService = new SaleService(new CustomerService(otherDao));
        Optional<Customer> customer = saleService.findCustomer(5);

        assertEquals("Distribuidora Central", customer.orElseThrow().getName());
        assertEquals(0, otherDao.findByIdQueries);
        assertSame(customerService.getCache(), saleService.getCustomerCache());
    }

    @Test
    void missingCustomerIsNotStoredAndLaterSearchesStillWork() {
        assertTrue(customerService.findById(999).isEmpty());
        assertFalse(CustomerCache.getInstance().contains(999));
        assertEquals(0, CustomerCache.getInstance().size());

        customerService.findById(999);
        assertEquals(2, dao.findByIdQueries); // not cached, so it is asked again

        assertTrue(customerService.findById(5).isPresent());
    }

    @Test
    void nullOrInvalidCustomersAreNeverStored() {
        CustomerCache cache = CustomerCache.getInstance();
        cache.put(null);
        cache.put(new Customer()); // id 0, no name, no document

        assertEquals(0, cache.size());
    }

    @Test
    void changingTheReturnedCopyDoesNotChangeTheCache() {
        customerService.findById(5).orElseThrow().setName("Changed");

        assertEquals("Distribuidora Central", customerService.findById(5).orElseThrow().getName());
    }

    @Test
    void searchByDocumentUsesTheCustomerAlreadyLoaded() {
        customerService.findById(5);

        assertTrue(customerService.findByDocument("900111").isPresent());
        assertEquals(0, dao.findByDocumentQueries);
    }

    @Test
    void updateRemovesTheOldCopy() {
        Customer customer = customerService.findById(5).orElseThrow();
        customer.setName("Distribuidora Central Norte");
        customerService.update(customer);

        assertEquals("Distribuidora Central Norte", customerService.findById(5).orElseThrow().getName());
        assertEquals(2, dao.findByIdQueries);
    }

    @Test
    void thereIsOnlyOneInstance() {
        assertSame(CustomerCache.getInstance(), CustomerCache.getInstance());
    }
}
