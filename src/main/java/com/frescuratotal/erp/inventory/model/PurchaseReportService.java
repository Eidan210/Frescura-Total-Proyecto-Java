package com.frescuratotal.erp.inventory.model;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

public class PurchaseReportService {
    private final PurchaseReportDao purchaseReportDao = new PurchaseReportDaoImpl();

    public PurchaseReportResult generateDailyReport() {
        // 1. La fecha de hoy, desde Java
        LocalDate today = LocalDate.now();                                     // A

        // 2. Las compras de hoy
        List<PurchaseRecord> purchases = purchaseReportDao.findPurchasesByDate(today);         // B

        // 3. El nombre del archivo: compras_AAAA-MM-DD.csv
        Path file = Path.of("compras_" + today + ".csv");

        // 4. Escribir el archivo
        try (BufferedWriter writer = Files.newBufferedWriter(file)) {
            writer.write("NumeroCompra;NombreProveedor;MontoTotal");
            writer.newLine();

            for (PurchaseRecord p : purchases) {
                writer.write(p.getLotId() + ";" + p.getSupplierName() + ";"                // C
                        + p.getTotalAmount().setScale(2, RoundingMode.HALF_UP).toPlainString());
                writer.newLine();                                                  // D
            }
        } catch (IOException e) {                                                      // E
            throw new RuntimeException("Error writing the report", e);
        }

        // 5. Devolver el resultado
        return new PurchaseReportResult(file, purchases.size());
    }
}
