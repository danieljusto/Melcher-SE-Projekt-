package com.group_2.repository.cleaning;

import com.group_2.model.WG;
import com.group_2.model.cleaning.Room;
import com.group_2.repository.WGRepository;
import com.group_2.testsupport.TestDataFactory;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RoomRepositoryTest {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private WGRepository wgRepository;

    private WG wg;

    @BeforeEach
    void setUp() {
        wg = wgRepository.save(TestDataFactory.wg("Test WG"));
    }

    @Test
    void savesAndRetrievesRoom() {
        // Given
        Room room = TestDataFactory.room("Kitchen", wg);

        // When
        Room saved = roomRepository.save(room);

        // Then
        assertThat(saved.getId()).isNotNull();
        Room found = roomRepository.findById(saved.getId()).orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Kitchen");
    }

    @Test
    void findsByWgId() {
        // Given
        WG otherWg = wgRepository.save(TestDataFactory.wg("Other WG"));

        roomRepository.save(TestDataFactory.room("Kitchen", wg));
        roomRepository.save(TestDataFactory.room("Bathroom", wg));
        roomRepository.save(TestDataFactory.room("Living Room", otherWg));

        // When
        List<Room> found = roomRepository.findByWgId(wg.getId());

        // Then
        assertThat(found).hasSize(2);
        assertThat(found).extracting(Room::getName)
                .containsExactlyInAnyOrder("Kitchen", "Bathroom");
    }
}
