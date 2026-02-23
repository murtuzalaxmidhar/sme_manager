package com.lax.sme_manager.repository.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrintLedgerEntry {
    private int id;
    private Integer userId;
    private String username; // Helper for display, join from users table
    private String payeeName;
    private double amount;
    private String chequeNumber;
    private String printStatus; // SUCCESS, FAILED, CANCELLED
    private String remarks;
    private LocalDateTime printedAt;
}
