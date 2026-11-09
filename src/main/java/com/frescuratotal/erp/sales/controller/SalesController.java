package com.frescuratotal.erp.sales.controller;

import com.frescuratotal.erp.common.exception.InsufficientStockException;
import com.frescuratotal.erp.inventory.model.ProductDao;
import com.frescuratotal.erp.sales.model.*;
import com.frescuratotal.erp.sales.view.SalesView;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.Optional;

public class SalesController {
    private final SalesView view;
    private final CustomerDao customerDao;
    private final ProductDao productDao;
    private final SaleService service;
    private final CustomerImportService importService = new CustomerImportService();   // EJERCICIO 4
    private final SalesStatsService statsService = new SalesStatsService();            // EJERCICIO 5

    public SalesController(SalesView view, CustomerDao customerDao, ProductDao productDao, SaleService service) {
        this.view = view;
        this.customerDao = customerDao;
        this.productDao = productDao;
        this.service = service;
    }

    public void start() {
        int option = 0;
        do {
            // The DAOs wrap SQLException in RuntimeException, so a database failure arrives here.
            try {
                option = view.showMenu();
                switch (option) {
                    case 1 -> registerCustomer();
                    case 2 -> view.showCustomers(customerDao.findAll()); // primero trae los datos y luego la vista los muestra
                    case 3 -> newSale();
                    case 4 -> view.showSales(service.findAllSales());
                    case 5 -> sendToPreparation();
                    case 6 -> importCustomers();                                  // EJERCICIO 4
                    case 7 -> view.showCommercialStats(statsService.generateStats()); // EJERCICIO 5
                    case 8 -> view.showMessage("Returning to main menu...");
                    default -> view.showMessage("Invalid option");
                }
            } catch (NoSuchElementException e) {
                throw e; // the input was closed: asking again would loop forever
            } catch (RuntimeException e) {
                view.showMessage("The operation could not be completed: " + e.getMessage());
            }
        } while (option != 8);
    }

    // EJERCICIO 4: importar clientes. Si el servicio lanza IllegalArgumentException
    // (archivo no existe o registros invalidos) se muestra "Error: ..." y se vuelve al menu.
    private void importCustomers() {
        try {
            int total = importService.importCustomers();
            view.showMessage("Importación completada. Total de clientes registrados: " + total);
        } catch (IllegalArgumentException e) {
            view.showMessage("Error: " + e.getMessage());
        }
    }

    private void registerCustomer() {
        Customer customer = view.readNewCustomer();

        if (customerDao.findByDocument(customer.getDocument()).isPresent()) {
            view.showMessage("A customer with document " + customer.getDocument() + " already exists.");
            return;
        }
        if (customerDao.save(customer)) {
            view.showMessage("Customer " + customer.getName() + " registered successfully.");
        }
    }

    private void newSale() {
        // 1. Select the customer
        Optional<Customer> box = customerDao.findByDocument(view.readDocument());
        if (box.isEmpty()) {
            view.showMessage("Customer not found. Register the customer first (option 1).");
            return;
        }

        Sale sale = service.createSale(box.get(), view.readSellerName());

        // 2. Add products until the seller types 0
        view.showProducts(productDao.findAll());
        int productId = view.readProductId();
        while (productId != 0) {
            BigDecimal quantity = view.readQuantity();
            try {
                service.addProduct(sale, productId, quantity);
            } catch (InsufficientStockException | IllegalArgumentException e) {
                view.showMessage("Error: " + e.getMessage());
            }
            view.showCart(sale);
            productId = view.readProductId();
        }

        // 3. Confirm or cancel (nothing was saved until now)
        if (sale.getSaleDetails().isEmpty()) {
            view.showMessage("Sale cancelled: no products were added.");
            return;
        }
        // Tres lineas significan: pregunta la entrega → calcula el total con recargo → muéstralo

        DeliveryType deliveryType = view.readDeliveryType();
        BigDecimal finalTotal = service.calculateTotalWithDelivery(sale, deliveryType);
        view.showMessage("Total to pay with " + deliveryType + " delivery: " + finalTotal);

        if (!view.confirm("Confirm the sale?")) {
            view.showMessage("Sale cancelled. Inventory was not changed.");
            return;
        }

        try {
            SaleReceipt receipt = service.confirmSale(sale);
            view.showReceipt(receipt);
        } catch (RuntimeException e) {
            view.showMessage("The sale could not be saved, nothing was changed: " + e.getMessage());
        }
    }

    private void sendToPreparation() {
        int saleId = view.readSaleId();
        if (service.sendToPreparation(saleId)) {
            view.showMessage("Sale " + saleId + " is now In Preparation and visible for logistics.");
        } else {
            view.showMessage("Sale " + saleId + " does not exist or is not Registered.");
        }
    }
}
