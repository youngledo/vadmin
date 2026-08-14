package io.github.youngledo.vadmin.springsecurity.auth;

import io.github.youngledo.vadmin.contracts.auth.CurrentUser;
import io.github.youngledo.vadmin.contracts.auth.LocalUserAccount;
import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public final class LocalUserPrincipal implements UserDetails {
    private final LocalUserAccount account;

    public LocalUserPrincipal(LocalUserAccount account) {
        this.account = account;
    }

    public LocalUserPrincipal(CurrentUser currentUser) {
        this(new LocalUserAccount(currentUser.userId(), currentUser.username(), "", true,
                currentUser.authVersion(), currentUser.permissions()));
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
