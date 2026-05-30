package com.financetracker.controller;

import com.financetracker.dto.AlertResponse;
import com.financetracker.entity.BudgetAlert;
import com.financetracker.service.BudgetAlertService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final BudgetAlertService budgetAlertService;

    public AlertController(BudgetAlertService budgetAlertService) {
        this.budgetAlertService = budgetAlertService;
    }

    @GetMapping
    public ResponseEntity<List<AlertResponse>> getUnread(Principal principal) {
        List<AlertResponse> alerts = budgetAlertService
                .getUnreadAlerts(userId(principal))
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(alerts);
    }

    // PUT not POST — we are updating the state of an existing resource, not creating one
    @PutMapping("/{id}/read")
    public ResponseEntity<AlertResponse> markAsRead(@PathVariable UUID id, Principal principal) {
        BudgetAlert alert = budgetAlertService.markAsRead(id, userId(principal));
        return ResponseEntity.ok(toResponse(alert));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private UUID userId(Principal principal) {
        return UUID.fromString(principal.getName());
    }

    private AlertResponse toResponse(BudgetAlert a) {
        return new AlertResponse(
                a.getId(),
                a.getBudget().getId(),
                a.getBudget().getCategory(),
                a.getMessage(),
                a.getAlertType(),
                a.isRead(),
                a.getCreatedAt());
    }
}
