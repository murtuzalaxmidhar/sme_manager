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
public class ChequeBook {
    private int id;
    private String bookName;
    private String bankName;
    private long startNumber;
    private long endNumber;
    private long nextNumber; // The next leaf to be used
    private boolean isActive;
    private LocalDateTime createdAt;

    /**
     * Helper to check if the book has any leaves left.
     */
    public boolean isExhausted() {
        return nextNumber > endNumber;
    }

    /**
     * Helper to get remaining leaves.
     */
    public long getRemainingLeaves() {
        if (isExhausted())
            return 0;
        return (endNumber - nextNumber) + 1;
    }
}
