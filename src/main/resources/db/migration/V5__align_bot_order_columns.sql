-- V5: Align existing bot_order columns with JPA expectations.

DO $$
BEGIN
    -- telegram_user_id
    IF NOT EXISTS (
        select 1 from information_schema.columns
        where table_schema='public' and table_name='bot_order' and column_name='telegram_user_id'
    ) THEN
        EXECUTE 'alter table bot_order add column telegram_user_id bigint';
    END IF;
    -- try to backfill from common legacy names
    IF EXISTS (
        select 1 from information_schema.columns
        where table_schema='public' and table_name='bot_order' and column_name='user_id'
    ) THEN
        EXECUTE 'update bot_order set telegram_user_id = coalesce(telegram_user_id, user_id)';
    END IF;
    EXECUTE 'update bot_order set telegram_user_id = coalesce(telegram_user_id, 0)';
    EXECUTE 'alter table bot_order alter column telegram_user_id set not null';

    -- customer_name
    IF NOT EXISTS (
        select 1 from information_schema.columns
        where table_schema='public' and table_name='bot_order' and column_name='customer_name'
    ) THEN
        EXECUTE 'alter table bot_order add column customer_name varchar(255)';
    END IF;
    EXECUTE 'update bot_order set customer_name = coalesce(customer_name, '''')';
    EXECUTE 'alter table bot_order alter column customer_name set not null';

    -- phone_number
    IF NOT EXISTS (
        select 1 from information_schema.columns
        where table_schema='public' and table_name='bot_order' and column_name='phone_number'
    ) THEN
        EXECUTE 'alter table bot_order add column phone_number varchar(255)';
    END IF;
    EXECUTE 'update bot_order set phone_number = coalesce(phone_number, '''')';
    EXECUTE 'alter table bot_order alter column phone_number set not null';

    -- delivery_address
    IF NOT EXISTS (
        select 1 from information_schema.columns
        where table_schema='public' and table_name='bot_order' and column_name='delivery_address'
    ) THEN
        EXECUTE 'alter table bot_order add column delivery_address text';
    END IF;
    EXECUTE 'update bot_order set delivery_address = coalesce(delivery_address, '''')';
    EXECUTE 'alter table bot_order alter column delivery_address set not null';

    -- status
    IF NOT EXISTS (
        select 1 from information_schema.columns
        where table_schema='public' and table_name='bot_order' and column_name='status'
    ) THEN
        EXECUTE 'alter table bot_order add column status varchar(64)';
    END IF;
    EXECUTE 'update bot_order set status = coalesce(status, ''CONFIRMED'')';
    EXECUTE 'alter table bot_order alter column status set not null';

    -- created_at
    IF NOT EXISTS (
        select 1 from information_schema.columns
        where table_schema='public' and table_name='bot_order' and column_name='created_at'
    ) THEN
        EXECUTE 'alter table bot_order add column created_at timestamptz';
    END IF;
    EXECUTE 'update bot_order set created_at = coalesce(created_at, now())';
    EXECUTE 'alter table bot_order alter column created_at set not null';
END $$;

