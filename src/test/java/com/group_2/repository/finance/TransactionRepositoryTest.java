package com.group_2.repository.finance;

import com.group_2.model.User;
import com.group_2.model.WG;
import com.group_2.model.finance.Transaction;
import com.group_2.repository.UserRepository;
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
class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WGRepository wgRepository;

    private WG wg;
    private User creditor;

    @BeforeEach
    void setUp() {
        wg = wgRepository.save(TestDataFactory.wg("Test WG"));
        creditor = userRepository.save(TestDataFactory.user("creditor@example.com", wg));
    }

    @Test
    void savesAndRetrievesTransaction() {
        // Given
        Transaction transaction = TestDataFactory.transaction(creditor, 100.0, "Groceries", wg);

        // When
        Transaction saved = transactionRepository.save(transaction);

        // Then
        assertThat(saved.getId()).isNotNull();
        Transaction found = transactionRepository.findById(saved.getId()).orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getTotalAmount()).isEqualTo(100.0);
        assertThat(found.getDescription()).isEqualTo("Groceries");
    }

    @Test
    void findsByWg() {
        // Given
        WG otherWg = wgRepository.save(TestDataFactory.wg("Other WG"));
        User otherCreditor = userRepository.save(TestDataFactory.user("other@example.com", otherWg));

        transactionRepository.save(TestDataFactory.transaction(creditor, 50.0, "T1", wg));
        transactionRepository.save(TestDataFactory.transaction(creditor, 75.0, "T2", wg));
        transactionRepository.save(TestDataFactory.transaction(otherCreditor, 100.0, "T3", otherWg));

        // When
        List<Transaction> found = transactionRepository.findByWg(wg);

        // Then
        assertThat(found).hasSize(2);
        assertThat(found).extracting(Transaction::getDescription)
                .containsExactlyInAnyOrder("T1", "T2");
    }

    @Test
    void findsByCreditor() {
        // Given
        User otherCreditor = userRepository.save(TestDataFactory.user("other@example.com", wg));

        transactionRepository.save(TestDataFactory.transaction(creditor, 50.0, "By Creditor", wg));
        transactionRepository.save(TestDataFactory.transaction(otherCreditor, 100.0, "By Other", wg));

        // When
        List<Transaction> found = transactionRepository.findByCreditor(creditor);

        // Then
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getDescription()).isEqualTo("By Creditor");
    }
}
