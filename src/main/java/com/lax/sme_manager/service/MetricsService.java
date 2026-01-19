package com.lax.sme_manager.service;

import com.lax.sme_manager.repository.IPurchaseRepository;
import lombok.Builder;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.concurrent.CompletableFuture;

/**
 * Service to calculate Dashboard Metrics.
 * Handles date ranges and delegates to Repository.
 */
public class MetricsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MetricsService.class);
    private final IPurchaseRepository purchaseRepository;

    public MetricsService(IPurchaseRepository purchaseRepository) {
        this.purchaseRepository = purchaseRepository;
    }

    @Data
    @Builder
    public static class DashboardMetrics {
        private int bagsToday;
        private int bagsThisWeek;
        private int bagsThisMonth;
        private double amountThisMonth;
        private int unpaidChequesTotal;
        private double avgRateThisMonth;
        private int chequesClearingToday;
    }

    /**
     * Fetch all dashboard metrics asynchronously.
     */
    public CompletableFuture<DashboardMetrics> getDashboardMetricsAsync() {
        return CompletableFuture.supplyAsync(this::getDashboardMetrics);
    }

    public DashboardMetrics getDashboardMetrics() {
        LocalDate today = LocalDate.now();

        // Date Ranges
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        LocalDate startOfMonth = today.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate endOfMonth = today.with(TemporalAdjusters.lastDayOfMonth());

        LOGGER.debug("Calculating metrics for Today: {}, Week: {} to {}, Month: {} to {}",
                today, startOfWeek, endOfWeek, startOfMonth, endOfMonth);

        // 1. Bags Count
        int bagsToday = purchaseRepository.getBagsCount(today, today);
        int bagsWeek = purchaseRepository.getBagsCount(startOfWeek, endOfWeek);
        int bagsMonth = purchaseRepository.getBagsCount(startOfMonth, endOfMonth);

        // 2. Amount (This Month)
        double amountMonth = purchaseRepository.getTotalAmount(startOfMonth, endOfMonth);

        // 3. Unpaid Cheques (Total All-time)
        // Count all entries where payment is CHEQUE but status is not PAID
        int unpaidCheques = purchaseRepository.countPendingCheques(LocalDate.of(2000, 1, 1),
                LocalDate.of(2100, 12, 31));

        // 4. Avg Rate (Month)
        double avgRate = (bagsMonth > 0) ? (amountMonth / bagsMonth) : 0.0;

        // 5. Cheques Clearing Today (using cheque_date)
        int clearingToday = purchaseRepository.countChequesByClearingDate(today);

        return DashboardMetrics.builder()
                .bagsToday(bagsToday)
                .bagsThisWeek(bagsWeek)
                .bagsThisMonth(bagsMonth)
                .amountThisMonth(amountMonth)
                .unpaidChequesTotal(unpaidCheques)
                .avgRateThisMonth(avgRate)
                .chequesClearingToday(clearingToday)
                .build();
    }
}
