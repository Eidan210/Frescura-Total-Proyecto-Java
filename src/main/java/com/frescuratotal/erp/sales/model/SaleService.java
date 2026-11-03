package com.frescuratotal.erp.sales.model;

import com.frescuratotal.erp.common.exception.InsufficientStockException;
import com.frescuratotal.erp.inventory.model.Lot;
import com.frescuratotal.erp.inventory.model.LotDao;
import com.frescuratotal.erp.inventory.model.LotDaoImpl;
import com.frescuratotal.erp.inventory.model.LotStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

            if (LotStatus.fromId( lot.getStatusId() ) == LotStatus.NEAR_EXPIRY){
                saleDetail.setDiscountApplied(NEAR_EXPIRY_DISCOUNT);
            }else{
                saleDetail.setDiscountApplied(BigDecimal.ZERO);
            }

            calculateDetail(saleDetail);

            sale.getSaleDetails().add(saleDetail);


        }
    }

    private void calculateDetail(SaleDetail saleDetail) {

        BigDecimal subtotal = saleDetail.getUnitPrice().multiply(saleDetail.getQuantitySold()).setScale(2, RoundingMode.HALF_UP);
        saleDetail.setSubtotal(subtotal);

        BigDecimal discountAmount = subtotal.multiply(saleDetail.getDiscountApplied()).setScale(2, RoundingMode.HALF_UP);
        saleDetail.setDiscountAmount(discountAmount);

        BigDecimal base = subtotal.subtract(discountAmount);

        saleDetail.setTaxRate(TAX_RATE);

        BigDecimal taxAmount = base.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        saleDetail.setTaxAmount(taxAmount);

        BigDecimal lineTotal = base.add(taxAmount);
        saleDetail.setLineTotal(lineTotal);



    }


}
