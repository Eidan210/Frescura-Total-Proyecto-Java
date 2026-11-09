package com.frescuratotal.erp.suppliers.model;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/**
 * Daily purchase report in CSV (Exercise 2).
 * The date comes from the system clock, never from the user.
 */
public class PurchaseReportService {

    public static final String HEADER = "NumeroCompra;NombreProveedor;MontoTotal";
    private static final String DELIMITER = ";";

    private final PurchaseReportDao dao;
    private final Clock clock;
    private final Path outputFolder;

    /** Today's date and the folder the project runs from. */
    public PurchaseReportService(PurchaseReportDao dao) {
        this(dao, Clock.systemDefaultZone(), Path.of("").toAbsolutePath());
    }

    /** The tests use a fixed clock and a temporary folder. */
    public PurchaseReportService(PurchaseReportDao dao, Clock clock, Path outputFolder) {
        this.dao = dao;
        this.clock = clock;
        this.outputFolder = outputFolder;
    }

    public PurchaseReport generateDailyReport() {
        LocalDate today = LocalDate.now(clock);
        List<PurchaseRecord> purchases = dao.findPurchasesByDate(today);

        // LocalDate.toString() is always yyyy-MM-dd, e.g. compras_2026-10-15.csv
        Path file = outputFolder.resolve("compras_" + today + ".csv");
        writeCsv(file, purchases);
        return new PurchaseReport(file, today, purchases.size());
    }

    /** The header is always written, so a day without purchases still gets its file. */
    private void writeCsv(Path file, List<PurchaseRecord> purchases) {
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writer.write(HEADER);
            writer.newLine();
            for (PurchaseRecord purchase : purchases) {
                writer.write(toCsvLine(purchase));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not write " + file, e);
        }
    }

    static String toCsvLine(PurchaseRecord purchase) {
        return purchase.purchaseNumber()
                + DELIMITER + cleanText(purchase.supplierName())
                + DELIMITER + formatAmount(purchase.totalAmount());
    }

    /** 1500000.00: decimal point, two decimals, no currency symbol and no thousands separator. */
    static String formatAmount(BigDecimal amount) {
        BigDecimal value = amount == null ? BigDecimal.ZERO : amount;
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    /** A ';' or a line break inside a name would move the columns, so they are replaced. */
    private static String cleanText(String text) {
        if (text == null) {
            return "";
        }
        return text.replace(DELIMITER, ",").replace("\r", " ").replace("\n", " ").trim();
    }
}
