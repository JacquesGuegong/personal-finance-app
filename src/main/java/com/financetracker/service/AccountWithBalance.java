package com.financetracker.service;

import com.financetracker.entity.Account;
import java.math.BigDecimal;

// Pairs an account with its computed balance so the controller can build an
// AccountResponse. Like BudgetStatus, this is an internal service return value,
// not an API DTO — balance is derived from transactions, never stored.
public record AccountWithBalance(Account account, BigDecimal balance) {}
