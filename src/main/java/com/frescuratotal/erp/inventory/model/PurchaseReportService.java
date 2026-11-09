package com.frescuratotal.erp.inventory.model;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

/**
 * EJERCICIO 2 - Reporte diario de compras en CSV.
 *
 * Pasos:
 *   1. Fecha de HOY con Java (LocalDate.now()), sin preguntarle al usuario.
 *   2. Consultar las compras de esa fecha (DAO).
 *   3. Escribir compras_AAAA-MM-DD.csv en la carpeta raíz del proyecto.
 *   4. Si no hay compras, el archivo igual se crea, solo con la cabecera.
 */
public class PurchaseReportService {

    private static final String HEADER = "NumeroCompra;NombreProveedor;MontoTotal";
    private static final String DELIMITER = ";";

    private final PurchaseReportDao purchaseDao;

    public PurchaseReportService() {
        this(new PurchaseReportDaoImpl());
    }

    public PurchaseReportService(PurchaseReportDao purchaseDao) {
        this.purchaseDao = purchaseDao;
    }

    /** Lo que llama el menú: fecha de hoy y carpeta donde se ejecuta el proyecto. */
    public PurchaseReportResult generateDailyReport() {
        // Paths.get("") = carpeta de trabajo actual (en IntelliJ: la raíz del proyecto).
        return generateReport(LocalDate.now(), Paths.get(""));
    }

    /** Separado con parámetros para poder probar con cualquier fecha y carpeta. */
    public PurchaseReportResult generateReport(LocalDate date, Path folder) {
        List<DailyPurchase> purchases = purchaseDao.findPurchasesByDate(date);

        // LocalDate.toString() ya da el formato AAAA-MM-DD (ISO): 2026-11-09
        Path file = folder.resolve("compras_" + date + ".csv");

        // try-with-resources: el writer se cierra solo (si no se cierra, el archivo puede quedar vacío).
        // newBufferedWriter SOBRESCRIBE el archivo si ya existía (reporte del día actualizado).
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writer.write(HEADER);
            writer.newLine();

            for (DailyPurchase purchase : purchases) {
                writer.write(purchase.purchaseNumber() + DELIMITER
                        + clean(purchase.supplierName()) + DELIMITER
                        + formatAmount(purchase.totalAmount()));
                writer.newLine();
            }
        } catch (IOException e) {
            // IOException es "chequeada"; se envuelve para que el controlador la muestre sin cerrar la app.
            throw new UncheckedIOException("Could not write " + file.getFileName(), e);
        }

        return new PurchaseReportResult(file.toAbsolutePath(), purchases.size());
    }

    /*
     * Formato del monto: punto decimal, 2 decimales, sin $ ni separador de miles.
     *   setScale(2, HALF_UP) -> siempre 2 decimales (850000.5 -> 850000.50)
     *   toPlainString()      -> nunca notación científica (toString() podría dar 1.5E+6)
     * BigDecimal SIEMPRE usa punto, sin importar el idioma del computador
     * (String.format("%.2f") en un Windows en español podría poner COMA).
     */
    static String formatAmount(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    // Si un nombre tuviera ";" rompería las columnas del CSV: se reemplaza por ",".
    private String clean(String text) {
        return text == null ? "" : text.replace(DELIMITER, ",");
    }

    /** Resultado para la vista: dónde quedó el archivo y cuántas compras tiene. */
    public record PurchaseReportResult(Path file, int records) {
    }
}
