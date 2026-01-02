package com.group_2.repository;

import com.group_2.model.User;
import com.group_2.model.WG;
import com.group_2.testsupport.TestDataFactory;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WGRepository wgRepository;

    @Test
    void savesAndRetrievesUser() {
        // Given
        WG wg = TestDataFactory.wg("Test WG");
        wg = wgRepository.save(wg);
        User user = TestDataFactory.user("user@example.com", wg);

        // When
        User saved = userRepository.save(user);

        // Then
        assertThat(saved.getId()).isNotNull();
        User found = userRepository.findById(saved.getId()).orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getEmail()).isEqualTo("user@example.com");
    }

    @Test
    void findsByWgId() {
        // Given
        WG wg = TestDataFactory.wg("Test WG");
        wg = wgRepository.save(wg);

        User user1 = TestDataFactory.user("user1@example.com", wg);
        User user2 = TestDataFactory.user("user2@example.com", wg);
        userRepository.save(user1);
        userRepository.save(user2);

        // When
        List<User> usersInWg = userRepository.findByWgId(wg.getId());

        // Then
        assertThat(usersInWg).hasSize(2);
        assertThat(usersInWg).extracting(User::getEmail)
                .containsExactlyInAnyOrder("user1@example.com", "user2@example.com");
    }

    @Test
    void countsByWgId() {
        // Given
        WG wg = TestDataFactory.wg("Test WG");
        wg = wgRepository.save(wg);

        User user1 = TestDataFactory.user("user1@example.com", wg);
        User user2 = TestDataFactory.user("user2@example.com", wg);
        userRepository.save(user1);
        userRepository.save(user2);

        // When
        long count = userRepository.countByWgId(wg.getId());

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void existsByIdAndWgIdReturnsTrue() {
        // Given
        WG wg = TestDataFactory.wg("Test WG");
        wg = wgRepository.save(wg);
        User user = TestDataFactory.user("user@example.com", wg);
        user = userRepository.save(user);

        // When/Then
        assertThat(userRepository.existsByIdAndWgId(user.getId(), wg.getId())).isTrue();
    }

    @Test
    void existsByIdAndWgIdReturnsFalseForDifferentWg() {
        // Given
        WG wg1 = TestDataFactory.wg("WG 1");
        WG wg2 = TestDataFactory.wg("WG 2");
        wg1 = wgRepository.save(wg1);
        wg2 = wgRepository.save(wg2);

        User user = TestDataFactory.user("user@example.com", wg1);
        user = userRepository.save(user);

        // When/Then
        assertThat(userRepository.existsByIdAndWgId(user.getId(), wg2.getId())).isFalse();
    }
}
