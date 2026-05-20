package com.financetracker.repository;

import com.financetracker.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {

    // "User_Id" traverses the @ManyToOne relationship:
    // Account → user → id → generates WHERE user_id = ?
    List<Account> findByUser_Id(UUID userId);

    // Used to verify ownership before writing: one query checks both existence and ownership.
    Optional<Account> findByIdAndUser_Id(UUID accountId, UUID userId);

    // @Query uses JPQL (object names, not table names).
    // COALESCE returns 0 instead of NULL when the user has no accounts yet.
    @Query("SELECT COALESCE(SUM(a.balance), 0) FROM Account a WHERE a.user.id = :userId")
    BigDecimal sumBalanceByUserId(@Param("userId") UUID userId);
}
