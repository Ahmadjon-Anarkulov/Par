-- V4: Align existing bot_order schema with JPA expectations.
-- Ensures bot_order.order_id exists (varchar) and is NOT NULL.

DO $$
BEGIN
    IF NOT EXISTS (
        select 1
        from information_schema.columns
        where table_schema = 'public'
          and table_name = 'bot_order'
          and column_name = 'order_id'
    ) THEN
        EXECUTE 'alter table bot_order add column order_id varchar(32)';
    END IF;

    -- Best-effort populate if NULLs exist
    IF EXISTS (
        select 1
        from information_schema.columns
        where table_schema = 'public'
          and table_name = 'bot_order'
          and column_name = 'id'
    ) THEN
        EXECUTE $sql$
            update bot_order
            set order_id = coalesce(order_id, 'PRL-' || lpad(id::text, 8, '0'))
            where order_id is null
        $sql$;
    ELSIF EXISTS (
        select 1
        from information_schema.columns
        where table_schema = 'public'
          and table_name = 'bot_order'
          and column_name = 'order_number'
    ) THEN
        EXECUTE $sql$
            update bot_order
            set order_id = coalesce(order_id, left(order_number, 32))
            where order_id is null
        $sql$;
    ELSE
        EXECUTE $sql$
            update bot_order
            set order_id = coalesce(order_id, 'PRL-' || upper(substring(md5(random()::text) from 1 for 8)))
            where order_id is null
        $sql$;
    END IF;

    -- Make NOT NULL if there are no remaining NULLs
    IF NOT EXISTS (select 1 from bot_order where order_id is null) THEN
        EXECUTE 'alter table bot_order alter column order_id set not null';
    END IF;
END $$;

