package com.financetracker.repository;

import com.financetracker.entity.Account;
import com.financetracker.entity.AccountType;
import com.financetracker.entity.Transaction;
import com.financetracker.entity.TransactionType;
import com.financetracker.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

// @DataJpaTest
//   A SLICE test. Instead of the whole app, it loads only the JPA layer:
//   Hibernate, the repositories, and an in-memory H2 database (it auto-replaces the
//   real datasource). Controllers, services, and security are NOT loaded — fast and
//   focused. Each test method runs in a transaction that is ROLLED BACK afterwards,
//   so tests never pollute each other.
@DataJpaTest
class TransactionRepositoryTest {

    // The repository under test — a REAL Spring Data proxy running real SQL on H2.
    @Autowired
    private TransactionRepository transactionRepository;

    // TestEntityManager: a thin test helper to persist seed rows directly, so we can
    // set up data without going through the repository we are trying to test.
    @Autowired
    private TestEntityManager em;

    @Test
    void findByDateRange_returnsOnlyTransactionsInsideRange_forThatUser() {
        // ── Arrange: one user, one account, three transactions on different dates ──
        User user = em.persist(User.builder()
                .email("alice@example.com")
                .passwordHash("hash")
                .build());

        Account account = em.persist(Account.builder()
                .user(user)
                .name("Checking")
                .type(AccountType.CHECKING)
                .build());

        persistTransaction(account, LocalDate.of(2026, 5, 1));   // inside range
        persistTransaction(account, LocalDate.of(2026, 5, 20));  // inside range
        persistTransaction(account, LocalDate.of(2026, 6, 5));   // OUTSIDE range

        // Flush + clear so the next query hits the database, not Hibernate's cache.
        em.flush();
        em.clear();

        // ── Act: the custom derived query under test ──────────────────────────────
        List<Transaction> results = transactionRepository.findByAccount_User_IdAndDateBetween(
                user.getId(), LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31));

        // ── Assert: only the two May transactions come back; the June one is excluded.
        assertThat(results).hasSize(2);
        assertThat(results)
                .extracting(Transaction::getDate)
                .containsExactlyInAnyOrder(LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 20));
    }

    @Test
    void findByDateRange_excludesOtherUsersTransactions() {
        // Two users, each with one account and one transaction on the same date.
        User alice = em.persist(User.builder().email("alice@x.com").passwordHash("h").build());
        User bob   = em.persist(User.builder().email("bob@x.com").passwordHash("h").build());

        Account aliceAccount = em.persist(Account.builder()
                .user(alice).name("A").type(AccountType.CHECKING).build());
        Account bobAccount = em.persist(Account.builder()
                .user(bob).name("B").type(AccountType.CHECKING).build());

        persistTransaction(aliceAccount, LocalDate.of(2026, 5, 10));
        persistTransaction(bobAccount,   LocalDate.of(2026, 5, 10));

        em.flush();
        em.clear();

        // Querying Alice's id must NOT leak Bob's transaction — this proves the
        // user-scoping in the query name (Account_User_Id) actually works.
        List<Transaction> results = transactionRepository.findByAccount_User_IdAndDateBetween(
                alice.getId(), LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31));

        assertThat(results).hasSize(1);
    }

    // ── helper ────────────────────────────────────────────────────────────────────
    private void persistTransaction(Account account, LocalDate date) {
        em.persist(Transaction.builder()
                .account(account)
                .amount(new BigDecimal("10.00"))
                .type(TransactionType.EXPENSE)
                .category("Groceries")
                .date(date)
                .build());
    }
}
