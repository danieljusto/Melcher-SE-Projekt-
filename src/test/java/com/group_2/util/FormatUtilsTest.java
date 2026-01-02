package com.group_2.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for FormatUtils.
 * These are plain JUnit tests with no Spring context (as recommended in the
 * guide).
 */
class FormatUtilsTest {

    @Test
    void formatsCurrency() {
        assertThat(FormatUtils.formatCurrency(1234.56)).isEqualTo("€1.234,56");
        assertThat(FormatUtils.formatCurrency(0.0)).isEqualTo("€0,00");
        assertThat(FormatUtils.formatCurrency(0.99)).isEqualTo("€0,99");
    }

    @Test
    void formatsCurrencyWithSign() {
        assertThat(FormatUtils.formatCurrencyWithSign(100.0)).isEqualTo("+€100,00");
        assertThat(FormatUtils.formatCurrencyWithSign(-100.0)).isEqualTo("-€100,00");
        assertThat(FormatUtils.formatCurrencyWithSign(0.0)).isEqualTo("€0,00");
    }

    @Test
    void getsDaySuffix() {
        assertThat(FormatUtils.getDaySuffix(1)).isEqualTo("st");
        assertThat(FormatUtils.getDaySuffix(2)).isEqualTo("nd");
        assertThat(FormatUtils.getDaySuffix(3)).isEqualTo("rd");
        assertThat(FormatUtils.getDaySuffix(4)).isEqualTo("th");
        assertThat(FormatUtils.getDaySuffix(11)).isEqualTo("th");
        assertThat(FormatUtils.getDaySuffix(12)).isEqualTo("th");
        assertThat(FormatUtils.getDaySuffix(13)).isEqualTo("th");
        assertThat(FormatUtils.getDaySuffix(21)).isEqualTo("st");
        assertThat(FormatUtils.getDaySuffix(22)).isEqualTo("nd");
        assertThat(FormatUtils.getDaySuffix(23)).isEqualTo("rd");
        assertThat(FormatUtils.getDaySuffix(31)).isEqualTo("st");
    }

    @Test
    void formatsDayWithSuffix() {
        assertThat(FormatUtils.formatDayWithSuffix(1)).isEqualTo("1st");
        assertThat(FormatUtils.formatDayWithSuffix(2)).isEqualTo("2nd");
        assertThat(FormatUtils.formatDayWithSuffix(3)).isEqualTo("3rd");
        assertThat(FormatUtils.formatDayWithSuffix(15)).isEqualTo("15th");
    }

    @Test
    void formatsDate() {
        LocalDate date = LocalDate.of(2026, 1, 15);
        assertThat(FormatUtils.formatDate(date)).isEqualTo("15.01.2026");
        assertThat(FormatUtils.formatDate(null)).isEqualTo("");
    }

    @Test
    void formatsShortDate() {
        LocalDate date = LocalDate.of(2026, 1, 15);
        assertThat(FormatUtils.formatShortDate(date)).isEqualTo("15.01");
        assertThat(FormatUtils.formatShortDate(null)).isEqualTo("");
    }
}
