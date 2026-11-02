package com.frescuratotal.erp.inventory.model;

import com.frescuratotal.erp.common.util.CodeGenerator;
import com.frescuratotal.erp.suppliers.model.SupplierDao;

import java.math.BigDecimal;
import java.time.LocalDate;

public class InventoryService {
    private final LotDao lotDao;
    private final ProductDao productDao;
    private final SupplierDao supplierDao;

    public InventoryService(LotDao lotDao, ProductDao productDao,  SupplierDao supplierDao) {
        this.lotDao = lotDao;
        this.productDao = productDao;
        this.supplierDao = supplierDao;
    }

    public Lot registerReception(Lot lot){
        if(productDao.findById(lot.getProductId()) == null){
            throw new IllegalArgumentException("Product " + lot.getProductId() + " does not exist.");
        }
        if (supplierDao.findById(lot.getSupplierId()).isEmpty()) {
            throw new IllegalArgumentException("Supplier " + lot.getSupplierId() + " does not exist.");
        }
        if (lot.getQuantity().compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("Quantity must be greater than 0.");
        }
        if (!lot.getExpirationDate().isAfter(LocalDate.now())){
            throw new IllegalArgumentException("Expiration date must be after today.");
        }

        lot.setLotNumber(CodeGenerator.generateLotNumber());
        lot.setInitialQuantity(lot.getQuantity());
        lot.setStatusId(LotStatus.AVAILABLE.getId());
        lotDao.save(lot);
        return lot;
    }
}
