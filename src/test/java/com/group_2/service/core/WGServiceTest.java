package com.group_2.service.core;

import com.group_2.model.User;
import com.group_2.model.WG;
import com.group_2.model.cleaning.Room;
import com.group_2.repository.UserRepository;
import com.group_2.repository.WGRepository;
import com.group_2.repository.cleaning.RoomRepository;
import com.group_2.service.finance.TransactionService;
import com.group_2.testsupport.TestDataFactory;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class WGServiceTest {

    @Autowired
    private WGService wgService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WGRepository wgRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private TransactionService transactionService;

    private User admin;

    @BeforeEach
    void setUp() {
        admin = userRepository.save(TestDataFactory.user("admin@example.com", null));
    }

    @Test
    void createsWGWithAdmin() {
        // When
        WG wg = wgService.createWG("Test WG", admin, List.of());

        // Then
        assertThat(wg.getId()).isNotNull();
        assertThat(wg.getName()).isEqualTo("Test WG");
        assertThat(wg.getAdmin()).isEqualTo(admin);
        assertThat(wg.getInviteCode()).isNotNull();
        assertThat(wg.getMitbewohner()).contains(admin);
    }

    @Test
    void createsWGWithRooms() {
        // Given
        Room room1 = roomRepository.save(TestDataFactory.room("Kitchen", null));
        Room room2 = roomRepository.save(TestDataFactory.room("Bathroom", null));

        // When
        WG wg = wgService.createWGWithRoomIds("Test WG", admin.getId(), List.of(room1.getId(), room2.getId()));

        // Then
        assertThat(wg.getRooms()).hasSize(2);
    }

    @Test
    void addsMitbewohner() {
        // Given
        WG wg = wgService.createWG("Test WG", admin, List.of());
        User newMember = userRepository.save(TestDataFactory.user("member@example.com", null));

        // When
        wgService.addMitbewohner(wg.getId(), newMember);

        // Then
        WG updated = wgRepository.findById(wg.getId()).orElseThrow();
        assertThat(updated.getMitbewohner()).hasSize(2);
        assertThat(newMember.getWg()).isEqualTo(wg);
    }

    @Test
    void addsMitbewohnerByInviteCode() {
        // Given
        WG wg = wgService.createWG("Test WG", admin, List.of());
        User newMember = userRepository.save(TestDataFactory.user("member@example.com", null));

        // When
        WG result = wgService.addMitbewohnerByInviteCode(wg.getInviteCode(), newMember);

        // Then
        assertThat(result.getMitbewohner()).hasSize(2);
        assertThat(newMember.getWg()).isEqualTo(wg);
    }

    @Test
    void addMitbewohnerWithInvalidCodeThrows() {
        // Given
        User newMember = userRepository.save(TestDataFactory.user("member@example.com", null));

        // When/Then
        assertThatThrownBy(() -> wgService.addMitbewohnerByInviteCode("INVALID", newMember))
                .isInstanceOf(RuntimeException.class).hasMessageContaining("WG not found");
    }

    @Test
    void removesMitbewohner() {
        // Given
        WG wg = wgService.createWG("Test WG", admin, List.of());
        User member = userRepository.save(TestDataFactory.user("member@example.com", null));
        wgService.addMitbewohner(wg.getId(), member);

        // When
        wgService.removeMitbewohner(wg.getId(), member.getId());

        // Then
        WG updated = wgRepository.findById(wg.getId()).orElseThrow();
        assertThat(updated.getMitbewohner()).hasSize(1);
        assertThat(updated.getMitbewohner()).doesNotContain(member);

        User updatedMember = userRepository.findById(member.getId()).orElseThrow();
        assertThat(updatedMember.getWg()).isNull();
    }

    @Test
    void updatesWG() {
        // Given
        WG wg = wgService.createWG("Original Name", admin, List.of());
        User newAdmin = userRepository.save(TestDataFactory.user("newadmin@example.com", null));
        wgService.addMitbewohner(wg.getId(), newAdmin);

        // When
        WG updated = wgService.updateWG(wg.getId(), "New Name", newAdmin.getId());

        // Then
        assertThat(updated.getName()).isEqualTo("New Name");
        assertThat(updated.getAdmin()).isEqualTo(newAdmin);
    }

    @Test
    void deletesWG() {
        // Given
        WG wg = wgService.createWG("Test WG", admin, List.of());
        Long wgId = wg.getId();

        // When
        wgService.deleteWG(wgId);

        // Then
        assertThat(wgRepository.findById(wgId)).isEmpty();
    }

    @Test
    void getsWGByInviteCode() {
        // Given
        WG wg = wgService.createWG("Test WG", admin, List.of());

        // When
        Optional<WG> found = wgService.getWGByInviteCode(wg.getInviteCode());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(wg.getId());
    }

    @Test
    void getMemberSummaries() {
        // Given
        WG wg = wgService.createWG("Test WG", admin, List.of());
        User member = userRepository.save(TestDataFactory.user("member@example.com", null));
        wgService.addMitbewohner(wg.getId(), member);

        // When
        var summaries = wgService.getMemberSummaries(wg.getId());

        // Then
        assertThat(summaries).hasSize(2);
    }

    @Test
    void checkUserCanLeaveWG_ZeroBalance_CanLeave() {
        // Given - user with no transactions (zero balance)
        WG wg = wgService.createWG("Test WG", admin, List.of());
        User member = userRepository.save(TestDataFactory.user("member@example.com", null));
        wgService.addMitbewohner(wg.getId(), member);

        // When
        var status = wgService.checkUserCanLeaveWG(member.getId());

        // Then
        assertThat(status.canLeave()).isTrue();
        assertThat(status.balance()).isEqualTo(0.0);
        assertThat(status.message()).isNull();
    }

    @Test
    void checkUserCanLeaveWG_PositiveBalance_CanLeaveWithWarning() {
        // Given - user is owed money (positive balance)
        WG wg = wgService.createWG("Test WG", admin, List.of());
        User creditor = userRepository.save(TestDataFactory.user("creditor@example.com", null));
        wgService.addMitbewohner(wg.getId(), creditor);

        // Admin owes creditor 50€
        transactionService.createTransaction(creditor.getId(), creditor.getId(), List.of(admin.getId()), List.of(100.0),
                50.0, "Shared expense");

        // When
        var status = wgService.checkUserCanLeaveWG(creditor.getId());

        // Then
        assertThat(status.canLeave()).isTrue();
        assertThat(status.balance()).isGreaterThan(0);
        assertThat(status.message()).contains("owed");
    }

    @Test
    void checkUserCanLeaveWG_NegativeBalance_CannotLeave() {
        // Given - user owes money (negative balance)
        WG wg = wgService.createWG("Test WG", admin, List.of());
        User debtor = userRepository.save(TestDataFactory.user("debtor@example.com", null));
        wgService.addMitbewohner(wg.getId(), debtor);

        // Debtor owes admin 50€
        transactionService.createTransaction(admin.getId(), admin.getId(), List.of(debtor.getId()), List.of(100.0),
                50.0, "Shared expense");

        // When
        var status = wgService.checkUserCanLeaveWG(debtor.getId());

        // Then
        assertThat(status.canLeave()).isFalse();
        assertThat(status.balance()).isLessThan(0);
        assertThat(status.message()).contains("owe").contains("roommate");
    }
}
