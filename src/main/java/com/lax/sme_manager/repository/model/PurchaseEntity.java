package com.lax.sme_manager.repository.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * PurchaseEntity - Database representation of purchase
 * Mapped directly to 'purchase_entries' table
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseEntity {
    private Integer id;
    private LocalDate entryDate;
    private Integer vendorId;
    private Integer bags;
    private BigDecimal rate;
    private BigDecimal weightKg;
    private Boolean isLumpsum;
    private BigDecimal marketFeePercent;
    private BigDecimal commissionPercent;
    private BigDecimal marketFeeAmount;
    private BigDecimal commissionFeeAmount;
    private BigDecimal baseAmount;
    private BigDecimal grandTotal;
    private String notes;
    private String paymentMode;
    private Boolean advancePaid;
    private String status;
    private String chequeNumber;
    private LocalDate chequeDate;
    private Boolean isDeleted;
    private String createdByUser;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
