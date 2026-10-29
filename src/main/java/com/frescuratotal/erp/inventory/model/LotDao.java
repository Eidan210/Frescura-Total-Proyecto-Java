package com.frescuratotal.erp.inventory.model;

import java.math.BigDecimal;
import java.util.List;

public interface LotDao {

    List<Lot> findAvailableLots(int productId);

    void decreaseQuantity(int lotId, BigDecimal amount);


}
