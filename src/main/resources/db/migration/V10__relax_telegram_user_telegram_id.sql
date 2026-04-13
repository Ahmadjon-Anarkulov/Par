-- V10: Compatibility for legacy telegram_user.telegram_id column (if present).
-- Some existing schemas use telegram_id NOT NULL. Our JPA uses telegram_user_id.

DO $$
BEGIN
    IF EXISTS (
        select 1 from information_schema.columns
        where table_schema='public' and table_name='telegram_user' and column_name='telegram_id'
    ) THEN
        -- backfill
        IF EXISTS (
            select 1 from information_schema.columns
            where table_schema='public' and table_name='telegram_user' and column_name='telegram_user_id'
        ) THEN
            EXECUTE 'update telegram_user set telegram_id = coalesce(telegram_id, telegram_user_id)';
        END IF;

        -- relax constraint to avoid insert failures
        BEGIN
            EXECUTE 'alter table telegram_user alter column telegram_id drop not null';
        EXCEPTION WHEN others THEN
            NULL;
        END;
    END IF;
END $$;

