package com.frescuratotal.erp.inventory.controller;

import com.frescuratotal.erp.inventory.model.InventoryService;
import com.frescuratotal.erp.inventory.model.Lot;
import com.frescuratotal.erp.inventory.model.LotDao;
import com.frescuratotal.erp.inventory.view.InventoryView;

public class InventoryController {
    private final InventoryView view;
    private final LotDao lotDao;
    private final InventoryService service;

    public InventoryController(InventoryView view, LotDao lotDao, InventoryService service) {
        this.view = view;
        this.lotDao = lotDao;
        this.service = service;
    }

    public void start() {
        int option;
        do {
            option = view.showMenu();
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
                case 4 -> view.showMessage("Returning to main menu...");
                default -> view.showMessage("Invalid option");
            }
        } while (option != 4);
    }
}

