package com.group_2.dto.cleaning;

import com.group_2.model.User;
import com.group_2.model.cleaning.CleaningTask;
import com.group_2.model.cleaning.CleaningTaskTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapper for cleaning domain DTOs.
 */
@Component
public class CleaningMapper {

    public CleaningTaskDTO toDTO(CleaningTask task) {
        if (task == null) {
            return null;
        }

        String roomName = task.getRoom() != null ? task.getRoom().getName() : "Unknown Room";
        User assignee = task.getAssignee();
        String assigneeName = assignee != null ? getDisplayName(assignee) : "Unassigned";

        return new CleaningTaskDTO(task.getId(), task.getRoom() != null ? task.getRoom().getId() : null, roomName,
                assignee != null ? assignee.getId() : null, assigneeName, task.getWeekStartDate(), task.getDueDate(),
                task.isCompleted(), task.isManualOverride());
    }

    public List<CleaningTaskDTO> toDTOList(List<CleaningTask> tasks) {
        List<CleaningTaskDTO> dtos = new ArrayList<>();
        if (tasks != null) {
            for (CleaningTask task : tasks) {
                dtos.add(toDTO(task));
            }
        }
        return dtos;
    }

    public CleaningTaskTemplateDTO toTemplateDTO(CleaningTaskTemplate template) {
        if (template == null) {
            return null;
        }

        String roomName = template.getRoom() != null ? template.getRoom().getName() : "Unknown Room";

        return new CleaningTaskTemplateDTO(template.getId(),
                template.getRoom() != null ? template.getRoom().getId() : null, roomName, template.getDayOfWeek(),
                template.getRecurrenceInterval());
    }

    public List<CleaningTaskTemplateDTO> toTemplateDTOList(List<CleaningTaskTemplate> templates) {
        List<CleaningTaskTemplateDTO> dtos = new ArrayList<>();
        if (templates != null) {
            for (CleaningTaskTemplate template : templates) {
                dtos.add(toTemplateDTO(template));
            }
        }
        return dtos;
    }

    private String getDisplayName(User user) {
        if (user == null) {
            return "Unknown";
        }
        String name = user.getName();
        if (user.getSurname() != null && !user.getSurname().isEmpty()) {
            name += " " + user.getSurname();
        }
        return name;
    }
}
