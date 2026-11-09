package com.frescuratotal.erp.sales.model.stats;

import com.frescuratotal.erp.sales.model.SaleStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Exercise 5: the data and expected output of the guide plus the mandatory cases. */
class CommercialStatisticsServiceTest {

    private static final SaleStatus CONFIRMED = SaleStatus.DELIVERED;
    private static final SaleStatus RETURNED = SaleStatus.RETURNED;

    private final CommercialStatisticsService service = new CommercialStatisticsService(List::of);

    @Test
    void exampleOfTheGuide() {
        CommercialStatistics stats = service.calculate(List.of(
                row(1, 30, "Frutas", "Tienda El Parque", CONFIRMED),
                row(2, 50, "Lácteos", "Distribuidora Omega", CONFIRMED),
                row(3, 40, "Frutas", "Minimercado Sol", CONFIRMED),
                row(4, 15, "Lácteos", "Tienda El Parque", CONFIRMED),
                row(5, 10, "Verduras", "Distribuidora Omega", RETURNED),
                row(6, 5, "Verduras", "Distribuidora Omega", RETURNED),
                row(7, 8, "Frutas", "Minimercado Sol", RETURNED)));

        CategoryTotal top = stats.topCategory().orElseThrow();
        assertEquals("Frutas", top.categoryName());
        assertEquals(0, new BigDecimal("70").compareTo(top.units())); // the returned 8 do not count
        assertEquals(List.of("Distribuidora Omega", "Minimercado Sol"), stats.customersWithReturns());
    }

    @Test
    void cancelledSalesDoNotCountAsSold() {
        CommercialStatistics stats = service.calculate(List.of(
                row(1, 10, "Frutas", "A", SaleStatus.REGISTERED),
                row(2, 99, "Carnes", "B", SaleStatus.CANCELLED)));

        assertEquals("Frutas", stats.topCategory().orElseThrow().categoryName());
    }

    @Test
    void tieReturnsOneOfTheTiedCategories() {
        CommercialStatistics stats = service.calculate(List.of(
                row(1, 20, "Frutas", "A", CONFIRMED),
                row(2, 20, "Lácteos", "B", CONFIRMED)));

        assertTrue(Set.of("Frutas", "Lácteos").contains(stats.topCategory().orElseThrow().categoryName()));
    }

    @Test
    void noSalesShowsTheInformativeCase() {
        CommercialStatistics stats = service.generate(); // the DAO returns an empty list

        assertFalse(stats.hasData());
        assertTrue(stats.topCategory().isEmpty());
        assertTrue(stats.customersWithReturns().isEmpty());
    }

    @Test
    void noReturnsGivesAnEmptyList() {
        CommercialStatistics stats = service.calculate(List.of(row(1, 5, "Frutas", "A", CONFIRMED)));

        assertTrue(stats.hasData());
        assertTrue(stats.customersWithReturns().isEmpty());
    }

    @Test
    void onlyReturnsMeansNoTopCategory() {
        CommercialStatistics stats = service.calculate(List.of(row(1, 5, "Frutas", "A", RETURNED)));

        assertTrue(stats.hasData());
        assertTrue(stats.topCategory().isEmpty());
        assertEquals(List.of("A"), stats.customersWithReturns());
    }

    private static SaleStatisticsRow row(int saleId, int quantity, String category, String customer, SaleStatus status) {
        return new SaleStatisticsRow(saleId, new BigDecimal(quantity), category, customer, status);
    }
}
