-- V3: Добавляем колонку order_id в таблицу bot_order

-- 1. Добавляем колонку (если её ещё нет)
ALTER TABLE bot_order
    ADD COLUMN IF NOT EXISTS order_id BIGINT;

-- 2. Добавляем внешний ключ (foreign key)
-- Замени `order` на реальное имя таблицы заказов, если она называется иначе!
ALTER TABLE bot_order
    ADD CONSTRAINT fk_bot_order_order
        FOREIGN KEY (order_id)
            REFERENCES "order"(id)
            ON DELETE SET NULL;   -- Можно изменить на CASCADE или RESTRICT

-- 3. Создаём индекс для быстрого поиска (рекомендуется)
CREATE INDEX IF NOT EXISTS idx_bot_order_order_id
    ON bot_order (order_id);