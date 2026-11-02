package com.frescuratotal.erp.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CodeGenerator {
    public static String generateLotNumber(){
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmssSSS");
        return "LOT-" + now.format(formatter);
    }
}
