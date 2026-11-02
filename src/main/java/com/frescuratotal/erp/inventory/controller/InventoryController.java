package com.frescuratotal.erp.inventory.controller;

import com.frescuratotal.erp.inventory.model.LotDao;
import com.frescuratotal.erp.inventory.view.InventoryView;

public class InventoryController {
    private final InventoryView view;
    private final LotDao lotDao;

    public InventoryController(InventoryView view, LotDao lotDao) {
        this.view = view;
        this.lotDao = lotDao;
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
                case 3 -> view.showMessage("Coming soon");
                case 4 -> view.showMessage("Returning to main menu...");
                default -> view.showMessage("Invalid option");
            }
        } while (option != 4);
    }
}

