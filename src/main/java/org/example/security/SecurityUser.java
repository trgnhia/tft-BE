package org.example.security;

import lombok.Getter;
import org.example.entities.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class SecurityUser implements UserDetails {
    @Getter
    private final Long id;
    private final String username;
    @Getter
    private final String email;
    @Getter
    private final String roleCode;
    @Getter
    private final Instant lastLogoutAt;
    private final String passwordHashed;

    private final boolean enabled;

    private final Collection<? extends GrantedAuthority> authorities;

    public SecurityUser(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.username = user.getUsername();
        this.enabled = user.isEnabled();
        this.lastLogoutAt = user.getLastLogoutAt();
        this.passwordHashed = user.getPasswordHash();

        this.roleCode = user.getRole().getCode();
        Set<GrantedAuthority> auths = new HashSet<>();
        auths.add(new SimpleGrantedAuthority("ROLE_" + this.roleCode));

        user.getRole().getPermissions().forEach(p ->
                auths.add(new SimpleGrantedAuthority(p.getCode()))
        );

        this.authorities = auths;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHashed;
    }


    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

}
