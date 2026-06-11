package com.financetracker.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A DRAFT transaction extracted from a receipt photo — deliberately NOT a saved
 * transaction. The flow is: scan → show this draft to the user → user reviews,
 * picks an account, fixes anything the AI misread → THEN the client calls the
 * normal POST /api/transactions. AI output becomes data only after a human
 * confirms it ("human-in-the-loop").
 *
 * That's also why there's no id and no accountId here: nothing was persisted,
 * and only the user knows which account this purchase belongs to.
 *
 * @param merchant    store name as printed on the receipt, or null if unreadable
 * @param amount      the final total paid — the one field we require
 * @param date        purchase date from the receipt; today's date if unreadable
 * @param category    suggested spending category (same set used by /api/ai/categorize)
 * @param description short human-readable summary, e.g. "Walmart — groceries"
 */
public record ReceiptScanResponse(
        String merchant,
        BigDecimal amount,
        LocalDate date,
        String category,
        String description
) {}
