package com.financetracker.service;

import com.financetracker.entity.Account;
import com.financetracker.entity.Transaction;
import com.financetracker.entity.TransactionType;
import com.financetracker.exception.UnauthorizedException;
import com.financetracker.repository.AccountRepository;
import com.financetracker.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public TransactionService(TransactionRepository transactionRepository,
                               AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional
    public Transaction create(UUID userId, UUID accountId, BigDecimal amount,
                               TransactionType type, String category,
                               String description, LocalDate date) {
        // findByIdAndUser_Id checks existence AND ownership in a single query —
        // prevents user A from posting transactions to user B's account
        Account account = accountRepository.findByIdAndUser_Id(accountId, userId)
                .orElseThrow(() -> new UnauthorizedException(
                        "Account not found or does not belong to this user"));

        Transaction transaction = Transaction.builder()
                .account(account)
                .amount(amount)
                .type(type)
                .category(category)
                .description(description)
                .date(date)
                .build();
        return transactionRepository.save(transaction);
    }

    public List<Transaction> getByDateRange(UUID userId, LocalDate start, LocalDate end) {
        return transactionRepository.findByAccount_User_IdAndDateBetween(userId, start, end);
    }

    public List<Transaction> getByCategory(UUID userId, String category) {
        return transactionRepository.findByAccount_User_IdAndCategory(userId, category);
    }
}
