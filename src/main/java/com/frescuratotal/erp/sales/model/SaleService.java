package com.frescuratotal.erp.sales.model;

import com.frescuratotal.erp.common.exception.InsufficientStockException;
import com.frescuratotal.erp.inventory.model.Lot;
import com.frescuratotal.erp.inventory.model.LotDao;
import com.frescuratotal.erp.inventory.model.LotDaoImpl;

import java.math.BigDecimal;
import java.util.List;

public class SaleService {
    private final LotDao lotDao = new LotDaoImpl();

    private static final BigDecimal NEAR_EXPIRY_DISCOUNT = new BigDecimal("0.50");
    private static final BigDecimal TAX_RATE = new BigDecimal("0.19");

    public void addProduct(Sale sale, int productId, BigDecimal quantity) {

        List<Lot> lots = lotDao.findAvailableLots(productId);

        BigDecimal available = BigDecimal.ZERO;

        for (Lot lot : lots) {
            available = available.add(lot.getQuantity());
        }
        if (available.compareTo(quantity) < 0) {
            throw new InsufficientStockException("Not enough stock. Available: " + available);
        }

        BigDecimal remaining = quantity;


        for (Lot lot : lots) {
            if (remaining.compareTo(BigDecimal.ZERO) == 0) {
                break;
            }
            BigDecimal taken = remaining.min(lot.getQuantity());
            remaining = remaining.subtract(taken);

            SaleDetail saleDetail = new SaleDetail(taken,lot);
            sale.getSaleDetails().add(saleDetail);
        }
    }


}
