package com.lax.sme_manager.util.i18n;

public enum LanguageMode {
    ENGLISH,
    GUJARATI,
    BILINGUAL; // English (Gujarati)

    @Override
    public String toString() {
        switch (this) {
            case ENGLISH:
                return "English";
            case GUJARATI:
                return "Gujarati (\u0A97\u0AC1\u0A9C\u0AB0\u0ABE\u0AA4\u0AC0)";
            case BILINGUAL:
                return "Both / Bilingual";
            default:
                return super.toString();
        }
    }
}
