package com.lax.sme_manager.util;

import java.math.BigDecimal;

/**
 * Utility to convert numeric amounts to words (Indian Numbering System).
 * Supports up to Crores.
 */
public class AmountToWordsConverter {

    private static final String[] UNITS = {
            "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten",
            "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"
    };

    private static final String[] TENS = {
            "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
    };

    /**
     * Converts a BigDecimal amount to words.
     * Example: 1234.50 -> "One Thousand Two Hundred Thirty Four Rupees and Fifty
     * Paise Only"
     */
    public static String convert(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
            return "Zero Rupees Only";
        }

        long rupees = amount.longValue();
        int paise = amount.remainder(BigDecimal.ONE).multiply(BigDecimal.valueOf(100)).intValue();

        StringBuilder result = new StringBuilder();

        if (rupees > 0) {
            result.append(convertToWords(rupees).trim()).append(" Rupees");
        }

        if (paise > 0) {
            if (rupees > 0) {
                result.append(" and ");
            }
            result.append(convertToWords(paise).trim()).append(" Paise");
        }

        result.append(" Only");
        return result.toString();
    }

    private static String convertToWords(long n) {
        if (n == 0)
            return "";

        if (n < 20) {
            return UNITS[(int) n];
        }

        if (n < 100) {
            return TENS[(int) (n / 10)] + " " + convertToWords(n % 10);
        }

        if (n < 1000) {
            return UNITS[(int) (n / 100)] + " Hundred " + convertToWords(n % 100);
        }

        if (n < 100000) {
            return convertToWords(n / 1000) + " Thousand " + convertToWords(n % 1000);
        }

        if (n < 10000000) {
            return convertToWords(n / 100000) + " Lakh " + convertToWords(n % 100000);
        }

        return convertToWords(n / 10000000) + " Crore " + convertToWords(n % 10000000);
    }
}
