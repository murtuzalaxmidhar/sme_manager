package com.lax.sme_manager.util.i18n;

/**
 * Modern Enum-based Label Management with built-in I18N support.
 * Replaces legacy AppLabels class.
 * ENSURING UNICODE ESCAPES.
 */
public enum AppLabel {
    // Titles
    TITLE_PURCHASE_ENTRY("➕ Purchase Entry", null),
    TITLE_PURCHASE_HISTORY("📋 Purchase History", null),
    TITLE_DASHBOARD("📊 Dashboard", null),
    TITLE_SETTINGS("⚙️ Settings", null),
    TITLE_CHEQUE_PREVIEW("🖋️ Cheque Preview", "ચેક પ્રિવ્યુ"),
    TITLE_PURCHASE_DETAILS("Purchase Details", null),

    // Actions & Buttons
    ACTION_SUBMIT("Submit Entry", null),
    ACTION_VIEW_DETAILS("View Details", "વિગત જુઓ"),
    ACTION_APPLY("Apply", null),
    ACTION_RESET("Reset", null),
    ACTION_EDIT("Edit", "ફેરફાર કરો"),
    ACTION_DELETE("Delete", "કાઢી નાખો"),
    ACTION_EXPORT("Export to Excel", null),

    // Form Labels
    LBL_VENDOR("Vendor", "વેપારી"),
    LBL_PURCHASE_DATE("Purchase Date", "ખરીદી તારીખ"),
    LBL_CHEQUE_DATE("Cheque Date", "ચેક તારીખ"),
    LBL_BAGS("Bags", "બેગ"),
    LBL_RATE("Rate", "ભાવ"),
    LBL_WEIGHT("Weight (kg)", "વજન"),
    LBL_AMOUNT("Amount", "રકમ"),
    LBL_TOTAL("Total", "ટોટલ"),
    LBL_STATUS("Payment Status", "પેમેન્ટ સ્ટેટસ"),
    LBL_PAYMENT_MODE("Payment Mode", "પેમેન્ટ પધ્ધતિ"),
    LBL_NOTES("Notes/Comments", "નોટસ/સુચના"),
    LBL_LUMPSUM("Lumpsum", "ઉધડું"), // Uchchak
    LBL_PAID_IN_ADVANCE("Paid in Advance", "એડવાન્સ પેમેન્ટ"),

    // Dashboard
    METRIC_BAGS_TODAY("Today", "આજ ની ખરીદી"),
    METRIC_BAGS_WEEK("This Week", "આ અઠવાડિયા ની ખરીદી"),
    METRIC_BAGS_MONTH("This Month", "આ મહિના ની ખરીદી"),
    METRIC_UNPAID_CHEQUES("Unpaid Cheques", "ચેક બાકી (UNPAID)"),

    // Misc
    MSG_LOADING("Loading...", "\u0AB2\u0ACB\u0AA1 \u0AA5\u0A88 \u0AB0\u0AB9\u0acd\u0AAF\u0AC1\u0A82"),

    // Wizard Specific
    ACTION_PRINT_CHEQUE("Print Cheque", "ચેક પ્રિન્ટ"),
    LBL_AMOUNT_WORDS("Amount in Words", "અંકે રૂપિયા"),
    LBL_SIGNATURE_CONFIG("Signature Config", "સહી સેટિંગ્સ");

    private final String english;
    private final String gujarati;

    AppLabel(String english, String gujarati) {
        this.english = english;
        this.gujarati = gujarati;
    }

    public String get() {
        return switch (LanguageManager.getInstance().getMode()) {
            case GUJARATI -> !(gujarati == null || gujarati.isEmpty()) ? gujarati : english;
            case BILINGUAL -> !(gujarati == null || gujarati.isEmpty()) ? english + " (" + gujarati + ")" : english;
            default -> english;
        };
    }

    public String getEnglish() {
        return english;
    }

    public String getGujarati() {
        return gujarati;
    }
}
