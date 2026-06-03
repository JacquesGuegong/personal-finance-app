package com.financetracker.service;

import com.financetracker.entity.Transaction;
import java.util.UUID;

/**
 * Published by {@link TransactionService} right after a transaction is saved.
 *
 * Why an event instead of TransactionService calling AiService directly?
 *   1. No circular dependency. AiService already depends on TransactionService
 *      (for spending summaries), so if TransactionService also depended on
 *      AiService, Spring could not build either bean. The event lets
 *      TransactionService announce "a transaction was created" without knowing
 *      who listens.
 *   2. It lets the listener run AFTER the DB transaction commits (see
 *      AiService's @TransactionalEventListener), so anomaly detection can never
 *      roll back or block the transaction it is reacting to.
 */
public record TransactionCreatedEvent(UUID userId, Transaction transaction) {}
