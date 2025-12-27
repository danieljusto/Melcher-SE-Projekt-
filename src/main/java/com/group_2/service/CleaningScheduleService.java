package com.group_2.service;

import com.group_2.repository.CleaningTaskRepository;
import com.group_2.repository.CleaningTaskTemplateRepository;
import com.group_2.repository.RoomAssignmentQueueRepository;
import com.group_2.repository.UserRepository;
import com.model.CleaningTask;
import com.model.CleaningTaskTemplate;
import com.model.Room;
import com.model.RoomAssignmentQueue;
import com.model.User;
import com.model.WG;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing cleaning schedules and tasks.
 * Uses round-robin queue system for fair task distribution.
 */
@Service
public class CleaningScheduleService {

    private final CleaningTaskRepository cleaningTaskRepository;
    private final CleaningTaskTemplateRepository templateRepository;
    private final RoomAssignmentQueueRepository queueRepository;
    private final UserRepository userRepository;

    @Autowired
    public CleaningScheduleService(CleaningTaskRepository cleaningTaskRepository,
            CleaningTaskTemplateRepository templateRepository,
            RoomAssignmentQueueRepository queueRepository,
            UserRepository userRepository) {
        this.cleaningTaskRepository = cleaningTaskRepository;
        this.templateRepository = templateRepository;
        this.queueRepository = queueRepository;
        this.userRepository = userRepository;
    }

    /**
     * Get the start of the current week (Monday).
     */
    public LocalDate getCurrentWeekStart() {
        return LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    /**
     * Get all cleaning tasks for the current week for a WG.
     */
    public List<CleaningTask> getCurrentWeekTasks(WG wg) {
        LocalDate weekStart = getCurrentWeekStart();
        return cleaningTaskRepository.findByWgAndWeekStartDate(wg, weekStart);
    }

    /**
     * Get all cleaning tasks for a specific week for a WG.
     * If no tasks exist for the week and a template exists, automatically generates
     * tasks from the template using round-robin assignment.
     */
    @Transactional
    public List<CleaningTask> getTasksForWeek(WG wg, LocalDate weekStart) {
        List<CleaningTask> tasks = cleaningTaskRepository.findByWgAndWeekStartDate(wg, weekStart);

        // If no tasks exist for this week, auto-generate from template
        if (tasks.isEmpty() && hasTemplate(wg)) {
            tasks = generateFromTemplateForWeek(wg, weekStart);
        }

        return tasks;
    }

    /**
     * Generate tasks from template for a specific week using round-robin
     * assignment.
     * Each room's queue determines the assignee, then the queue rotates.
     */
    @Transactional
    public List<CleaningTask> generateFromTemplateForWeek(WG wg, LocalDate weekStart) {
        List<CleaningTaskTemplate> templates = templateRepository.findByWg(wg);
        if (templates.isEmpty()) {
            return new ArrayList<>();
        }

        List<User> members = wg.getMitbewohner();
        if (members.isEmpty()) {
            return new ArrayList<>();
        }

        List<CleaningTask> newTasks = new ArrayList<>();
        for (CleaningTaskTemplate template : templates) {
            // Get or create queue for this room
            RoomAssignmentQueue queue = getOrCreateQueueForRoom(wg, template.getRoom(), members);

            // Get the next assignee from the queue
            User assignee = getNextAssigneeFromQueue(queue, members);
            if (assignee == null) {
                continue; // Skip if no valid assignee
            }

            LocalDate dueDate = weekStart.plusDays(template.getDayOfWeek() - 1);
            CleaningTask task = new CleaningTask(
                    template.getRoom(),
                    assignee,
                    wg,
                    weekStart,
                    dueDate);
            newTasks.add(cleaningTaskRepository.save(task));

            // Rotate the queue for next time
            queue.rotate();
            queueRepository.save(queue);
        }

        return newTasks;
    }

    /**
     * Get the next assignee from a queue, validating the user still exists.
     */
    private User getNextAssigneeFromQueue(RoomAssignmentQueue queue, List<User> currentMembers) {
        Long nextId = queue.getNextAssigneeId();
        if (nextId == null) {
            return currentMembers.isEmpty() ? null : currentMembers.get(0);
        }

        // Find the user in current members
        for (User member : currentMembers) {
            if (member.getId().equals(nextId)) {
                return member;
            }
        }

        // If user not found (left WG), sync queue and try again
        syncQueueWithMembers(queue, currentMembers);
        queueRepository.save(queue);

        nextId = queue.getNextAssigneeId();
        if (nextId == null) {
            return null;
        }

        return userRepository.findById(nextId).orElse(null);
    }

    /**
     * Get existing queue or create a new one with the correct offset.
     */
    private RoomAssignmentQueue getOrCreateQueueForRoom(WG wg, Room room, List<User> members) {
        Optional<RoomAssignmentQueue> existingQueue = queueRepository.findByWgAndRoom(wg, room);
        if (existingQueue.isPresent()) {
            return existingQueue.get();
        }

        // Create new queue with offset based on existing queue count
        int offset = (int) queueRepository.countByWg(wg);
        RoomAssignmentQueue newQueue = new RoomAssignmentQueue(room, wg, members, offset);
        return queueRepository.save(newQueue);
    }

    /**
     * Sync a queue with current WG members (add new members, remove departed ones).
     */
    private void syncQueueWithMembers(RoomAssignmentQueue queue, List<User> currentMembers) {
        List<Long> queueIds = queue.getMemberIds();
        List<Long> currentIds = new ArrayList<>();
        for (User m : currentMembers) {
            currentIds.add(m.getId());
        }

        // Remove departed members
        queueIds.removeIf(id -> !currentIds.contains(id));

        // Add new members at the end
        for (Long id : currentIds) {
            if (!queueIds.contains(id)) {
                queueIds.add(id);
            }
        }

        // Update the queue
        queue.setMemberQueueOrder(
                queueIds.stream()
                        .map(String::valueOf)
                        .collect(java.util.stream.Collectors.joining(",")));
    }

    /**
     * Sync all queues for a WG with current members.
     * Call this when members join or leave the WG.
     */
    @Transactional
    public void syncAllQueuesWithMembers(WG wg) {
        List<User> currentMembers = wg.getMitbewohner();
        List<RoomAssignmentQueue> queues = queueRepository.findByWg(wg);

        for (RoomAssignmentQueue queue : queues) {
            syncQueueWithMembers(queue, currentMembers);
            queueRepository.save(queue);
        }
    }

    /**
     * Get all tasks assigned to a user for the current week.
     */
    public List<CleaningTask> getUserTasksForCurrentWeek(User user) {
        LocalDate weekStart = getCurrentWeekStart();
        return cleaningTaskRepository.findByAssigneeAndWeekStartDate(user, weekStart);
    }

    /**
     * Generate a new weekly schedule, randomly distributing rooms among WG members
     * and assigning each task to a random day of the week.
     */
    @Transactional
    public List<CleaningTask> generateWeeklySchedule(WG wg) {
        if (wg == null || wg.rooms == null || wg.rooms.isEmpty()) {
            return new ArrayList<>();
        }

        List<User> members = wg.getMitbewohner();
        if (members.isEmpty()) {
            return new ArrayList<>();
        }

        LocalDate weekStart = getCurrentWeekStart();
        java.util.Random random = new java.util.Random();

        // Delete existing tasks for this week
        List<CleaningTask> existingTasks = cleaningTaskRepository.findByWgAndWeekStartDate(wg, weekStart);
        cleaningTaskRepository.deleteAll(existingTasks);

        // Randomly distribute rooms among members with random days
        List<CleaningTask> newTasks = new ArrayList<>();
        List<Room> rooms = new ArrayList<>(wg.rooms);
        java.util.Collections.shuffle(rooms, random);

        for (Room room : rooms) {
            User assignee = members.get(random.nextInt(members.size()));
            LocalDate dueDate = weekStart.plusDays(random.nextInt(7));
            CleaningTask task = new CleaningTask(room, assignee, wg, weekStart, dueDate);
            newTasks.add(cleaningTaskRepository.save(task));
        }

        return newTasks;
    }

    /**
     * Generate a new weekly schedule from saved templates using round-robin.
     */
    @Transactional
    public List<CleaningTask> generateFromTemplate(WG wg) {
        LocalDate weekStart = getCurrentWeekStart();

        // Delete existing tasks for this week
        List<CleaningTask> existingTasks = cleaningTaskRepository.findByWgAndWeekStartDate(wg, weekStart);
        cleaningTaskRepository.deleteAll(existingTasks);

        // Generate new tasks using round-robin
        return generateFromTemplateForWeek(wg, weekStart);
    }

    /**
     * Save current week's schedule as the default template.
     * Also initializes assignment queues for each room.
     */
    @Transactional
    public List<CleaningTaskTemplate> saveAsTemplate(WG wg) {
        LocalDate weekStart = getCurrentWeekStart();
        List<CleaningTask> currentTasks = cleaningTaskRepository.findByWgAndWeekStartDate(wg, weekStart);

        // Delete existing templates and queues
        templateRepository.deleteByWg(wg);
        queueRepository.deleteByWg(wg);

        List<User> members = wg.getMitbewohner();

        // Create templates and queues from current tasks
        List<CleaningTaskTemplate> templates = new ArrayList<>();
        int offset = 0;
        for (CleaningTask task : currentTasks) {
            int dayOfWeek = task.getDueDate() != null
                    ? task.getDueDate().getDayOfWeek().getValue()
                    : 1;
            CleaningTaskTemplate template = new CleaningTaskTemplate(
                    task.getRoom(),
                    wg,
                    dayOfWeek);
            templates.add(templateRepository.save(template));

            // Create queue for this room with offset
            RoomAssignmentQueue queue = new RoomAssignmentQueue(task.getRoom(), wg, members, offset);
            queueRepository.save(queue);
            offset++;
        }

        return templates;
    }

    /**
     * Get all templates for a WG.
     */
    public List<CleaningTaskTemplate> getTemplates(WG wg) {
        return templateRepository.findByWgOrderByDayOfWeekAsc(wg);
    }

    /**
     * Check if templates exist for a WG.
     */
    public boolean hasTemplate(WG wg) {
        return !templateRepository.findByWg(wg).isEmpty();
    }

    /**
     * Get the next assignee for a room (who would be assigned if tasks were
     * generated now).
     */
    public User getNextAssigneeForRoom(WG wg, Room room) {
        Optional<RoomAssignmentQueue> queueOpt = queueRepository.findByWgAndRoom(wg, room);
        if (queueOpt.isEmpty()) {
            return null;
        }

        Long nextId = queueOpt.get().getNextAssigneeId();
        if (nextId == null) {
            return null;
        }

        return userRepository.findById(nextId).orElse(null);
    }

    /**
     * Assign a cleaning task for a specific room to a user.
     */
    @Transactional
    public CleaningTask assignTask(Room room, User assignee, WG wg) {
        LocalDate weekStart = getCurrentWeekStart();

        List<CleaningTask> existingTasks = cleaningTaskRepository.findByWgAndWeekStartDate(wg, weekStart);
        for (CleaningTask task : existingTasks) {
            if (task.getRoom().getId().equals(room.getId())) {
                task.setAssignee(assignee);
                return cleaningTaskRepository.save(task);
            }
        }

        CleaningTask task = new CleaningTask(room, assignee, wg, weekStart);
        return cleaningTaskRepository.save(task);
    }

    /**
     * Reassign a task to a different user.
     * Also swaps positions in the room's queue to maintain fairness.
     */
    @Transactional
    public CleaningTask reassignTask(CleaningTask task, User newAssignee) {
        User originalAssignee = task.getAssignee();

        // Swap positions in the room's queue for fairness
        if (!originalAssignee.getId().equals(newAssignee.getId())) {
            Optional<RoomAssignmentQueue> queueOpt = queueRepository.findByWgAndRoom(
                    task.getWg(), task.getRoom());
            if (queueOpt.isPresent()) {
                RoomAssignmentQueue queue = queueOpt.get();
                queue.swapPositions(originalAssignee.getId(), newAssignee.getId());
                queueRepository.save(queue);
            }
        }

        // Update the task assignment
        task.setAssignee(newAssignee);
        return cleaningTaskRepository.save(task);
    }

    /**
     * Reschedule a task to a different day.
     */
    @Transactional
    public CleaningTask rescheduleTask(CleaningTask task, LocalDate newDueDate) {
        task.setDueDate(newDueDate);
        return cleaningTaskRepository.save(task);
    }

    /**
     * Mark a cleaning task as complete.
     */
    @Transactional
    public CleaningTask markTaskComplete(CleaningTask task) {
        task.markComplete();
        return cleaningTaskRepository.save(task);
    }

    /**
     * Mark a cleaning task as incomplete.
     */
    @Transactional
    public CleaningTask markTaskIncomplete(CleaningTask task) {
        task.markIncomplete();
        return cleaningTaskRepository.save(task);
    }

    /**
     * Get a task by ID.
     */
    public Optional<CleaningTask> getTask(Long id) {
        return cleaningTaskRepository.findById(id);
    }

    /**
     * Delete a cleaning task.
     */
    @Transactional
    public void deleteTask(CleaningTask task) {
        cleaningTaskRepository.delete(task);
    }

    // ========== Template CRUD Methods ==========

    /**
     * Add a new template task with round-robin queue.
     * No assignee needed - queue handles assignment automatically.
     */
    @Transactional
    public CleaningTaskTemplate addTemplate(WG wg, Room room, DayOfWeek dayOfWeek) {
        CleaningTaskTemplate template = new CleaningTaskTemplate(room, wg, dayOfWeek);
        template = templateRepository.save(template);

        // Create queue for this room with offset based on existing queue count
        List<User> members = wg.getMitbewohner();
        int offset = (int) queueRepository.countByWg(wg);
        RoomAssignmentQueue queue = new RoomAssignmentQueue(room, wg, members, offset);
        queueRepository.save(queue);

        return template;
    }

    /**
     * Update an existing template (only day can be changed, assignee is
     * auto-managed). Also updates due dates for all existing tasks.
     */
    @Transactional
    public CleaningTaskTemplate updateTemplate(CleaningTaskTemplate template, DayOfWeek newDay) {
        // Update due dates for all existing tasks for this room
        List<CleaningTask> tasks = cleaningTaskRepository.findByWgAndRoom(template.getWg(), template.getRoom());
        for (CleaningTask task : tasks) {
            LocalDate newDueDate = task.getWeekStartDate().plusDays(newDay.getValue() - 1);
            task.setDueDate(newDueDate);
            cleaningTaskRepository.save(task);
        }

        template.setDayOfWeek(newDay);
        return templateRepository.save(template);
    }

    /**
     * Delete a single template and its associated queue.
     * Also deletes all existing tasks for this room across all weeks.
     */
    @Transactional
    public void deleteTemplate(CleaningTaskTemplate template) {
        // Delete all existing tasks for this room across all weeks
        cleaningTaskRepository.deleteByWgAndRoom(template.getWg(), template.getRoom());
        queueRepository.deleteByRoom(template.getRoom());
        templateRepository.delete(template);
    }

    /**
     * Clear all templates and queues for a WG.
     * Also deletes all tasks across all weeks.
     */
    @Transactional
    public void clearTemplates(WG wg) {
        // Delete all tasks for this WG across all weeks
        cleaningTaskRepository.deleteByWg(wg);
        queueRepository.deleteByWg(wg);
        templateRepository.deleteByWg(wg);
    }
}
