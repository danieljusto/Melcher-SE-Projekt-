package com.group_2.util;

// Utility class for string manipulation
public final class StringUtils {

    private StringUtils() {
        // Utility class - prevent instantiation
    }

    // Returns "?" if null/empty, otherwise the first letter uppercased
    public static String getInitial(String text) {
        if (text == null || text.isEmpty()) {
            return "?";
        }
        return text.substring(0, 1).toUpperCase();
    }

    public static String getInitial(String text, String fallback) {
        if (text == null || text.isEmpty()) {
            return fallback;
        }
        return text.substring(0, 1).toUpperCase();
    }

    // Pluralizes words: pluralize(1, "room", "rooms") -> "1 room"
    public static String pluralize(int count, String singular, String plural) {
        return count + " " + (count == 1 ? singular : plural);
    }

    // Returns only the word without the count
    public static String pluralizeWord(int count, String singular, String plural) {
        return count == 1 ? singular : plural;
    }
}
