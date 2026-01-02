package com.group_2.service.finance;

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
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TransactionServiceTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WGRepository wgRepository;

    private WG wg;
    private User creditor;
    private User debtor;

    @BeforeEach
    void setUp() {
        wg = wgRepository.save(TestDataFactory.wg("Test WG"));
        creditor = userRepository.save(TestDataFactory.user("creditor@example.com", wg));
        debtor = userRepository.save(TestDataFactory.user("debtor@example.com", wg));
    }

    @Test
    void createsTransaction() {
        // When
        Transaction transaction = transactionService.createTransaction(
                creditor.getId(),
                creditor.getId(),
                List.of(debtor.getId()),
                List.of(100.0),
                100.0,
                "Groceries");

        // Then
        assertThat(transaction.getId()).isNotNull();
        assertThat(transaction.getTotalAmount()).isEqualTo(100.0);
        assertThat(transaction.getDescription()).isEqualTo("Groceries");
        assertThat(transaction.getCreditor().getEmail()).isEqualTo("creditor@example.com");
        assertThat(transaction.getSplits()).hasSize(1);
    }

    @Test
    void createsTransactionWithMultipleDebtors() {
        // Given
        User debtor2 = userRepository.save(TestDataFactory.user("debtor2@example.com", wg));

        // When
        Transaction transaction = transactionService.createTransaction(
                creditor.getId(),
                creditor.getId(),
                List.of(debtor.getId(), debtor2.getId()),
                List.of(50.0, 50.0),
                100.0,
                "Shared expense");

        // Then
        assertThat(transaction.getSplits()).hasSize(2);
    }

    @Test
    void getTransactionsByWG() {
        // Given
        transactionService.createTransaction(
                creditor.getId(), creditor.getId(), List.of(debtor.getId()),
                List.of(100.0), 100.0, "T1");
        transactionService.createTransaction(
                creditor.getId(), creditor.getId(), List.of(debtor.getId()),
                List.of(100.0), 50.0, "T2");

        // When
        List<Transaction> transactions = transactionService.getTransactionsByWG(wg.getId());

        // Then
        assertThat(transactions).hasSize(2);
    }

    @Test
    void calculatesBalanceBetweenUsers() {
        // Given - creditor paid 100, debtor owes 100
        transactionService.createTransaction(
                creditor.getId(), creditor.getId(), List.of(debtor.getId()),
                List.of(100.0), 100.0, "Groceries");

        // When
        double balance = transactionService.calculateBalanceWithUser(creditor.getId(), debtor.getId());

        // Then - debtor owes creditor 100
        assertThat(balance).isEqualTo(100.0);
    }

    @Test
    void calculatesAllBalances() {
        // Given
        User debtor2 = userRepository.save(TestDataFactory.user("debtor2@example.com", wg));

        transactionService.createTransaction(
                creditor.getId(), creditor.getId(), List.of(debtor.getId()),
                List.of(100.0), 100.0, "Groceries");
        transactionService.createTransaction(
                creditor.getId(), creditor.getId(), List.of(debtor2.getId()),
                List.of(100.0), 50.0, "Utilities");

        // When
        Map<Long, Double> balances = transactionService.calculateAllBalances(creditor.getId());

        // Then
        assertThat(balances.get(debtor.getId())).isEqualTo(100.0);
        assertThat(balances.get(debtor2.getId())).isEqualTo(50.0);
    }

    @Test
    void deleteTransaction() {
        // Given
        Transaction transaction = transactionService.createTransaction(
                creditor.getId(), creditor.getId(), List.of(debtor.getId()),
                List.of(100.0), 100.0, "Groceries");
        Long transactionId = transaction.getId();

        // When
        transactionService.deleteTransaction(transactionId, creditor.getId());

        // Then - getTransactionById throws if not found, so expect exception
        assertThatThrownBy(() -> transactionService.getTransactionById(transactionId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Transaction not found");
    }

    @Test
    void deleteTransactionFailsForNonCreator() {
        // Given
        Transaction transaction = transactionService.createTransaction(
                creditor.getId(), creditor.getId(), List.of(debtor.getId()),
                List.of(100.0), 100.0, "Groceries");

        // When/Then
        assertThatThrownBy(() -> transactionService.deleteTransaction(transaction.getId(), debtor.getId()))
                .isInstanceOf(RuntimeException.class);
    }
}
