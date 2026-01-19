package com.lax.sme_manager.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * PurchaseAggregate - Core business model for purchase entry
 * Contains all business logic for purchase operations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseAggregate {
    private Integer id;
    private LocalDate entryDate;
    private Integer vendorId;
    private Integer bags;
    private Double rate;
    private Double weightKg;
    private Boolean isLumpsum;
    private Double yardFeePercent;
    private Double vendorFeePercent;
    private Double yardFeeAmount;
    private Double vendorFeeAmount;
    private Double marketFeeAmount;
    private Double baseAmount;
    private Double grandTotal;
    private String paymentMode;          // CASH or CHEQUE_LATER
    private Boolean advancePaid;
    private Double advanceAmount;
    private String status;               // DRAFT, COMPLETED, CANCELLED
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Calculate all fees based on current values
     * Business rule: If advance paid, no fees are charged (set to 0)
     * But user can override fees if they choose to
     */
    public void calculateFees() {
        // Base amount = Bags × Rate
        this.baseAmount = this.bags * this.rate;

        // If advance paid, set fees to 0 by default
        if (Boolean.TRUE.equals(this.advancePaid)) {
            // Reset to 0, but caller can override if needed
            if (this.yardFeeAmount == null) this.yardFeeAmount = 0.0;
            if (this.vendorFeeAmount == null) this.vendorFeeAmount = 0.0;
        } else {
            // Calculate fees based on percentages
            if (this.yardFeePercent != null && this.yardFeePercent > 0) {
                this.yardFeeAmount = (this.baseAmount * this.yardFeePercent) / 100;
            } else {
                this.yardFeeAmount = 0.0;
            }

            if (this.vendorFeePercent != null && this.vendorFeePercent > 0) {
                this.vendorFeeAmount = (this.baseAmount * this.vendorFeePercent) / 100;
            } else {
                this.vendorFeeAmount = 0.0;
            }
        }

        // Market Fee = Yard Fee + Vendor Fee (derived, always calculated)
        this.marketFeeAmount = (this.yardFeeAmount != null ? this.yardFeeAmount : 0.0) +
                               (this.vendorFeeAmount != null ? this.vendorFeeAmount : 0.0);

        // Grand Total = Base Amount + Market Fee
        this.grandTotal = this.baseAmount + this.marketFeeAmount;
    }

    /**
     * Validate purchase entry
     */
    public void validate() throws IllegalArgumentException {
        if (this.entryDate == null) {
            throw new IllegalArgumentException("Entry date is required");
        }
        if (this.vendorId == null || this.vendorId <= 0) {
            throw new IllegalArgumentException("Valid vendor is required");
        }
        if (this.bags == null || this.bags <= 0) {
            throw new IllegalArgumentException("Bags must be greater than 0");
        }
        if (this.rate == null || this.rate <= 0) {
            throw new IllegalArgumentException("Rate must be greater than 0");
        }
        if (!Boolean.TRUE.equals(this.isLumpsum) && (this.weightKg == null || this.weightKg <= 0)) {
            throw new IllegalArgumentException("Weight is required if not lumpsum");
        }
    }
}
