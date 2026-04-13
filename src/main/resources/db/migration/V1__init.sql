create table if not exists telegram_user (
    telegram_user_id bigint primary key,
    username varchar(255),
    first_name varchar(255),
    last_name varchar(255),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table if not exists user_session (
    telegram_user_id bigint primary key references telegram_user(telegram_user_id) on delete cascade,
    state varchar(64) not null,
    checkout_name varchar(255),
    checkout_phone varchar(255),
    checkout_address text,
    updated_at timestamptz not null default now()
);

create table if not exists bot_order (
    order_id varchar(32) primary key,
    telegram_user_id bigint not null references telegram_user(telegram_user_id) on delete restrict,
    customer_name varchar(255) not null,
    phone_number varchar(255) not null,
    delivery_address text not null,
    status varchar(64) not null,
    created_at timestamptz not null
);

create index if not exists idx_bot_order_user_created_at on bot_order(telegram_user_id, created_at desc);

create table if not exists bot_order_item (
    id bigserial primary key,
    order_id varchar(32) not null references bot_order(order_id) on delete cascade,
    product_id varchar(64) not null,
    product_name varchar(255) not null,
    unit_price numeric(12,2) not null,
    quantity int not null
);
