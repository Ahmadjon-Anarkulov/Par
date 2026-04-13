alter table telegram_user
    add column if not exists role varchar(16) not null default 'USER';

update telegram_user
set role = 'USER'
where role is null or role = '';

