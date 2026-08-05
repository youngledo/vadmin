package io.github.vaadinadminstarter.springjpa;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.DriverManager;
import java.util.List;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class AccessControlMigrationTest {
    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Test
    void flywayCreatesAccessControlTables() throws Exception {
        var flyway = Flyway.configure()
                .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .load();

        flyway.migrate();
        var repeatMigration = flyway.migrate();

        assertThat(repeatMigration.migrationsExecuted).isZero();

        try (var connection = DriverManager.getConnection(
                postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
                var statement = connection.prepareStatement("""
                        select table_name
                        from information_schema.tables
                        where table_schema = 'public'
                          and table_name = any (?)
                        order by table_name
                        """)) {
            statement.setArray(1, connection.createArrayOf("text", new String[] {
                "audit_entries", "permissions", "role_permissions", "roles", "user_roles", "users"
            }));
            try (var result = statement.executeQuery()) {
                var tableNames = new java.util.ArrayList<String>();
                while (result.next()) {
                    tableNames.add(result.getString(1));
                }
                assertThat(tableNames).containsExactlyElementsOf(List.of(
                        "audit_entries", "permissions", "role_permissions", "roles", "user_roles", "users"));
            }
        }
    }
}
