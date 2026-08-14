package io.github.vaadinadminstarter.app.auth;

import io.github.vaadinadminstarter.contracts.auth.CurrentUser;
import io.github.vaadinadminstarter.contracts.auth.ExternalIdentity;
import io.github.vaadinadminstarter.contracts.auth.ExternalIdentityMapper;
import io.github.vaadinadminstarter.contracts.auth.LocalUserAccount;
import io.github.vaadinadminstarter.contracts.auth.LocalUserAccountLookup;
import java.util.List;
import java.util.Optional;

public final class ConfiguredExternalIdentityMapper implements ExternalIdentityMapper {
    private final List<OidcIdentityLinkProperties.Link> links;
    private final LocalUserAccountLookup accountLookup;

    public ConfiguredExternalIdentityMapper(List<OidcIdentityLinkProperties.Link> links,
                                            LocalUserAccountLookup accountLookup) {
        this.links = List.copyOf(links);
        this.accountLookup = accountLookup;
    }

    @Override
    public Optional<CurrentUser> map(ExternalIdentity identity) {
        var matches = links.stream().filter(link -> matches(link, identity)).toList();
        if (matches.size() != 1) {
            return Optional.empty();
        }
        return accountLookup.findByUsername(matches.getFirst().username())
                .filter(LocalUserAccount::enabled)
                .map(account -> new CurrentUser(account.userId(), account.username(), account.permissions(),
                        account.authVersion()));
    }

    private boolean matches(OidcIdentityLinkProperties.Link link, ExternalIdentity identity) {
        return link != null
                && link.issuer() != null
                && link.issuer().isAbsolute()
                && link.subject() != null
                && !link.subject().isBlank()
                && link.username() != null
                && !link.username().isBlank()
                && link.issuer().equals(identity.issuer())
                && link.subject().equals(identity.subject());
    }
}
