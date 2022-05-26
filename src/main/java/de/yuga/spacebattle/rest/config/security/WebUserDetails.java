package de.yuga.spacebattle.rest.config.security;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.enums.EWebUserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.stream.Collectors;

public class WebUserDetails implements UserDetails {

    @Nonnull
    private final User user;

    public WebUserDetails(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");

        this.user = user;
    }

    @Nonnull
    public User getUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        final EWebUserRole userRole = user.getUserRole();
        return userRole.getAllowedRoles().stream().map(WebUserRole::new).collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
