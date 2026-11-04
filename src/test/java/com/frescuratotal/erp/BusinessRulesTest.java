package com.frescuratotal.erp;

import com.frescuratotal.erp.common.util.InputUtil;
import com.frescuratotal.erp.inventory.model.LotStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BusinessRulesTest {

    @Test
    void lotStatusFollowsTheExpirationDate() {
        LocalDate today = LocalDate.now();
        assertEquals(LotStatus.EXPIRED, LotStatus.forExpiration(today.minusDays(1)));
        assertEquals(LotStatus.NEAR_EXPIRY, LotStatus.forExpiration(today));
        assertEquals(LotStatus.NEAR_EXPIRY, LotStatus.forExpiration(today.plusDays(7)));
        assertEquals(LotStatus.AVAILABLE, LotStatus.forExpiration(today.plusDays(8)));
    }

    @Test
    void amountsMustFitInDecimal10_2() {
        for (String valid : new String[]{"0.01", "1", "1.50", "1.500", "99999999.99"}) {
            assertDoesNotThrow(() -> InputUtil.requireAmount(new BigDecimal(valid), "Amount"), valid);
        }
        for (String invalid : new String[]{"0", "-1", "0.004", "100000000", "1e999999999", "1e-1000000"}) {
            assertThrows(IllegalArgumentException.class,
                    () -> InputUtil.requireAmount(new BigDecimal(invalid), "Amount"), invalid);
        }
        assertThrows(IllegalArgumentException.class, () -> InputUtil.requireAmount(null, "Amount"));
    }
}
