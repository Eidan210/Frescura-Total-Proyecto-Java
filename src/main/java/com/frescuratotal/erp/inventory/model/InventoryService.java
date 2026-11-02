package com.frescuratotal.erp.inventory.model;

import com.frescuratotal.erp.common.util.CodeGenerator;
import com.frescuratotal.erp.suppliers.model.SupplierDao;

import java.math.BigDecimal;
import java.time.LocalDate;

public class InventoryService {
    private final LotDao lotDao;
    private final ProductDao productDao;
    private final SupplierDao supplierDao;
    private final QualityDao qualityDao;

    public InventoryService(LotDao lotDao, ProductDao productDao,  SupplierDao supplierDao,  QualityDao qualityDao) {
        this.lotDao = lotDao;
        this.productDao = productDao;
        this.supplierDao = supplierDao;
        this.qualityDao = qualityDao;
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

    public void putInQuarantine(QuarantineRecord quarantine) {
        Lot lot = lotDao.findById(quarantine.getLotId());

        if(lot == null){
            throw new IllegalArgumentException("Lot " + quarantine.getLotId() + " does not exist.");
        }

        if(lot.getStatusId() == LotStatus.IN_QUARANTINE.getId()){
            throw new IllegalArgumentException("Lot " + lot.getLotNumber() + " is already in quarantine.");
        }

        if(quarantine.getReason().isEmpty()){
            throw new IllegalArgumentException("Reason cannot be empty.");
        }

        qualityDao.saveQuarantine(quarantine);

        lotDao.updateStatus(lot.getId(),LotStatus.IN_QUARANTINE);
    }

    public void inspectLot(QualityInspection inspection) {
        Lot lot = lotDao.findById(inspection.getLotId());

        if (lot == null) {
            throw new IllegalArgumentException("Lot " + inspection.getLotId() + " does not exist.");
        }

        if (lot.getStatusId() != LotStatus.IN_QUARANTINE.getId()) {
            throw new IllegalArgumentException("Lot " + lot.getLotNumber() + " is not in quarantine.");
        }

        if (inspection.getInspectorName().isEmpty()){
            throw new IllegalArgumentException("Inspector name cannot be empty.");
        }

        if (inspection.getResult() == InspectionResult.REJECTED && (inspection.getRejectionReason() == null || inspection.getRejectionReason().isEmpty())) {
            throw new IllegalArgumentException("Reason cannot be empty.");
        }

        int inspectionId = qualityDao.saveInspection(inspection);


        if (inspection.getResult() == InspectionResult.APPROVED) {
            qualityDao.resolveQuarantine(inspection.getLotId(),"Approved by: "+inspection.getInspectorName());
            lotDao.updateStatus(inspection.getLotId(), LotStatus.AVAILABLE);
        }

        if (inspection.getResult() == InspectionResult.REJECTED) {
            WasteRecord waste = new WasteRecord(lot.getId(), inspectionId, lot.getQuantity(), inspection.getRejectionReason(), null, inspection.getInspectorName());
            qualityDao.saveWaste(waste);

            lotDao.decreaseQuantity(lot.getId(), lot.getQuantity());

            qualityDao.resolveQuarantine(lot.getId(),"Rejected by: "+inspection.getInspectorName());

        }
    }

    public void registerWaste(WasteRecord waste) {
        Lot lot = lotDao.findById(waste.getLotId());

        if (lot == null) {
            throw new IllegalArgumentException("Lot " + waste.getLotId() + " does not exist.");
        }

        if (waste.getLostQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0.");
        }

        if (waste.getLostQuantity().compareTo(lot.getQuantity()) > 0) {
            throw new IllegalArgumentException("Lost quantity exceeds available stock (" + lot.getQuantity() + ").");
        }
        if (waste.getReason().isEmpty()) {
            throw new IllegalArgumentException("Reason cannot be empty.");
        }
        qualityDao.saveWaste(waste);

        lotDao.decreaseQuantity(lot.getId(), waste.getLostQuantity());
    }
}

