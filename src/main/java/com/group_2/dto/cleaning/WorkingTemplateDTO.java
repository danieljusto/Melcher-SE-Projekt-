package com.group_2.dto.cleaning;

import com.group_2.model.cleaning.RecurrenceInterval;
import com.group_2.util.MonthlyScheduleUtil;

import java.time.LocalDate;

/**
 * A working copy of a template that may or may not exist in the database yet.
 * Uses IDs and names instead of entity references to avoid JPA entity leaking
 * into UI.
 * 
 * This is a mutable DTO specifically for the template editor's working state.
 */
public class WorkingTemplateDTO {
    private Long roomId;
    private String roomName;
    private int dayOfWeek;
    private RecurrenceInterval recurrenceInterval;
    private LocalDate baseWeekStart;
    private boolean isDeleted = false; // marks for deletion on save

    /**
     * Creates a WorkingTemplateDTO from an existing CleaningTaskTemplateDTO.
     */
    public WorkingTemplateDTO(CleaningTaskTemplateDTO dto) {
        this.roomId = dto.roomId();
        this.roomName = dto.roomName();
        this.dayOfWeek = dto.dayOfWeek();
        this.recurrenceInterval = dto.recurrenceInterval();
        this.baseWeekStart = dto.baseWeekStart();
    }

    /**
     * Creates a new WorkingTemplateDTO from room information and a base date.
     * Automatically calculates dayOfWeek and baseWeekStart from the given date.
     */
    public WorkingTemplateDTO(Long roomId, String roomName, LocalDate baseDate, RecurrenceInterval interval) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.dayOfWeek = baseDate.getDayOfWeek().getValue();
        this.recurrenceInterval = interval;
        this.baseWeekStart = MonthlyScheduleUtil.calculateBaseWeekStart(baseDate);
    }

    // Getters
    public Long getRoomId() {
        return roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public RecurrenceInterval getRecurrenceInterval() {
        return recurrenceInterval;
    }

    public LocalDate getBaseWeekStart() {
        return baseWeekStart;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    // Setters for mutable state
    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public void setRecurrenceInterval(RecurrenceInterval recurrenceInterval) {
        this.recurrenceInterval = recurrenceInterval;
    }

    public void setBaseWeekStart(LocalDate baseWeekStart) {
        this.baseWeekStart = baseWeekStart;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    /**
     * Updates this template's schedule from a resolved base date.
     * Calculates dayOfWeek and baseWeekStart from the given date.
     */
    public void updateFromBaseDate(LocalDate baseDate) {
        this.dayOfWeek = baseDate.getDayOfWeek().getValue();
        this.baseWeekStart = MonthlyScheduleUtil.calculateBaseWeekStart(baseDate);
    }

    /**
     * Calculates the base date for this template (specific day within the base
     * week).
     * If baseWeekStart is set, adds (dayOfWeek - 1) days to get the actual date.
     * 
     * @param currentWeekStart fallback week start if baseWeekStart is null
     * @return the specific date this template refers to
     */
    public LocalDate calculateBaseDate(LocalDate currentWeekStart) {
        LocalDate baseWeek = baseWeekStart != null ? baseWeekStart : currentWeekStart;
        return baseWeek.plusDays(dayOfWeek - 1);
    }
}
