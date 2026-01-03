package com.group_2.util;

import javafx.scene.text.Text;

import java.util.Collection;

// Utility for split validation in transactions and standing orders
// Percentage splits must sum to 100%, amount splits must equal total
public final class SplitValidationHelper {

    // CSS classes for validation states
    private static final String CLASS_VALIDATION_LABEL = "validation-label";
    private static final String CLASS_SUCCESS = "validation-label-success";
    private static final String CLASS_ERROR = "validation-label-error";
    private static final String CLASS_MUTED = "validation-label-muted";

    private SplitValidationHelper() {
        // Utility class - prevent instantiation
    }

    // Result of a validation
    public record ValidationResult(
            double total,
            double remaining,
            ValidationStatus status,
            String message) {

        public boolean isValid() {
            return status == ValidationStatus.SUCCESS;
        }
    }

    public enum ValidationStatus {
        SUCCESS,
        ERROR,
        MUTED
    }

    public static ValidationResult validatePercentageSplit(Collection<Double> values) {
        double total = sumValues(values);
        double remaining = 100.0 - total;
        ValidationStatus status = determineStatus(remaining);
        String message = String.format("Total: %.1f%% of 100%%\n%.1f%% left", total, remaining);
        return new ValidationResult(total, remaining, status, message);
    }

    public static ValidationResult validateAmountSplit(Collection<Double> values, double totalAmount) {
        double total = sumValues(values);
        double remaining = totalAmount - total;
        ValidationStatus status = determineStatus(remaining);
        String message = String.format("Total: %.2f€ of %.2f€\n%.2f€ left", total, totalAmount, remaining);
        return new ValidationResult(total, remaining, status, message);
    }

    public static double calculateEqualSplit(double totalAmount, int participantCount) {
        if (participantCount <= 0) {
            return 0.0;
        }
        return totalAmount / participantCount;
    }

    public static String formatEqualSplitMessage(double perPerson) {
        return String.format("Each person pays %.2f€", perPerson);
    }

    // Applies CSS styling based on validation result
    public static void applyValidationStyling(Text label, ValidationResult result) {
        clearValidationClasses(label);
        ensureValidationLabelClass(label);

        switch (result.status()) {
            case SUCCESS -> label.getStyleClass().add(CLASS_SUCCESS);
            case ERROR -> label.getStyleClass().add(CLASS_ERROR);
            case MUTED -> label.getStyleClass().add(CLASS_MUTED);
        }

        label.setText(result.message());
    }

    public static void applySuccessStyling(Text label, String message) {
        clearValidationClasses(label);
        ensureValidationLabelClass(label);
        label.getStyleClass().add(CLASS_SUCCESS);
        label.setText(message);
    }

    public static void applyErrorStyling(Text label, String message) {
        clearValidationClasses(label);
        ensureValidationLabelClass(label);
        label.getStyleClass().add(CLASS_ERROR);
        label.setText(message);
    }

    public static void applyMutedStyling(Text label, String message) {
        clearValidationClasses(label);
        ensureValidationLabelClass(label);
        label.getStyleClass().add(CLASS_MUTED);
        label.setText(message);
    }

    public static void clearValidationClasses(Text label) {
        label.getStyleClass().removeAll(CLASS_SUCCESS, CLASS_ERROR, CLASS_MUTED);
    }

    // Parses string to double, replaces comma with dot
    public static double parseAmount(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0.0;
        }
        return Double.parseDouble(text.replace(",", "."));
    }

    public static boolean isPercentageSumValid(double sum) {
        return Math.abs(sum - 100.0) <= 0.1;
    }

    public static boolean isAmountSumValid(double sum, double totalAmount) {
        return Math.abs(sum - totalAmount) <= 0.01;
    }

    // ==================== Private Helper Methods ====================

    private static double sumValues(Collection<Double> values) {
        if (values == null) {
            return 0.0;
        }
        return values.stream()
                .filter(v -> v != null)
                .mapToDouble(Double::doubleValue)
                .sum();
    }

    private static ValidationStatus determineStatus(double remaining) {
        if (Math.abs(remaining) < 0.01) {
            return ValidationStatus.SUCCESS;
        } else if (remaining < 0) {
            return ValidationStatus.ERROR;
        } else {
            return ValidationStatus.MUTED;
        }
    }

    private static void ensureValidationLabelClass(Text label) {
        if (!label.getStyleClass().contains(CLASS_VALIDATION_LABEL)) {
            label.getStyleClass().add(CLASS_VALIDATION_LABEL);
        }
    }
}
