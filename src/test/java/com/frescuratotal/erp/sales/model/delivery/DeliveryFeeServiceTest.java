package com.frescuratotal.erp.sales.model.delivery;

import com.frescuratotal.erp.sales.model.Sale;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Exercise 3: mandatory test cases of the guide. */
class DeliveryFeeServiceTest {

    private final DeliveryFeeService service = new DeliveryFeeService();

    @ParameterizedTest(name = "{0} with {1} -> {2}")
    @CsvSource({
            "100000, STANDARD, 100000",
            "100000, URGENT,   112000",
            "250000, URGENT,   280000",
            "50000,  STANDARD, 50000",
            "0,      URGENT,   0"
    })
    void finalAmountOfASale(String purchase, DeliveryMode mode, String expected) {
        Sale sale = new Sale("S-TEST", null, "test");
        sale.setTotalAmount(new BigDecimal(purchase));

        BigDecimal result = service.calculateFinalAmount(sale, mode);

        assertEquals(0, new BigDecimal(expected).compareTo(result), "got " + result);
    }

    @Test
    void negativeValueOrMissingModeIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> service.calculateFinalAmount(new BigDecimal("-1"), DeliveryMode.URGENT));
        assertThrows(IllegalArgumentException.class,
                () -> service.calculateFinalAmount(BigDecimal.TEN, null));
    }
}
