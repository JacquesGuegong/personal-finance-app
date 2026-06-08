package com.financetracker.controller;

import com.financetracker.dto.AccountRequest;
import com.financetracker.dto.AccountResponse;
import com.financetracker.entity.Account;
import com.financetracker.service.AccountService;
import com.financetracker.service.AccountWithBalance;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> getAll(Principal principal) {
        List<AccountResponse> accounts = accountService.getByUser(userId(principal))
                .stream().map(this::toResponse).toList();
        return ResponseEntity.ok(accounts);
    }

    // 201 Created + Location header best practice for REST: new resource was made
    @PostMapping
    public ResponseEntity<AccountResponse> create(@Valid @RequestBody AccountRequest request,
                                                   Principal principal) {
        AccountWithBalance account = accountService.create(userId(principal), request.name(), request.type());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(account));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getById(@PathVariable UUID id, Principal principal) {
        return ResponseEntity.ok(toResponse(accountService.getWithBalance(id, userId(principal))));
    }

    // PUT replaces the full resource — both name and type are required
    @PutMapping("/{id}")
    public ResponseEntity<AccountResponse> update(@PathVariable UUID id,
                                                   @Valid @RequestBody AccountRequest request,
                                                   Principal principal) {
        AccountWithBalance account = accountService.update(id, userId(principal), request.name(), request.type());
        return ResponseEntity.ok(toResponse(account));
    }

    // 204 No Content — success, but nothing to return
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, Principal principal) {
        accountService.delete(id, userId(principal));
        return ResponseEntity.noContent().build();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private UUID userId(Principal principal) {
        return UUID.fromString(principal.getName());
    }

    private AccountResponse toResponse(AccountWithBalance awb) {
        Account a = awb.account();
        return new AccountResponse(a.getId(), a.getName(), a.getType(), awb.balance());
    }
}
