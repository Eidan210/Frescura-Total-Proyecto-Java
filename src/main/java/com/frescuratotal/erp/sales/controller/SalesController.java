package com.frescuratotal.erp.sales.controller;

import com.frescuratotal.erp.common.exception.ImportException;
import com.frescuratotal.erp.common.exception.InsufficientStockException;
import com.frescuratotal.erp.inventory.model.ProductDao;
import com.frescuratotal.erp.sales.model.*;
import com.frescuratotal.erp.sales.view.SalesView;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public class SalesController {
    private final SalesView view;
    private final CustomerDao customerDao;
    private final ProductDao productDao;
    private final SaleService service;
    // EJERCICIO 4 y 5: servicios nuevos. Se crean aquí para no cambiar el constructor ni Main.
    private final CustomerImportService importService = new CustomerImportService();
    private final CommercialStatsService statsService = new CommercialStatsService();

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
                    case 2 -> view.showCustomers(customerDao.findAll());
                    case 3 -> newSale();
                    case 4 -> view.showSales(service.findAllSales());
                    case 5 -> sendToPreparation();
                    case 6 -> findCustomerById();      // EJERCICIO 1
                    case 7 -> importCustomers();       // EJERCICIO 4
                    case 8 -> showCommercialStats();   // EJERCICIO 5
                    case 9 -> view.showMessage("Returning to main menu...");
                    default -> view.showMessage("Invalid option");
                }
            } catch (NoSuchElementException e) {
                throw e; // the input was closed: asking again would loop forever
            } catch (RuntimeException e) {
                view.showMessage("The operation could not be completed: " + e.getMessage());
            }
        } while (option != 9); // "Back" ahora es la opción 9
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

        // EJERCICIO 3: se pregunta la modalidad ANTES de confirmar (el cliente debe saber
        // si le cobran el 12% antes de aceptar) y DESPUÉS de revisar que haya productos.
        DeliveryType delivery = view.readDeliveryType();

        if (!view.confirm("Confirm the sale?")) {
            view.showMessage("Sale cancelled. Inventory was not changed.");
            return;
        }

        try {
            // EJERCICIO 3: dos parámetros separados por coma; la firma en SaleService debe coincidir.
            SaleReceipt receipt = service.confirmSale(sale, delivery);
            view.showReceipt(receipt);
            view.showMessage(service.getDeliveryDescription(delivery));
        } catch (RuntimeException e) {
            view.showMessage("The sale could not be saved, nothing was changed: " + e.getMessage());
        }
    }

    // EJERCICIO 1: el controlador NO sabe si el cliente vino de la caché o de la BD
    // (eso lo decide CustomerService). Solo recibe la "caja" Optional y la muestra.
    private void findCustomerById() {
        int id = view.readCustomerId();
        Optional<Customer> customer = service.findCustomerById(id);
        if (customer.isPresent()) {
            view.showCustomers(List.of(customer.get()));
        } else {
            view.showMessage("Cliente " + id + " no encontrado.");
        }
    }

    // EJERCICIO 4: todo el trabajo está en el servicio. Aquí solo se muestran los mensajes.
    // ImportException trae el mensaje exacto del enunciado + el detalle de las filas malas.
    // Se captura para regresar al menú sin cerrar la aplicación.
    private void importCustomers() {
        try {
            int total = importService.importCustomers();
            view.showMessage("Importación completada. Total de clientes registrados: " + total);
        } catch (ImportException e) {
            view.showMessage(e.getMessage());
            view.showImportErrors(e.getDetails());
        }
    }

    // EJERCICIO 5: Optional vacío = no hay ventas -> mensaje informativo, sin excepciones.
    private void showCommercialStats() {
        Optional<CommercialStats> stats = statsService.generate();
        if (stats.isPresent()) {
            view.showCommercialStats(stats.get());
        } else {
            view.showMessage("No existen datos suficientes para generar estadísticas comerciales.");
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
