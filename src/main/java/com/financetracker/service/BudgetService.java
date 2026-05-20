package com.financetracker.service;

import com.financetracker.entity.Budget;
import com.financetracker.entity.User;
import com.financetracker.exception.ResourceNotFoundException;
import com.financetracker.repository.BudgetRepository;
import com.financetracker.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;

    public BudgetService(BudgetRepository budgetRepository, UserRepository userRepository) {
        this.budgetRepository = budgetRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Budget create(UUID userId, String category, BigDecimal limitAmount,
                          int month, int year) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        Budget budget = Budget.builder()
                .user(user)
                .category(category)
                .limitAmount(limitAmount)
                .spentAmount(BigDecimal.ZERO)
                .month(month)
                .year(year)
                .build();
        return budgetRepository.save(budget);
    }

    public BudgetStatus getBudgetStatus(UUID userId, String category, int month, int year) {
        Budget budget = budgetRepository
                .findByUser_IdAndCategoryAndMonthAndYear(userId, category, month, year)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Budget not found: " + category + " " + month + "/" + year));

        // Guard against divide-by-zero if limitAmount is somehow 0
        BigDecimal percentUsed = BigDecimal.ZERO;
        if (budget.getLimitAmount().compareTo(BigDecimal.ZERO) > 0) {
            percentUsed = budget.getSpentAmount()
                    .divide(budget.getLimitAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .setScale(2, RoundingMode.HALF_UP);
        }
        return new BudgetStatus(budget, percentUsed);
    }
}
