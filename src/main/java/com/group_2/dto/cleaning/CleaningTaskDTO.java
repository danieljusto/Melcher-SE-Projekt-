package com.group_2.dto.cleaning;

import java.time.LocalDate;

/**
 * Immutable DTO for cleaning tasks to decouple UI from JPA entities.
 */
public record CleaningTaskDTO(
                Long id,
                Long roomId,
                String roomName,
                Long assigneeId,
                String assigneeName,
                LocalDate weekStartDate,
                LocalDate dueDate,
                boolean completed,
                boolean manualOverride) {

        // Checks if this task is assigned to the given user
        public boolean isAssignedTo(Long userId) {
                return assigneeId != null && assigneeId.equals(userId);
        }

        // Gets the initial letter of the assignee's name or "?" if none
        public String getAssigneeInitial() {
                if (assigneeName == null || assigneeName.isEmpty()) {
                        return "?";
                }
                return assigneeName.substring(0, 1).toUpperCase();
        }

        // Gets the effective due date, falling back to weekStartDate if dueDate is null
        public LocalDate getEffectiveDueDate() {
                return dueDate != null ? dueDate : weekStartDate;
        }
}
