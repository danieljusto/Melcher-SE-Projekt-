package com.group_2.ui;

import com.group_2.model.User;
import java.util.*;

/**
 * State management for the transaction dialog.
 * Centralized storage for all dialog data to prevent field reference errors.
 */
public class TransactionDialogState {

    public enum SplitMode {
        EQUAL,
        PERCENTAGE,
        CUSTOM_AMOUNT
    }

    // Core transaction details
    private User payer;
    private Set<User> participants;
    private SplitMode splitMode;
    private double totalAmount;
    private String description;

    // Mode-specific data
    private Map<User, Double> customValues; // For percentage or custom amounts

    public TransactionDialogState() {
        this.participants = new HashSet<>();
        this.customValues = new HashMap<>();
        this.splitMode = SplitMode.EQUAL;
        this.totalAmount = 0.0;
        this.description = "";
    }

    /**
     * Reset all state to defaults
     */
    public void reset(User currentUser, List<User> allWgMembers) {
        this.payer = currentUser;
        this.participants = new HashSet<>(allWgMembers);
        // Remove payer from participants by default
        this.participants.remove(currentUser);
        this.splitMode = SplitMode.EQUAL;
        this.customValues.clear();
        this.totalAmount = 0.0;
        this.description = "";
    }

    // Getters and setters

    public User getPayer() {
        return payer;
    }

    public void setPayer(User payer) {
        this.payer = payer;
    }

    public Set<User> getParticipants() {
        return participants;
    }

    public void setParticipants(Set<User> participants) {
        this.participants = participants;
    }

    public void addParticipant(User user) {
        this.participants.add(user);
    }

    public void removeParticipant(User user) {
        this.participants.remove(user);
        this.customValues.remove(user);
    }

    public boolean isParticipant(User user) {
        return this.participants.contains(user);
    }

    public SplitMode getSplitMode() {
        return splitMode;
    }

    public void setSplitMode(SplitMode splitMode) {
        this.splitMode = splitMode;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<User, Double> getCustomValues() {
        return customValues;
    }

    public void setCustomValue(User user, double value) {
        this.customValues.put(user, value);
    }

    public Double getCustomValue(User user) {
        return this.customValues.get(user);
    }

    /**
     * Get participant names as comma-separated string
     */
    public String getParticipantNamesString() {
        if (participants.isEmpty()) {
            return "No participants selected";
        }

        List<String> names = new ArrayList<>();
        for (User user : participants) {
            names.add(user.getName());
        }
        return String.join(", ", names);
    }

    /**
     * Validate that all required fields are set
     */
    public boolean isValid() {
        if (payer == null)
            return false;
        if (description == null || description.trim().isEmpty())
            return false;
        if (totalAmount <= 0)
            return false;
        if (participants.isEmpty())
            return false;

        // Mode-specific validation
        switch (splitMode) {
            case EQUAL:
                return true; // Just need participants

            case PERCENTAGE:
                // All participants must have a percentage
                if (customValues.size() != participants.size())
                    return false;
                // Sum must equal 100%
                double sum = customValues.values().stream()
                        .mapToDouble(Double::doubleValue)
                        .sum();
                return Math.abs(sum - 100.0) < 0.01;

            case CUSTOM_AMOUNT:
                // All participants must have an amount
                if (customValues.size() != participants.size())
                    return false;
                // Sum must equal total amount
                double totalCustom = customValues.values().stream()
                        .mapToDouble(Double::doubleValue)
                        .sum();
                return Math.abs(totalCustom - totalAmount) < 0.01;

            default:
                return false;
        }
    }

    /**
     * Get validation error message
     */
    public String getValidationError() {
        if (payer == null)
            return "Please select a payer";
        if (description == null || description.trim().isEmpty())
            return "Please enter a description";
        if (totalAmount <= 0)
            return "Please enter an amount greater than 0";
        if (participants.isEmpty())
            return "Please select at least one participant";

        switch (splitMode) {
            case PERCENTAGE:
                if (customValues.size() != participants.size()) {
                    return "Please enter percentages for all participants";
                }
                double sum = customValues.values().stream()
                        .mapToDouble(Double::doubleValue)
                        .sum();
                if (Math.abs(sum - 100.0) >= 0.01) {
                    return String.format("Percentages must sum to 100%% (current: %.1f%%)", sum);
                }
                break;

            case CUSTOM_AMOUNT:
                if (customValues.size() != participants.size()) {
                    return "Please enter amounts for all participants";
                }
                double totalCustom = customValues.values().stream()
                        .mapToDouble(Double::doubleValue)
                        .sum();
                if (Math.abs(totalCustom - totalAmount) >= 0.01) {
                    return String.format("Custom amounts must sum to €%.2f (current: €%.2f)",
                            totalAmount, totalCustom);
                }
                break;
        }

        return "";
    }
}
