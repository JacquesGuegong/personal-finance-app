package com.financetracker.service;

import com.financetracker.entity.Account;
import com.financetracker.entity.AccountType;
import com.financetracker.entity.User;
import com.financetracker.exception.ResourceNotFoundException;
import com.financetracker.repository.AccountRepository;
import com.financetracker.repository.TransactionRepository;
import com.financetracker.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public AccountService(AccountRepository accountRepository,
                          TransactionRepository transactionRepository,
                          UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public AccountWithBalance create(UUID userId, String name, AccountType type) {
        log.debug("Creating {} account '{}' for userId={}", type, name, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        Account saved = accountRepository.save(Account.builder()
                .user(user).name(name).type(type)
                .build());

        log.info("Account created: id={}, name='{}', type={}, userId={}", saved.getId(), name, type, userId);
        // A brand-new account has no transactions yet, so its balance is 0.
        return new AccountWithBalance(saved, BigDecimal.ZERO);
    }

    public List<AccountWithBalance> getByUser(UUID userId) {
        List<Account> accounts = accountRepository.findByUser_Id(userId);
        log.debug("Fetched {} accounts for userId={}", accounts.size(), userId);
        return accounts.stream()
                .map(a -> new AccountWithBalance(a, transactionRepository.calculateBalance(a.getId())))
                .toList();
    }

    public BigDecimal calculateNetWorth(UUID userId) {
        BigDecimal netWorth = transactionRepository.sumNetWorthByUserId(userId);
        log.debug("Net worth for userId={}: {}", userId, netWorth);
        return netWorth;
    }

    // Internal lookup used by update/delete — verifies existence and ownership.
    public Account findById(UUID accountId, UUID userId) {
        return accountRepository.findByIdAndUser_Id(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountId));
    }

    // Single account plus its computed balance — used by GET /api/accounts/{id}.
    public AccountWithBalance getWithBalance(UUID accountId, UUID userId) {
        Account account = findById(accountId, userId);
        return new AccountWithBalance(account, transactionRepository.calculateBalance(accountId));
    }

    @Transactional
    public AccountWithBalance update(UUID accountId, UUID userId, String name, AccountType type) {
        log.debug("Updating account id={}, userId={}", accountId, userId);

        Account account = findById(accountId, userId);
        account.setName(name);
        account.setType(type);
        Account saved = accountRepository.save(account);

        log.info("Account updated: id={}, name='{}', type={}, userId={}", accountId, name, type, userId);
        return new AccountWithBalance(saved, transactionRepository.calculateBalance(accountId));
    }

    @Transactional
    public void delete(UUID accountId, UUID userId) {
        log.debug("Deleting account id={}, userId={}", accountId, userId);

        Account account = findById(accountId, userId);
        accountRepository.delete(account);

        log.info("Account deleted: id={}, userId={}", accountId, userId);
    }
}
