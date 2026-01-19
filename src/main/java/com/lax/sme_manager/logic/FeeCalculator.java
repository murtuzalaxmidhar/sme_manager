package com.lax.sme_manager.logic;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Pure business logic for Fee and Amount calculations.
 * Stateless and unit-testable.
 */
public class FeeCalculator {

    private static final BigDecimal TWENTY = new BigDecimal("20");
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    /**
     * Calculate Base Amount.
     * If Lumpsum: Bags * Rate
     * If Weight-based: (Weight * Rate) / 20
     */
    public static BigDecimal calculateBaseAmount(boolean isLumpsum, int bags, BigDecimal weight, BigDecimal rate) {
        if (rate == null || rate.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        if (isLumpsum) {
            return new BigDecimal(bags).multiply(rate).setScale(2, RoundingMode.HALF_UP);
        } else {
            if (weight == null)
                weight = BigDecimal.ZERO;
            // (Weight * Rate) / 20
            return weight.multiply(rate)
                    .divide(TWENTY, 6, RoundingMode.HALF_UP)
                    .setScale(2, RoundingMode.HALF_UP);
        }
    }

    /**
     * Calculate Fee Amount based on percentage.
     * Fee = BaseAmount * (Percent / 100)
     */
    public static BigDecimal calculateFee(BigDecimal baseAmount, BigDecimal percent) {
        if (baseAmount == null || percent == null)
            return BigDecimal.ZERO;
        return baseAmount.multiply(percent)
                .divide(HUNDRED, 6, RoundingMode.HALF_UP)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate Grand Total.
     * Total = Base + MarketFee + Commission
     */
    public static BigDecimal calculateGrandTotal(BigDecimal baseAmount, BigDecimal marketFee, BigDecimal commission) {
        BigDecimal total = (baseAmount != null ? baseAmount : BigDecimal.ZERO);
        if (marketFee != null)
            total = total.add(marketFee);
        if (commission != null)
            total = total.add(commission);
        return total.setScale(2, RoundingMode.HALF_UP);
    }
}
