package com.group_2.service.cleaning;

import com.group_2.dto.cleaning.CleaningMapper;
import com.group_2.dto.cleaning.CleaningTaskDTO;
import com.group_2.model.cleaning.CleaningTask;
import com.group_2.model.cleaning.CleaningTaskTemplate;
import com.group_2.model.cleaning.Room;
import com.group_2.repository.cleaning.CleaningTaskRepository;
import com.group_2.repository.cleaning.CleaningTaskTemplateRepository;
import com.group_2.repository.cleaning.RoomAssignmentQueueRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// Service for task lifecycle operations (status changes, rescheduling, deletion)
@Service
public class CleaningTaskLifecycleService {

    private final CleaningTaskRepository cleaningTaskRepository;
    private final CleaningTaskTemplateRepository templateRepository;
    private final RoomAssignmentQueueRepository queueRepository;
    private final CleaningMapper cleaningMapper;

    @Autowired
    public CleaningTaskLifecycleService(CleaningTaskRepository cleaningTaskRepository,
            CleaningTaskTemplateRepository templateRepository, RoomAssignmentQueueRepository queueRepository,
            CleaningMapper cleaningMapper) {
        this.cleaningTaskRepository = cleaningTaskRepository;
        this.templateRepository = templateRepository;
        this.queueRepository = queueRepository;
        this.cleaningMapper = cleaningMapper;
    }

    @Transactional
    public CleaningTask markTaskComplete(CleaningTask task) {
        task.markComplete();
        return cleaningTaskRepository.save(task);
    }

    @Transactional
    public CleaningTaskDTO markTaskComplete(Long taskId) {
        CleaningTask task = cleaningTaskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));
        CleaningTask updated = markTaskComplete(task);
        return cleaningMapper.toDTO(updated);
    }

    @Transactional
    public CleaningTask markTaskIncomplete(CleaningTask task) {
        task.markIncomplete();
        return cleaningTaskRepository.save(task);
    }

    @Transactional
    public CleaningTaskDTO markTaskIncomplete(Long taskId) {
        CleaningTask task = cleaningTaskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));
        CleaningTask updated = markTaskIncomplete(task);
        return cleaningMapper.toDTO(updated);
    }

    @Transactional
    public CleaningTask rescheduleTask(CleaningTask task, LocalDate newDueDate) {
        if (newDueDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot reschedule a task to a date in the past.");
        }
        task.setDueDate(newDueDate);
        task.setManualOverride(true);
        return cleaningTaskRepository.save(task);
    }

    @Transactional
    public CleaningTaskDTO rescheduleTask(Long taskId, LocalDate newDueDate) {
        CleaningTask task = cleaningTaskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));
        CleaningTask updated = rescheduleTask(task, newDueDate);
        return cleaningMapper.toDTO(updated);
    }

    public Optional<CleaningTask> getTask(Long id) {
        return cleaningTaskRepository.findById(id);
    }

    @Transactional
    public void deleteTask(CleaningTask task) {
        cleaningTaskRepository.delete(task);
    }

    @Transactional
    public void deleteTask(Long taskId) {
        CleaningTask task = cleaningTaskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));
        cleaningTaskRepository.delete(task);
    }

    // Must be called before deleting room itself
    @Transactional
    public void deleteRoomData(Room room) {
        // Delete all tasks for this room
        List<CleaningTask> tasks = cleaningTaskRepository.findAll().stream()
                .filter(t -> t.getRoom().getId().equals(room.getId())).collect(Collectors.toList());
        cleaningTaskRepository.deleteAll(tasks);

        // Delete all templates for this room
        List<CleaningTaskTemplate> templates = templateRepository.findAll().stream()
                .filter(t -> t.getRoom().getId().equals(room.getId())).collect(Collectors.toList());
        templateRepository.deleteAll(templates);

        // Delete all queues for this room
        queueRepository.deleteByRoom(room);
    }
}
