package com.lax.sme_manager.util;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Immutable filter object for purchases
 * Combines all filter criteria
 */
public class PurchaseFilter {
    
    public enum DateRangeType {
        TODAY,
        LAST_7_DAYS,
        LAST_30_DAYS,
        CUSTOM_RANGE
    }
    
    private final DateRangeType dateRangeType;
    private final LocalDate customStartDate;
    private final LocalDate customEndDate;
    private final Integer vendorId;
    private final Double minAmount;
    private final Double maxAmount;
    private final Boolean chequeIssued;  // null = no filter, true = only cheque, false = no cheque
    private final int pageNumber;
    private final int pageSize;
    
    public PurchaseFilter(DateRangeType dateRangeType, LocalDate customStartDate, LocalDate customEndDate,
                         Integer vendorId, Double minAmount, Double maxAmount,
                         Boolean chequeIssued, int pageNumber, int pageSize) {
        this.dateRangeType = Objects.requireNonNull(dateRangeType, "DateRangeType cannot be null");
        this.customStartDate = customStartDate;
        this.customEndDate = customEndDate;
        this.vendorId = vendorId;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.chequeIssued = chequeIssued;
        this.pageNumber = Math.max(1, pageNumber);
        this.pageSize = Math.max(10, pageSize);
    }
    
    // Getters
    public DateRangeType getDateRangeType() { return dateRangeType; }
    public LocalDate getCustomStartDate() { return customStartDate; }
    public LocalDate getCustomEndDate() { return customEndDate; }
    public Integer getVendorId() { return vendorId; }
    public Double getMinAmount() { return minAmount; }
    public Double getMaxAmount() { return maxAmount; }
    public Boolean getChequeIssued() { return chequeIssued; }
    public int getPageNumber() { return pageNumber; }
    public int getPageSize() { return pageSize; }
    public int getOffset() { return (pageNumber - 1) * pageSize; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PurchaseFilter that = (PurchaseFilter) o;
        return pageNumber == that.pageNumber &&
                dateRangeType == that.dateRangeType &&
                Objects.equals(customStartDate, that.customStartDate) &&
                Objects.equals(customEndDate, that.customEndDate) &&
                Objects.equals(vendorId, that.vendorId) &&
                Objects.equals(minAmount, that.minAmount) &&
                Objects.equals(maxAmount, that.maxAmount) &&
                Objects.equals(chequeIssued, that.chequeIssued);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(dateRangeType, customStartDate, customEndDate, vendorId, minAmount, maxAmount, chequeIssued, pageNumber);
    }
}
