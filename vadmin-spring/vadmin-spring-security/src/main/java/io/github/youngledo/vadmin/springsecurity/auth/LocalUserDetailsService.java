package io.github.youngledo.vadmin.springsecurity.auth;

import io.github.youngledo.vadmin.contracts.auth.LocalUserAccountLookup;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public final class LocalUserDetailsService implements UserDetailsService {
    private final LocalUserAccountLookup accountLookup;

    public LocalUserDetailsService(LocalUserAccountLookup accountLookup) {
        this.accountLookup = accountLookup;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return accountLookup.findByUsername(username)
                .map(LocalUserPrincipal::new)
                .orElseThrow(() -> new UsernameNotFoundException("user not found"));
    }
}
