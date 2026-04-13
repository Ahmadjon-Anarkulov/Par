-- V3: No-op migration (legacy cleanup).
-- The project uses bot_order.order_id (varchar) as the primary key.
-- Older drafts referenced a separate "order" table; this migration is kept to preserve Flyway version history.

DO $$
BEGIN
    IF EXISTS (select 1 from pg_constraint where conname = 'fk_bot_order_order') THEN
        EXECUTE 'alter table bot_order drop constraint fk_bot_order_order';
    END IF;

    IF EXISTS (select 1 from pg_class where relname = 'idx_bot_order_order_id') THEN
        EXECUTE 'drop index if exists idx_bot_order_order_id';
    END IF;
END $$;