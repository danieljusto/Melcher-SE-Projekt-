package com.group_2.dto.cleaning;

// Statistics for a week's cleaning tasks
public record WeekStatsDTO(
        int totalTasks,
        int completedTasks,
        int myTasks) {
}
