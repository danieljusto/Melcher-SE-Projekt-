package com.group_2.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for MonthlyScheduleUtil.
 * These are plain JUnit tests with no Spring context (as recommended in the
 * guide).
 */
class MonthlyScheduleUtilTest {

    @Test
    void resolvesMonthlyDateWithLastDay() {
        LocalDate january = LocalDate.of(2026, 1, 1);
        LocalDate result = MonthlyScheduleUtil.resolveMonthlyDate(january, null, true);
        assertThat(result).isEqualTo(LocalDate.of(2026, 1, 31));
    }

    @Test
    void resolvesMonthlyDateWithSpecificDay() {
        LocalDate january = LocalDate.of(2026, 1, 1);
        LocalDate result = MonthlyScheduleUtil.resolveMonthlyDate(january, 15, false);
        assertThat(result).isEqualTo(LocalDate.of(2026, 1, 15));
    }

    @Test
    void resolvesMonthlyDateDefaultsToFirst() {
        LocalDate january = LocalDate.of(2026, 1, 1);
        LocalDate result = MonthlyScheduleUtil.resolveMonthlyDate(january, null, null);
        assertThat(result).isEqualTo(LocalDate.of(2026, 1, 1));
    }

    @Test
    void getsEffectiveDayForFebruary() {
        LocalDate february = LocalDate.of(2026, 2, 1);
        // February 2026 has 28 days
        assertThat(MonthlyScheduleUtil.getEffectiveDay(february, 31)).isEqualTo(28);
        assertThat(MonthlyScheduleUtil.getEffectiveDay(february, 29)).isEqualTo(28);
        assertThat(MonthlyScheduleUtil.getEffectiveDay(february, 15)).isEqualTo(15);
    }

    @Test
    void getsEffectiveDayFor30DayMonth() {
        LocalDate april = LocalDate.of(2026, 4, 1);
        // April has 30 days
        assertThat(MonthlyScheduleUtil.getEffectiveDay(april, 31)).isEqualTo(30);
        assertThat(MonthlyScheduleUtil.getEffectiveDay(april, 15)).isEqualTo(15);
    }

    @Test
    void getsEffectiveLastDayForFebruary() {
        LocalDate february = LocalDate.of(2026, 2, 1);
        assertThat(MonthlyScheduleUtil.getEffectiveLastDay(february)).isEqualTo(28);
    }

    @Test
    void getsEffectiveLastDayFor31DayMonth() {
        LocalDate january = LocalDate.of(2026, 1, 1);
        assertThat(MonthlyScheduleUtil.getEffectiveLastDay(january)).isEqualTo(31);
    }

    @Test
    void getsEffectiveLastDayFor30DayMonth() {
        LocalDate april = LocalDate.of(2026, 4, 1);
        assertThat(MonthlyScheduleUtil.getEffectiveLastDay(april)).isEqualTo(30);
    }
}
