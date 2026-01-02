package com.group_2.dto.finance;

/**
 * DTO representing a transaction split (debtor's portion of a transaction).
 * Immutable record containing only the display data needed by controllers.
 */
public record TransactionSplitDTO(Long id, Long debtorId, String debtorName, Double percentage, Double amount) {
    public String getFormattedAmount() {
        return String.format("%.2f€", amount);
    }

    public String getFormattedPercentage() {
        return String.format("%.1f%%", percentage);
    }
}
