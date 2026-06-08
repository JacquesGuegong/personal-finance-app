package com.financetracker.repository;

import com.financetracker.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {

    // "User_Id" traverses the @ManyToOne relationship:
    // Account → user → id → generates WHERE user_id = ?
    List<Account> findByUser_Id(UUID userId);

    // Used to verify ownership before writing: one query checks both existence and ownership.
    Optional<Account> findByIdAndUser_Id(UUID accountId, UUID userId);

    // Balance is no longer stored on the account — it is computed from transactions.
    // See TransactionRepository.calculateBalance / sumNetWorthByUserId.
}
