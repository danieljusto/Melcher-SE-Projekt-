package com.group_2.util;

import javafx.scene.text.Text;

import java.util.Collection;

/**
 * Centralized utility class for split validation in transaction and standing
 * order dialogs.
 * Provides validation logic for percentage splits (must sum to 100%) and amount
 * splits
 * (must equal total amount), along with consistent UI feedback.
 */
public final class SplitValidationHelper {

    // CSS classes for validation states
    private static final String CLASS_VALIDATION_LABEL = "validation-label";
    private static final String CLASS_SUCCESS = "validation-label-success";
    private static final String CLASS_ERROR = "validation-label-error";
    private static final String CLASS_MUTED = "validation-label-muted";

    private SplitValidationHelper() {
        // Utility class - prevent instantiation
    }

    /**
     * Result of a split validation containing the calculated values and status.
     */
    public record ValidationResult(
            double total,
            double remaining,
            ValidationStatus status,
            String message) {

        public boolean isValid() {
            return status == ValidationStatus.SUCCESS;
        }
    }

    /**
     * Validation status enum for UI styling.
     */
    public enum ValidationStatus {
        SUCCESS,
        ERROR,
        MUTED
    }

    /**
     * Validates a percentage split where the sum must equal 100%.
     * 
     * @param values the percentage values entered by the user
     * @return validation result with status and formatted message
     */
    public static ValidationResult validatePercentageSplit(Collection<Double> values) {
        double total = sumValues(values);
        double remaining = 100.0 - total;
        ValidationStatus status = determineStatus(remaining);
        String message = String.format("Total: %.1f%% of 100%%\n%.1f%% left", total, remaining);
        return new ValidationResult(total, remaining, status, message);
    }

    /**
     * Validates an amount split where the sum must equal the total amount.
     * 
     * @param values      the amount values entered by the user
     * @param totalAmount the total amount that must be matched
     * @return validation result with status and formatted message
     */
    public static ValidationResult validateAmountSplit(Collection<Double> values, double totalAmount) {
        double total = sumValues(values);
        double remaining = totalAmount - total;
        ValidationStatus status = determineStatus(remaining);
        String message = String.format("Total: %.2f€ of %.2f€\n%.2f€ left", total, totalAmount, remaining);
        return new ValidationResult(total, remaining, status, message);
    }

    /**
     * Calculates the per-person amount for equal splits.
     * 
     * @param totalAmount      the total amount to split
     * @param participantCount number of participants
     * @return the amount each person pays
     */
    public static double calculateEqualSplit(double totalAmount, int participantCount) {
        if (participantCount <= 0) {
            return 0.0;
        }
        return totalAmount / participantCount;
    }

    /**
     * Formats the equal split summary message.
     * 
     * @param perPerson the amount each person pays
     * @return formatted message like "Each person pays 10.00€"
     */
    public static String formatEqualSplitMessage(double perPerson) {
        return String.format("Each person pays %.2f€", perPerson);
    }

    /**
     * Updates a Text node with validation result styling.
     * Applies the appropriate CSS classes based on validation status.
     * 
     * @param label  the Text node to update
     * @param result the validation result to display
     */
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

    /**
     * Applies success styling to a validation label.
     * 
     * @param label   the Text node to update
     * @param message the message to display
     */
    public static void applySuccessStyling(Text label, String message) {
        clearValidationClasses(label);
        ensureValidationLabelClass(label);
        label.getStyleClass().add(CLASS_SUCCESS);
        label.setText(message);
    }

    /**
     * Applies error styling to a validation label.
     * 
     * @param label   the Text node to update
     * @param message the message to display
     */
    public static void applyErrorStyling(Text label, String message) {
        clearValidationClasses(label);
        ensureValidationLabelClass(label);
        label.getStyleClass().add(CLASS_ERROR);
        label.setText(message);
    }

    /**
     * Applies muted styling to a validation label.
     * 
     * @param label   the Text node to update
     * @param message the message to display
     */
    public static void applyMutedStyling(Text label, String message) {
        clearValidationClasses(label);
        ensureValidationLabelClass(label);
        label.getStyleClass().add(CLASS_MUTED);
        label.setText(message);
    }

    /**
     * Clears all validation-related CSS classes from a label.
     * 
     * @param label the Text node to clear
     */
    public static void clearValidationClasses(Text label) {
        label.getStyleClass().removeAll(CLASS_SUCCESS, CLASS_ERROR, CLASS_MUTED);
    }

    /**
     * Parses a string value to double, handling comma as decimal separator.
     * 
     * @param text the text to parse
     * @return the parsed double value
     * @throws NumberFormatException if the text cannot be parsed
     */
    public static double parseAmount(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0.0;
        }
        return Double.parseDouble(text.replace(",", "."));
    }

    /**
     * Validates if the percentage sum equals 100% within tolerance.
     * 
     * @param sum the sum of percentages
     * @return true if valid (sum equals 100% ± 0.1%)
     */
    public static boolean isPercentageSumValid(double sum) {
        return Math.abs(sum - 100.0) <= 0.1;
    }

    /**
     * Validates if the amount sum equals the total within tolerance.
     * 
     * @param sum         the sum of amounts
     * @param totalAmount the expected total
     * @return true if valid (sum equals total ± 0.01€)
     */
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
