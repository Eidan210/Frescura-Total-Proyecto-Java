package com.frescuratotal.erp.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CodeGenerator {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmssSSS");

    public static String generateLotNumber(){
        return "LOT-" + LocalDateTime.now().format(FORMATTER);
    }

    public static String generateSaleNumber() {
        return "SALE-" + LocalDateTime.now().format(FORMATTER);
    }

    public static String generateInvoiceNumber() {
        return "FT-" + LocalDateTime.now().format(FORMATTER);
    }
}
