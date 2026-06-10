package com.financetracker.controller;

import com.financetracker.dto.PageResponse;
import com.financetracker.dto.TransactionRequest;
import com.financetracker.dto.TransactionResponse;
import com.financetracker.dto.TransactionUpdateRequest;
import com.financetracker.entity.Transaction;
import com.financetracker.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    // A hard ceiling on page size: clients may ask for less, never more. Without
    // it, ?size=2000000 is a self-service denial-of-service endpoint.
    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_PAGE_SIZE = 20;

    // GET /api/transactions?startDate=2024-01-01&endDate=2024-01-31&page=0&size=20
    // GET /api/transactions?category=Groceries&page=2
    // GET /api/transactions  ← defaults: current month, first page of 20
    @GetMapping
    public ResponseEntity<PageResponse<TransactionResponse>> getAll(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + DEFAULT_PAGE_SIZE) int size,
            Principal principal) {

        UUID userId = userId(principal);

        // Sorting by date alone is not deterministic — many transactions share a
        // date, and the database is free to order ties differently per query, so
        // a row could show up on two pages (or neither). The unique id tie-breaker
        // makes the order total, and therefore the pagination stable.
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.clamp(size, 1, MAX_PAGE_SIZE),
                Sort.by(Sort.Direction.DESC, "date").and(Sort.by("id")));

        Page<Transaction> transactions;
        if (category != null) {
            transactions = transactionService.getByCategory(userId, category, pageable);
        } else {
            LocalDate start = startDate != null ? startDate : LocalDate.now().withDayOfMonth(1);
            LocalDate end   = endDate   != null ? endDate   : LocalDate.now();
            transactions = transactionService.getByDateRange(userId, start, end, pageable);
        }

        return ResponseEntity.ok(PageResponse.of(transactions.map(this::toResponse)));
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> create(@Valid @RequestBody TransactionRequest request,
                                                       Principal principal) {
        Transaction transaction = transactionService.createTransaction(
                userId(principal), request.accountId(), request.amount(),
                request.type(), request.category(), request.description(), request.date());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(transaction));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getById(@PathVariable UUID id, Principal principal) {
        return ResponseEntity.ok(toResponse(transactionService.findById(id, userId(principal))));
    }

    // PUT uses TransactionUpdateRequest — accountId is intentionally excluded
    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> update(@PathVariable UUID id,
                                                       @Valid @RequestBody TransactionUpdateRequest request,
                                                       Principal principal) {
        Transaction transaction = transactionService.update(
                id, userId(principal), request.amount(), request.type(),
                request.category(), request.description(), request.date());
        return ResponseEntity.ok(toResponse(transaction));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, Principal principal) {
        transactionService.delete(id, userId(principal));
        return ResponseEntity.noContent().build();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private UUID userId(Principal principal) {
        return UUID.fromString(principal.getName());
    }

    private TransactionResponse toResponse(Transaction t) {
        // t.getAccount().getId() works because Spring Boot's Open Session In View
        // keeps the Hibernate session open for the full HTTP request by default.
        return new TransactionResponse(
                t.getId(), t.getAccount().getId(), t.getAmount(),
                t.getType(), t.getCategory(), t.getDescription(), t.getDate());
    }
}
