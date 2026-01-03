package com.group_2.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;

import com.group_2.model.cleaning.RecurrenceInterval;

/**
 * Utility class for monthly schedule calculations.
 * Provides methods for date resolution and base week calculations
 * used by template editors and schedule generation.
 */
public final class MonthlyScheduleUtil {
    private MonthlyScheduleUtil() {
    }

    public static LocalDate resolveMonthlyDate(LocalDate targetMonth, Integer monthlyDay, Boolean monthlyLastDay) {
        if (Boolean.TRUE.equals(monthlyLastDay)) {
            return targetMonth.withDayOfMonth(getEffectiveLastDay(targetMonth));
        }
        if (monthlyDay != null && monthlyDay >= 1 && monthlyDay <= 31) {
            return targetMonth.withDayOfMonth(getEffectiveDay(targetMonth, monthlyDay));
        }
        return targetMonth.withDayOfMonth(1);
    }

    public static int getEffectiveDay(LocalDate targetMonth, int preferredDay) {
        if (targetMonth.getMonth() == Month.FEBRUARY && preferredDay > 28) {
            return 28;
        }
        return Math.min(preferredDay, targetMonth.lengthOfMonth());
    }

    public static int getEffectiveLastDay(LocalDate targetMonth) {
        if (targetMonth.getMonth() == Month.FEBRUARY) {
            return 28;
        }
        return targetMonth.lengthOfMonth();
    }

    /**
     * Calculates the Monday of the week containing the given date.
     * This is used as the base week start for templates.
     *
     * @param date the date to find the week start for
     * @return the Monday of the same week as the given date
     */
    public static LocalDate calculateBaseWeekStart(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    /**
     * Resolves the base date for a template based on interval and selection.
     * For monthly intervals with "last day" selected, finds a month with 31 days.
     * Otherwise, uses the selected date or falls back to the provided default.
     *
     * @param selectedDate    the user-selected date (may be null for last-day
     *                        monthly)
     * @param interval        the recurrence interval
     * @param lastDaySelected whether "last day of month" is selected
     * @param fallbackDate    fallback date if selectedDate is null
     * @return the resolved base date, or null if resolution fails
     */
    public static LocalDate resolveBaseDate(LocalDate selectedDate, RecurrenceInterval interval,
            boolean lastDaySelected, LocalDate fallbackDate) {
        if (interval == RecurrenceInterval.MONTHLY && lastDaySelected) {
            LocalDate reference = fallbackDate != null ? fallbackDate : LocalDate.now();
            return resolveLastDayBaseDate(reference);
        }
        return selectedDate != null ? selectedDate : fallbackDate;
    }

    /**
     * Finds a base date representing the 31st day of a month.
     * Searches up to 12 months ahead from the reference date to find a month
     * with 31 days (e.g., January, March, May, July, August, October, December).
     * 
     * This is used for templates scheduled for "last day of month" to ensure
     * the template's base date is on day 31, which will then be adjusted
     * during task generation based on the actual month length.
     *
     * @param reference the reference date to start searching from
     * @return a date on the 31st of a month, or the last day of the reference month
     *         if none found
     */
    public static LocalDate resolveLastDayBaseDate(LocalDate reference) {
        LocalDate base = reference.withDayOfMonth(1);
        for (int i = 0; i < 12; i++) {
            LocalDate month = base.plusMonths(i);
            if (month.lengthOfMonth() >= 31) {
                return month.withDayOfMonth(31);
            }
        }
        // Fallback: use last day of current month
        return reference.withDayOfMonth(Math.min(31, reference.lengthOfMonth()));
    }

    /**
     * Checks if a date picker is required for the given interval and last-day
     * selection.
     * Returns true unless it's a monthly interval with "last day" selected.
     *
     * @param interval        the recurrence interval
     * @param lastDaySelected whether "last day of month" is selected
     * @return true if a date needs to be selected, false if not required
     */
    public static boolean isDateRequired(RecurrenceInterval interval, boolean lastDaySelected) {
        return interval != RecurrenceInterval.MONTHLY || !lastDaySelected;
    }
}
