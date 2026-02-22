package com.lax.sme_manager.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO representing the data needed to print a cheque.
 * Decoupled from PurchaseEntity to allow manual/standalone cheque printing.
 */
public record ChequeData(
        String payeeName,
        BigDecimal amount,
        LocalDate date,
        boolean isAcPayee,
        Integer purchaseId,
        String chequeNumber) {
}
