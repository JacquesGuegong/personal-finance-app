CREATE TABLE budget_alerts (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID         NOT NULL REFERENCES users(id)   ON DELETE CASCADE,
    budget_id   UUID         NOT NULL REFERENCES budgets(id) ON DELETE CASCADE,
    message     VARCHAR(500) NOT NULL,
    alert_type  VARCHAR(10)  NOT NULL CHECK (alert_type IN ('WARNING', 'EXCEEDED')),
    is_read     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- Most common query: fetch all unread alerts for a user
CREATE INDEX idx_budget_alerts_user_id ON budget_alerts(user_id);

-- Used by the job's duplicate-check before inserting a new alert
CREATE INDEX idx_budget_alerts_budget_id ON budget_alerts(budget_id);
