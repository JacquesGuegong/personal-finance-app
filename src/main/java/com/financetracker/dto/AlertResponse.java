package com.financetracker.dto;

import com.financetracker.entity.AlertType;
import java.time.LocalDateTime;
import java.util.UUID;

public record AlertResponse(
        UUID id,
        UUID budgetId,
        String category,
        String message,
        AlertType alertType,
        boolean isRead,
        LocalDateTime createdAt
) {}
