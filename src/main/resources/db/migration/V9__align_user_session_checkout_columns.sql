-- V9: Ensure checkout columns exist in user_session.

DO $$
BEGIN
    IF NOT EXISTS (
        select 1 from information_schema.columns
        where table_schema='public' and table_name='user_session' and column_name='checkout_name'
    ) THEN
        EXECUTE 'alter table user_session add column checkout_name varchar(255)';
    END IF;

    IF NOT EXISTS (
        select 1 from information_schema.columns
        where table_schema='public' and table_name='user_session' and column_name='checkout_phone'
    ) THEN
        EXECUTE 'alter table user_session add column checkout_phone varchar(255)';
    END IF;

    IF NOT EXISTS (
        select 1 from information_schema.columns
        where table_schema='public' and table_name='user_session' and column_name='checkout_address'
    ) THEN
        EXECUTE 'alter table user_session add column checkout_address text';
    END IF;
END $$;

