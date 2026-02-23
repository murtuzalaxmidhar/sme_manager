package com.lax.sme_manager.repository;

import java.time.LocalDate;
import java.util.Map;

public interface ITrendRepository {
    /**
     * Gets daily bags count for the last N days.
     * key: date, value: total bags
     */
    Map<LocalDate, Integer> getWeeklyBagsTrend();

    /**
     * Gets distribution of payment modes.
     * key: payment_mode, value: count
     */
    Map<String, Integer> getPaymentModeDistribution();

    /**
     * Gets top vendors by transaction volume (bags).
     * key: vendor_name, value: total bags
     */
    Map<String, Integer> getTopVendors(int limit);
}
