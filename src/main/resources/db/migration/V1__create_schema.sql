-- Flyway runs this exactly once and records it in flyway_schema_history.
-- If you need to change the schema later, create V2__..., V3__..., etc.

-- ── Users ──────────────────────────────────────────────────────────────────────
CREATE TABLE users (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- ── Accounts ───────────────────────────────────────────────────────────────────
CREATE TABLE accounts (
    id      UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID           NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name    VARCHAR(255)   NOT NULL,
    -- VARCHAR + CHECK is friendlier to JPA's @Enumerated(EnumType.STRING) than a native PG enum type
    type    VARCHAR(20)    NOT NULL CHECK (type IN ('CHECKING', 'SAVINGS', 'CREDIT')),
    balance NUMERIC(19, 4) NOT NULL DEFAULT 0
);

CREATE INDEX idx_accounts_user_id ON accounts(user_id);

-- ── Transactions ───────────────────────────────────────────────────────────────
CREATE TABLE transactions (
    id          UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id  UUID           NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    amount      NUMERIC(19, 4) NOT NULL,
    type        VARCHAR(10)    NOT NULL CHECK (type IN ('INCOME', 'EXPENSE')),
    category    VARCHAR(100)   NOT NULL,
    description VARCHAR(500),
    date        DATE           NOT NULL
);

CREATE INDEX idx_transactions_account_id ON transactions(account_id);

-- ── Budgets ────────────────────────────────────────────────────────────────────
CREATE TABLE budgets (
    id           UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID           NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category     VARCHAR(100)   NOT NULL,
    limit_amount NUMERIC(19, 4) NOT NULL,
    spent_amount NUMERIC(19, 4) NOT NULL DEFAULT 0,
    month        SMALLINT       NOT NULL CHECK (month BETWEEN 1 AND 12),
    year         SMALLINT       NOT NULL,
    -- one budget per category per month/year per user
    CONSTRAINT uq_budget_user_category_month_year UNIQUE (user_id, category, month, year)
);

CREATE INDEX idx_budgets_user_id ON budgets(user_id);
