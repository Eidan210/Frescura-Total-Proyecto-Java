package com.frescuratotal.erp.suppliers.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Exercise 2: the CSV part (the filter by date is done by the SQL). */
class PurchaseReportServiceTest {

    private static final LocalDate DAY = LocalDate.of(2026, 10, 15);

    @TempDir
    Path folder;

    @Test
    void writesEveryPurchaseOfTheDay() throws IOException {
        List<PurchaseRecord> purchases = List.of(
                new PurchaseRecord(201, "Distribuidora Andina", new BigDecimal("1500000")),
                new PurchaseRecord(202, "Lacteos del Norte", new BigDecimal("850000.5")),
                new PurchaseRecord(203, "AgroCampo", new BigDecimal("320000.00")));

        PurchaseReport report = service(purchases).generateDailyReport();

        assertEquals(folder.resolve("compras_2026-10-15.csv"), report.file());
        assertEquals(List.of(
                "NumeroCompra;NombreProveedor;MontoTotal",
                "201;Distribuidora Andina;1500000.00",
                "202;Lacteos del Norte;850000.50",
                "203;AgroCampo;320000.00"), Files.readAllLines(report.file()));
    }

    @Test
    void dayWithoutPurchasesHasOnlyTheHeader() throws IOException {
        PurchaseReport report = service(List.of()).generateDailyReport();

        assertTrue(report.isEmpty());
        assertEquals(List.of("NumeroCompra;NombreProveedor;MontoTotal"), Files.readAllLines(report.file()));
    }

    @Test
    void asksTheDaoForTodayOnly() {
        LocalDate[] asked = new LocalDate[1];
        PurchaseReportDao dao = date -> {
            asked[0] = date;
            return List.of();
        };
        new PurchaseReportService(dao, fixedClock(), folder).generateDailyReport();

        assertEquals(DAY, asked[0]);
    }

    @Test
    void amountsUseDecimalPointWithoutSeparators() {
        assertEquals("1234567.89", PurchaseReportService.formatAmount(new BigDecimal("1234567.891")));
        assertEquals("0.00", PurchaseReportService.formatAmount(BigDecimal.ZERO));
    }

    @Test
    void semicolonInsideTheSupplierNameDoesNotBreakTheColumns() {
        String line = PurchaseReportService.toCsvLine(new PurchaseRecord(1, "Agro; Campo", BigDecimal.ONE));

        assertEquals(3, line.split(";").length);
    }

    private PurchaseReportService service(List<PurchaseRecord> purchases) {
        return new PurchaseReportService(date -> purchases, fixedClock(), folder);
    }

    private static Clock fixedClock() {
        ZoneId zone = ZoneId.of("America/Bogota");
        return Clock.fixed(ZonedDateTime.of(DAY.atTime(10, 30), zone).toInstant(), zone);
    }
}
