-- V11: Add address sub-fields and admin temp field to user_session

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema='public' AND table_name='user_session' AND column_name='checkout_country'
    ) THEN
        EXECUTE 'ALTER TABLE user_session ADD COLUMN checkout_country VARCHAR(128)';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema='public' AND table_name='user_session' AND column_name='checkout_city'
    ) THEN
        EXECUTE 'ALTER TABLE user_session ADD COLUMN checkout_city VARCHAR(128)';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema='public' AND table_name='user_session' AND column_name='admin_temp_order_id'
    ) THEN
        EXECUTE 'ALTER TABLE user_session ADD COLUMN admin_temp_order_id VARCHAR(64)';
    END IF;
END $$;
