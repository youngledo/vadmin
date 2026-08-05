create table customers (
    id uuid primary key,
    name varchar(160) not null,
    email varchar(320) not null,
    active boolean not null default true,
    created_at timestamp with time zone not null default current_timestamp,
    updated_at timestamp with time zone not null default current_timestamp,
    constraint uk_customers_email unique (email)
);

create index ix_customers_name on customers (name);
