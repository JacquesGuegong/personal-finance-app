package com.financetracker.service;

import com.financetracker.entity.Account;
import com.financetracker.entity.AccountType;
import com.financetracker.entity.User;
import com.financetracker.exception.ResourceNotFoundException;
import com.financetracker.repository.AccountRepository;
import com.financetracker.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public AccountService(AccountRepository accountRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Account create(UUID userId, String name, AccountType type) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        Account account = Account.builder()
                .user(user)
                .name(name)
                .type(type)
                .balance(BigDecimal.ZERO)
                .build();
        return accountRepository.save(account);
    }

    public List<Account> getByUser(UUID userId) {
        return accountRepository.findByUser_Id(userId);
    }

    public BigDecimal calculateNetWorth(UUID userId) {
        // Delegates the SUM to the DB — avoids loading every account row into memory
        return accountRepository.sumBalanceByUserId(userId);
    }
}
