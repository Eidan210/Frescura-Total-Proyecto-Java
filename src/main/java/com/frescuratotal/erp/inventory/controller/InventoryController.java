package com.frescuratotal.erp.inventory.controller;

import com.frescuratotal.erp.inventory.model.*;
import com.frescuratotal.erp.inventory.view.InventoryView;

import java.util.List;
import java.util.NoSuchElementException;

public class InventoryController {
    private final InventoryView view;
    private final LotDao lotDao;
    private final InventoryService service;
    // EJERCICIO 2: servicio del reporte. Se crea aquí para no cambiar el constructor ni Main.
    private final PurchaseReportService purchaseReportService = new PurchaseReportService();

    public InventoryController(InventoryView view, LotDao lotDao, InventoryService service) {
        this.view = view;
        this.lotDao = lotDao;
        this.service = service;
    }

    public void start() {
        int option = 0;
        do {
            // The DAOs wrap SQLException in RuntimeException, so a database failure arrives here.
            try {
                option = view.showMenu();
                handle(option);
            } catch (NoSuchElementException e) {
                throw e; // the input was closed: asking again would loop forever
            } catch (RuntimeException e) {
                view.showMessage("The operation could not be completed: " + e.getMessage());
            }
        } while (option != 11); // "Back" ahora es la opción 11
    }

    private void handle(int option) {
            switch (option) {
                case 1 -> view.showLots(lotDao.findAll());
                case 2 -> {
                    int productId = view.readProductId();
                    view.showLots(lotDao.findAvailableLots(productId));
                }
                case 3 -> {
                    try {
                        Lot newLot = view.readNewLot();
                        Lot saved = service.registerReception(newLot);
                        view.showMessage("Lot "+saved.getLotNumber()+" registered successfully");
                    } catch (IllegalArgumentException e) {
                        view.showMessage("Error: "+ e.getMessage());
                    }
                }
                case 4 -> {
                    try {
                        QuarantineRecord newQuarantine = view.readNewQuarantine();
                        service.putInQuarantine(newQuarantine);
                        view.showMessage("Lot " + newQuarantine.getLotId() + " is now in quarantine");
                    } catch (IllegalArgumentException e){
                        view.showMessage("Error: "+ e.getMessage());
                    }
                }
                case 5 -> {
                    try{
                        QualityInspection newInspection = view.readNewInspection();
                        service.inspectLot(newInspection);
                        if (newInspection.getResult() == InspectionResult.APPROVED) {
                            view.showMessage("Lot " + newInspection.getLotId() + " approved and available for sale.");
                        } else {
                            view.showMessage("Lot " + newInspection.getLotId() + " rejected and registered as waste.");
                        }
                    } catch (IllegalArgumentException e){
                        view.showMessage("Error: "+ e.getMessage());
                    }
                }
                case 6 -> {
                    try {
                        WasteRecord newWaste = view.readNewWaste();
                        service.registerWaste(newWaste);
                        view.showMessage("Waste of " + newWaste.getLostQuantity() + " registered for lot " + newWaste.getLotId() + ".");
                    } catch (IllegalArgumentException e) {
                        view.showMessage("Error: " + e.getMessage());
                    }
                }
                case 7 -> {
                    ExpirationCheckResult result = service.runExpirationCheck();
                    view.showMessage("Expiration check done: " + result.nearExpiry() + " lot(s) near expiry, " + result.expired() + " lot(s) expired.");;
                }
                case 8 -> {
                    int lotId = view.readLotId();
                    LotTrace trace = lotDao.findTrace(lotId);
                    if (trace == null) {
                        view.showMessage("Lot: " + lotId + " does not exist.");
                    } else {
                        List<LotDelivery> deliveries = lotDao.findDeliveries(lotId);
                        view.showTrace(trace, deliveries);
                    }
                }
                case 9 -> {
                    LotStatus status = view.readLotStatus();
                    service.runExpirationCheck(); // the stored status may be out of date
                    view.showLots(service.findLotsByStatus(status));
                }
                // EJERCICIO 2: no pide ningún dato; la fecha la toma el servicio con LocalDate.now().
                case 10 -> view.showPurchaseReport(purchaseReportService.generateDailyReport());
                case 11 -> view.showMessage("Returning to main menu...");
                default -> view.showMessage("Invalid option");
            }
    }
}

