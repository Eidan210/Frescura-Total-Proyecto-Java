package com.frescuratotal.erp.inventory.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LotTrace(
        String lotNumber,
        String productName,
        String supplierName,
        String supplierNit,
        BigDecimal quantity,
        BigDecimal initialQuantity,
        LotStatus status,
        LocalDate expirationDate
) {}
