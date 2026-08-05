create table customer_attachments (
    id uuid primary key,
    customer_id uuid not null references customers(id) on delete cascade,
    stored_file_id uuid not null unique,
    filename varchar(255) not null,
    content_type varchar(255) not null,
    size bigint not null check (size >= 0),
    created_at timestamptz not null
);

create index ix_customer_attachments_customer on customer_attachments (customer_id, created_at);
