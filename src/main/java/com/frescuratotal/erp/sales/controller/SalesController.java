package com.frescuratotal.erp.sales.controller;

import com.frescuratotal.erp.common.exception.InsufficientStockException;
import com.frescuratotal.erp.inventory.model.ProductDao;
import com.frescuratotal.erp.sales.model.Customer;
import com.frescuratotal.erp.sales.model.CustomerDao;
import com.frescuratotal.erp.sales.model.CustomerImportResult;
import com.frescuratotal.erp.sales.model.CustomerImportService;
import com.frescuratotal.erp.sales.model.Sale;
import com.frescuratotal.erp.sales.model.SaleReceipt;
import com.frescuratotal.erp.sales.model.SaleService;
import com.frescuratotal.erp.sales.model.delivery.DeliveryMode;
import com.frescuratotal.erp.sales.model.stats.CommercialStatisticsService;
import com.frescuratotal.erp.sales.view.SalesView;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.Optional;

public class SalesController {
    private final SalesView view;
    private final CustomerDao customerDao;
    private final ProductDao productDao;
    private final SaleService service;
    private final CustomerImportService importService;
    private final CommercialStatisticsService statisticsService;

    /** customerDao is the CustomerService from Main: the calls stay the same, the cache works behind them. */
    public SalesController(SalesView view, CustomerDao customerDao, ProductDao productDao, SaleService service,
                           CustomerImportService importService, CommercialStatisticsService statisticsService) {
        this.view = view;
        this.customerDao = customerDao;
        this.productDao = productDao;
        this.service = service;
        this.importService = importService;
        this.statisticsService = statisticsService;
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
                    case 6 -> findCustomerById();
                    case 7 -> importCustomers();
                    case 8 -> view.showStatistics(statisticsService.generate());
                    case 9 -> registerReturn();
                    case 10 -> view.showMessage("Returning to main menu...");
                    default -> view.showMessage("Invalid option");
                }
            } catch (NoSuchElementException e) {
                throw e; // the input was closed: asking again would loop forever
            } catch (RuntimeException e) {
                view.showMessage("The operation could not be completed: " + e.getMessage());
            }
        } while (option != 10);
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
        Optional<Customer> box = service.findCustomerByDocument(view.readDocument());
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

        // 4. Delivery mode: the strategy calculates what the customer pays before confirming
        DeliveryMode mode = view.readDeliveryMode();
        view.showAmountToPay(sale, mode, service.previewTotal(sale, mode));

        if (!view.confirm("Confirm the sale?")) {
            view.showMessage("Sale cancelled. Inventory was not changed.");
            return;
        }

        try {
            SaleReceipt receipt = service.confirmSale(sale, mode);
            view.showReceipt(receipt);
        } catch (RuntimeException e) {
            view.showMessage("The sale could not be saved, nothing was changed: " + e.getMessage());
        }
    }

    /** Goes through the cache: the second search of the same ID does not run SQL. */
    private void findCustomerById() {
        int id = view.readCustomerId();
        Optional<Customer> customer = customerDao.findById(id);
        if (customer.isPresent()) {
            view.showCustomer(customer.get());
        } else {
            view.showMessage("Customer " + id + " was not found.");
        }
        view.showCacheStatus(service.getCustomerCache());
    }

    private void importCustomers() {
        CustomerImportResult result = importService.importDefaultFile();
        switch (result.status()) {
            case FILE_NOT_FOUND -> view.showMessage("Error: No se encontró el archivo " + CustomerImportService.FILE_NAME + ".");
            case INVALID_RECORDS -> {
                view.showMessage("Error: Se encontraron registros inválidos. Importación cancelada.");
                view.showErrors(result.errors());
            }
            case SAVE_FAILED -> {
                view.showMessage("Error: No se pudo guardar un cliente. Importación cancelada, no se guardó ningún registro.");
                view.showErrors(result.errors());
            }
            case COMPLETED -> view.showMessage("Importación completada. Total de clientes registrados: " + result.importedCount());
        }
    }

    private void registerReturn() {
        int saleId = view.readSaleId();
        if (service.registerReturn(saleId)) {
            view.showMessage("Sale " + saleId + " is now Returned.");
        } else {
            view.showMessage("Sale " + saleId + " does not exist or is not Delivered: only delivered sales can be returned.");
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
