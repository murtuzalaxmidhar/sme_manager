package com.lax.sme_manager.repository.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignatureConfig {
    private int id;
    private String name;
    private String path;
    private double opacity; // 0.0 to 1.0
    private double thickness; // Tuning factor for "pen-authentic" look
    private boolean isTransparent; // Whether to remove white background
    private double scale; // Scale factor for the signature
}
