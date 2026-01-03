package com.group_2.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;

import com.group_2.model.cleaning.RecurrenceInterval;

// Utility for monthly schedule calculations and base week computations
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

    // Calculates the Monday of the week containing the given date
    public static LocalDate calculateBaseWeekStart(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    // For monthly with "last day" selected, finds a month with 31 days
    public static LocalDate resolveBaseDate(LocalDate selectedDate, RecurrenceInterval interval,
            boolean lastDaySelected, LocalDate fallbackDate) {
        if (interval == RecurrenceInterval.MONTHLY && lastDaySelected) {
            LocalDate reference = fallbackDate != null ? fallbackDate : LocalDate.now();
            return resolveLastDayBaseDate(reference);
        }
        return selectedDate != null ? selectedDate : fallbackDate;
    }

    // Sucht ab reference bis zu 12 Monate voraus nach einem Monat mit 31 Tagen
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

    // True if date selection is required (except for monthly + last day)
    public static boolean isDateRequired(RecurrenceInterval interval, boolean lastDaySelected) {
        return interval != RecurrenceInterval.MONTHLY || !lastDaySelected;
    }
}
