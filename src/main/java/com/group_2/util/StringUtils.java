package com.group_2.util;

/**
 * Centralized utility class for string manipulation used across the
 * application.
 * Provides consistent methods for common string operations.
 */
public final class StringUtils {

    private StringUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Gets the initial letter of a string in uppercase.
     * Returns "?" if the input is null or empty.
     * This method is used for avatar initials, user display, etc.
     *
     * @param text the text to extract the initial from
     * @return the first character in uppercase, or "?" if null/empty
     */
    public static String getInitial(String text) {
        if (text == null || text.isEmpty()) {
            return "?";
        }
        return text.substring(0, 1).toUpperCase();
    }

    /**
     * Gets the initial letter of a string in uppercase with a custom fallback.
     *
     * @param text     the text to extract the initial from
     * @param fallback the fallback string if text is null or empty
     * @return the first character in uppercase, or the fallback if null/empty
     */
    public static String getInitial(String text, String fallback) {
        if (text == null || text.isEmpty()) {
            return fallback;
        }
        return text.substring(0, 1).toUpperCase();
    }

    /**
     * Pluralizes a word based on count.
     * Example: pluralize(1, "room", "rooms") returns "1 room"
     * Example: pluralize(3, "room", "rooms") returns "3 rooms"
     *
     * @param count    the count to use
     * @param singular the singular form of the word
     * @param plural   the plural form of the word
     * @return the count followed by the appropriate word form
     */
    public static String pluralize(int count, String singular, String plural) {
        return count + " " + (count == 1 ? singular : plural);
    }

    /**
     * Pluralizes a word based on count (without including the count in the result).
     * Example: pluralizeWord(1, "room", "rooms") returns "room"
     * Example: pluralizeWord(3, "room", "rooms") returns "rooms"
     *
     * @param count    the count to use
     * @param singular the singular form of the word
     * @param plural   the plural form of the word
     * @return the appropriate word form based on count
     */
    public static String pluralizeWord(int count, String singular, String plural) {
        return count == 1 ? singular : plural;
    }
}
