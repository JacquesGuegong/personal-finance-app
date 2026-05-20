package com.financetracker.repository;

import com.financetracker.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BudgetRepository extends JpaRepository<Budget, UUID> {

    // All budgets for a user in a given month — used to show the full budget dashboard.
    List<Budget> findByUser_IdAndMonthAndYear(UUID userId, int month, int year);

    // Single budget for ownership checks and status calculation.
    Optional<Budget> findByUser_IdAndCategoryAndMonthAndYear(
            UUID userId, String category, int month, int year);
}
