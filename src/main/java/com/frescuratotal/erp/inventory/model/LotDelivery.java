package com.frescuratotal.erp.inventory.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LotDelivery(
        String saleNumber,
        LocalDateTime saleDate,
        String customerName,
        String customerDocument,
        BigDecimal quantity
) {}
