package com.group_2.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Locale;

// Centralized utility for formatting (currency, dates, ordinal numbers)
public final class FormatUtils {

    private static final DecimalFormat CURRENCY_FORMAT;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter SHORT_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM");

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.GERMANY);
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        CURRENCY_FORMAT = new DecimalFormat("€#,##0.00", symbols);
    }

    private FormatUtils() {
        // Utility class - prevent instantiation
    }

    public static String formatCurrency(double amount) {
        return CURRENCY_FORMAT.format(amount);
    }

    // Format with "+" for positive, "-" for negative amounts
    public static String formatCurrencyWithSign(double amount) {
        if (amount == 0) {
            return formatCurrency(0);
        }
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.GERMANY);
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        // subpattern: positive;negative
        DecimalFormat signedFormat = new DecimalFormat("+€#,##0.00;-€#,##0.00", symbols);
        return signedFormat.format(amount);
    }

    public static String getDaySuffix(int day) {
        if (day >= 11 && day <= 13) {
            return "th";
        }
        switch (day % 10) {
            case 1:
                return "st";
            case 2:
                return "nd";
            case 3:
                return "rd";
            default:
                return "th";
        }
    }

    public static String formatDayWithSuffix(int day) {
        return day + getDaySuffix(day);
    }

    public static String formatDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(DATE_FORMATTER);
    }

    public static String formatShortDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(SHORT_DATE_FORMATTER);
    }

    // Formats week title like "Week 1, 2026"
    public static String formatWeekTitle(LocalDate weekStart) {
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int weekNumber = weekStart.get(weekFields.weekOfWeekBasedYear());
        int year = weekStart.getYear();
        return "Week " + weekNumber + ", " + year;
    }

    // Formats week date range like "January 6 - January 12"
    public static String formatWeekDateRange(LocalDate weekStart) {
        LocalDate weekEnd = weekStart.plusDays(6);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d");
        return weekStart.format(formatter) + " - " + weekEnd.format(formatter);
    }

    // Truncates a string to maxLength with ellipsis
    public static String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text == null ? "" : text;
        }
        return text.substring(0, maxLength - 1) + "...";
    }

    // Formats day name with number like "Monday, 6"
    public static String formatDayNameWithNumber(LocalDate date) {
        if (date == null) {
            return "";
        }
        String dayName = date.getDayOfWeek().toString().substring(0, 1)
                + date.getDayOfWeek().toString().substring(1).toLowerCase();
        return dayName + ", " + date.getDayOfMonth();
    }
}
