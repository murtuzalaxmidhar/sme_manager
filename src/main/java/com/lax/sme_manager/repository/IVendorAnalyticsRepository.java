package com.lax.sme_manager.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public interface IVendorAnalyticsRepository {
    /**
     * Gets monthly bags count for the last 12 months for a specific vendor.
     * key: YearMonth string (YYYY-MM), value: total bags
     */
    Map<String, Integer> getMonthlySupplyTrend(int vendorId);

    /**
     * Gets price history (rate changes) for a specific vendor.
     * key: entry_date, value: rate
     */
    Map<LocalDate, BigDecimal> getPriceHistory(int vendorId);

    /**
     * Gets summary metrics for a vendor.
     * key: metric_name, value: value (as Object)
     */
    Map<String, Object> getVendorSummary(int vendorId);
}
