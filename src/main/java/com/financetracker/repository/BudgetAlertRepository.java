package com.financetracker.repository;

import com.financetracker.entity.AlertType;
import com.financetracker.entity.BudgetAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BudgetAlertRepository extends JpaRepository<BudgetAlert, UUID> {

    // Returns unread alerts for a user, newest first — used by GET /api/alerts
    List<BudgetAlert> findByUser_IdAndIsReadFalseOrderByCreatedAtDesc(UUID userId);

    // Prevents duplicate alerts: if an unread alert of this type already exists
    // for this budget, we skip creating another one
    boolean existsByBudget_IdAndAlertTypeAndIsReadFalse(UUID budgetId, AlertType alertType);

    // Ownership-scoped lookup for marking an alert as read
    Optional<BudgetAlert> findByIdAndUser_Id(UUID alertId, UUID userId);
}
