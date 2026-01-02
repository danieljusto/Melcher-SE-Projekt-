package com.group_2.dto.finance;

/**
 * DTO representing a balance between two users. Positive = other user owes
 * current user. Negative = current user owes other user.
 */
public record BalanceDTO(Long userId, String userName, Double balance) {
    public String getFormattedBalance() {
        return String.format("%.2f€", Math.abs(balance));
    }

    public boolean isCredit() {
        return balance > 0;
    }

    public boolean isDebt() {
        return balance < 0;
    }

    // Uses 0.01 threshold to handle floating-point precision
    public boolean isSettled() {
        return Math.abs(balance) < 0.01;
    }

    public Double getAbsoluteBalance() {
        return Math.abs(balance);
    }

    public String getStatusText() {
        if (isSettled()) {
            return "Settled";
        } else if (isCredit()) {
            return "owes you " + getFormattedBalance();
        } else {
            return "you owe " + getFormattedBalance();
        }
    }
}
