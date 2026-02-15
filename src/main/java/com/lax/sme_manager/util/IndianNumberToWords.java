package com.lax.sme_manager.util;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class IndianNumberToWords {

    private static final String[] units = {
            "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten",
            "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"
    };

    private static final String[] tens = {
            "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
    };

    public static String convert(BigDecimal amount) {
        if (amount == null)
            return "";

        long rupees = amount.longValue();
        int paise = amount.remainder(BigDecimal.ONE).multiply(new BigDecimal(100)).intValue();

        String words = convertNumber(rupees) + " Rupees";

        if (paise > 0) {
            words += " and " + convertNumber(paise) + " Paise";
        }

        return words + " Only";
    }

    private static String convertNumber(long n) {
        if (n < 0)
            return "Minus " + convertNumber(-n);
        if (n < 20)
            return units[(int) n];
        if (n < 100)
            return tens[(int) n / 10] + ((n % 10 != 0) ? " " : "") + units[(int) n % 10];
        if (n < 1000)
            return units[(int) n / 100] + " Hundred" + ((n % 100 != 0) ? " " : "") + convertNumber(n % 100);
        if (n < 100000)
            return convertNumber(n / 1000) + " Thousand" + ((n % 1000 != 0) ? " " : "") + convertNumber(n % 1000);
        if (n < 10000000)
            return convertNumber(n / 100000) + " Lakh" + ((n % 100000 != 0) ? " " : "") + convertNumber(n % 100000);
        return convertNumber(n / 10000000) + " Crore" + ((n % 10000000 != 0) ? " " : "") + convertNumber(n % 10000000);
    }
}
