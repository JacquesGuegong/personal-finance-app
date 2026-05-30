package com.financetracker.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

// @Component makes this a Spring bean so @Scheduled methods are picked up.
// The actual business logic lives in BudgetAlertService — the job's only
// responsibility is triggering it on a schedule.
@Component
public class BudgetAlertJob {

    private static final Logger log = LoggerFactory.getLogger(BudgetAlertJob.class);

    private final BudgetAlertService budgetAlertService;

    public BudgetAlertJob(BudgetAlertService budgetAlertService) {
        this.budgetAlertService = budgetAlertService;
    }

    // Cron: "second minute hour day-of-month month day-of-week"
    // "0 0 8 * * *" → at 00:00 seconds, 00 minutes, 08 hours, every day
    @Scheduled(cron = "0 0 8 * * *")
    public void checkBudgets() {
        LocalDate today = LocalDate.now();
        log.info("Budget alert job triggered for {}/{}", today.getMonthValue(), today.getYear());
        budgetAlertService.processMonthlyBudgetAlerts(today.getMonthValue(), today.getYear());
    }
}
