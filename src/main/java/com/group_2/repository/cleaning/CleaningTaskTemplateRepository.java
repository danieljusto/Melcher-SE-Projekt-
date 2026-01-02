package com.group_2.repository.cleaning;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.group_2.model.WG;
import com.group_2.model.cleaning.CleaningTaskTemplate;

import java.util.List;

/**
 * Repository for CleaningTaskTemplate entities.
 */
@Repository
public interface CleaningTaskTemplateRepository extends JpaRepository<CleaningTaskTemplate, Long> {

    List<CleaningTaskTemplate> findByWg(WG wg);

    List<CleaningTaskTemplate> findByWgOrderByDayOfWeekAsc(WG wg);

    void deleteByWg(WG wg);
}
