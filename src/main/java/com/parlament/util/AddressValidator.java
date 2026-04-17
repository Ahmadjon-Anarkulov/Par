package com.parlament.util;

import java.util.Set;

/**
 * Validates user input for checkout: name, phone, country, city, street.
 */
public final class AddressValidator {

    private AddressValidator() {}

    // ─── Name ───────────────────────────────────────────────────────────────

    /**
     * Valid name: only letters (Cyrillic / Latin), spaces, hyphens. Length 2–50.
     */
    public static boolean isValidName(String input) {
        if (input == null) return false;
        String trimmed = input.trim();
        if (trimmed.length() < 2 || trimmed.length() > 50) return false;
        // Allow Cyrillic letters, Latin letters, spaces, hyphens; no digits, no symbols
        return trimmed.matches("[\\p{L} \\-]+");
    }

    // ─── Phone ───────────────────────────────────────────────────────────────

    public static boolean isValidPhone(String input) {
        if (input == null) return false;
        String trimmed = input.trim();
        // Accepts: +998901234567  +7 900 123 45 67  8-800-555-35-35  etc.
        return trimmed.matches("[+\\d][\\d\\s\\-().]{5,20}");
    }

    // ─── Country ─────────────────────────────────────────────────────────────

    /** Normalised to lowercase for comparison */
    private static final Set<String> SUPPORTED_COUNTRIES = Set.of(
            "узбекистан", "uzbekistan",
            "россия", "russia",
            "казахстан", "kazakhstan",
            "кыргызстан", "kyrgyzstan",
            "таджикистан", "tajikistan",
            "туркменистан", "turkmenistan",
            "беларусь", "belarus",
            "германия", "germany",
            "сша", "usa", "united states",
            "великобритания", "uk", "united kingdom",
            "турция", "turkey",
            "оаэ", "uae",
            "другая"
    );

    public static boolean isValidCountry(String input) {
        if (input == null || input.isBlank()) return false;
        return SUPPORTED_COUNTRIES.contains(input.trim().toLowerCase());
    }

    public static boolean isUzbekistan(String input) {
        if (input == null) return false;
        String lower = input.trim().toLowerCase();
        return lower.equals("узбекистан") || lower.equals("uzbekistan");
    }

    // ─── Uzbek cities ─────────────────────────────────────────────────────────

    public static final Set<String> UZBEK_CITIES = Set.of(
            "ташкент", "tashkent",
            "самарканд", "samarkand",
            "бухара", "bukhara",
            "андижан", "andijan",
            "наманган", "namangan",
            "фергана", "fergana",
            "нукус", "nukus",
            "карши", "karshi", "qarshi",
            "термез", "termez",
            "джизак", "jizzakh",
            "навои", "navoi",
            "гулистан", "gulistan",
            "ургенч", "urgench",
            "коканд", "kokand",
            "маргилан", "margilan",
            "чирчик", "chirchiq",
            "ангрен", "angren",
            "алмалык", "almalyk",
            "беруни", "beruni",
            "бекабад", "bekabad"
    );

    public static boolean isValidUzbekCity(String input) {
        if (input == null || input.isBlank()) return false;
        return UZBEK_CITIES.contains(input.trim().toLowerCase());
    }

    // ─── Street / house ───────────────────────────────────────────────────────

    public static boolean isValidStreet(String input) {
        if (input == null || input.isBlank()) return false;
        String trimmed = input.trim();
        return trimmed.length() >= 5 && trimmed.length() <= 300;
    }
}
