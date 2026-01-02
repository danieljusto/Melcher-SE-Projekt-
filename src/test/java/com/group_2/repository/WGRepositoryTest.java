package com.group_2.repository;

import com.group_2.model.WG;
import com.group_2.testsupport.TestDataFactory;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class WGRepositoryTest {

    @Autowired
    private WGRepository wgRepository;

    @Test
    void savesAndRetrievesWG() {
        // Given
        WG wg = TestDataFactory.wg("Test WG");

        // When
        WG saved = wgRepository.save(wg);

        // Then
        assertThat(saved.getId()).isNotNull();
        WG found = wgRepository.findById(saved.getId()).orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Test WG");
    }

    @Test
    void findsByInviteCode() {
        // Given
        WG wg = TestDataFactory.wg("Test WG");
        WG saved = wgRepository.save(wg);
        String inviteCode = saved.getInviteCode();

        // When
        Optional<WG> found = wgRepository.findByInviteCode(inviteCode);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test WG");
    }

    @Test
    void findByInviteCodeReturnsEmptyForNonexistent() {
        // When
        Optional<WG> found = wgRepository.findByInviteCode("NONEXIST");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void existsByInviteCodeReturnsTrue() {
        // Given
        WG wg = TestDataFactory.wg("Test WG");
        WG saved = wgRepository.save(wg);

        // When/Then
        assertThat(wgRepository.existsByInviteCode(saved.getInviteCode())).isTrue();
    }

    @Test
    void existsByInviteCodeReturnsFalse() {
        // When/Then
        assertThat(wgRepository.existsByInviteCode("NONEXIST")).isFalse();
    }

    @Test
    void existsByInviteCodeAndIdNotExcludesSpecificWg() {
        // Given
        WG wg = TestDataFactory.wg("Test WG");
        WG saved = wgRepository.save(wg);
        String inviteCode = saved.getInviteCode();

        // When/Then
        // Should return false when excluding this WG
        assertThat(wgRepository.existsByInviteCodeAndIdNot(inviteCode, saved.getId())).isFalse();
        // Should return true when excluding a different WG
        assertThat(wgRepository.existsByInviteCodeAndIdNot(inviteCode, 999L)).isTrue();
    }
}
