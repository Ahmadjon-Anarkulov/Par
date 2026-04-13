-- V8: Align existing user_session schema with JPA expectations.

DO $$
BEGIN
    IF NOT EXISTS (
        select 1 from information_schema.columns
        where table_schema='public' and table_name='user_session' and column_name='telegram_user_id'
    ) THEN
        EXECUTE 'alter table user_session add column telegram_user_id bigint';
    END IF;

    -- Backfill from legacy id/user_id columns if present
    IF EXISTS (
        select 1 from information_schema.columns
        where table_schema='public' and table_name='user_session' and column_name='user_id'
    ) THEN
        EXECUTE 'update user_session set telegram_user_id = coalesce(telegram_user_id, user_id)';
    END IF;
    IF EXISTS (
        select 1 from information_schema.columns
        where table_schema='public' and table_name='user_session' and column_name='id'
    ) THEN
        EXECUTE 'update user_session set telegram_user_id = coalesce(telegram_user_id, id)';
    END IF;

    EXECUTE 'update user_session set telegram_user_id = coalesce(telegram_user_id, 0)';
    EXECUTE 'alter table user_session alter column telegram_user_id set not null';

    -- state
    IF NOT EXISTS (
        select 1 from information_schema.columns
        where table_schema='public' and table_name='user_session' and column_name='state'
    ) THEN
        EXECUTE 'alter table user_session add column state varchar(64)';
    END IF;
    EXECUTE 'update user_session set state = coalesce(state, ''IDLE'')';
    EXECUTE 'alter table user_session alter column state set not null';

    -- updated_at
    IF NOT EXISTS (
        select 1 from information_schema.columns
        where table_schema='public' and table_name='user_session' and column_name='updated_at'
    ) THEN
        EXECUTE 'alter table user_session add column updated_at timestamptz';
    END IF;
    EXECUTE 'update user_session set updated_at = coalesce(updated_at, now())';
    EXECUTE 'alter table user_session alter column updated_at set not null';
END $$;

