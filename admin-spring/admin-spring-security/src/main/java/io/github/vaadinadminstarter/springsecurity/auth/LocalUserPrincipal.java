package io.github.vaadinadminstarter.springsecurity.auth;

import io.github.vaadinadminstarter.contracts.auth.CurrentUser;
import io.github.vaadinadminstarter.contracts.auth.LocalUserAccount;
import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public final class LocalUserPrincipal implements UserDetails {
    private final LocalUserAccount account;

    public LocalUserPrincipal(LocalUserAccount account) {
        this.account = account;
    }

    public CurrentUser currentUser() {
        return new CurrentUser(account.userId(), account.username(), account.permissions(), account.authVersion());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return account.permissions().stream()
                .map(permission -> new SimpleGrantedAuthority(permission.value()))
                .toList();
    }

    @Override
    public String getPassword() {
        return account.passwordHash();
    }

    @Override
    public String getUsername() {
        return account.username();
    }

    @Override
    public boolean isEnabled() {
        return account.enabled();
    }
}
