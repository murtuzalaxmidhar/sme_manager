package com.lax.sme_manager.repository.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrintQueueItem {
    private int id;
    private Integer purchaseId; // Nullable for generic cheques
    private String payeeName;
    private double amount;
    private LocalDate chequeDate;
    private boolean isAcPayee;
    private LocalDateTime createdAt;
}
