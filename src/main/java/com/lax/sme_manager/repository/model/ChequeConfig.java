package com.lax.sme_manager.repository.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChequeConfig {
    private int id;
    private String bankName;
    private boolean isAcPayee;
    private int fontSize;
    private int activeSignatureId;

    // Coordinates in mm (Millimeters)
    private double dateX;
    private double dateY;

    private double payeeX;
    private double payeeY;

    private double amountWordsX;
    private double amountWordsY;

    private double amountDigitsX;
    private double amountDigitsY;

    private double signatureX;
    private double signatureY;

    private String signaturePath; // Path to signature image

    /**
     * Stores individual digit positions as JSON or CSV
     * Format: "x,y;x,y;..." for 8 digits
     */
    private String datePositions;
}
