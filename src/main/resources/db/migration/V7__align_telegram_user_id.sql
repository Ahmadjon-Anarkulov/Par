-- V7: Align existing telegram_user primary key column with JPA expectations.
-- Ensures telegram_user.telegram_user_id exists and is NOT NULL.

DO $$
BEGIN
    IF NOT EXISTS (
        select 1 from information_schema.columns
        where table_schema='public' and table_name='telegram_user' and column_name='telegram_user_id'
    ) THEN
        EXECUTE 'alter table telegram_user add column telegram_user_id bigint';
    END IF;

    -- Backfill from legacy id column if present
    IF EXISTS (
        select 1 from information_schema.columns
        where table_schema='public' and table_name='telegram_user' and column_name='id'
    ) THEN
        EXECUTE 'update telegram_user set telegram_user_id = coalesce(telegram_user_id, id)';
    END IF;

    -- Ensure not null for Hibernate validate
    EXECUTE 'update telegram_user set telegram_user_id = coalesce(telegram_user_id, 0)';
    EXECUTE 'alter table telegram_user alter column telegram_user_id set not null';

    -- Ensure role exists (in case table was created manually)
    IF NOT EXISTS (
        select 1 from information_schema.columns
        where table_schema='public' and table_name='telegram_user' and column_name='role'
    ) THEN
        EXECUTE 'alter table telegram_user add column role varchar(16) not null default ''USER''';
    END IF;
END $$;

