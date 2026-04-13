-- V6: Align existing bot_order_item columns with JPA expectations.

DO $$
BEGIN
    -- order_id (FK target bot_order.order_id)
    IF NOT EXISTS (
        select 1 from information_schema.columns
        where table_schema='public' and table_name='bot_order_item' and column_name='order_id'
    ) THEN
        EXECUTE 'alter table bot_order_item add column order_id varchar(32)';
    ELSE
        -- Drop legacy FK if it blocks type change
        IF EXISTS (select 1 from pg_constraint where conname = 'bot_order_item_order_id_fkey') THEN
            EXECUTE 'alter table bot_order_item drop constraint bot_order_item_order_id_fkey';
        END IF;

        -- If legacy order_id is not varchar, coerce it to varchar to match bot_order(order_id)
        IF EXISTS (
            select 1
            from information_schema.columns
            where table_schema='public'
              and table_name='bot_order_item'
              and column_name='order_id'
              and data_type <> 'character varying'
        ) THEN
            EXECUTE 'alter table bot_order_item alter column order_id type varchar(32) using order_id::text';
        END IF;
    END IF;
    EXECUTE 'update bot_order_item set order_id = coalesce(order_id, ''UNKNOWN'')';
    EXECUTE 'alter table bot_order_item alter column order_id set not null';

    -- Re-create FK to bot_order(order_id) if possible (best-effort)
    IF EXISTS (
        select 1 from information_schema.columns
        where table_schema='public' and table_name='bot_order' and column_name='order_id'
    ) THEN
        IF NOT EXISTS (select 1 from pg_constraint where conname = 'fk_bot_order_item_order_id') THEN
            BEGIN
                EXECUTE 'alter table bot_order_item add constraint fk_bot_order_item_order_id foreign key (order_id) references bot_order(order_id) on delete cascade';
            EXCEPTION WHEN others THEN
                -- ignore if legacy data prevents FK creation
                NULL;
            END;
        END IF;
    END IF;

    -- product_id
    IF NOT EXISTS (
        select 1 from information_schema.columns
        where table_schema='public' and table_name='bot_order_item' and column_name='product_id'
    ) THEN
        EXECUTE 'alter table bot_order_item add column product_id varchar(64)';
    END IF;
    EXECUTE 'update bot_order_item set product_id = coalesce(product_id, ''unknown'')';
    EXECUTE 'alter table bot_order_item alter column product_id set not null';

    -- product_name
    IF NOT EXISTS (
        select 1 from information_schema.columns
        where table_schema='public' and table_name='bot_order_item' and column_name='product_name'
    ) THEN
        EXECUTE 'alter table bot_order_item add column product_name varchar(255)';
    END IF;
    EXECUTE 'update bot_order_item set product_name = coalesce(product_name, ''unknown'')';
    EXECUTE 'alter table bot_order_item alter column product_name set not null';

    -- unit_price
    IF NOT EXISTS (
        select 1 from information_schema.columns
        where table_schema='public' and table_name='bot_order_item' and column_name='unit_price'
    ) THEN
        EXECUTE 'alter table bot_order_item add column unit_price numeric(12,2)';
    END IF;
    EXECUTE 'update bot_order_item set unit_price = coalesce(unit_price, 0)';
    EXECUTE 'alter table bot_order_item alter column unit_price set not null';

    -- quantity
    IF NOT EXISTS (
        select 1 from information_schema.columns
        where table_schema='public' and table_name='bot_order_item' and column_name='quantity'
    ) THEN
        EXECUTE 'alter table bot_order_item add column quantity int';
    END IF;
    EXECUTE 'update bot_order_item set quantity = coalesce(quantity, 1)';
    EXECUTE 'alter table bot_order_item alter column quantity set not null';
END $$;

