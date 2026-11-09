package com.frescuratotal.erp.suppliers.controller;

import com.frescuratotal.erp.suppliers.model.Supplier;
import com.frescuratotal.erp.suppliers.model.SupplierData;
import com.frescuratotal.erp.suppliers.model.SupplierDao;
import com.frescuratotal.erp.suppliers.model.SuppliersPerformanceReport;
import com.frescuratotal.erp.suppliers.view.SupplierView;

import java.util.List;
import java.util.Optional;

public class SupplierController {
    private final SupplierView view;
    private final SupplierDao dao;

    public SupplierController(SupplierView view, SupplierDao dao) {
        this.view = view;
        this.dao = dao;
    }

    public void start() {
        int option;
        do {
            option = view.showMenu();
            switch (option) {
                case 1 -> register();
                case 2 -> view.showSuppliers(dao.findAll());
                case 3 -> findById();
                case 4 -> update();
                case 5 -> delete();
                case 6 -> searchByName();
                case 7 -> performanceReport();
                case 8 -> view.showMessage("Returning to main menu...");
                default -> view.showMessage("Invalid option");
            }
        } while (option != 8);
    }

    /** Reporte de rendimiento: datos del DAO, cálculo con Streams en el modelo, impresión en la vista. */
    public void performanceReport() {
        List<SupplierData> lots = dao.stadistics();
        if (lots.isEmpty()) {
            view.showMessage("No hay datos de inventario suficientes para generar estadísticas.");
            return;
        }
        String top = SuppliersPerformanceReport.topSupplierByVolume(lots).orElse("(ninguno)");
        view.showPerformanceReport(top, SuppliersPerformanceReport.suppliersWithExpiredLots(lots));
    }

    private void register() {
        Supplier supplier = view.readNewSupplier();
        view.showMessage(dao.save(supplier) ? "Supplier registered successfully." : "Error registering supplier.");
    }

    private void findById() {
        findSupplier().ifPresent(view::showSupplier);
    }

    private void searchByName() {
        String text = view.readSearchText().toLowerCase();
        view.showSuppliers(dao.findAll().stream()
                .filter(supplier -> supplier.getName().toLowerCase().contains(text))
                .toList());
    }

    private void update() {
        Optional<Supplier> found = findSupplier();
        if (found.isEmpty()) return;
        Supplier supplier = found.get();
        view.readSupplierChanges(supplier);
        view.showMessage(dao.update(supplier) ? "Supplier updated successfully." : "Error updating supplier.");
    }

    private void delete() {
        Optional<Supplier> found = findSupplier();
        if (found.isEmpty()) return;
        view.showSupplier(found.get());
        if (!view.confirm("Delete this supplier and its product catalog?")) {
            view.showMessage("Deletion cancelled.");
            return;
        }
        view.showMessage(dao.delete(found.get().getId()) ? "Supplier deleted successfully." : "Error deleting supplier.");
    }

    private Optional<Supplier> findSupplier() {
        Optional<Supplier> supplier = dao.findById(view.readId());
        if (supplier.isEmpty()) view.showMessage("Supplier not found.");
        return supplier;
    }
}
