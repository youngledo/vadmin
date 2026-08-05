package io.github.vaadinadminstarter.app.administration;

import io.github.vaadinadminstarter.contracts.navigation.PagedQuery;
import io.github.vaadinadminstarter.contracts.navigation.PagedResult;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdministrationQueryService {
    private final JdbcTemplate jdbcTemplate;

    public AdministrationQueryService(JdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }

    @Transactional(readOnly = true)
    public List<UserRow> users(String filter) {
        var value = "%" + (filter == null ? "" : filter.strip()) + "%";
        return jdbcTemplate.query("""
                select id, username, enabled, auth_version from users
                where username ilike ? order by username limit 100
                """, (result, row) -> new UserRow(result.getObject("id", UUID.class), result.getString("username"),
                result.getBoolean("enabled"), result.getLong("auth_version")), value);
    }

    @Transactional(readOnly = true)
    public PagedResult<UserRow> users(PagedQuery query) {
        var value = "%" + query.filters().getOrDefault("q", "").strip() + "%";
        var total = jdbcTemplate.queryForObject("select count(*) from users where username ilike ?", Long.class, value);
        var items = jdbcTemplate.query("""
                        select id, username, enabled, auth_version from users
                        where username ilike ? order by username limit ? offset ?
                        """, (result, row) -> new UserRow(result.getObject("id", UUID.class), result.getString("username"),
                        result.getBoolean("enabled"), result.getLong("auth_version")), value, query.pageSize(), offset(query));
        return new PagedResult<>(items, total);
    }

    @Transactional(readOnly = true)
    public List<RoleRow> roles() {
        return jdbcTemplate.query("""
                select role.id, role.code, count(grant_item.permission_id) as permission_count
                from roles role left join role_permissions grant_item on grant_item.role_id = role.id
                group by role.id, role.code order by role.code
                """, (result, row) -> new RoleRow(result.getObject("id", UUID.class), result.getString("code"),
                result.getLong("permission_count")));
    }

    @Transactional(readOnly = true)
    public PagedResult<RoleRow> roles(PagedQuery query) {
        var total = jdbcTemplate.queryForObject("select count(*) from roles", Long.class);
        var items = jdbcTemplate.query("""
                        select role.id, role.code, count(grant_item.permission_id) as permission_count
                        from roles role left join role_permissions grant_item on grant_item.role_id = role.id
                        group by role.id, role.code order by role.code limit ? offset ?
                        """, (result, row) -> new RoleRow(result.getObject("id", UUID.class), result.getString("code"),
                        result.getLong("permission_count")), query.pageSize(), offset(query));
        return new PagedResult<>(items, total);
    }

    @Transactional(readOnly = true)
    public List<PermissionRow> permissions() {
        return jdbcTemplate.query("select code, system_managed from permissions order by code",
                (result, row) -> new PermissionRow(result.getString("code"), result.getBoolean("system_managed")));
    }

    @Transactional(readOnly = true)
    public PagedResult<PermissionRow> permissions(PagedQuery query) {
        var total = jdbcTemplate.queryForObject("select count(*) from permissions", Long.class);
        var items = jdbcTemplate.query("select code, system_managed from permissions order by code limit ? offset ?",
                (result, row) -> new PermissionRow(result.getString("code"), result.getBoolean("system_managed")),
                query.pageSize(), offset(query));
        return new PagedResult<>(items, total);
    }

    @Transactional(readOnly = true)
    public List<AuditRow> audit() {
        return jdbcTemplate.query("""
                select occurred_at, action_code, target_type, target_id, outcome, correlation_id
                from audit_entries order by occurred_at desc limit 100
                """, (result, row) -> new AuditRow(result.getObject("occurred_at", Instant.class),
                result.getString("action_code"), result.getString("target_type"), result.getString("target_id"),
                result.getString("outcome"), result.getString("correlation_id")));
    }

    @Transactional(readOnly = true)
    public PagedResult<AuditRow> audit(PagedQuery query) {
        var total = jdbcTemplate.queryForObject("select count(*) from audit_entries", Long.class);
        var items = jdbcTemplate.query("""
                        select occurred_at, action_code, target_type, target_id, outcome, correlation_id
                        from audit_entries order by occurred_at desc limit ? offset ?
                        """, (result, row) -> new AuditRow(result.getObject("occurred_at", Instant.class),
                        result.getString("action_code"), result.getString("target_type"), result.getString("target_id"),
                        result.getString("outcome"), result.getString("correlation_id")), query.pageSize(), offset(query));
        return new PagedResult<>(items, total);
    }

    private int offset(PagedQuery query) {
        return Math.multiplyExact(query.page(), query.pageSize());
    }

    public record UserRow(UUID id, String username, boolean enabled, long authVersion) { }
    public record RoleRow(UUID id, String code, long permissionCount) { }
    public record PermissionRow(String code, boolean systemManaged) { }
    public record AuditRow(Instant occurredAt, String action, String targetType, String targetId, String outcome,
                           String correlationId) { }
}
