-- Account balance is now computed on demand from the transactions table
-- (sum of INCOME amounts minus EXPENSE amounts), so the stored column is gone.
-- A stored column could silently drift out of sync with the real transactions;
-- computing it means there is a single source of truth.
ALTER TABLE accounts DROP COLUMN balance;
