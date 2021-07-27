package de.yuga.spacebattle.rest.config.security;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.enums.EWebUserRole;
import org.springframework.security.core.GrantedAuthority;

import javax.annotation.Nonnull;

public class WebUserRole implements GrantedAuthority {

    @Nonnull
    private final String authority;

    public WebUserRole(@Nonnull final EWebUserRole webUserRole) {
        Preconditions.checkNotNull(webUserRole, "webUserRole shouldn't be null!");

        authority = webUserRole.getName();
    }

    @Override
    public String getAuthority() {
        return authority;
    }
}
