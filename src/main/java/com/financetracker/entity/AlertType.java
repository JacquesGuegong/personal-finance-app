package com.financetracker.entity;

public enum AlertType {
    WARNING,   // > 80% of budget used
    EXCEEDED,  // > 100% of budget used
    ANOMALY    // unusual single charge vs the category's recent average (not tied to a budget)
}
