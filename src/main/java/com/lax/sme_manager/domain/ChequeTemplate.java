package com.lax.sme_manager.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChequeTemplate implements Serializable {
    private Integer id;
    private String bankName;
    private String templateName;
    private String backgroundImagePath;

    // Coordinates in pixels or percentage (percentage is better for scaling)
    @Builder.Default
    private double dateX = 0.85;
    @Builder.Default
    private double dateY = 0.10;

    @Builder.Default
    private double payeeX = 0.15;
    @Builder.Default
    private double payeeY = 0.25;

    @Builder.Default
    private double amountWordsX = 0.15;
    @Builder.Default
    private double amountWordsY = 0.35;

    @Builder.Default
    private double amountDigitsX = 0.80;
    @Builder.Default
    private double amountDigitsY = 0.45;

    @Builder.Default
    private double signatureX = 0.80;
    @Builder.Default
    private double signatureY = 0.80;

    @Builder.Default
    private int fontSize = 14;
    @Builder.Default
    private String fontFamily = "Arial";
    @Builder.Default
    private String fontColor = "#000000";

    // MM-based coordinates for professional designer (V2 schema)
    // These take precedence over percentage coordinates when set
    private Double dateXMm;
    private Double dateYMm;
    private Double payeeXMm;
    private Double payeeYMm;
    private Double amountWordsXMm;
    private Double amountWordsYMm;
    private Double amountDigitsXMm;
    private Double amountDigitsYMm;
    private Double signatureXMm;
    private Double signatureYMm;

    /**
     * Default template layout based on common Indian cheques
     */
    public static ChequeTemplate createDefault() {
        return ChequeTemplate.builder()
                .id(-1)
                .bankName("Default Bank")
                .templateName("Default Indian Cheque")
                .dateX(0.82).dateY(0.08)
                .payeeX(0.12).payeeY(0.22)
                .amountWordsX(0.12).amountWordsY(0.32)
                .amountDigitsX(0.82).amountDigitsY(0.42)
                .signatureX(0.75).signatureY(0.80)
                .fontSize(16)
                .fontFamily("Inter")
                .fontColor("#1e293b")
                .build();
    }
}
