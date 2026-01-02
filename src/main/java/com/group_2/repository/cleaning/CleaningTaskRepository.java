package com.group_2.repository.cleaning;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.group_2.model.User;
import com.group_2.model.WG;
import com.group_2.model.cleaning.CleaningTask;
import com.group_2.model.cleaning.Room;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for CleaningTask entities.
 */
@Repository
public interface CleaningTaskRepository extends JpaRepository<CleaningTask, Long> {

    List<CleaningTask> findByWgAndWeekStartDate(WG wg, LocalDate weekStartDate);

    List<CleaningTask> findByAssigneeAndWeekStartDate(User assignee, LocalDate weekStartDate);

    List<CleaningTask> findByWg(WG wg);

    List<CleaningTask> findByWgOrderByWeekStartDateDesc(WG wg);

    List<CleaningTask> findByWgAndRoom(WG wg, Room room);

    void deleteByWgAndRoom(WG wg, Room room);

    void deleteByWg(WG wg);
}
