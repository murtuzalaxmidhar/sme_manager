package com.lax.sme_manager.logic;

import org.junit.Test;
import java.math.BigDecimal;
import static org.junit.Assert.assertEquals;

public class FeeCalculatorTest {

    @Test
    public void testBaseAmountCalculation_Lumpsum() {
        // Bags 10, Rate 100, Lumpsum = 1000
        BigDecimal result = FeeCalculator.calculateBaseAmount(true, 10, BigDecimal.ZERO, new BigDecimal("100"));
        assertEquals(new BigDecimal("1000.00"), result);
    }

    @Test
    public void testBaseAmountCalculation_Weight() {
        // Weight 100kg, Rate 200, Formula: (100 * 200) / 20 = 20000 / 20 = 1000
        BigDecimal result = FeeCalculator.calculateBaseAmount(false, 0, new BigDecimal("100"), new BigDecimal("200"));
        assertEquals(new BigDecimal("1000.00"), result);
    }

    @Test
    public void testFeeCalculation() {
        // Amount 1000, Fee 0.70% = 7.00
        BigDecimal fee = FeeCalculator.calculateFee(new BigDecimal("1000"), new BigDecimal("0.70"));
        assertEquals(new BigDecimal("7.00"), fee);
    }

    @Test
    public void testGrandTotal() {
        // Base 1000, Market 7, Commission 20 = 1027
        BigDecimal total = FeeCalculator.calculateGrandTotal(
                new BigDecimal("1000"),
                new BigDecimal("7"),
                new BigDecimal("20"));
        assertEquals(new BigDecimal("1027.00"), total);
    }
}
