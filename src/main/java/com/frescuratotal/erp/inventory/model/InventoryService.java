package com.frescuratotal.erp.inventory.model;

import com.frescuratotal.erp.common.DatabaseConnection;
import com.frescuratotal.erp.common.util.CodeGenerator;
import com.frescuratotal.erp.common.util.InputUtil;
import com.frescuratotal.erp.suppliers.model.SupplierDao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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
        InputUtil.requireAmount(lot.getQuantity(), "Quantity");
        InputUtil.requireAmount(lot.getPurchasePrice(), "Purchase price");
        InputUtil.requireAmount(lot.getSalePrice(), "Sale price");
        if (!lot.getExpirationDate().isAfter(LocalDate.now())){
            throw new IllegalArgumentException("Expiration date must be after today.");
        }

        lot.setLotNumber(CodeGenerator.generateLotNumber());
        lot.setInitialQuantity(lot.getQuantity());
        lot.setStatusId(LotStatus.forExpiration(lot.getExpirationDate()).getId());
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

        DatabaseConnection.getInstance().inTransaction(() -> {
            qualityDao.saveQuarantine(quarantine);
            lotDao.updateStatus(lot.getId(), LotStatus.IN_QUARANTINE);
        });
    }

    public List<Lot> findLotsByStatus(LotStatus status) {
        return lotDao.findAll().stream()
                .filter(lot -> lot.getStatusId() == status.getId())
                .toList();
    }

    public ExpirationCheckResult runExpirationCheck() {
        LocalDate today = LocalDate.now();
        LocalDate limit = today.plusDays(7);

        int nearExpiry = 0;
        int expired = 0;
        for (Lot lot : lotDao.findAll()) {
            if (lot.getQuantity().compareTo(BigDecimal.ZERO) <= 0
                    || lot.getStatusId() == LotStatus.IN_QUARANTINE.getId()
                    || lot.getStatusId() == LotStatus.EXPIRED.getId()
                    || lot.getExpirationDate().isAfter(limit)) {
                continue;
            }

            if(lot.getExpirationDate().isBefore(today)){
                lotDao.updateStatus(lot.getId(),LotStatus.EXPIRED);
                expired++;
            } else if(lot.getStatusId() == LotStatus.AVAILABLE.getId()){
                lotDao.updateStatus(lot.getId(),LotStatus.NEAR_EXPIRY);
                nearExpiry++;
            }
        }
        return new ExpirationCheckResult(nearExpiry, expired);
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

        LotStatus statusByDate = LotStatus.forExpiration(lot.getExpirationDate());

        if (inspection.getResult() == InspectionResult.APPROVED && statusByDate == LotStatus.EXPIRED) {
            throw new IllegalArgumentException("Lot " + lot.getLotNumber() + " is expired: it can only be rejected.");
        }

        DatabaseConnection.getInstance().inTransaction(() -> {
            int inspectionId = qualityDao.saveInspection(inspection);

            if (inspection.getResult() == InspectionResult.APPROVED) {
                qualityDao.resolveQuarantine(inspection.getLotId(), "Approved by: " + inspection.getInspectorName());
                lotDao.updateStatus(inspection.getLotId(), statusByDate);
            }

            if (inspection.getResult() == InspectionResult.REJECTED) {
                WasteRecord waste = new WasteRecord(lot.getId(), inspectionId, lot.getQuantity(), inspection.getRejectionReason(), null, inspection.getInspectorName());
                qualityDao.saveWaste(waste);
                lotDao.decreaseQuantity(lot.getId(), lot.getQuantity());
                qualityDao.resolveQuarantine(lot.getId(), "Rejected by: " + inspection.getInspectorName());
            }
        });
    }

    public void registerWaste(WasteRecord waste) {
        Lot lot = lotDao.findById(waste.getLotId());

        if (lot == null) {
            throw new IllegalArgumentException("Lot " + waste.getLotId() + " does not exist.");
        }

        InputUtil.requireAmount(waste.getLostQuantity(), "Lost quantity");

        if (waste.getLostQuantity().compareTo(lot.getQuantity()) > 0) {
            throw new IllegalArgumentException("Lost quantity exceeds available stock (" + lot.getQuantity() + ").");
        }
        if (waste.getReason().isEmpty()) {
            throw new IllegalArgumentException("Reason cannot be empty.");
        }
        DatabaseConnection.getInstance().inTransaction(() -> {
            // Stock first: it locks the lot, so two sessions queue here instead of deadlocking
            lotDao.decreaseQuantity(lot.getId(), waste.getLostQuantity());
            qualityDao.saveWaste(waste);
        });
    }
}

