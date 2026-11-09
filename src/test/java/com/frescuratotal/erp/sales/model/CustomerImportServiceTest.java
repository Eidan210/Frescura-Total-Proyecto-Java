package com.frescuratotal.erp.sales.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Exercise 4: mandatory test cases of the guide. */
class CustomerImportServiceTest {

    private static final String HEADER = "nombre;documento;correo;telefono;tipo_cliente";
    private static final String OMEGA = "Distribuidora Omega;901234567;contacto@omega.com;3001234567;MAYORISTA";
    private static final String PARQUE = "Tienda El Parque;1020304050;tienda@parque.com;3109876543;MINORISTA";

    @TempDir
    Path folder;

    private FakeCustomerDao dao;
    private List<Customer> committed;
    private CustomerImportService service;

    @BeforeEach
    void setUp() {
        dao = new FakeCustomerDao();
        committed = new ArrayList<>();
        // Fake transaction: what was saved only "stays" if no exception happened
        service = new CustomerImportService(dao, work -> {
            dao.saved.clear();
            work.run();
            committed.addAll(dao.saved);
        });
    }

    @Test
    void missingFile() {
        CustomerImportResult result = service.importFrom(folder.resolve("clientes_importar.txt"));

        assertEquals(CustomerImportResult.Status.FILE_NOT_FOUND, result.status());
        assertTrue(committed.isEmpty());
    }

    @Test
    void wrongHeaderCancels() throws IOException {
        CustomerImportResult result = run("nombre;documento;email;telefono;tipo", OMEGA);

        assertEquals(CustomerImportResult.Status.INVALID_RECORDS, result.status());
        assertTrue(committed.isEmpty());
    }

    @Test
    void lineWithFourFieldsCancelsEverything() throws IOException {
        CustomerImportResult result = run(HEADER, OMEGA, "Tienda El Parque;1020304050;tienda@parque.com;3109876543");

        assertEquals(CustomerImportResult.Status.INVALID_RECORDS, result.status());
        assertTrue(committed.isEmpty());
        assertTrue(dao.saved.isEmpty(), "nothing is inserted while there are invalid lines");
    }

    @Test
    void documentRepeatedInTheFileCancels() throws IOException {
        CustomerImportResult result = run(HEADER, OMEGA, "Otra Omega;901234567;otra@omega.com;300;MINORISTA");

        assertEquals(CustomerImportResult.Status.INVALID_RECORDS, result.status());
        assertTrue(committed.isEmpty());
    }

    @Test
    void documentAlreadyInTheDatabaseCancels() throws IOException {
        dao.add(1, "Cliente existente", "1020304050");

        CustomerImportResult result = run(HEADER, OMEGA, PARQUE);

        assertEquals(CustomerImportResult.Status.INVALID_RECORDS, result.status());
        assertTrue(committed.isEmpty());
    }

    @Test
    void allValidInsertsEveryCustomer() throws IOException {
        CustomerImportResult result = run(HEADER, OMEGA, PARQUE,
                "Supermercado Central;800456789;ventas@central.com;3205551234;MAYORISTA",
                "Restaurante Primavera;1080998877;admin@primavera.com;3157891234;MINORISTA",
                "Minimercado Sol;1099887766;info@minisol.com;3014567890;MINORISTA");

        assertEquals(CustomerImportResult.Status.COMPLETED, result.status());
        assertEquals(5, result.importedCount());
        assertEquals(5, committed.size());
        assertEquals(CustomerType.WHOLESALE, committed.get(0).getType());
        assertEquals(CustomerType.RETAIL, committed.get(1).getType());
    }

    @Test
    void emptyLineInTheMiddleIsIgnored() throws IOException {
        CustomerImportResult result = run(HEADER, OMEGA, "", "   ", PARQUE);

        assertEquals(CustomerImportResult.Status.COMPLETED, result.status());
        assertEquals(2, committed.size());
    }

    @Test
    void invalidEmailCancels() throws IOException {
        assertEquals(CustomerImportResult.Status.INVALID_RECORDS,
                run(HEADER, "Tienda;123;correo-sin-arroba.com;300;MINORISTA").status());
    }

    @Test
    void customerTypeOtherThanMayoristaOrMinoristaCancels() throws IOException {
        assertEquals(CustomerImportResult.Status.INVALID_RECORDS,
                run(HEADER, "Tienda;123;a@b.com;300;DISTRIBUIDOR").status());
    }

    @Test
    void emptyFieldCancels() throws IOException {
        assertEquals(CustomerImportResult.Status.INVALID_RECORDS,
                run(HEADER, "Tienda;;a@b.com;300;MINORISTA").status());
    }

    @Test
    void failedInsertSavesNothing() throws IOException {
        dao.failOnDocument = "1020304050"; // the second insert fails

        CustomerImportResult result = run(HEADER, OMEGA, PARQUE);

        assertEquals(CustomerImportResult.Status.SAVE_FAILED, result.status());
        assertTrue(committed.isEmpty());
    }

    private CustomerImportResult run(String... lines) throws IOException {
        Path file = folder.resolve("clientes_importar.txt");
        Files.write(file, List.of(lines), StandardCharsets.UTF_8);
        return service.importFrom(file);
    }
}
