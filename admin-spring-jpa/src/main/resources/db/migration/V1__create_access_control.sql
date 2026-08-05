create table users (
    id uuid primary key,
    username varchar(120) not null,
    password_hash varchar(255) not null,
    enabled boolean not null default true,
    auth_version bigint not null default 0,
    created_at timestamp with time zone not null default current_timestamp,
    updated_at timestamp with time zone not null default current_timestamp,
    constraint uk_users_username unique (username),
    constraint ck_users_auth_version_nonnegative check (auth_version >= 0)
);

create table roles (
    id uuid primary key,
    code varchar(120) not null,
    created_at timestamp with time zone not null default current_timestamp,
    constraint uk_roles_code unique (code)
);

create table permissions (
    id uuid primary key,
    code varchar(120) not null,
    system_managed boolean not null,
    created_at timestamp with time zone not null default current_timestamp,
    constraint uk_permissions_code unique (code)
);

create table user_roles (
    user_id uuid not null references users (id) on delete cascade,
    role_id uuid not null references roles (id) on delete cascade,
    primary key (user_id, role_id)
);

create table role_permissions (
    role_id uuid not null references roles (id) on delete cascade,
    permission_id uuid not null references permissions (id) on delete cascade,
    primary key (role_id, permission_id)
);

create table audit_entries (
    id uuid primary key,
    actor_user_id uuid not null references users (id),
    action_code varchar(120) not null,
    target_type varchar(120) not null,
    target_id varchar(255) not null,
    outcome varchar(16) not null,
    occurred_at timestamp with time zone not null,
    correlation_id varchar(64),
    metadata jsonb not null default '{}'::jsonb,
    constraint ck_audit_entries_outcome check (outcome in ('SUCCESS', 'DENIED', 'FAILURE'))
);

create index ix_audit_entries_occurred_at on audit_entries (occurred_at desc);
create index ix_audit_entries_actor_user_id on audit_entries (actor_user_id);
