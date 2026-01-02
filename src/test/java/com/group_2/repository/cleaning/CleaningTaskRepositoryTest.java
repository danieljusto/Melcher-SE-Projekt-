package com.group_2.repository.cleaning;

import com.group_2.model.User;
import com.group_2.model.WG;
import com.group_2.model.cleaning.CleaningTask;
import com.group_2.model.cleaning.Room;
import com.group_2.repository.UserRepository;
import com.group_2.repository.WGRepository;
import com.group_2.testsupport.TestDataFactory;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CleaningTaskRepositoryTest {

    @Autowired
    private CleaningTaskRepository cleaningTaskRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WGRepository wgRepository;

    private WG wg;
    private User assignee;
    private Room room;
    private LocalDate weekStartDate;

    @BeforeEach
    void setUp() {
        wg = wgRepository.save(TestDataFactory.wg("Test WG"));
        assignee = userRepository.save(TestDataFactory.user("assignee@example.com", wg));
        room = roomRepository.save(TestDataFactory.room("Kitchen", wg));
        weekStartDate = LocalDate.of(2026, 1, 5);
    }

    @Test
    void savesAndRetrievesCleaningTask() {
        // Given
        CleaningTask task = TestDataFactory.cleaningTask(room, assignee, wg, weekStartDate);

        // When
        CleaningTask saved = cleaningTaskRepository.save(task);

        // Then
        assertThat(saved.getId()).isNotNull();
        CleaningTask found = cleaningTaskRepository.findById(saved.getId()).orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getRoom().getName()).isEqualTo("Kitchen");
        assertThat(found.getAssignee().getEmail()).isEqualTo("assignee@example.com");
    }

    @Test
    void findsByWgAndWeekStartDate() {
        // Given
        LocalDate otherWeek = LocalDate.of(2026, 1, 12);
        cleaningTaskRepository.save(TestDataFactory.cleaningTask(room, assignee, wg, weekStartDate));
        cleaningTaskRepository.save(TestDataFactory.cleaningTask(room, assignee, wg, otherWeek));

        // When
        List<CleaningTask> found = cleaningTaskRepository.findByWgAndWeekStartDate(wg, weekStartDate);

        // Then
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getWeekStartDate()).isEqualTo(weekStartDate);
    }

    @Test
    void findsByAssigneeAndWeekStartDate() {
        // Given
        User otherAssignee = userRepository.save(TestDataFactory.user("other@example.com", wg));
        cleaningTaskRepository.save(TestDataFactory.cleaningTask(room, assignee, wg, weekStartDate));
        cleaningTaskRepository.save(TestDataFactory.cleaningTask(room, otherAssignee, wg, weekStartDate));

        // When
        List<CleaningTask> found = cleaningTaskRepository.findByAssigneeAndWeekStartDate(assignee, weekStartDate);

        // Then
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getAssignee().getEmail()).isEqualTo("assignee@example.com");
    }

    @Test
    void findsByWg() {
        // Given
        WG otherWg = wgRepository.save(TestDataFactory.wg("Other WG"));
        User otherAssignee = userRepository.save(TestDataFactory.user("other@example.com", otherWg));
        Room otherRoom = roomRepository.save(TestDataFactory.room("Bathroom", otherWg));

        cleaningTaskRepository.save(TestDataFactory.cleaningTask(room, assignee, wg, weekStartDate));
        cleaningTaskRepository.save(TestDataFactory.cleaningTask(otherRoom, otherAssignee, otherWg, weekStartDate));

        // When
        List<CleaningTask> found = cleaningTaskRepository.findByWg(wg);

        // Then
        assertThat(found).hasSize(1);
    }

    @Test
    void findsByWgAndRoom() {
        // Given
        Room otherRoom = roomRepository.save(TestDataFactory.room("Bathroom", wg));
        cleaningTaskRepository.save(TestDataFactory.cleaningTask(room, assignee, wg, weekStartDate));
        cleaningTaskRepository.save(TestDataFactory.cleaningTask(otherRoom, assignee, wg, weekStartDate));

        // When
        List<CleaningTask> found = cleaningTaskRepository.findByWgAndRoom(wg, room);

        // Then
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getRoom().getName()).isEqualTo("Kitchen");
    }
}
