package com.frescuratotal.erp.inventory.model;

import java.math.BigDecimal;
import java.util.List;

public interface LotDao {

    List<Lot> findAvailableLots(int productId);

    void decreaseQuantity(int lotId, BigDecimal amount);
    void save(Lot lot);
    Lot findById(int lotId);
    List<Lot> findAll();
    void updateStatus(int lotId, LotStatus status);
    LotTrace findTrace(int lotId);
    List<LotDelivery> findDeliveries(int lotId);

}
