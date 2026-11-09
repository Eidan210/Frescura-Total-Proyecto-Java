package com.frescuratotal.erp.sales.model;

import com.frescuratotal.erp.common.DatabaseConnection;

import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Imports customers from clientes_importar.txt (Exercise 4).
 *
 * Two phases: first every line is checked; only if the whole file is valid the customers are
 * saved, all of them inside one transaction. Any error (in a line or in an insert) cancels
 * everything, so the table never keeps half an import.
 */
public class CustomerImportService {

    public static final String FILE_NAME = "clientes_importar.txt";
    public static final String HEADER = "nombre;documento;correo;telefono;tipo_cliente";

    private static final int FIELD_COUNT = 5;
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$");
    private static final Map<String, CustomerType> TYPES = Map.of(
            "MAYORISTA", CustomerType.WHOLESALE,
            "MINORISTA", CustomerType.RETAIL);

    // Column sizes of the clientes table: a longer value would make MySQL reject the insert
    private static final int MAX_NAME = 255;
    private static final int MAX_DOCUMENT = 20;
    private static final int MAX_EMAIL = 100;
    private static final int MAX_PHONE = 20;

    private final CustomerDao customerDao;
    private final Consumer<Runnable> transaction;

    public CustomerImportService(CustomerDao customerDao) {
        this(customerDao, work -> DatabaseConnection.getInstance().inTransaction(work));
    }

    /** The tests pass their own "transaction" to check that a failed insert saves nothing. */
    public CustomerImportService(CustomerDao customerDao, Consumer<Runnable> transaction) {
        this.customerDao = customerDao;
        this.transaction = transaction;
    }

    /** The file lives in the project root, next to README.md (the folder the program runs from). */
    public CustomerImportResult importDefaultFile() {
        return importFrom(Path.of(FILE_NAME));
    }

    public CustomerImportResult importFrom(Path file) {
        if (!Files.isRegularFile(file)) {
            return CustomerImportResult.fileNotFound();
        }

        List<String> lines;
        try {
            lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (MalformedInputException e) {
            return CustomerImportResult.invalid(List.of("The file is not saved as UTF-8."));
        } catch (IOException e) {
            return CustomerImportResult.invalid(List.of("The file could not be read: " + e.getMessage()));
        }

        // Phase 1: validate everything, nothing is saved yet
        List<String> errors = new ArrayList<>();
        List<Customer> customers = parse(lines, errors);
        if (!errors.isEmpty()) {
            return CustomerImportResult.invalid(errors);
        }
        if (customers.isEmpty()) {
            return CustomerImportResult.invalid(List.of("The file has no customers after the header."));
        }

        // Phase 2: all or nothing
        try {
            transaction.accept(() -> customers.forEach(this::insert));
        } catch (RuntimeException e) {
            return CustomerImportResult.saveFailed(rootMessage(e));
        }
        return CustomerImportResult.completed(customers.size());
    }

    private void insert(Customer customer) {
        if (!customerDao.save(customer)) {
            throw new IllegalStateException("Customer " + customer.getDocument() + " was not saved");
        }
    }

    private List<Customer> parse(List<String> lines, List<String> errors) {
        List<Customer> customers = new ArrayList<>();
        if (lines.isEmpty() || !HEADER.equals(stripBom(lines.get(0)))) {
            errors.add("Line 1: the header must be exactly " + HEADER);
            return customers;
        }

        // One query for every document already in the database
        Set<String> documentsInDatabase = customerDao.findAll().stream()
                .map(Customer::getDocument)
                .collect(Collectors.toSet());
        Set<String> documentsInFile = new HashSet<>();

        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank()) {
                continue; // empty lines are ignored
            }
            int lineNumber = i + 1;
            int errorsBefore = errors.size();
            Customer customer = parseLine(line, lineNumber, errors);
            if (customer == null) {
                continue;
            }

            String document = customer.getDocument();
            if (documentsInDatabase.contains(document)) {
                errors.add("Line " + lineNumber + ": document " + document + " is already registered.");
            }
            if (!documentsInFile.add(document)) {
                errors.add("Line " + lineNumber + ": document " + document + " is repeated in the file.");
            }
            if (errors.size() == errorsBefore) {
                customers.add(customer);
            }
        }
        return customers;
    }

    /** Returns null when the line cannot even be split in five fields. */
    private Customer parseLine(String line, int lineNumber, List<String> errors) {
        String[] fields = line.split(";", -1); // -1 keeps empty fields at the end
        if (fields.length != FIELD_COUNT) {
            errors.add("Line " + lineNumber + ": expected " + FIELD_COUNT + " fields and found " + fields.length + ".");
            return null;
        }
        for (int f = 0; f < fields.length; f++) {
            fields[f] = fields[f].trim();
        }

        String name = fields[0];
        String document = fields[1];
        String email = fields[2];
        String phone = fields[3];
        String type = fields[4];

        String prefix = "Line " + lineNumber + ": ";
        int errorsBefore = errors.size();
        String[] headers = HEADER.split(";");
        for (int f = 0; f < fields.length; f++) {
            if (fields[f].isEmpty()) {
                errors.add(prefix + "the field " + headers[f] + " is empty.");
            }
        }
        if (errors.size() > errorsBefore) {
            return null;
        }

        checkLength(name, MAX_NAME, "nombre", prefix, errors);
        checkLength(document, MAX_DOCUMENT, "documento", prefix, errors);
        checkLength(email, MAX_EMAIL, "correo", prefix, errors);
        checkLength(phone, MAX_PHONE, "telefono", prefix, errors);

        if (!EMAIL.matcher(email).matches()) {
            errors.add(prefix + "'" + email + "' is not a valid email.");
        }
        CustomerType customerType = TYPES.get(type);
        if (customerType == null) {
            errors.add(prefix + "tipo_cliente must be MAYORISTA or MINORISTA, not '" + type + "'.");
        }

        // The file has no address: it stays empty until someone updates the customer
        return new Customer(name, document, email, phone, null, customerType);
    }

    private static void checkLength(String value, int max, String field, String prefix, List<String> errors) {
        if (value.length() > max) {
            errors.add(prefix + field + " is longer than " + max + " characters.");
        }
    }

    /** The DAO wraps the SQLException: its message (e.g. a duplicate key) is the useful one. */
    private static String rootMessage(Throwable e) {
        Throwable cause = e;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause.getMessage();
    }

    /** Some editors (Notepad) start UTF-8 files with an invisible BOM character. */
    private static String stripBom(String line) {
        return line.startsWith("﻿") ? line.substring(1) : line;
    }
}
