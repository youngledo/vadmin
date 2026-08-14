package io.github.youngledo.vadmin.app.auth;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.youngledo.vadmin.contracts.auth.CurrentUser;
import io.github.youngledo.vadmin.contracts.auth.ExternalIdentity;
import io.github.youngledo.vadmin.contracts.auth.LocalUserAccount;
import io.github.youngledo.vadmin.contracts.auth.LocalUserAccountLookup;
import io.github.youngledo.vadmin.contracts.auth.PermissionCode;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ConfiguredExternalIdentityMapperTest {
    private final LocalUserAccount enabledAdministrator = account("admin", true);
    private final LocalUserAccount disabledOperator = account("operator", false);
    private final LocalUserAccountLookup accountLookup = new AccountLookup(Map.of(
            enabledAdministrator.username(), enabledAdministrator,
            disabledOperator.username(), disabledOperator));

    @Test
    void resolvesOnlyAnExplicitIssuerAndSubjectLink() {
        var mapper = new ConfiguredExternalIdentityMapper(List.of(
                new OidcIdentityLinkProperties.Link(URI.create("https://issuer.example"), "subject-42", "admin")),
                accountLookup);

        assertThat(mapper.map(identity("https://issuer.example", "subject-42")))
                .contains(currentUser(enabledAdministrator));
        assertThat(mapper.map(identity("https://issuer.example", "other-subject"))).isEmpty();
        assertThat(mapper.map(identity("https://other-issuer.example", "subject-42"))).isEmpty();
    }

    @Test
    void deniesMissingAndDisabledLinkedLocalUsers() {
        var mapper = new ConfiguredExternalIdentityMapper(List.of(
                new OidcIdentityLinkProperties.Link(URI.create("https://issuer.example"), "missing", "missing"),
                new OidcIdentityLinkProperties.Link(URI.create("https://issuer.example"), "disabled", "operator")),
                accountLookup);

        assertThat(mapper.map(identity("https://issuer.example", "missing"))).isEmpty();
        assertThat(mapper.map(identity("https://issuer.example", "disabled"))).isEmpty();
    }

    @Test
    void deniesAnInvalidConfiguredLink() {
        var mapper = new ConfiguredExternalIdentityMapper(List.of(
                new OidcIdentityLinkProperties.Link(URI.create("https://issuer.example"), "subject-42", " ")),
                accountLookup);

        assertThat(mapper.map(identity("https://issuer.example", "subject-42"))).isEmpty();
    }

    private ExternalIdentity identity(String issuer, String subject) {
        return new ExternalIdentity(URI.create(issuer), subject, null, null, Map.of());
    }

    private LocalUserAccount account(String username, boolean enabled) {
        return new LocalUserAccount(UUID.randomUUID(), username, "stored-hash", enabled, 2,
                Set.of(PermissionCode.of("system:user:read")));
    }

    private CurrentUser currentUser(LocalUserAccount account) {
        return new CurrentUser(account.userId(), account.username(), account.permissions(), account.authVersion());
    }

    private record AccountLookup(Map<String, LocalUserAccount> accounts) implements LocalUserAccountLookup {
        @Override
        public Optional<LocalUserAccount> findByUsername(String username) {
            return Optional.ofNullable(accounts.get(username));
        }

        @Override
        public Optional<LocalUserAccount> findByUserId(UUID userId) {
            return accounts.values().stream().filter(account -> account.userId().equals(userId)).findFirst();
        }
    }
}
