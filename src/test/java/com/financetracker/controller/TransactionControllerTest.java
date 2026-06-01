package com.financetracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financetracker.entity.Account;
import com.financetracker.entity.Transaction;
import com.financetracker.entity.TransactionType;
import com.financetracker.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// @SpringBootTest
//   Boots the WHOLE Spring application context (all real beans, real security
//   filter chain, real JSON config) against the H2 test database. This is an
//   INTEGRATION test: HTTP routing, validation, security and serialization are all
//   exercised together, exactly as they wire up at runtime.
@SpringBootTest
// @AutoConfigureMockMvc
//   Builds a MockMvc that sends fake HTTP requests straight into the dispatcher
//   without opening a real network port. Because spring-security-test is present,
//   the security filters are applied too — so auth is genuinely tested.
@AutoConfigureMockMvc
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Jackson mapper (already configured with Java-time support by Spring Boot) so we
    // can turn request objects into JSON the same way real clients would.
    @Autowired
    private ObjectMapper objectMapper;

    // @MockBean
    //   Replaces the real TransactionService bean in the application context with a
    //   Mockito mock. The controller still runs for real; only its service dependency
    //   is faked. That keeps this test about the WEB layer (routing/validation/JSON/
    //   security) and not about business logic or the database.
    @MockBean
    private TransactionService transactionService;

    // The controller does UUID.fromString(principal.getName()), so the authenticated
    // username MUST be a valid UUID string, not the literal "user".
    private static final String USER_ID = "11111111-1111-1111-1111-111111111111";

    // ── 1. Valid request, authenticated → 201 Created ─────────────────────────────
    @Test
    @WithMockUser(username = USER_ID)   // injects an authenticated principal named with our UUID
    void createTransaction_validBody_returns201() throws Exception {
        // The mocked service returns a fully-built Transaction so the controller's
        // toResponse(...) can read its fields (including account.getId()).
        Account account = Account.builder().id(UUID.randomUUID()).build();
        Transaction saved = Transaction.builder()
                .id(UUID.randomUUID())
                .account(account)
                .amount(new BigDecimal("42.50"))
                .type(TransactionType.EXPENSE)
                .category("Groceries")
                .description("Weekly shop")
                .date(LocalDate.of(2026, 5, 30))
                .build();
        when(transactionService.create(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(saved);

        String body = objectMapper.writeValueAsString(Map.of(
                "accountId", account.getId().toString(),
                "amount", "42.50",
                "type", "EXPENSE",
                "category", "Groceries",
                "description", "Weekly shop",
                "date", "2026-05-30"));

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())                         // 201
                .andExpect(jsonPath("$.category").value("Groceries"))
                .andExpect(jsonPath("$.type").value("EXPENSE"));
    }

    // ── 2. No authentication → request is rejected before reaching the controller ──
    @Test   // note: NO @WithMockUser, so the request is anonymous
    void createTransaction_noAuth_isRejected() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "accountId", UUID.randomUUID().toString(),
                "amount", "42.50",
                "type", "EXPENSE",
                "category", "Groceries",
                "date", "2026-05-30"));

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                // Spring Security blocks the unauthenticated request. With no
                // formLogin/httpBasic configured, the default entry point returns 403.
                // (A REST API often prefers 401; see the note in the chat.)
                .andExpect(status().isForbidden());
    }

    // ── 3. Authenticated but invalid body (missing required fields) → 400 ─────────
    @Test
    @WithMockUser(username = USER_ID)
    void createTransaction_missingFields_returns400() throws Exception {
        // Empty JSON: accountId, amount, type, category and date are all missing.
        // @Valid on the @RequestBody fails binding BEFORE the controller body runs,
        // and GlobalExceptionHandler turns that into a 400.
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
