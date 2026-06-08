package com.financetracker.repository;

import com.financetracker.entity.AlertType;
import com.financetracker.entity.BudgetAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BudgetAlertRepository extends JpaRepository<BudgetAlert, UUID> {

    // Returns unread alerts for a user, newest first — used by GET /api/alerts
    List<BudgetAlert> findByUser_IdAndIsReadFalseOrderByCreatedAtDesc(UUID userId);

    // Every alert for a user, newest first (read + unread, all types)
    // — used by GET /api/alerts/all (the "ALL" tab).
    List<BudgetAlert> findByUser_IdOrderByCreatedAtDesc(UUID userId);

    // Counts unread alerts for a user — used by GET /api/alerts/count.
    // Spring Data turns "countBy..." into SELECT COUNT(*), so we never load the
    // rows themselves; the database does the counting and returns a single number.
    long countByUser_IdAndIsReadFalse(UUID userId);

    // Returns all alerts of one type for a user, newest first
    // — used by GET /api/alerts?type=ANOMALY
    List<BudgetAlert> findByUser_IdAndAlertTypeOrderByCreatedAtDesc(UUID userId, AlertType alertType);

    // Prevents duplicate alerts: if an unread alert of this type already exists
    // for this budget, we skip creating another one
    boolean existsByBudget_IdAndAlertTypeAndIsReadFalse(UUID budgetId, AlertType alertType);

    // Ownership-scoped lookup for marking an alert as read
    Optional<BudgetAlert> findByIdAndUser_Id(UUID alertId, UUID userId);

    // Marks ALL of a user's unread alerts as read in a single UPDATE statement,
    // and returns how many rows changed — used by PUT /api/alerts/read-all.
    // @Modifying tells Spring Data this query writes (not reads), so it runs an
    // executeUpdate instead of a SELECT. Must run inside a transaction.
    @Modifying
    @Query("UPDATE BudgetAlert a SET a.isRead = true WHERE a.user.id = :userId AND a.isRead = false")
    int markAllReadByUserId(@Param("userId") UUID userId);
}
